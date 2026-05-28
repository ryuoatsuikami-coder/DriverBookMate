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
    private var ttsReady = false

    private val prefs by lazy {
        getSharedPreferences("driver_mate_settings", Context.MODE_PRIVATE)
    }

    override fun onCreate() {
        super.onCreate()
        tts = TextToSpeech(this, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ttsReady = true

            tts?.language = Locale.US
            tts?.setSpeechRate(0.88f)
            tts?.setPitch(1.02f)

            tts?.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
        } else {
            ttsReady = false
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (!prefs.getBoolean("voice_enabled", true)) return

        val appName = detectBookingApp(sbn.packageName) ?: return

        if (!isAppEnabled(appName)) return

        val text = extractNotificationText(sbn.notification)
        if (text.isBlank()) return

        val booking = parseBooking(text)

        if (booking.pickup.isBlank() && booking.dropoff.isBlank()) return

        val message = buildSpokenMessage(appName, booking)
        speak(message)
    }

    private fun detectBookingApp(packageName: String): String? {
        return when {
            packageName.contains("lalamove", true) -> "Lalamove"
            packageName.contains("grab", true) -> "Grab"
            packageName.contains("transportify", true) -> "Transportify"
            packageName.contains("deliveree", true) -> "Transportify"
            packageName.contains("moveit", true) -> "Move It"
            packageName.contains("angkas", true) -> "Angkas"
            else -> null
        }
    }

    private fun isAppEnabled(appName: String): Boolean {
        return when (appName) {
            "Lalamove" -> prefs.getBoolean("enable_lalamove", true)
            "Grab" -> prefs.getBoolean("enable_grab", true)
            "Transportify" -> prefs.getBoolean("enable_transportify", true)
            "Move It" -> prefs.getBoolean("enable_moveit", true)
            "Angkas" -> prefs.getBoolean("enable_angkas", true)
            else -> true
        }
    }

    private fun extractNotificationText(notification: Notification): String {
        val extras: Bundle = notification.extras

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val infoText = extras.getCharSequence(Notification.EXTRA_INFO_TEXT)?.toString().orEmpty()

        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)
            ?.joinToString(" ") { it.toString() }
            .orEmpty()

        return listOf(title, text, bigText, subText, infoText, lines)
            .filter { it.isNotBlank() }
            .joinToString(" ")
            .replace("\n", " ")
            .replace("  ", " ")
            .trim()
    }

    private fun parseBooking(rawText: String): BookingInfo {
        val text = normalize(rawText)

        val fare = extractFare(text)
        val distance = extractDistance(text)

        val pickup = extractPickup(text)
        val dropoff = extractDropoff(text)

        if (pickup.isNotBlank() || dropoff.isNotBlank()) {
            return BookingInfo(
                pickup = cleanPlace(pickup),
                dropoff = cleanPlace(dropoff),
                fare = fare,
                distance = distance
            )
        }

        val route = extractRouteBySeparator(text)

        return BookingInfo(
            pickup = cleanPlace(route.first),
            dropoff = cleanPlace(route.second),
            fare = fare,
            distance = distance
        )
    }

    private fun normalize(value: String): String {
        return value
            .replace("–", " → ")
            .replace("—", " → ")
            .replace("➡", " → ")
            .replace("➜", " → ")
            .replace("→", " → ")
            .replace(" to ", " → ", ignoreCase = true)
            .replace(" from ", " from ", ignoreCase = true)
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun extractPickup(text: String): String {
        val patterns = listOf(
            "(?i)(pickup|pick up|pick-up|from|origin|sender)\\s*[:\\-]?\\s*(.*?)(?=\\s+(dropoff|drop off|drop-off|destination|receiver|to)\\s*[:\\-]|₱|PHP|\\d+\\s?km|$)",
            "(?i)\\bfrom\\b\\s*(.*?)(?=\\s+to\\s+|₱|PHP|\\d+\\s?km|$)"
        )

        for (p in patterns) {
            val matcher = Pattern.compile(p).matcher(text)
            if (matcher.find()) {
                return matcher.group(2)?.trim().orEmpty()
            }
        }

        return ""
    }

    private fun extractDropoff(text: String): String {
        val patterns = listOf(
            "(?i)(dropoff|drop off|drop-off|destination|receiver|to)\\s*[:\\-]?\\s*(.*?)(?=₱|PHP|fare|distance|\\d+\\s?km|$)",
            "(?i)\\bto\\b\\s*(.*?)(?=₱|PHP|fare|distance|\\d+\\s?km|$)"
        )

        for (p in patterns) {
            val matcher = Pattern.compile(p).matcher(text)
            if (matcher.find()) {
                return matcher.group(2)?.trim().orEmpty()
            }
        }

        return ""
    }

    private fun extractRouteBySeparator(text: String): Pair<String, String> {
        if (!text.contains(" → ")) return Pair("", "")

        val parts = text.split(" → ", limit = 2)
        if (parts.size < 2) return Pair("", "")

        val left = removeNoise(parts[0])
        val right = removeNoise(parts[1])

        return Pair(left, right)
    }

    private fun extractFare(text: String): String {
        val patterns = listOf(
            "₱\\s?\\d+[,.]?\\d*",
            "PHP\\s?\\d+[,.]?\\d*",
            "P\\s?\\d+[,.]?\\d*",
            "(?i)fare\\s*[:\\-]?\\s*₱?\\s?\\d+[,.]?\\d*"
        )

        for (p in patterns) {
            val matcher = Pattern.compile(p).matcher(text)
            if (matcher.find()) {
                return matcher.group()
                    .replace("Fare", "", ignoreCase = true)
                    .trim()
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
            .replace(Regex("PHP\\s?\\d+[,.]?\\d*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("\\d+[.]?\\d*\\s?km", RegexOption.IGNORE_CASE), "")
            .trim()
    }

    private fun cleanPlace(place: String): String {
        return place
            .replace(Regex("(?i)fare.*"), "")
            .replace(Regex("(?i)distance.*"), "")
            .replace(Regex("(?i)new booking.*"), "")
            .replace("|", "")
            .replace("•", "")
            .replace(",", "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun buildSpokenMessage(appName: String, booking: BookingInfo): String {
        return buildString {
            append("$appName booking detected. ")

            if (booking.pickup.isNotBlank()) {
                append("Pickup, ${booking.pickup}. ")
            }

            if (booking.dropoff.isNotBlank()) {
                append("Drop off, ${booking.dropoff}. ")
            }

            if (booking.fare.isNotBlank()) {
                append("Fare, ${booking.fare}. ")
            }

            if (booking.distance.isNotBlank()) {
                append("Distance, ${booking.distance}. ")
            }
        }
    }

    private fun speak(message: String) {
        if (!prefs.getBoolean("voice_enabled", true)) return

        if (!ttsReady || tts == null) {
            tts = TextToSpeech(this, this)
            return
        }

        val volume = prefs.getFloat("voice_volume", 1.0f).coerceIn(0.1f, 1.0f)

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
