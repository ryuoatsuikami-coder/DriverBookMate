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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "PH")
                tts?.setSpeechRate(0.90f)
            }
        }

        val orange = Color.rgb(255, 103, 0)
        val yellow = Color.rgb(255, 193, 7)
        val dark = Color.rgb(25, 25, 25)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(36, 60, 36, 36)
            setBackgroundColor(orange)
        }

        val title = TextView(this).apply {
            text = "DriverMate PH"
            textSize = 32f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val subtitle = TextView(this).apply {
            text = "Smart booking alerts for drivers"
            textSize = 16f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 30)
        }

        val switchPreferredOnly = Switch(this).apply {
            text = "Read preferred routes only"
            textSize = 18f
            setTextColor(Color.WHITE)
            isChecked = prefs.getBoolean("preferred_only", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("preferred_only", checked).apply()
            }
        }

        val keywordLabel = TextView(this).apply {
            text = "Preferred route keywords"
            textSize = 16f
            setTextColor(Color.WHITE)
            setPadding(0, 24, 0, 8)
        }

        val keywordInput = EditText(this).apply {
            setText(prefs.getString("preferred_keywords", "tanza,cavite city,imus,bacoor,dasma"))
            hint = "Example: tanza,cavite city,imus"
            setTextColor(Color.BLACK)
            setHintTextColor(Color.GRAY)
            setBackgroundColor(Color.WHITE)
            setPadding(20, 10, 20, 10)
        }

        val saveButton = Button(this).apply {
            text = "Save Preferred Routes"
            setBackgroundColor(yellow)
            setTextColor(dark)
            setOnClickListener {
                prefs.edit()
                    .putString("preferred_keywords", keywordInput.text.toString())
                    .apply()

                Toast.makeText(this@MainActivity, "Preferred routes saved", Toast.LENGTH_SHORT).show()
            }
        }

        val allowButton = Button(this).apply {
            text = "Allow Notification Access"
            setBackgroundColor(Color.WHITE)
            setTextColor(orange)
            setOnClickListener {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
        }

        val testButton = Button(this).apply {
            text = "Test Filipino Accent Voice"
            setBackgroundColor(dark)
            setTextColor(Color.WHITE)
            setOnClickListener {
                tts?.speak(
                    "Immediate booking. Tanza to Cavite City. 200 pesos. Distance from you not shown.",
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "driver_mate_test"
                )
            }
        }

        val footer = TextView(this).apply {
            text = "Version 1.0.1"
            textSize = 12f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, 30, 0, 0)
        }

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(switchPreferredOnly)
        layout.addView(keywordLabel)
        layout.addView(keywordInput)
        layout.addView(saveButton)
        layout.addView(allowButton)
        layout.addView(testButton)
        layout.addView(footer)

        setContentView(layout)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
