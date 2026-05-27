package com.drivermate.ph

import android.app.Notification
import android.content.Intent
import android.media.AudioAttributes
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
    private val handler = Handler(Looper.getMainLooper())

    private val allPlaces = listOf(
        // Cavite
        "Alfonso", "Amadeo", "Bacoor", "Carmona", "Cavite City",
        "Dasmarinas", "Dasma", "General Trias", "Gen Trias", "Imus",
        "Indang", "Kawit", "Magallanes Cavite", "Maragondon", "Mendez",
        "Naic", "Noveleta", "Rosario", "Silang", "Tagaytay", "Tanza",
        "Ternate", "Trece Martires",

        // Metro Manila
        "Manila", "Caloocan", "Las Pinas", "Makati", "Malabon",
        "Mandaluyong", "Marikina", "Muntinlupa", "Navotas", "Paranaque",
        "Pasay", "Pasig", "Pateros", "Quezon City", "QC", "San Juan",
        "Taguig", "Valenzuela", "BGC", "Bonifacio Global City", "Alabang",

        // Laguna
        "Alaminos Laguna", "Bay", "Binan", "Cabuyao", "Calamba",
        "Calauan", "Cavinti", "Famy", "Kalayaan", "Liliw", "Los Banos",
        "Luisiana", "Lumban", "Mabitac", "Magdalena", "Majayjay",
        "Nagcarlan", "Paete", "Pagsanjan", "Pakil", "Pangil", "Pila",
        "Rizal Laguna", "San Pablo", "San Pedro", "Santa Cruz Laguna",
        "Santa Maria Laguna", "Santa Rosa", "Siniloan", "Victoria",

        // Batangas
        "Agoncillo", "Alitagtag", "Balayan", "Balete", "Batangas City",
        "Bauan", "Calaca", "Calatagan", "Cuenca", "Ibaan", "Laurel",
        "Lemery", "Lian", "Lipa", "Lobo", "Mabini", "Malvar",
        "Mataasnakahoy", "Nasugbu", "Padre Garcia", "Rosario Batangas",
        "San Jose Batangas", "San Juan Batangas", "San Luis Batangas",
        "San Nicolas", "San Pascual", "Santa Teresita",
        "Santo Tomas Batangas", "Taal", "Talisay Batangas", "Tanauan",
        "Taysan", "Tingloy", "Tuy",

        // Bulacan
        "Angat", "Balagtas", "Baliwag", "Bocaue", "Bulakan", "Bustos",
        "Calumpit", "Dona Remedios Trinidad", "Guiguinto", "Hagonoy",
        "Malolos", "Marilao", "Meycauayan", "Norzagaray", "Obando",
        "Pandi", "Paombong", "Plaridel", "Pulilan", "San Ildefonso",
        "San Jose del Monte", "San Miguel", "San Rafael",
        "Santa Maria Bulacan",

        // Pampanga
        "Angeles", "Apalit", "Arayat", "Bacolor", "Candaba",
        "Floridablanca", "Guagua", "Lubao", "Mabalacat", "Macabebe",
        "Magalang", "Masantol", "Mexico", "Minalin", "Porac",
        "San Fernando Pampanga", "San Luis Pampanga", "San Simon",
        "Santa Ana Pampanga", "Santa Rita", "Santo Tomas Pampanga",
        "Sasmuan"
    )

    override fun onCreate() {
        super.onCreate()
        initTts()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        initTts()
        handler.postDelayed({
            speakNow("DriverMate PH notification reader is active.")
        }, 800)
    }

    private fun initTts() {
        if (tts != null) return

        tts = TextToSpeech(applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true

                val langResult = tts?.setLanguage(Locale("en", "PH"))
                if (
                    langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    tts?.language = Locale.US
                }

                tts?.setSpeechRate(0.85f)
                tts?.setPitch(1.03f)

                // IMPORTANT:
                // This makes TTS follow the phone MEDIA VOLUME.
                tts?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
            }
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        initTts()

        val appPackage = sbn.packageName.lowercase()
        val prefs = getSharedPreferences("driver_mate_settings", MODE_PRIVATE)

        val enabled = when {
            appPackage.contains("lalamove") -> prefs.getBoolean("enable_lalamove", true)
            appPackage.contains("grab") -> prefs.getBoolean("enable_grab", true)
            appPackage.contains("transportify") -> prefs.getBoolean("enable_transportify", true)
            appPackage.contains("moveit") || appPackage.contains("move.it") -> prefs.getBoolean("enable_moveit", true)
            else -> false
        }

        if (!enabled) return
        if (!prefs.getBoolean("voice_enabled", true)) return

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
            .replace("→", " to ")
            .replace("➡", " to ")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (fullText.isBlank()) return

        val bookingType = detectBookingType(fullText)
        val route = detectRoute(fullText)
        val fare = detectFare(fullText)
        val distance = detectDistance(fullText)

        val speakAllRoutes = prefs.getBoolean("speak_all_routes", false)
        val preferred = isPreferredRoute(route)
        val firstPriority = getFirstPriorityRoute()

        if (!speakAllRoutes && !preferred) return

        val cleanFare = if (fare == "not detected") "not detected" else "$fare pesos"
        val cleanDistance = if (distance == "not detected") "based on app notification" else "$distance kilometers"

        val message = "$bookingType booking. $route. Fare $cleanFare. Distance $cleanDistance."

        speakNow(message)

        val autoOpenWaze = prefs.getBoolean("auto_open_waze", true)

        if (
            autoOpenWaze &&
            route != "Route not detected" &&
            firstPriority.equals(route, true)
        ) {
            handler.postDelayed({
                openWaze(route)
            }, 5000)
        }
    }

    private fun speakNow(message: String) {
        initTts()

        handler.postDelayed({
            if (ttsReady) {
                tts?.stop()
                tts?.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "booking_alert_${System.currentTimeMillis()}"
                )
            } else {
                handler.postDelayed({
                    tts?.speak(
                        message,
                        TextToSpeech.QUEUE_FLUSH,
                        null,
                        "booking_alert_retry_${System.currentTimeMillis()}"
                    )
                }, 1500)
            }
        }, 300)
    }

    private fun detectBookingType(text: String): String {
        val lower = text.lowercase()

        return when {
            lower.contains("priority") -> "Priority"
            lower.contains("preferred") -> "Preferred"
            lower.contains("immediate") -> "Immediate"
            lower.contains("regular") -> "Regular"
            lower.contains("pooling") -> "Pooling"
            lower.contains("rush") -> "Priority"
            else -> "Booking"
        }
    }

    private fun detectRoute(text: String): String {
        val cleanText = text
            .replace("\n", " ")
            .replace("→", " to ")
            .replace("➡", " to ")
            .replace(" - ", " to ")
            .replace(Regex("\\s+"), " ")
            .trim()

        val lower = cleanText.lowercase()

        val normalizedPlaces = allPlaces
            .map { raw -> raw to normalizePlace(raw) }
            .distinctBy { it.second.lowercase() }

        for ((rawFrom, cleanFrom) in normalizedPlaces) {
            for ((rawTo, cleanTo) in normalizedPlaces) {
                if (cleanFrom.equals(cleanTo, true)) continue

                val pattern = Regex(
                    """\b${Regex.escape(rawFrom)}\b\s*(to|going to|drop.?off|destination|→|➡)\s*\b${Regex.escape(rawTo)}\b""",
                    RegexOption.IGNORE_CASE
                )

                if (pattern.containsMatchIn(cleanText)) {
                    return "$cleanFrom to $cleanTo"
                }
            }
        }

        for (saved in getSavedRoutes()) {
            val parts = saved.route.split(" to ", ignoreCase = true)

            if (parts.size == 2) {
                val savedFrom = normalizePlace(parts[0])
                val savedTo = normalizePlace(parts[1])

                if (
                    !savedFrom.equals(savedTo, true) &&
                    lower.contains(savedFrom.lowercase()) &&
                    lower.contains(savedTo.lowercase())
                ) {
                    return "$savedFrom to $savedTo"
                }
            }
        }

        val found = normalizedPlaces
            .mapNotNull { (raw, clean) ->
                val index = lower.indexOf(raw.lowercase())
                if (index >= 0) index to clean else null
            }
            .sortedBy { it.first }
            .map { it.second }
            .distinctBy { it.lowercase() }

        return if (found.size >= 2) {
            "${found[0]} to ${found[1]}"
        } else {
            "Route not detected"
        }
    }

    private fun normalizePlace(place: String): String {
        return when (place.trim().lowercase()) {
            "gen trias" -> "General Trias"
            "dasma" -> "Dasmarinas"
            "qc" -> "Quezon City"
            "bgc" -> "BGC"
            "bonifacio global city" -> "BGC"
            else -> place.trim()
        }
    }

    private fun detectFare(text: String): String {
        val patterns = listOf(
            Regex("""(?:fare|amount|total|price|fee)\s*[:\-]?\s*(?:pesos)?\s*([0-9]{2,6})""", RegexOption.IGNORE_CASE),
            Regex("""(?:pesos)\s*([0-9]{2,6})""", RegexOption.IGNORE_CASE),
            Regex("""([0-9]{2,6})\s*(?:pesos)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) return match.groupValues[1]
        }

        return "not detected"
    }

    private fun detectDistance(text: String): String {
        val patterns = listOf(
            Regex("""([0-9]+(?:\.[0-9]+)?)\s*(?:km|kilometer|kilometers)""", RegexOption.IGNORE_CASE)
        )

        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) return match.groupValues[1]
        }

        return "not detected"
    }

    private fun isPreferredRoute(route: String): Boolean {
        if (route == "Route not detected") return false

        return getSavedRoutes().any {
            it.route.equals(route, true)
        }
    }

    private fun getFirstPriorityRoute(): String {
        val prefs = getSharedPreferences("driver_mate_settings", MODE_PRIVATE)
        return prefs.getString("first_priority_route", "") ?: ""
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

    private fun openWaze(route: String) {
        val destination = extractDestination(route)
        if (destination.isBlank()) return

        val encodedDestination = Uri.encode(destination)
        val wazeUri = Uri.parse("waze://?q=$encodedDestination&navigate=yes")

        val intent = Intent(Intent.ACTION_VIEW, wazeUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://waze.com/ul?q=$encodedDestination&navigate=yes")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

    private fun extractDestination(route: String): String {
        val parts = route.split(" to ", ignoreCase = true)
        return if (parts.size >= 2) parts[1].trim() else route.trim()
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
