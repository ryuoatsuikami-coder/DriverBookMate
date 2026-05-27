package com.drivermate.ph

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class NotificationReaderService : NotificationListenerService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var ready = false

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("en", "PH"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            }
            tts?.setSpeechRate(0.90f)
            ready = true
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras

        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val bigText = extras.getCharSequence("android.bigText")?.toString().orEmpty()

        val raw = "$title $text $bigText"
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (raw.isBlank()) return

        val prefs = getSharedPreferences("driver_mate_settings", MODE_PRIVATE)
        val preferredOnly = prefs.getBoolean("preferred_only", false)

        val savedRoutes = getSavedList("saved_routes")
        val savedFares = getSavedList("saved_fares")
        val savedDistances = getSavedList("saved_distances")

        val bookingName = detectBookingName(raw, title)
        val route = detectRoute(raw)
        val fare = detectFare(raw)
        val distance = detectDistance(raw)

        val routeMatch = savedRoutes.isEmpty() || savedRoutes.any { raw.lowercase().contains(it.lowercase()) }
        val fareMatch = savedFares.isEmpty() || fareMatches(savedFares, fare)
        val distanceMatch = savedDistances.isEmpty() || distanceMatches(savedDistances, distance)

        if (preferredOnly && !(routeMatch && fareMatch && distanceMatch)) return

        val message = buildString {
            append("$bookingName. ")

            if (route.isNotBlank()) {
                append("From $route. ")
            }

            if (fare.isNotBlank()) {
                append("Fare $fare pesos.")
            }
        }.trim()

        if (message.isNotBlank()) speak(message)
    }

    private fun getSavedList(key: String): List<String> {
        val prefs = getSharedPreferences("driver_mate_settings", MODE_PRIVATE)
        val saved = prefs.getString(key, "") ?: ""
        return saved.split("|").map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun fareMatches(savedFares: List<String>, fare: String): Boolean {
        val fareValue = fare.toDoubleOrNull() ?: return false

        return savedFares.any {
            val savedValue = it.toDoubleOrNull()
            savedValue != null && fareValue >= savedValue
        }
    }

    private fun distanceMatches(savedDistances: List<String>, distance: String): Boolean {
        val distanceValue = distance.toDoubleOrNull() ?: return false

        return savedDistances.any {
            val savedValue = it.toDoubleOrNull()
            savedValue != null && distanceValue <= savedValue
        }
    }

    private fun detectBookingName(message: String, title: String): String {
        val lower = message.lowercase()

        return when {
            lower.contains("priority") -> "Priority booking"
            lower.contains("immediate") -> "Immediate booking"
            lower.contains("regular") -> "Regular booking"
            lower.contains("pooling") -> "Pooling booking"
            title.isNotBlank() -> cleanShort(title)
            else -> "New booking"
        }
    }

    private fun cleanShort(text: String): String {
        return text
            .replace(Regex("[^A-Za-z0-9ñÑ .'-]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
            .take(40)
    }

    private fun detectRoute(message: String): String {
        val cleaned = message.replace("→", " to ")

        val patterns = listOf(
            Regex("from\\s+([A-Za-zñÑ .'-]+)\\s+to\\s+([A-Za-zñÑ .'-]+)", RegexOption.IGNORE_CASE),
            Regex("([A-Za-zñÑ .'-]+)\\s+to\\s+([A-Za-zñÑ .'-]+)", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(cleaned)
            if (match != null) {
                val pickup = match.groupValues[1].trim()
                val dropoff = match.groupValues[2].trim()
                return "$pickup to $dropoff"
            }
        }

        return ""
    }

    private fun detectFare(message: String): String {
        val patterns = listOf(
            Regex("₱\\s?([0-9]+(?:[,.][0-9]+)?)"),
            Regex("PHP\\s?([0-9]+(?:[,.][0-9]+)?)", RegexOption.IGNORE_CASE),
            Regex("([0-9]+(?:[,.][0-9]+)?)\\s?pesos", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.groupValues[1].replace(",", "")
            }
        }

        return ""
    }

    private fun detectDistance(message: String): String {
        val pattern = Regex("([0-9]+(?:\\.[0-9]+)?)\\s?(km|kilometers)", RegexOption.IGNORE_CASE)
        val match = pattern.find(message)
        return match?.groupValues?.get(1) ?: ""
    }

    private fun speak(message: String) {
        if (!ready) return
        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            System.currentTimeMillis().toString()
        )
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
