package com.example.kingmaker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.kingmaker.service.BackendClient
import com.example.kingmaker.service.ContactStats
import com.example.kingmaker.service.DashboardData
import com.example.kingmaker.service.MonitorService
import com.example.kingmaker.service.QueuedPerson
import com.example.kingmaker.ui.dashboard.DashboardScreen
import com.example.kingmaker.ui.theme.KingmakerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val DASHBOARD_REFRESH_INTERVAL_MS = 5_000L

private val PLACEHOLDER_DASHBOARD = DashboardData(
    activeGoal = "Connect with enterprise PMs working on customer support AI",
    queuedPeople = listOf(
        QueuedPerson("SC", "Sarah Chen", "LinkedIn", "Reply", 1),
        QueuedPerson("AM", "Alex Morgan", "LinkedIn", "Follow up", 2),
        QueuedPerson("DP", "David Park", "Gmail", "New outreach", 3)
    ),
    stats = ContactStats(total = 412, active = 187, needAttention = 64)
)

class MainActivity : ComponentActivity() {

    private var showOverlayRationale by mutableStateOf(false)
    private var showNotificationRationale by mutableStateOf(false)
    private var dashboardData by mutableStateOf(PLACEHOLDER_DASHBOARD)

    private val overlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            startMonitoring()
        }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            startMonitoring()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        startMonitoring()

        // Only polls while the screen is actually visible (RESUMED); pauses
        // automatically when the app is backgrounded and resumes on return.
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (isActive) {
                    val fetched = withContext(Dispatchers.IO) { BackendClient.fetchDashboard() }
                    if (fetched != null) dashboardData = fetched
                    delay(DASHBOARD_REFRESH_INTERVAL_MS)
                }
            }
        }

        setContent {
            KingmakerTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    DashboardScreen(
                        activeGoal = dashboardData.activeGoal,
                        queuedPeople = dashboardData.queuedPeople,
                        stats = dashboardData.stats
                    )

                    if (showOverlayRationale) {
                        PermissionRationaleDialog(
                            title = "Allow pop-ups over other apps",
                            message = "Kingmaker shows your next action on top of whatever app you're using. " +
                                "Allow it to display over other apps.",
                            onConfirm = {
                                showOverlayRationale = false
                                overlayPermissionLauncher.launch(
                                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                                )
                            },
                            onDismiss = { showOverlayRationale = false }
                        )
                    } else if (showNotificationRationale) {
                        PermissionRationaleDialog(
                            title = "Allow notifications",
                            message = "Kingmaker runs a background monitor and needs a notification to stay active.",
                            onConfirm = {
                                showNotificationRationale = false
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            onDismiss = { showNotificationRationale = false }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MonitorService.isAppInForeground = true
    }

    override fun onPause() {
        super.onPause()
        MonitorService.isAppInForeground = false
    }

    private fun startMonitoring() {
        if (!Settings.canDrawOverlays(this)) {
            showOverlayRationale = true
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            showNotificationRationale = true
            return
        }
        ContextCompat.startForegroundService(this, Intent(this, MonitorService::class.java))
    }
}

@Composable
private fun PermissionRationaleDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text("Allow") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Not now") }
        }
    )
}
