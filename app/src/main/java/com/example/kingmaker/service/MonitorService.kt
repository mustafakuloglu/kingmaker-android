package com.example.kingmaker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.kingmaker.R
import com.example.kingmaker.ui.popup.OverlayPopupContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val NOTIF_ID = 1905
private const val NOTIF_CHANNEL_ID = "monitor_channel"
private const val CHECK_INTERVAL_MS = 3_000L
private const val POPUP_WIDTH_RATIO = 0.9

class MonitorService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val overlayLifecycleOwner = OverlayLifecycleOwner()
    private var overlayView: ComposeView? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIF_ID, buildForegroundNotification())

        scope.launch {
            while (isActive) {
                delay(CHECK_INTERVAL_MS)
                val action = checkForNextAction()
                if (action != null) showPopup(action)
            }
        }
    }

    private suspend fun checkForNextAction(): NextAction? =
        withContext(Dispatchers.IO) { BackendClient.checkAction() }

    private suspend fun takeAction(id: Int, message: String) =
        withContext(Dispatchers.IO) { BackendClient.takeAction(id, message) }

    private fun showPopup(action: NextAction) {
        if (overlayView != null || !Settings.canDrawOverlays(this)) return

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(overlayLifecycleOwner)
            setViewTreeViewModelStoreOwner(overlayLifecycleOwner)
            setViewTreeSavedStateRegistryOwner(overlayLifecycleOwner)
            setContent {
                OverlayPopupContent(
                    action = action,
                    onSend = { finalMessage ->
                        scope.launch { takeAction(action.id, finalMessage) }
                        dismissOverlay(windowManager)
                    },
                    onSkip = { dismissOverlay(windowManager) }
                )
            }
        }

        val width = (resources.displayMetrics.widthPixels * POPUP_WIDTH_RATIO).toInt()
        val params = WindowManager.LayoutParams(
            width,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.CENTER
        }

        windowManager.addView(composeView, params)
        overlayView = composeView
        overlayLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_START)
        overlayLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    private fun dismissOverlay(windowManager: WindowManager) {
        overlayLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        overlayLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        overlayView?.let { windowManager.removeView(it) }
        overlayView = null
    }

    private fun buildForegroundNotification(): Notification {
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                NOTIF_CHANNEL_ID,
                getString(R.string.monitor_channel_name),
                NotificationManager.IMPORTANCE_MIN
            )
        )
        return NotificationCompat.Builder(this, NOTIF_CHANNEL_ID)
            .setContentTitle(getString(R.string.monitor_notification_title))
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        overlayView?.let { (getSystemService(WINDOW_SERVICE) as WindowManager).removeView(it) }
        overlayLifecycleOwner.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
