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
            textSize = 12f
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
        addTitle("Add Preferred Option")

        addLabel("Choose option type")

        val options = arrayOf("Route", "Fare", "Distance")
        val spinner = Spinner(this)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, options)

        addLabel("Enter value")

        val input = EditText(this).apply {
            hint = "Example: Tanza to Imus / 200 / 5"
        }

        val saveButton = Button(this).apply {
            text = "Save to List"
            setBackgroundColor(orange)
            setTextColor(Color.WHITE)
            setOnClickListener {
                val type = spinner.selectedItem.toString()
                val value = input.text.toString().trim()

                if (value.isBlank()) {
                    Toast.makeText(this@MainActivity, "Please enter a value", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                saveOption(type, value)
                input.setText("")
                Toast.makeText(this@MainActivity, "$type saved", Toast.LENGTH_SHORT).show()
                showSavedList()
            }
        }

        contentArea.addView(spinner)
        contentArea.addView(input)
        contentArea.addView(saveButton)
    }

    private fun saveOption(type: String, value: String) {
        val key = when (type) {
            "Route" -> "saved_routes"
            "Fare" -> "saved_fares"
            "Distance" -> "saved_distances"
            else -> "saved_routes"
        }

        val current = prefs.getString(key, "") ?: ""
        val updated = if (current.isBlank()) {
            value
        } else {
            "$current|$value"
        }

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
