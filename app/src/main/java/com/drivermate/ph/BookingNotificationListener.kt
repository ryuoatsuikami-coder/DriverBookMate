package com.drivermate.ph

import android.app.Notification
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class BookingNotificationListener : NotificationListenerService() {

    private var tts: TextToSpeech? = null
    private val prefs by lazy {
        getSharedPreferences("driver_mate_settings", Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "PH")
                tts?.setSpeechRate(0.88f)
                tts?.setPitch(1.02f)
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return
        if (!prefs.getBoolean("voice_enabled", true)) return

        val packageName = sbn.packageName.lowercase()
        val appName = detectAppName(packageName) ?: return

        if (!isAppEnabled(appName)) return

        val extras = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""

        val fullText = "$title $text $bigText"
            .replace("\n", " ")
            .replace("  ", " ")
            .trim()

        if (fullText.isBlank()) return
        if (!looksLikeBooking(fullText)) return

        val route = extractRoute(fullText)
        val fare = extractFare(fullText)

        val speakAllRoutes = prefs.getBoolean("speak_all_routes", false)
        val priorityRoute = prefs.getString("first_priority_route", "") ?: ""

        val isPriority = priorityRoute.isNotBlank() &&
            normalize(route).contains(normalize(priorityRoute))

        if (!speakAllRoutes && !isPriority) return

        val message = buildVoiceMessage(appName, route, fare, isPriority)
        speak(message)

        if (isPriority && prefs.getBoolean("auto_open_waze", true)) {
            openWazeFromRoute(route)
        }
    }

    private fun detectAppName(packageName: String): String? {
        return when {
            packageName.contains("lalamove") -> "Lalamove"
            packageName.contains("grab") -> "Grab"
            packageName.contains("transportify") -> "Transportify"
            packageName.contains("moveit") || packageName.contains("move.it") -> "Move It"
            else -> null
        }
    }

    private fun isAppEnabled(appName: String): Boolean {
        return when (appName) {
            "Lalamove" -> prefs.getBoolean("enable_lalamove", true)
            "Grab" -> prefs.getBoolean("enable_grab", true)
            "Transportify" -> prefs.getBoolean("enable_transportify", true)
            "Move It" -> prefs.getBoolean("enable_moveit", true)
            else -> false
        }
    }

    private fun looksLikeBooking(text: String): Boolean {
        val lower = text.lowercase()

        return listOf(
            "booking",
            "new order",
            "new booking",
            "delivery",
            "pickup",
            "pick up",
            "drop off",
            "dropoff",
            "fare",
            "₱",
            "php"
        ).any { lower.contains(it) }
    }

    private fun extractRoute(text: String): String {
        val clean = text
            .replace("Pick-up", "Pickup", true)
            .replace("Pick up", "Pickup", true)
            .replace("Drop-off", "Dropoff", true)
            .replace("Drop off", "Dropoff", true)
            .replace("→", " to ")
            .replace("➡", " to ")
            .replace("-", " ")
            .replace("\n", " ")
            .replace("  ", " ")

        val pickupRegex = Regex(
            "(?i)(pickup|from)[:\\s]+([A-Za-z0-9 .,'#]+?)(?=\\s+(dropoff|to|destination|fare|₱|php|$))"
        )

        val dropoffRegex = Regex(
            "(?i)(dropoff|destination|to)[:\\s]+([A-Za-z0-9 .,'#]+?)(?=\\s+(fare|₱|php|pickup|from|$))"
        )

        val pickup = pickupRegex.find(clean)?.groupValues?.getOrNull(2)?.trim()
        val dropoff = dropoffRegex.find(clean)?.groupValues?.getOrNull(2)?.trim()

        if (!pickup.isNullOrBlank() && !dropoff.isNullOrBlank()) {
            return "${pickup.cleanPlace()} to ${dropoff.cleanPlace()}"
        }

        val arrowRegex = Regex(
            "(?i)([A-Za-z0-9 .,'#]+?)\\s+(to)\\s+([A-Za-z0-9 .,'#]+)"
        )

        val arrow = arrowRegex.find(clean)
        if (arrow != null) {
            val from = arrow.groupValues[1].cleanPlace()
            val to = arrow.groupValues[3].cleanPlace()

            if (from.isNotBlank() && to.isNotBlank() && !from.equals(to, true)) {
                return "$from to $to"
            }
        }

        return "route not detected"
    }

    private fun extractFare(text: String): String {
        val pesoRegex = Regex("(₱|PHP|php)\\s?([0-9,]+)")
        val match = pesoRegex.find(text)

        return if (match != null) {
            match.groupValues[2].replace(",", "")
        } else {
            "not detected"
        }
    }

    private fun buildVoiceMessage(
        appName: String,
        route: String,
        fare: String,
        isPriority: Boolean
    ): String {
        val priorityText = if (isPriority) "Priority booking. " else ""

        val fareText = if (fare == "not detected") {
            "Fare not detected."
        } else {
            "Fare $fare pesos."
        }

        return "$priorityText$appName booking detected. Pickup first. $route. $fareText"
    }

    private fun openWazeFromRoute(route: String) {
        if (!route.contains(" to ", true)) return

        val destination = route.substringAfter(" to ").trim()
        if (destination.isBlank()) return

        val encoded = Uri.encode(destination)
        val uri = Uri.parse("waze://?q=$encoded&navigate=yes")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://waze.com/ul?q=$encoded&navigate=yes")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    private fun normalize(value: String): String {
        return value
            .lowercase()
            .replace("→", " to ")
            .replace("-", " ")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun String.cleanPlace(): String {
        return this
            .replace("pickup", "", true)
            .replace("dropoff", "", true)
            .replace("destination", "", true)
            .replace("fare", "", true)
            .replace("php", "", true)
            .replace("₱", "", true)
            .replace(Regex("[^A-Za-z0-9 .,'#]"), "")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun speak(message: String) {
        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "booking_${System.currentTimeMillis()}"
        )
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
