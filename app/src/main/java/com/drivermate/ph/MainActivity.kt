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

        val homeTab = makeTabButton("Home") { showHome() }
        val routeTab = makeTabButton("Routes") { showRoutes() }
        val fareTab = makeTabButton("Fare") { showFare() }
        val distanceTab = makeTabButton("Distance") { showDistance() }

        tabRow.addView(homeTab)
        tabRow.addView(routeTab)
        tabRow.addView(fareTab)
        tabRow.addView(distanceTab)

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
            text = "Read preferred routes only"
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
                    "Priority. From Tanza to Imus. Fare 200 pesos.",
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

    private fun showRoutes() {
        clearContent()
        addTitle("Preferred Routes")

        addLabel("Enter preferred places separated by comma")

        val input = EditText(this).apply {
            setText(prefs.getString("preferred_keywords", "tanza,imus,cavite city,bacoor,dasma"))
            hint = "Example: tanza, imus, cavite city"
        }

        val save = Button(this).apply {
            text = "Save Routes"
            setBackgroundColor(orange)
            setTextColor(Color.WHITE)
            setOnClickListener {
                prefs.edit().putString("preferred_keywords", input.text.toString()).apply()
                Toast.makeText(this@MainActivity, "Preferred routes saved", Toast.LENGTH_SHORT).show()
            }
        }

        contentArea.addView(input)
        contentArea.addView(save)
    }

    private fun showFare() {
        clearContent()
        addTitle("Fare Settings")

        addLabel("Minimum fare in pesos")

        val input = EditText(this).apply {
            setText(prefs.getString("minimum_fare", "200"))
            hint = "Example: 200"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        val save = Button(this).apply {
            text = "Save Fare"
            setBackgroundColor(orange)
            setTextColor(Color.WHITE)
            setOnClickListener {
                prefs.edit().putString("minimum_fare", input.text.toString()).apply()
                Toast.makeText(this@MainActivity, "Minimum fare saved", Toast.LENGTH_SHORT).show()
            }
        }

        contentArea.addView(input)
        contentArea.addView(save)
    }

    private fun showDistance() {
        clearContent()
        addTitle("Preferred Distance")

        addLabel("Maximum pickup distance in kilometers")

        val input = EditText(this).apply {
            setText(prefs.getString("maximum_distance", "5"))
            hint = "Example: 5"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        val save = Button(this).apply {
            text = "Save Distance"
            setBackgroundColor(orange)
            setTextColor(Color.WHITE)
            setOnClickListener {
                prefs.edit().putString("maximum_distance", input.text.toString()).apply()
                Toast.makeText(this@MainActivity, "Preferred distance saved", Toast.LENGTH_SHORT).show()
            }
        }

        contentArea.addView(input)
        contentArea.addView(save)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
