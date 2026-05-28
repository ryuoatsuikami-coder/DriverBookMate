package com.drivermate.ph

import android.app.Notification
import android.content.Context
import android.media.AudioAttributes
import android.os.Bundle
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.regex.Pattern

class DriverNotificationListener : NotificationListenerService(), TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val prefs by lazy {
        getSharedPreferences("driver_mate_settings", Context.MODE_PRIVATE)
    }

    private val allowedPackages = mapOf(
        "com.lalamove.huolala.client" to "Lalamove",
        "com.grabtaxi.passenger" to "Grab",
        "com.grabtaxi.driver2" to "Grab",
        "com.transportify.driver" to "Transportify",
        "com.deliveree.driver" to "Transportify"
    )

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.language = Locale("en", "PH")
            tts?.setSpeechRate(0.88f)
            tts?.setPitch(1.02f)

            tts?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val voiceEnabled = prefs.getBoolean("voice_enabled", true)
        if (!voiceEnabled) return

        val appName = allowedPackages[sbn.packageName] ?: detectAppName(sbn.packageName)
        if (appName.isBlank()) return

        val notificationText = extractNotificationText(sbn.notification)
        if (notificationText.isBlank()) return

        val booking = parseBooking(notificationText)

        if (booking.pickup.isBlank() && booking.dropoff.isBlank()) return

        val spokenMessage = buildSpokenMessage(
            appName = appName,
            pickup = booking.pickup,
            dropoff = booking.dropoff,
            fare = booking.fare,
            distance = booking.distance
        )

        speak(spokenMessage)
    }

    private fun extractNotificationText(notification: Notification): String {
        val extras: Bundle = notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()

        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.joinToString(" ") { it.toString() }
            .orEmpty()

        return listOf(title, text, bigText, subText, lines)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .replace("\n", " ")
            .replace("  ", " ")
            .trim()
    }

    private fun parseBooking(rawText: String): BookingInfo {
        val text = rawText
            .replace("–", "-")
            .replace("—", "-")
            .replace("➡", "→")
            .replace("➜", "→")
            .replace(" to ", " → ", ignoreCase = true)
            .replace(" TO ", " → ")
            .trim()

        val fare = extractFare(text)
        val distance = extractDistance(text)

        val pickupByLabel = extractAfterLabels(
            text,
            listOf("pickup", "pick up", "from", "origin", "sender")
        )

        val dropoffByLabel = extractAfterLabels(
            text,
            listOf("dropoff", "drop off", "drop-off", "to", "destination", "receiver")
        )

        if (pickupByLabel.isNotBlank() || dropoffByLabel.isNotBlank()) {
            return BookingInfo(
                pickup = cleanPlace(pickupByLabel),
                dropoff = cleanPlace(dropoffByLabel),
                fare = fare,
                distance = distance
            )
        }

        val route = extractRouteByArrow(text)

        return BookingInfo(
            pickup = cleanPlace(route.first),
            dropoff = cleanPlace(route.second),
            fare = fare,
            distance = distance
        )
    }

    private fun extractRouteByArrow(text: String): Pair<String, String> {
        val separators = listOf("→", " - ", " > ")

        for (separator in separators) {
            if (text.contains(separator)) {
                val parts = text.split(separator, limit = 2)
                if (parts.size == 2) {
                    val left = removeNoise(parts[0])
                    val right = removeNoise(parts[1])
                    return Pair(left, right)
                }
            }
        }

        return Pair("", "")
    }

    private fun extractAfterLabels(text: String, labels: List<String>): String {
        for (label in labels) {
            val pattern = Pattern.compile(
                "(?i)$label\\s*[:\\-]?\\s*([^|•,]+)"
            )
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                return matcher.group(1)?.trim().orEmpty()
            }
        }
        return ""
    }

    private fun extractFare(text: String): String {
        val patterns = listOf(
            "₱\\s?\\d+[,.]?\\d*",
            "PHP\\s?\\d+[,.]?\\d*",
            "P\\s?\\d+[,.]?\\d*",
            "(?i)fare\\s*[:\\-]?\\s*\\d+[,.]?\\d*"
        )

        for (p in patterns) {
            val matcher = Pattern.compile(p).matcher(text)
            if (matcher.find()) {
                return matcher.group().replace("Fare", "", ignoreCase = true).trim()
            }
        }

        return ""
    }

    private fun extractDistance(text: String): String {
        val patterns = listOf(
            "\\d+[.]?\\d*\\s?km",
            "\\d+[.]?\\d*\\s?kilometers",
            "\\d+[.]?\\d*\\s?kilometres"
        )

        for (p in patterns) {
            val matcher = Pattern.compile(p, Pattern.CASE_INSENSITIVE).matcher(text)
            if (matcher.find()) return matcher.group().trim()
        }

        return ""
    }

    private fun removeNoise(value: String): String {
        return value
            .replace(Regex("(?i)new booking"), "")
            .replace(Regex("(?i)booking detected"), "")
            .replace(Regex("(?i)pickup"), "")
            .replace(Regex("(?i)pick up"), "")
            .replace(Regex("(?i)from"), "")
            .replace(Regex("(?i)route"), "")
            .replace(Regex("₱\\s?\\d+[,.]?\\d*"), "")
            .replace(Regex("\\d+[.]?\\d*\\s?km", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun cleanPlace(place: String): String {
        return place
            .replace(Regex("(?i)dropoff.*"), "")
            .replace(Regex("(?i)drop off.*"), "")
            .replace(Regex("(?i)destination.*"), "")
            .replace(Regex("(?i)fare.*"), "")
            .replace(Regex("(?i)distance.*"), "")
            .replace("|", "")
            .replace("•", "")
            .replace(",", "")
            .trim()
    }

    private fun buildSpokenMessage(
        appName: String,
        pickup: String,
        dropoff: String,
        fare: String,
        distance: String
    ): String {
        return buildString {
            append("$appName booking detected. ")

            if (pickup.isNotBlank()) {
                append("Pickup, $pickup. ")
            }

            if (dropoff.isNotBlank()) {
                append("Drop off, $dropoff. ")
            }

            if (fare.isNotBlank()) {
                append("Fare, $fare. ")
            }

            if (distance.isNotBlank()) {
                append("Distance, $distance. ")
            }
        }
    }

    private fun speak(message: String) {
        if (!isTtsReady) return

        val volume = prefs.getFloat("voice_volume", 1.0f)
            .coerceIn(0.0f, 1.0f)

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
        }

        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            params,
            "booking_voice_${System.currentTimeMillis()}"
        )
    }

    private fun detectAppName(packageName: String): String {
        return when {
            packageName.contains("lalamove", true) -> "Lalamove"
            packageName.contains("grab", true) -> "Grab"
            packageName.contains("transportify", true) -> "Transportify"
            packageName.contains("deliveree", true) -> "Transportify"
            else -> ""
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    data class BookingInfo(
        val pickup: String = "",
        val dropoff: String = "",
        val fare: String = "",
        val distance: String = ""
    )
}
