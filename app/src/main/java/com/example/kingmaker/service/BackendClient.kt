package com.example.kingmaker.service

import android.util.Log
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

// Laptop's LAN IP (from `ipconfig getifaddr en0`) — this can change on reconnect,
// re-check with the machine running the backend if the app can't connect.
private const val BASE_URL = "http://10.10.52.213:8000"
private const val CONNECT_TIMEOUT_MS = 5_000
private const val READ_TIMEOUT_MS = 5_000
private const val TAG = "BackendClient"

object BackendClient {

    // GET /check_action -> {"status":"NO_ACTION"} or {"status":"ACTION", ...}
    fun checkAction(): NextAction? {
        val response = get("$BASE_URL/check_action") ?: return null
        if (response.optString("status") != "ACTION") return null
        return NextAction(
            id = response.getInt("id"),
            title = response.optString("title"),
            who = response.optString("who"),
            context = response.optString("context"),
            lastInteraction = response.optString("last_interaction"),
            whyNow = response.optString("why_now"),
            message = response.optString("message")
        )
    }

    // POST /take_action {"id": ..., "message": "final text to send"}
    fun takeAction(id: Int, message: String) {
        post(
            "$BASE_URL/take_action",
            JSONObject().apply {
                put("id", id)
                put("message", message)
            }
        )
    }

    // GET /dashboard -> {"active_goal": ..., "queued_people": [...], "contact_stats": {...}}
    fun fetchDashboard(): DashboardData? {
        val response = get("$BASE_URL/dashboard") ?: return null
        return try {
            val peopleArray = response.getJSONArray("queued_people")
            val queuedPeople = (0 until peopleArray.length()).map { i ->
                val person = peopleArray.getJSONObject(i)
                QueuedPerson(
                    initials = person.optString("initials"),
                    name = person.optString("name"),
                    channel = person.optString("channel"),
                    reason = person.optString("reason"),
                    position = person.optInt("position")
                )
            }
            val stats = response.getJSONObject("contact_stats")
            DashboardData(
                activeGoal = response.optString("active_goal"),
                queuedPeople = queuedPeople,
                stats = ContactStats(
                    total = stats.optInt("total"),
                    active = stats.optInt("active"),
                    needAttention = stats.optInt("need_attention")
                )
            )
        } catch (e: Exception) {
            Log.w(TAG, "dashboard parse failed: ${e.message}")
            null
        }
    }

    private fun get(urlString: String): JSONObject? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = "GET"
            }
            val body = connection.inputStream.bufferedReader().readText()
            JSONObject(body)
        } catch (e: Exception) {
            Log.w(TAG, "GET $urlString failed: ${e.message}")
            null
        } finally {
            connection?.disconnect()
        }
    }

    private fun post(urlString: String, body: JSONObject) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(urlString).openConnection() as HttpURLConnection).apply {
                connectTimeout = CONNECT_TIMEOUT_MS
                readTimeout = READ_TIMEOUT_MS
                requestMethod = "POST"
                doOutput = true
                setRequestProperty("Content-Type", "application/json")
            }
            connection.outputStream.use { it.write(body.toString().toByteArray()) }
            connection.inputStream.close()
        } catch (e: Exception) {
            Log.w(TAG, "take_action failed: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }
}
