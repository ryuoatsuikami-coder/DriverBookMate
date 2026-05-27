package com.drivermate.ph

import android.app.Notification
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class BookingNotificationListener : NotificationListenerService() {

    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingMessage: String? = null

    override fun onCreate() {
        super.onCreate()
        initTts()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        initTts()
        speakNow("DriverMate PH notification reader is active.")
    }

    private fun initTts() {
        if (tts != null) return

        tts = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true

                val result = tts?.setLanguage(Locale("en", "PH"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.US
                }

                tts?.setSpeechRate(0.88f)
                tts?.setPitch(1.02f)

                pendingMessage?.let {
                    speakNow(it)
                    pendingMessage = null
                }
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        initTts()

        val packageName = sbn.packageName.lowercase()

        val allowedApps = listOf(
            "lalamove",
            "transportify",
            "grab",
            "moveit",
            "angkas",
            "joyride"
        )

        if (allowedApps.none { packageName.contains(it) }) return

        val extras = sbn.notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString() ?: ""
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString() ?: ""
        val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString() ?: ""

        val fullText = "$title $text $bigText $subText $infoText"
            .replace("\n", " ")
            .replace("₱", " pesos ")
            .replace("PHP", " pesos ")
            .replace("php", " pesos ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (fullText.isBlank()) return

        val bookingType = detectBookingType(fullText)
        val route = detectRoute(fullText)
        val fare = detectFare(fullText)
        val distance = detectDistance(fullText, route)

        val prefs = getSharedPreferences("driver_mate_settings", MODE_PRIVATE)
        val preferredOnly = prefs.getBoolean("preferred_only", false)
        val autoOpenWaze = prefs.getBoolean("auto_open_waze", true)

        val preferred = isPreferredRoute(route, fare, distance)

        if (preferredOnly && !preferred) return

        val message = "$bookingType booking. $route. Fare $fare pesos. Distance $distance kilometers."

        speakNow(message)

        if (preferred) {
            openApp()

            if (autoOpenWaze) {
                openWaze(route)
            }
        }
    }

    private fun speakNow(message: String) {
        initTts()

        if (!ttsReady) {
            pendingMessage = message
            Handler(Looper.getMainLooper()).postDelayed({
                if (ttsReady) {
                    tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "booking_alert")
                    pendingMessage = null
                }
            }, 1200)
            return
        }

        Handler(Looper.getMainLooper()).post {
            tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "booking_alert")
        }
    }

    private fun detectBookingType(text: String): String {
        val lower = text.lowercase()

        return when {
            lower.contains("priority") -> "Priority"
            lower.contains("immediate") -> "Immediate"
            lower.contains("regular") -> "Regular"
            lower.contains("pooling") -> "Pooling"
            lower.contains("rush") -> "Priority"
            else -> "Booking"
        }
    }

    private fun detectRoute(text: String): String {
        val savedRoutes = getSavedRoutes()

        for (route in savedRoutes) {
            if (text.lowercase().contains(route.route.lowercase())) {
                return route.route
            }

            val parts = route.route.split(" to ")
            if (parts.size == 2) {
                val from = parts[0].trim()
                val to = parts[1].trim()

                if (text.lowercase().contains(from.lowercase()) &&
                    text.lowercase().contains(to.lowercase())
                ) {
                    return "$from to $to"
                }
            }
        }

        val knownPlaces = listOf(
            "Alfonso", "Amadeo", "Bacoor", "Carmona", "Cavite City",
            "Dasmarinas", "General Emilio Aguinaldo", "General Mariano Alvarez",
            "General Trias", "Imus", "Indang", "Kawit", "Magallanes",
            "Maragondon", "Mendez", "Naic", "Noveleta", "Rosario",
            "Silang", "Tagaytay", "Tanza", "Ternate", "Trece Martires",
            "Manila", "Pasay", "Makati", "BGC", "Taguig", "Paranaque",
            "Las Pinas", "Alabang", "Quezon City", "Mandaluyong",
            "San Juan", "Pasig", "Marikina", "Caloocan", "Malabon",
            "Navotas", "Valenzuela", "Muntinlupa"
        )

        val found = knownPlaces.filter {
            text.lowercase().contains(it.lowercase())
        }

        return if (found.size >= 2) {
            "${found[0]} to ${found[1]}"
        } else {
            "Route not detected"
        }
    }

    private fun detectFare(text: String): String {
        val patterns = listOf(
            Regex("""(?:fare|amount|total|price|fee)\s*[:\-]?\s*(?:pesos)?\s*([0-9]{2,5})""", RegexOption.IGNORE_CASE),
            Regex("""(?:pesos)\s*([0-9]{2,5})""", RegexOption.IGNORE_CASE),
            Regex("""([0-9]{2,5})\s*(?:pesos)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) return match.groupValues[1]
        }

        val route = detectRoute(text)
        getSavedRoutes().firstOrNull { it.route.equals(route, true) }?.let {
            if (it.fare.isNotBlank()) return it.fare
        }

        return "not detected"
    }

    private fun detectDistance(text: String, route: String): String {
        val patterns = listOf(
            Regex("""(?:distance|total distance|trip distance|route distance)\s*[:\-]?\s*([0-9]+(?:\.[0-9]+)?)\s*(?:km|kilometer|kilometers)""", RegexOption.IGNORE_CASE),
            Regex("""(?:pickup distance|pickup)\s*[:\-]?\s*([0-9]+(?:\.[0-9]+)?)\s*(?:km|kilometer|kilometers)""", RegexOption.IGNORE_CASE),
            Regex("""(?:dropoff distance|drop off distance|dropoff|drop off)\s*[:\-]?\s*([0-9]+(?:\.[0-9]+)?)\s*(?:km|kilometer|kilometers)""", RegexOption.IGNORE_CASE),
            Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:km|kilometer|kilometers)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) return match.groupValues[1]
        }

        getSavedRoutes().firstOrNull { it.route.equals(route, true) }?.let {
            if (it.distance.isNotBlank()) return it.distance
        }

        return "not detected"
    }

    private fun isPreferredRoute(route: String, fare: String, distance: String): Boolean {
        val saved = getSavedRoutes()

        if (saved.isEmpty()) return true

        return saved.any {
            val routeMatch = route.equals(it.route, true)

            val fareMatch =
                it.fare.isBlank() ||
                fare == "not detected" ||
                fare.toDoubleOrNull()?.let { detectedFare ->
                    it.fare.toDoubleOrNull()?.let { savedFare ->
                        detectedFare >= savedFare
                    }
                } ?: true

            val distanceMatch =
                it.distance.isBlank() ||
                distance == "not detected" ||
                distance.toDoubleOrNull()?.let { detectedDistance ->
                    it.distance.toDoubleOrNull()?.let { savedDistance ->
                        detectedDistance <= savedDistance
                    }
                } ?: true

            routeMatch && fareMatch && distanceMatch
        }
    }

    private fun getSavedRoutes(): List<RouteData> {
        val prefs = getSharedPreferences("driver_mate_settings", MODE_PRIVATE)
        val raw = prefs.getString("saved_full_routes", "") ?: ""

        if (raw.isBlank()) return emptyList()

        return raw.split("|").mapNotNull {
            val p = it.split("~")
            if (p.size >= 3) RouteData(p[0], p[1], p[2]) else null
        }
    }

    private fun openApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        startActivity(intent)
    }

    private fun openWaze(route: String) {
        val destination = extractDestination(route)
        if (destination.isBlank() || destination == "Route not detected") return

        val encodedDestination = Uri.encode(destination)
        val wazeUri = Uri.parse("waze://?q=$encodedDestination&navigate=yes")

        val intent = Intent(Intent.ACTION_VIEW, wazeUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            val browserUri = Uri.parse("https://waze.com/ul?q=$encodedDestination&navigate=yes")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(browserIntent)
        }
    }

    private fun extractDestination(route: String): String {
        val parts = route.split(" to ")
        return if (parts.size >= 2) {
            parts[1].trim()
        } else {
            route.trim()
        }
    }

    data class RouteData(
        val route: String,
        val fare: String,
        val distance: String
    )

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
