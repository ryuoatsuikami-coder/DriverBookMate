package com.drivermate.ph

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("driver_mate_settings", MODE_PRIVATE) }
    private var tts: TextToSpeech? = null

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var scrollView: ScrollView

    private val blue = Color.rgb(0, 122, 255)
    private val green = Color.rgb(32, 190, 95)
    private val red = Color.rgb(230, 35, 35)
    private val bg = Color.rgb(248, 250, 253)
    private val cardStroke = Color.rgb(220, 230, 245)
    private val dark = Color.rgb(20, 20, 25)
    private val gray = Color.rgb(120, 120, 130)
    private val lightGray = Color.rgb(245, 247, 250)

    private val savedRoutes = mutableListOf(
        "General Trias → Bacoor",
        "Noveleta → Trece Martires",
        "Tanza → Kawit",
        "Bacoor → Tagaytay"
    )

    private val suggestedRoutes = mutableListOf(
        "Cavite → Manila",
        "Dasma → Alabang",
        "Gen. Trias → Taguig"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupVoice()
        setupLayout()
        showHome()
    }

    private fun setupVoice() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "PH")
                tts?.setSpeechRate(0.88f)
                tts?.setPitch(1.02f)
            }
        }
    }

    private fun setupLayout() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(bg)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(10))
        }

        scrollView.addView(content)
        root.addView(scrollView)
        root.addView(bottomNavigation())

        setContentView(root)
    }

    private fun clear() {
        content.removeAllViews()
        scrollView.post { scrollView.scrollTo(0, 0) }
    }

    // ================= HOME =================

    private fun showHome() {
        clear()
        homeHeader()
        voiceAssistantCard()
        heroCard()
        bookingAppsCard()
        priorityRouteCard()
    }

    private fun homeHeader() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(TextView(this).apply {
            text = "☰"
            textSize = 28f
            setTextColor(dark)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(44), dp(44)))

        row.addView(TextView(this).apply {
            text = "DriverMate "
            textSize = 23f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(TextView(this).apply {
            text = "PH"
            textSize = 23f
            setTextColor(blue)
            setTypeface(null, Typeface.BOLD)
        })

        content.addView(row)
    }

    private fun voiceAssistantCard() {
        val card = cardBox().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(16), dp(22), dp(16))
        }

        card.addView(TextView(this).apply {
            text = "▌▌▌▌"
            textSize = 26f
            setTextColor(green)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(80), -2))

        card.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = "Voice Assistant Active"
                textSize = 15f
                setTextColor(green)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = "Listening for bookings from your enabled apps"
                textSize = 13f
                setTextColor(gray)
            })
        })

        content.addView(card, margin(0, dp(16), 0, dp(16)))
    }

    private fun heroCard() {
        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(26), dp(26), dp(26), dp(20))
            setBackgroundColor(Color.rgb(232, 244, 255))
        }

        card.addView(TextView(this).apply {
            text = "DriverMate PH\nis active"
            textSize = 25f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(28), 0, 0)
        }

        buttons.addView(outlineButton("🔊  Test Voice") {
            speak("Voice assistant is active. Booking alerts are ready.")
        }, LinearLayout.LayoutParams(0, dp(46), 1f))

        buttons.addView(blueButton("🔇  Turn Off") {
            prefs.edit().putBoolean("voice_enabled", false).apply()
            Toast.makeText(this, "Voice turned off", Toast.LENGTH_SHORT).show()
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            setMargins(dp(14), 0, 0, 0)
        })

        card.addView(buttons)
        content.addView(card, margin(0, 0, 0, dp(18)))
    }

    private fun bookingAppsCard() {
        content.addView(appRow("🚚", "LalaMove", "Get delivery bookings from LalaMove", "enable_lalamove"))
        content.addView(appRow("🚗", "Grab", "Get delivery bookings from Grab", "enable_grab"))
        content.addView(appRow("🚙", "Transportify", "Get delivery bookings from Transportify", "enable_transportify"))
    }

    private fun appRow(icon: String, title: String, desc: String, key: String): LinearLayout {
        val card = cardBox().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        card.addView(TextView(this).apply {
            text = icon
            textSize = 34f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(76), dp(70)))

        card.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 16f
                setTextColor(if (title == "LalaMove") Color.rgb(255, 100, 35) else green)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = desc
                textSize = 13f
                setTextColor(gray)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))

        card.addView(Switch(this).apply {
            isChecked = prefs.getBoolean(key, true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(key, checked).apply()
            }
        })

        card.addView(TextView(this).apply {
            text = "›"
            textSize = 28f
            setTextColor(gray)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(28), -1))

        return card.apply {
            layoutParams = margin(0, dp(4), 0, dp(4))
        }
    }

    // ================= ROUTES =================

    private fun showRoutes() {
        clear()
        pageTitle("Routes")
        priorityRouteCard()
        savedRoutesSection()
        suggestedRoutesSection()
    }

    private fun priorityRouteCard() {
        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(18), dp(24), dp(18))
        }

        card.addView(TextView(this).apply {
            text = "★  Priority Route"
            textSize = 16f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })

        card.addView(TextView(this).apply {
            text = prefs.getString("first_priority_route", "Kawit → General Trias")
            textSize = 23f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(14), 0, 0)
        })

        card.addView(TextView(this).apply {
            text = "Only this selected route can auto-open Waze"
            textSize = 13f
            setTextColor(gray)
            setPadding(0, dp(4), 0, dp(16))
        })

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        row.addView(outlineButton("🗑  Clear Priority") {
            prefs.edit().remove("first_priority_route").apply()
            showRoutes()
        }, LinearLayout.LayoutParams(0, dp(46), 1f))

        row.addView(outlineButton("✎  Change Route") {
            Toast.makeText(this, "Select from saved routes", Toast.LENGTH_SHORT).show()
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            setMargins(dp(14), 0, 0, 0)
        })

        card.addView(row)
        content.addView(card, margin(0, dp(8), 0, dp(18)))
    }

    private fun savedRoutesSection() {
        sectionTitle("Saved Routes")

        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(10), dp(24), dp(10))
        }

        savedRoutes.forEach { route ->
            card.addView(routeListRow(route, true))
        }

        content.addView(card, margin(0, dp(8), 0, dp(18)))
    }

    private fun suggestedRoutesSection() {
        sectionTitle("Suggested Routes")

        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(10), dp(24), dp(10))
        }

        suggestedRoutes.forEach { route ->
            card.addView(routeListRow(route, false))
        }

        content.addView(card)
    }

    private fun routeListRow(route: String, saved: Boolean): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
        }

        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = route
                textSize = 15f
                setTextColor(dark)
            })

            if (!saved) {
                addView(TextView(this@MainActivity).apply {
                    text = "₱380 - ₱650        30 km"
                    textSize = 13f
                    setTextColor(gray)
                })
            }
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(TextView(this).apply {
            text = if (saved) "🗑" else "⊕"
            textSize = 24f
            setTextColor(blue)
            gravity = Gravity.CENTER
            setOnClickListener {
                if (saved) savedRoutes.remove(route) else savedRoutes.add(route)
                showRoutes()
            }
        }, LinearLayout.LayoutParams(dp(44), dp(44)))

        return row
    }

    // ================= ALERTS =================

    private fun showAlerts() {
        clear()
        pageTitle("Alerts")
        sectionTitleRed("Current Alert")
        currentAlertCard()
        sectionTitle("Received Alerts")
        receivedAlert("🟧", "Lalamove booking detected", "Route: Cavite to Manila", "2 mins ago")
        receivedAlert("🟩", "Grab booking detected", "Route: Noveleta to Trece Martires", "8 mins ago")
        receivedAlert("🟩", "Transportify booking detected", "Route: Tanza to Kawit", "15 mins ago")
        sectionTitle("Missed/Ignored Alerts")
        missedAlertCard()
    }

    private fun currentAlertCard() {
        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(18))
        }

        card.addView(TextView(this).apply {
            text = "🟧  Lalamove Booking\nDetected   NEW"
            textSize = 22f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })

        card.addView(TextView(this).apply {
            text = "\n📍 Cavite → Manila\n💵 Fare: ₱580\n🕘 Received: 2 mins ago"
            textSize = 15f
            setTextColor(dark)
        })

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(18), 0, 0)
        }

        row.addView(outlineButton("🔊 Speak Again") {
            speak("Lalamove booking detected. Cavite to Manila. Fare 580 pesos.")
        }, LinearLayout.LayoutParams(0, dp(46), 1f))

        row.addView(blueButton("↗ Open App") {
            Toast.makeText(this, "Opening app", Toast.LENGTH_SHORT).show()
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            setMargins(dp(14), 0, 0, 0)
        })

        card.addView(row)
        content.addView(card, margin(0, dp(8), 0, dp(16)))
    }

    private fun receivedAlert(icon: String, title: String, route: String, time: String) {
        val card = cardBox().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }

        card.addView(TextView(this).apply {
            text = icon
            textSize = 28f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(48), dp(48)))

        card.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 14f
                setTextColor(dark)
                setTypeface(null, Typeface.BOLD)
            })
            addView(TextView(this@MainActivity).apply {
                text = route
                textSize = 12f
                setTextColor(gray)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))

        card.addView(TextView(this).apply {
            text = "$time  ●"
            textSize = 11f
            setTextColor(green)
        })

        content.addView(card, margin(0, dp(4), 0, dp(4)))
    }

    private fun missedAlertCard() {
        val card = cardBox().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(18))
        }

        card.addView(TextView(this).apply {
            text = "🔔"
            textSize = 32f
        }, LinearLayout.LayoutParams(dp(54), -2))

        card.addView(TextView(this).apply {
            text = "2 missed alerts today\nTap to review missed alerts"
            textSize = 15f
            setTextColor(dark)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        card.addView(TextView(this).apply {
            text = "›"
            textSize = 28f
            setTextColor(gray)
        })

        content.addView(card, margin(0, dp(8), 0, dp(20)))
    }

    // ================= SETTINGS =================

    private fun showSettings() {
        clear()
        pageTitle("Settings")
        settingsGroup("Voice & Sound", listOf(
            settingSwitch("🔊", "Voice Alerts", "voice_enabled"),
            settingSwitch("▌▌", "Speak Every Route", "speak_all_routes"),
            settingText("🔔", "Alert Volume", "Slider controlled by phone volume"),
            settingSwitch("📳", "Vibrate Alerts", "vibrate_alerts")
        ))

        settingsGroup("Driving Preferences", listOf(
            settingText("➤", "Navigation Mode", "Choose your preferred navigation app"),
            settingSwitch("📍", "Auto-Start Navigation", "auto_open_waze")
        ))

        settingsGroup("App & System", listOf(
            settingText("🛡", "Permissions", "Manage app permissions"),
            settingText("☼", "Appearance", "Choose app theme"),
            settingText("ⓘ", "About", "Version, terms, and more")
        ))

        content.addView(blueButton("Open Notification Access") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }, margin(0, dp(12), 0, dp(20)))
    }

    private fun settingsGroup(title: String, rows: List<LinearLayout>) {
        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        card.addView(TextView(this).apply {
            text = title
            textSize = 16f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 0, 0, dp(8))
        })

        rows.forEach { card.addView(it) }
        content.addView(card, margin(0, dp(8), 0, dp(8)))
    }

    private fun settingSwitch(icon: String, label: String, key: String): LinearLayout {
        val row = settingBase(icon, label, "")
        row.addView(Switch(this).apply {
            isChecked = prefs.getBoolean(key, true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(key, checked).apply()
            }
        })
        return row
    }

    private fun settingText(icon: String, label: String, sub: String): LinearLayout {
        val row = settingBase(icon, label, sub)
        row.addView(TextView(this).apply {
            text = "›"
            textSize = 24f
            setTextColor(gray)
        })
        return row
    }

    private fun settingBase(icon: String, label: String, sub: String): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }

        row.addView(TextView(this).apply {
            text = icon
            textSize = 18f
            setTextColor(blue)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(38), dp(42)))

        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 15f
                setTextColor(dark)
            })
            if (sub.isNotBlank()) {
                addView(TextView(this@MainActivity).apply {
                    text = sub
                    textSize = 10f
                    setTextColor(gray)
                })
            }
        }, LinearLayout.LayoutParams(0, -2, 1f))

        return row
    }

    // ================= COMMON UI =================

    private fun pageTitle(title: String) {
        content.addView(TextView(this).apply {
            text = title
            textSize = 25f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(8), 0, 0, dp(16))
        })
    }

    private fun sectionTitle(title: String) {
        content.addView(TextView(this).apply {
            text = title
            textSize = 17f
            setTextColor(dark)
            setPadding(dp(12), dp(8), 0, dp(6))
        })
    }

    private fun sectionTitleRed(title: String) {
        content.addView(TextView(this).apply {
            text = title
            textSize = 15f
            setTextColor(red)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(8), 0, 0, dp(6))
        })
    }

    private fun cardBox(): LinearLayout {
        return LinearLayout(this).apply {
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(14).toFloat()
                setStroke(1, cardStroke)
            }
        }
    }

    private fun blueButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.WHITE)
            setBackgroundColor(blue)
            setOnClickListener { action() }
        }
    }

    private fun outlineButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(blue)
            setBackgroundColor(Color.WHITE)
            setOnClickListener { action() }
        }
    }

    private fun bottomNavigation(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(10))
            setBackgroundColor(Color.WHITE)

            addView(navItem("⌂", "Home") { showHome() })
            addView(navItem("⌖", "Routes") { showRoutes() })
            addView(navItem("⌁", "Alerts") { showAlerts() })
            addView(navItem("⚙", "Settings") { showSettings() })
        }
    }

    private fun navItem(icon: String, label: String, action: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)

            addView(TextView(this@MainActivity).apply {
                text = icon
                textSize = 24f
                setTextColor(blue)
                gravity = Gravity.CENTER
            })

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 10f
                setTextColor(gray)
                gravity = Gravity.CENTER
            })
        }
    }

    private fun speak(message: String) {
        if (!prefs.getBoolean("voice_enabled", true)) {
            Toast.makeText(this, "Voice Alerts are OFF", Toast.LENGTH_SHORT).show()
            return
        }

        tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "voice_${System.currentTimeMillis()}"
        )
    }

    private fun openWaze(destination: String) {
        val encoded = Uri.encode(destination)
        val uri = Uri.parse("waze://?q=$encoded&navigate=yes")

        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: Exception) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://waze.com/ul?q=$encoded&navigate=yes")))
        }
    }

    private fun margin(l: Int, t: Int, r: Int, b: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(l, t, r, b)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}
