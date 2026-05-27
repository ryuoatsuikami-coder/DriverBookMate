package com.drivermate.ph

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.text.InputType
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {

    private var tts: TextToSpeech? = null
    private val prefs by lazy { getSharedPreferences("driver_mate_settings", MODE_PRIVATE) }

    private lateinit var contentArea: LinearLayout

    private val orange = Color.rgb(255, 90, 0)
    private val orange2 = Color.rgb(255, 138, 0)
    private val yellow = Color.rgb(255, 210, 0)
    private val dark = Color.rgb(25, 25, 25)

    private val cavitePlaces = arrayOf(
        "Tanza", "Imus", "Bacoor", "Dasmarinas", "General Trias",
        "Cavite City", "Trece Martires", "Kawit", "Noveleta",
        "Rosario", "Naic", "Silang", "Tagaytay"
    )

    private val manilaPlaces = arrayOf(
        "Manila", "Pasay", "Makati", "BGC", "Taguig",
        "Paranaque", "Las Pinas", "Alabang", "Quezon City"
    )

    private val suggestedRoutes = arrayOf(
        "Tanza to Imus | Fare 200 | Distance 18",
        "Imus to Tanza | Fare 200 | Distance 18",
        "Tanza to Cavite City | Fare 250 | Distance 22",
        "Cavite City to Tanza | Fare 250 | Distance 22",
        "Tanza to Bacoor | Fare 250 | Distance 25",
        "Bacoor to Tanza | Fare 250 | Distance 25",
        "General Trias to Imus | Fare 180 | Distance 15",
        "Imus to General Trias | Fare 180 | Distance 15",
        "Dasmarinas to Bacoor | Fare 220 | Distance 20",
        "Bacoor to Dasmarinas | Fare 220 | Distance 20",
        "Naic to Tanza | Fare 180 | Distance 16",
        "Tanza to Naic | Fare 180 | Distance 16",
        "Trece Martires to Dasmarinas | Fare 200 | Distance 18",
        "Dasmarinas to Trece Martires | Fare 200 | Distance 18",

        "Tanza to Manila | Fare 500 | Distance 45",
        "Manila to Tanza | Fare 500 | Distance 45",
        "Imus to Manila | Fare 400 | Distance 32",
        "Manila to Imus | Fare 400 | Distance 32",
        "Bacoor to Pasay | Fare 300 | Distance 22",
        "Pasay to Bacoor | Fare 300 | Distance 22",
        "Dasmarinas to Makati | Fare 500 | Distance 40",
        "Makati to Dasmarinas | Fare 500 | Distance 40",
        "General Trias to BGC | Fare 550 | Distance 42",
        "BGC to General Trias | Fare 550 | Distance 42",
        "Cavite City to Manila | Fare 450 | Distance 35",
        "Manila to Cavite City | Fare 450 | Distance 35"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "PH")
                tts?.setSpeechRate(0.88f)
                tts?.setPitch(1.02f)
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(orange)
            setPadding(24, 36, 24, 24)
        }

        val logo = TextView(this).apply {
            text = "🚚"
            textSize = 42f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "DriverMate PH"
            textSize = 30f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
        }

        val subtitle = TextView(this).apply {
            text = "Smart booking alerts for drivers"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            setPadding(0, 4, 0, 18)
        }

        val tabRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        contentArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 22)
            setBackgroundColor(Color.WHITE)
        }

        tabRow.addView(tab("Home") { showHome() })
        tabRow.addView(tab("Add") { showAdd() })
        tabRow.addView(tab("Suggest") { showSuggest() })
        tabRow.addView(tab("Saved") { showSaved() })

        root.addView(logo)
        root.addView(title)
        root.addView(subtitle)
        root.addView(tabRow)
        root.addView(contentArea)

        setContentView(root)
        showHome()
    }

    private fun tab(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 12f
            setTextColor(Color.BLACK)
            setBackgroundColor(yellow)
            setOnClickListener { action() }
        }
    }

    private fun clear() {
        contentArea.removeAllViews()
    }

    private fun title(text: String) {
        contentArea.addView(TextView(this).apply {
            this.text = text
            textSize = 23f
            setTextColor(dark)
            setPadding(0, 0, 0, 18)
        })
    }

    private fun label(text: String) {
        contentArea.addView(TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(0, 10, 0, 5)
        })
    }

    private fun showHome() {
        clear()
        title("Driver Setup")

        val switch = Switch(this).apply {
            text = "Read preferred saved options only"
            textSize = 16f
            isChecked = prefs.getBoolean("preferred_only", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("preferred_only", checked).apply()
            }
        }

        val allow = Button(this).apply {
            text = "Allow Notification Access"
            setTextColor(Color.WHITE)
            setBackgroundColor(orange2)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val test = Button(this).apply {
            text = "Test Voice"
            setTextColor(Color.WHITE)
            setBackgroundColor(dark)
            setOnClickListener {
                speakBooking("Priority", "Tanza to Imus", "200", "18")
            }
        }

        contentArea.addView(switch)
        contentArea.addView(allow)
        contentArea.addView(test)
    }

    private fun showAdd() {
        clear()
        title("Create Route")

        val places = cavitePlaces + manilaPlaces

        label("From")
        val from = Spinner(this)
        from.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, places)

        label("To")
        val to = Spinner(this)
        to.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, places)

        label("Fare")
        val fare = EditText(this).apply {
            hint = "Example: 200"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        label("Distance in km")
        val distance = EditText(this).apply {
            hint = "Example: 18"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val save = Button(this).apply {
            text = "Save Route"
            setTextColor(Color.WHITE)
            setBackgroundColor(orange)
            setOnClickListener {
                val route = "${from.selectedItem} to ${to.selectedItem}"

                if (from.selectedItem.toString() == to.selectedItem.toString()) {
                    Toast.makeText(this@MainActivity, "Choose different places", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                saveFullRoute(route, fare.text.toString(), distance.text.toString())
                Toast.makeText(this@MainActivity, "Route saved", Toast.LENGTH_SHORT).show()
                showSaved()
            }
        }

        contentArea.addView(from)
        contentArea.addView(to)
        contentArea.addView(fare)
        contentArea.addView(distance)
        contentArea.addView(save)
    }

    private fun showSuggest() {
        clear()
        title("Suggested Routes")

        label("Choose a suggested route")

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, suggestedRoutes)

        val add = Button(this).apply {
            text = "Add Selected Route"
            setTextColor(Color.WHITE)
            setBackgroundColor(orange)
            setOnClickListener {
                val selected = spinner.selectedItem.toString()
                val data = parseSuggested(selected)
                saveFullRoute(data.route, data.fare, data.distance)
                Toast.makeText(this@MainActivity, "Suggested route saved", Toast.LENGTH_SHORT).show()
                showSaved()
            }
        }

        contentArea.addView(spinner)
        contentArea.addView(add)
    }

    private fun showSaved() {
        clear()
        title("Saved Places List")

        val saved = getSavedRoutes()

        if (saved.isEmpty()) {
            label("No saved routes yet")
        } else {
            saved.forEach {
                contentArea.addView(TextView(this).apply {
                    text = "• ${it.route} | Fare ${it.fare} | Distance ${it.distance} km"
                    textSize = 15f
                    setTextColor(dark)
                    setPadding(0, 5, 0, 5)
                })
            }
        }

        val clearBtn = Button(this).apply {
            text = "Delete All Saved"
            setTextColor(Color.WHITE)
            setBackgroundColor(dark)
            setOnClickListener {
                prefs.edit().remove("saved_full_routes").apply()
                showSaved()
            }
        }

        contentArea.addView(clearBtn)
    }

    private fun speakBooking(type: String, route: String, fare: String, distance: String) {
        val cleanType = if (type.isBlank()) "Booking" else type
        val cleanRoute = if (route.isBlank()) "Route not detected" else route
        val cleanFare = if (fare.isBlank()) "not detected" else fare
        val cleanDistance = if (distance.isBlank()) "not detected" else distance

        val message = "$cleanType booking. $cleanRoute. Fare $cleanFare pesos. Distance $cleanDistance kilometers."

        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "driver_mate_voice")
    }

    private fun saveFullRoute(route: String, fare: String, distance: String) {
        val cleanRoute = route.trim()
        val cleanFare = fare.trim().replace("Fare", "").replace("pesos", "").trim()
        val cleanDistance = distance.trim().replace("Distance", "").replace("km", "").trim()

        val current = prefs.getString("saved_full_routes", "") ?: ""
        val newItem = "$cleanRoute~$cleanFare~$cleanDistance"

        if (current.lowercase().contains(cleanRoute.lowercase())) return

        val updated = if (current.isBlank()) newItem else "$current|$newItem"
        prefs.edit().putString("saved_full_routes", updated).apply()
    }

    private fun getSavedRoutes(): List<RouteData> {
        val raw = prefs.getString("saved_full_routes", "") ?: ""
        if (raw.isBlank()) return emptyList()

        return raw.split("|").mapNotNull {
            val p = it.split("~")
            if (p.size >= 3) RouteData(p[0], p[1], p[2]) else null
        }
    }

    private fun parseSuggested(text: String): RouteData {
        val p = text.split("|").map { it.trim() }
        val route = p.getOrNull(0) ?: ""
        val fare = p.getOrNull(1)?.replace("Fare", "")?.trim() ?: ""
        val distance = p.getOrNull(2)?.replace("Distance", "")?.trim() ?: ""
        return RouteData(route, fare, distance)
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
