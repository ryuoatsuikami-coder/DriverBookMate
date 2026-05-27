package com.drivermate.ph

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {

    private var tts: TextToSpeech? = null
    private val prefs by lazy { getSharedPreferences("driver_mate_settings", MODE_PRIVATE) }

    private lateinit var contentArea: LinearLayout

    private val orange = Color.rgb(255, 103, 0)
    private val yellow = Color.rgb(255, 193, 7)
    private val dark = Color.rgb(24, 24, 24)

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
        "Tanza to Imus | Fare 200 | Distance 18 km",
        "Imus to Tanza | Fare 200 | Distance 18 km",
        "Tanza to Cavite City | Fare 250 | Distance 22 km",
        "Cavite City to Tanza | Fare 250 | Distance 22 km",
        "Tanza to Bacoor | Fare 250 | Distance 25 km",
        "Bacoor to Tanza | Fare 250 | Distance 25 km",
        "General Trias to Imus | Fare 180 | Distance 15 km",
        "Imus to General Trias | Fare 180 | Distance 15 km",
        "Dasmarinas to Bacoor | Fare 220 | Distance 20 km",
        "Bacoor to Dasmarinas | Fare 220 | Distance 20 km",
        "Naic to Tanza | Fare 180 | Distance 16 km",
        "Tanza to Naic | Fare 180 | Distance 16 km",
        "Trece Martires to Dasmarinas | Fare 200 | Distance 18 km",
        "Dasmarinas to Trece Martires | Fare 200 | Distance 18 km",

        "Tanza to Manila | Fare 500 | Distance 45 km",
        "Manila to Tanza | Fare 500 | Distance 45 km",
        "Imus to Manila | Fare 400 | Distance 32 km",
        "Manila to Imus | Fare 400 | Distance 32 km",
        "Bacoor to Pasay | Fare 300 | Distance 22 km",
        "Pasay to Bacoor | Fare 300 | Distance 22 km",
        "Dasmarinas to Makati | Fare 500 | Distance 40 km",
        "Makati to Dasmarinas | Fare 500 | Distance 40 km",
        "General Trias to BGC | Fare 550 | Distance 42 km",
        "BGC to General Trias | Fare 550 | Distance 42 km",
        "Cavite City to Manila | Fare 450 | Distance 35 km",
        "Manila to Cavite City | Fare 450 | Distance 35 km"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "PH")
                tts?.setSpeechRate(0.90f)
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(orange)
            setPadding(24, 40, 24, 24)
        }

        val title = TextView(this).apply {
            text = "DriverMate PH"
            textSize = 30f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Smart booking alerts for drivers"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 6, 0, 20)
        }

        val tabRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        contentArea = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 24, 20, 20)
            setBackgroundColor(Color.WHITE)
        }

        tabRow.addView(makeTabButton("Home") { showHome() })
        tabRow.addView(makeTabButton("Add") { showAddOptions() })
        tabRow.addView(makeTabButton("Suggest") { showSuggestedRoutes() })
        tabRow.addView(makeTabButton("Saved") { showSavedList() })

        root.addView(title)
        root.addView(subtitle)
        root.addView(tabRow)
        root.addView(contentArea)

        setContentView(root)
        showHome()
    }

    private fun makeTabButton(label: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 11f
            setTextColor(dark)
            setBackgroundColor(yellow)
            setOnClickListener { action() }
        }
    }

    private fun clearContent() {
        contentArea.removeAllViews()
    }

    private fun addTitle(text: String) {
        contentArea.addView(TextView(this).apply {
            this.text = text
            textSize = 22f
            setTextColor(dark)
            setPadding(0, 0, 0, 16)
        })
    }

    private fun addLabel(text: String) {
        contentArea.addView(TextView(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.DKGRAY)
            setPadding(0, 12, 0, 6)
        })
    }

    private fun showHome() {
        clearContent()
        addTitle("Driver Setup")

        val preferredOnly = Switch(this).apply {
            text = "Read preferred saved options only"
            textSize = 16f
            isChecked = prefs.getBoolean("preferred_only", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("preferred_only", checked).apply()
            }
        }

        val allowButton = Button(this).apply {
            text = "Allow Notification Access"
            setBackgroundColor(orange)
            setTextColor(Color.WHITE)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val testButton = Button(this).apply {
            text = "Test Voice"
            setBackgroundColor(dark)
            setTextColor(Color.WHITE)
            setOnClickListener {
                tts?.speak(
                    "Priority booking. From Tanza to Imus. Fare 200 pesos.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "driver_mate_test"
                )
            }
        }

        contentArea.addView(preferredOnly)
        contentArea.addView(allowButton)
        contentArea.addView(testButton)
    }

    private fun showAddOptions() {
        clearContent()
        addTitle("Create Route")

        addLabel("From")

        val fromSpinner = Spinner(this)
        fromSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            cavitePlaces + manilaPlaces
        )

        addLabel("To")

        val toSpinner = Spinner(this)
        toSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            cavitePlaces + manilaPlaces
        )

        addLabel("Fare")

        val fareInput = EditText(this).apply {
            hint = "Example: 200"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        addLabel("Distance in km")

        val distanceInput = EditText(this).apply {
            hint = "Example: 18"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val saveButton = Button(this).apply {
            text = "Save Route"
            setBackgroundColor(orange)
            setTextColor(Color.WHITE)
            setOnClickListener {
                val from = fromSpinner.selectedItem.toString()
                val to = toSpinner.selectedItem.toString()
                val fare = fareInput.text.toString().trim()
                val distance = distanceInput.text.toString().trim()

                if (from == to) {
                    Toast.makeText(this@MainActivity, "Choose different places", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                saveRouteFareDistance(from, to, fare, distance)

                Toast.makeText(this@MainActivity, "Route saved", Toast.LENGTH_SHORT).show()
                showSavedList()
            }
        }

        contentArea.addView(fromSpinner)
        contentArea.addView(toSpinner)
        contentArea.addView(fareInput)
        contentArea.addView(distanceInput)
        contentArea.addView(saveButton)
    }

    private fun showSuggestedRoutes() {
        clearContent()
        addTitle("Suggested Routes")

        addLabel("Tap one suggested route to save automatically")

        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            suggestedRoutes
        )

        val addButton = Button(this).apply {
            text = "Add Selected Route"
            setBackgroundColor(orange)
            setTextColor(Color.WHITE)
            setOnClickListener {
                val selected = spinner.selectedItem.toString()
                saveSuggestedRoute(selected)
                Toast.makeText(this@MainActivity, "Suggested route saved", Toast.LENGTH_SHORT).show()
                showSavedList()
            }
        }

        contentArea.addView(spinner)
        contentArea.addView(addButton)
    }

    private fun saveSuggestedRoute(selected: String) {
        val parts = selected.split("|").map { it.trim() }

        val route = parts.getOrNull(0)?.trim() ?: ""
        val fare = parts.getOrNull(1)?.replace("Fare", "")?.trim() ?: ""
        val distance = parts.getOrNull(2)?.replace("Distance", "")?.replace("km", "")?.trim() ?: ""

        if (route.isNotBlank()) saveOption("saved_routes", route)
        if (fare.isNotBlank()) saveOption("saved_fares", fare)
        if (distance.isNotBlank()) saveOption("saved_distances", distance)
    }

    private fun saveRouteFareDistance(from: String, to: String, fare: String, distance: String) {
        val route = "$from to $to"
        saveOption("saved_routes", route)

        if (fare.isNotBlank()) saveOption("saved_fares", fare)
        if (distance.isNotBlank()) saveOption("saved_distances", distance)
    }

    private fun saveOption(key: String, value: String) {
        val current = prefs.getString(key, "") ?: ""

        val alreadySaved = current.split("|")
            .map { it.trim().lowercase() }
            .contains(value.trim().lowercase())

        if (alreadySaved) return

        val updated = if (current.isBlank()) value else "$current|$value"
        prefs.edit().putString(key, updated).apply()
    }

    private fun showSavedList() {
        clearContent()
        addTitle("Saved Places List")

        addSavedSection("Routes", "saved_routes")
        addSavedSection("Fares", "saved_fares")
        addSavedSection("Distances", "saved_distances")

        val clearButton = Button(this).apply {
            text = "Delete All Saved"
            setBackgroundColor(dark)
            setTextColor(Color.WHITE)
            setOnClickListener {
                prefs.edit()
                    .remove("saved_routes")
                    .remove("saved_fares")
                    .remove("saved_distances")
                    .apply()

                Toast.makeText(this@MainActivity, "Saved list cleared", Toast.LENGTH_SHORT).show()
                showSavedList()
            }
        }

        contentArea.addView(clearButton)
    }

    private fun addSavedSection(title: String, key: String) {
        addLabel(title)

        val saved = prefs.getString(key, "") ?: ""

        if (saved.isBlank()) {
            contentArea.addView(TextView(this).apply {
                text = "No saved $title yet"
                textSize = 14f
                setTextColor(Color.GRAY)
            })
            return
        }

        saved.split("|").filter { it.isNotBlank() }.forEach { item ->
            contentArea.addView(TextView(this).apply {
                text = "• $item"
                textSize = 16f
                setTextColor(dark)
                setPadding(0, 4, 0, 4)
            })
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
