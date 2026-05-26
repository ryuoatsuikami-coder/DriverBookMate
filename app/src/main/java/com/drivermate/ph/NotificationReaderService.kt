package com.drivermate.ph

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale

class NotificationReaderService : NotificationListenerService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Filipino/Philippines accent if available on phone
            val result = tts?.setLanguage(Locale("en", "PH"))

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.language = Locale.ENGLISH
            }

            tts?.setSpeechRate(0.90f)
            tts?.setPitch(1.0f)
            isTtsReady = true
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras = sbn.notification.extras

        val title = extras.getCharSequence("android.title")?.toString().orEmpty()
        val text = extras.getCharSequence("android.text")?.toString().orEmpty()
        val bigText = extras.getCharSequence("android.bigText")?.toString().orEmpty()

        val rawMessage = "$title $text $bigText"
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (rawMessage.isBlank()) return

        val bookingType = detectBookingType(rawMessage)
        val route = detectRoute(rawMessage)
        val fare = detectFare(rawMessage)
        val distance = detectDistance(rawMessage)

        val spokenMessage = buildString {
            append("$bookingType booking. ")

            if (route.isNotBlank()) {
                append("$route. ")
            } else {
                append(rawMessage.take(120))
                append(". ")
            }

            if (fare.isNotBlank()) {
                append("$fare pesos. ")
            }

            if (distance.isNotBlank()) {
                append("Distance from you: $distance. ")
            } else {
                append("Distance not shown. ")
            }
        }

        speak(spokenMessage)
    }

    private fun detectBookingType(message: String): String {
        val lower = message.lowercase()

        return when {
            lower.contains("immediate") -> "Immediate"
            lower.contains("regular") -> "Regular"
            lower.contains("pooling") -> "Pooling"
            lower.contains("priority") -> "Priority"
            lower.contains("rush") -> "Priority"
            else -> "New"
        }
    }

    private fun detectRoute(message: String): String {
        val cleaned = message.replace("→", " to ")

        val patterns = listOf(
            Regex("([A-Za-zñÑ .'-]+)\\s+to\\s+([A-Za-zñÑ .'-]+)", RegexOption.IGNORE_CASE),
            Regex("pickup[:\\s]+([A-Za-zñÑ .'-]+).*drop\\s?off[:\\s]+([A-Za-zñÑ .'-]+)", RegexOption.IGNORE_CASE),
            Regex("from[:\\s]+([A-Za-zñÑ .'-]+).*to[:\\s]+([A-Za-zñÑ .'-]+)", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(cleaned)
            if (match != null && match.groupValues.size >= 3) {
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
        val patterns = listOf(
            Regex("([0-9]+(?:\\.[0-9]+)?)\\s?km", RegexOption.IGNORE_CASE),
            Regex("([0-9]+(?:\\.[0-9]+)?)\\s?kilometers", RegexOption.IGNORE_CASE),
            Regex("distance[:\\s]+([0-9]+(?:\\.[0-9]+)?\\s?(?:km|kilometers))", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(message)
            if (match != null) {
                return match.value
            }
        }

        return ""
    }

    private fun speak(message: String) {
        if (!isTtsReady) return

        tts?.speak(
            message,
            TextToSpeech.QUEUE_ADD,
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
