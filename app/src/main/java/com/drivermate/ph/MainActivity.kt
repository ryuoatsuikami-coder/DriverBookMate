package com.drivermate.ph

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.media.AudioAttributes
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
    private var ttsReady = false

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var scrollView: ScrollView

    private var voiceIsOn = true
    private var isTestingVoice = false
    private var selectedRouteCategory = "Cavite Area"

    private val blue = Color.rgb(0, 122, 255)
    private val green = Color.rgb(32, 190, 95)
    private val red = Color.rgb(230, 35, 35)
    private val orange = Color.rgb(255, 100, 35)
    private val bg = Color.rgb(248, 250, 253)
    private val cardStroke = Color.rgb(220, 230, 245)
    private val dark = Color.rgb(20, 20, 25)
    private val gray = Color.rgb(120, 120, 130)

    private val defaultSavedRoutes = mutableSetOf(
        "General Trias → Bacoor",
        "Noveleta → Trece Martires",
        "Tanza → Kawit",
        "Bacoor → Tagaytay"
    )

    private val cavitePoints = listOf(
        "Bacoor", "Imus", "Dasmariñas", "General Trias", "Kawit", "Noveleta",
        "Rosario", "Tanza", "Naic", "Ternate", "Maragondon", "Trece Martires",
        "Indang", "Silang", "Tagaytay", "Carmona", "Gen. Mariano Alvarez"
    )

    private val manilaPoints = listOf(
        "Manila", "Quezon City", "Makati", "Taguig", "Pasay", "Parañaque",
        "Las Piñas", "Muntinlupa", "Pasig", "Mandaluyong", "San Juan Manila",
        "Marikina", "Caloocan", "Malabon", "Navotas", "Valenzuela", "Pateros"
    )

    private val lagunaPoints = listOf(
        "San Pedro Laguna", "Biñan", "Santa Rosa", "Cabuyao", "Calamba",
        "Los Baños", "Bay Laguna", "San Pablo", "Santa Cruz Laguna", "Pagsanjan"
    )

    private val batangasPoints = listOf(
        "Santo Tomas Batangas", "Tanauan", "Lipa", "Malvar", "Batangas City",
        "Bauan", "Lemery", "Taal", "Nasugbu", "Balayan", "Calatagan"
    )

    private val quezonPoints = listOf(
        "Lucena", "Tayabas", "Candelaria Quezon", "Sariaya", "Tiaong",
        "Lucban", "Pagbilao", "Atimonan", "Gumaca", "Calauag"
    )

    private val luzonPoints = listOf(
        "Antipolo", "Cainta", "Taytay Rizal", "Angono", "Binangonan",
        "Malolos", "Meycauayan", "Baliwag", "San Jose del Monte",
        "San Fernando Pampanga", "Angeles", "Mabalacat",
        "Balanga", "Olongapo", "Subic", "Tarlac City", "Cabanatuan",
        "Baguio", "Dagupan", "La Union", "Naga", "Legazpi", "Sorsogon"
    )

    private val routeCategories = listOf(
        "Cavite Area",
        "Cavite to Manila",
        "Manila to Cavite",
        "Cavite to Laguna",
        "Laguna to Cavite",
        "Cavite to Batangas",
        "Batangas to Cavite",
        "Cavite to Quezon",
        "Quezon to Cavite",
        "General Luzon"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!prefs.contains("voice_enabled")) {
            prefs.edit()
                .putBoolean("voice_enabled", true)
                .putFloat("voice_volume", 1.0f)
                .apply()
        }

        selectedRouteCategory = prefs.getString("route_category", "Cavite Area") ?: "Cavite Area"

        setupVoice()
        setupLayout()
        showHome()
    }

    private fun setupVoice() {
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                ttsReady = true

                val result = tts?.setLanguage(Locale.US)

                tts?.setSpeechRate(0.88f)
                tts?.setPitch(1.02f)

                tts?.setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_NAVIGATION_GUIDANCE)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )

                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "TTS language not supported on this phone", Toast.LENGTH_LONG).show()
                }
            } else {
                ttsReady = false
                Toast.makeText(this, "Voice engine failed to start", Toast.LENGTH_LONG).show()
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

    private fun clear(resetScroll: Boolean = true) {
        content.removeAllViews()
        if (resetScroll) scrollView.post { scrollView.scrollTo(0, 0) }
    }

    private fun showHome() {
        clear()
        homeHeader()
        voiceAssistantCard()
        heroCard()
        bookingAppsCard()
        priorityRouteCard(true)
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
        val isOn = prefs.getBoolean("voice_enabled", true)

        val card = cardBox().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(22), dp(16), dp(22), dp(16))
        }

        card.addView(TextView(this).apply {
            text = "▌▌▌▌"
            textSize = 26f
            setTextColor(if (isOn) green else red)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(80), -2))

        card.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = if (isOn) "Voice Assistant Active" else "Voice Assistant Off"
                textSize = 15f
                setTextColor(if (isOn) green else red)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = if (isOn) "Ready to read pickup, drop-off, fare, and distance"
                else "Tap Turn On to enable voice alerts"
                textSize = 13f
                setTextColor(gray)
            })
        })

        content.addView(card, margin(0, dp(16), 0, dp(16)))
    }

    private fun heroCard() {
        voiceIsOn = prefs.getBoolean("voice_enabled", true)

        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(22))
            background = android.graphics.drawable.GradientDrawable().apply {
                setColor(Color.rgb(232, 244, 255))
                cornerRadius = dp(18).toFloat()
                setStroke(1, cardStroke)
            }
        }

        card.addView(TextView(this).apply {
            text = if (voiceIsOn) "DriverMate PH\nis active" else "DriverMate PH\nis off"
            textSize = 25f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(8), 0, 0, dp(8))
        })

        card.addView(ImageView(this).apply {
            setImageResource(R.drawable.hero_car)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
        }, LinearLayout.LayoutParams(-1, dp(150)))

        val buttons = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(dp(8), dp(18), dp(8), 0)
        }

        val testButton = Button(this).apply {
            text = if (isTestingVoice) "🔊  SPEAKING..." else "🔊  TEST VOICE"
            textSize = 14f
            setTextColor(if (isTestingVoice) Color.WHITE else blue)
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(if (isTestingVoice) blue else Color.WHITE)

            setOnClickListener {
                if (!prefs.getBoolean("voice_enabled", true)) {
                    Toast.makeText(this@MainActivity, "Turn on voice first", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                isTestingVoice = true
                showHome()

                speakBookingRoute(
                    appName = "DriverMate PH",
                    route = getPriorityRoute(),
                    fare = "500 pesos",
                    distance = "20 kilometers"
                )

                scrollView.postDelayed({
                    isTestingVoice = false
                    showHome()
                }, 3500)
            }
        }

        val powerButton = Button(this).apply {
            text = if (voiceIsOn) "🔇  TURN OFF" else "🔊  TURN ON"
            textSize = 14f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(if (voiceIsOn) green else red)

            setOnClickListener {
                voiceIsOn = !voiceIsOn
                prefs.edit().putBoolean("voice_enabled", voiceIsOn).apply()
                showHome()
            }
        }

        buttons.addView(testButton, LinearLayout.LayoutParams(0, dp(52), 1f))
        buttons.addView(powerButton, LinearLayout.LayoutParams(0, dp(52), 1f).apply {
            setMargins(dp(14), 0, 0, 0)
        })

        card.addView(buttons)
        content.addView(card, margin(0, 0, 0, dp(18)))
    }

    private fun bookingAppsCard() {
        content.addView(appImageRow(R.drawable.lalamove_truck, "LalaMove", "Get delivery bookings from LalaMove", "enable_lalamove", orange))
        content.addView(appImageRow(R.drawable.grab_car, "Grab", "Get delivery bookings from Grab", "enable_grab", green))
        content.addView(appImageRow(R.drawable.transportify_car, "Transportify", "Get delivery bookings from Transportify", "enable_transportify", blue))
    }

    private fun appImageRow(imageRes: Int, title: String, desc: String, key: String, titleColor: Int): LinearLayout {
        val card = cardBox().apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        card.addView(ImageView(this).apply {
            setImageResource(imageRes)
            scaleType = ImageView.ScaleType.FIT_CENTER
            adjustViewBounds = true
        }, LinearLayout.LayoutParams(dp(76), dp(70)))

        card.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 16f
                setTextColor(titleColor)
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

        return card.apply { layoutParams = margin(0, dp(4), 0, dp(4)) }
    }

    private fun showRoutes(resetScroll: Boolean = true) {
        clear(resetScroll)
        pageTitle("Routes")
        priorityRouteCard(false)
        savedRoutesSection()
        suggestedRoutesSection()
    }

    private fun priorityRouteCard(isHome: Boolean) {
        val currentPriority = getPriorityRoute()

        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(18), dp(24), dp(30))
        }

        card.addView(TextView(this).apply {
            text = "★  Priority Route"
            textSize = 16f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })

        card.addView(TextView(this).apply {
            text = currentPriority
            textSize = 21f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(14), 0, 0)
        })

        card.addView(TextView(this).apply {
            text = if (isHome) "This updates when you choose a route in Routes page"
            else "Tap any route below to set as priority"
            textSize = 13f
            setTextColor(gray)
            setPadding(0, dp(4), 0, dp(18))
        })

        if (!isHome) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }

            row.addView(outlineButton("🗑 Clear Priority") {
                prefs.edit().remove("first_priority_route").apply()
                showRoutes()
            }, LinearLayout.LayoutParams(0, dp(54), 1f))

            row.addView(outlineButton("🔊 Speak Priority") {
                speakBookingRoute("DriverMate PH", getPriorityRoute(), "500 pesos", "20 kilometers")
            }, LinearLayout.LayoutParams(0, dp(54), 1f).apply {
                setMargins(dp(14), 0, 0, 0)
            })

            card.addView(row)
        }

        content.addView(card, margin(0, dp(8), 0, dp(18)))
    }

    private fun savedRoutesSection() {
        sectionTitle("Saved Routes")

        val saved = getSavedRoutes()
        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(10), dp(24), dp(10))
        }

        if (saved.isEmpty()) {
            card.addView(TextView(this).apply {
                text = "No saved routes yet. Add from suggested routes below."
                textSize = 14f
                setTextColor(gray)
                setPadding(0, dp(14), 0, dp(14))
            })
        } else {
            saved.sorted().forEach { route ->
                card.addView(routeListRow(route, true))
            }
        }

        content.addView(card, margin(0, dp(8), 0, dp(18)))
    }

    private fun suggestedRoutesSection() {
        sectionTitle("Suggested Routes")
        routeCategoryButtons()

        val currentRoutes = getRoutesByCategory(selectedRouteCategory)

        val tools = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, dp(10))
        }

        tools.addView(blueButton("Add All") {
            val saved = getSavedRoutes().toMutableSet()
            saved.addAll(currentRoutes)
            saveRoutes(saved)
            showRoutes(false)
        }, LinearLayout.LayoutParams(0, dp(46), 1f))

        tools.addView(outlineButton("Remove All") {
            saveRoutes(emptySet())
            showRoutes(false)
        }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
            setMargins(dp(12), 0, 0, 0)
        })

        content.addView(tools)

        content.addView(TextView(this).apply {
            text = "$selectedRouteCategory • ${currentRoutes.size} routes"
            textSize = 13f
            setTextColor(gray)
            setPadding(dp(8), 0, 0, dp(8))
        })

        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(10), dp(24), dp(10))
        }

        currentRoutes.forEach { route ->
            card.addView(routeListRow(route, false))
        }

        content.addView(card)
    }

    private fun routeCategoryButtons() {
        val wrapper = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        routeCategories.chunked(2).forEach { pair ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, 0, 0, dp(8))
            }

            pair.forEach { category ->
                val active = category == selectedRouteCategory
                row.addView(Button(this).apply {
                    text = category
                    textSize = 11f
                    setTextColor(if (active) Color.WHITE else blue)
                    setTypeface(null, Typeface.BOLD)
                    setBackgroundColor(if (active) blue else Color.WHITE)
                    setOnClickListener {
                        selectedRouteCategory = category
                        prefs.edit().putString("route_category", category).apply()
                        showRoutes(false)
                    }
                }, LinearLayout.LayoutParams(0, dp(46), 1f).apply {
                    setMargins(dp(4), 0, dp(4), 0)
                })
            }

            wrapper.addView(row)
        }

        content.addView(wrapper)
    }

    private fun routeListRow(route: String, saved: Boolean): LinearLayout {
        val alreadyAdded = getSavedRoutes().contains(route)
        val currentPriority = getPriorityRoute()

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(12), 0, dp(12))
            setOnClickListener {
                prefs.edit().putString("first_priority_route", route).apply()
                showRoutes(false)
            }
        }

        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = if (route == currentPriority) "★  $route" else route
                textSize = 15f
                setTextColor(if (route == currentPriority) blue else dark)
                setTypeface(null, if (route == currentPriority) Typeface.BOLD else Typeface.NORMAL)
            })

            addView(TextView(this@MainActivity).apply {
                text = if (saved) "Tap to set as priority"
                else if (alreadyAdded) "Already added • Tap to set priority"
                else "Tap + to save • Tap route to set priority"
                textSize = 12f
                setTextColor(gray)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))

        if (saved) {
            row.addView(TextView(this).apply {
                text = "🗑"
                textSize = 24f
                setTextColor(red)
                gravity = Gravity.CENTER
                setOnClickListener {
                    val updated = getSavedRoutes().toMutableSet()
                    updated.remove(route)
                    saveRoutes(updated)
                    showRoutes(false)
                }
            }, LinearLayout.LayoutParams(dp(44), dp(44)))
        } else {
            row.addView(TextView(this).apply {
                text = if (alreadyAdded) "✓" else "⊕"
                textSize = 25f
                setTextColor(if (alreadyAdded) green else blue)
                gravity = Gravity.CENTER
                setOnClickListener {
                    val updated = getSavedRoutes().toMutableSet()
                    updated.add(route)
                    saveRoutes(updated)
                    showRoutes(false)
                }
            }, LinearLayout.LayoutParams(dp(44), dp(44)))
        }

        return row
    }

    private fun getRoutesByCategory(category: String): List<String> {
        return when (category) {
            "Cavite Area" -> generateRoutes(cavitePoints, cavitePoints)
            "Cavite to Manila" -> generateRoutes(cavitePoints, manilaPoints)
            "Manila to Cavite" -> generateRoutes(manilaPoints, cavitePoints)
            "Cavite to Laguna" -> generateRoutes(cavitePoints, lagunaPoints)
            "Laguna to Cavite" -> generateRoutes(lagunaPoints, cavitePoints)
            "Cavite to Batangas" -> generateRoutes(cavitePoints, batangasPoints)
            "Batangas to Cavite" -> generateRoutes(batangasPoints, cavitePoints)
            "Cavite to Quezon" -> generateRoutes(cavitePoints, quezonPoints)
            "Quezon to Cavite" -> generateRoutes(quezonPoints, cavitePoints)
            else -> {
                val routes = mutableListOf<String>()
                routes.addAll(generateRoutes(cavitePoints, luzonPoints))
                routes.addAll(generateRoutes(luzonPoints, cavitePoints))
                routes.addAll(generateRoutes(manilaPoints, lagunaPoints))
                routes.addAll(generateRoutes(lagunaPoints, manilaPoints))
                routes.addAll(generateRoutes(manilaPoints, batangasPoints))
                routes.addAll(generateRoutes(batangasPoints, manilaPoints))
                routes.distinct().sorted()
            }
        }.distinct().sorted()
    }

    private fun generateRoutes(fromList: List<String>, toList: List<String>): List<String> {
        return fromList.flatMap { from ->
            toList.filter { to -> to != from }.map { to -> "$from → $to" }
        }
    }

    private fun showAlerts() {
        clear()
        pageTitle("Alerts")
        currentAlertCard()
    }

    private fun currentAlertCard() {
        val route = "Cavite → Manila"
        val fare = "580 pesos"
        val distance = "35 kilometers"

        val card = cardBox().apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(22), dp(18), dp(22), dp(18))
        }

        card.addView(TextView(this).apply {
            text = "Lalamove Booking Detected"
            textSize = 22f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })

        card.addView(TextView(this).apply {
            text = "\nPickup: Cavite\nDrop-off: Manila\nFare: ₱580\nDistance: 35 km"
            textSize = 15f
            setTextColor(dark)
        })

        card.addView(blueButton("🔊 Speak Again") {
            speakBookingRoute("Lalamove", route, fare, distance)
        }, margin(0, dp(18), 0, 0))

        content.addView(card, margin(0, dp(8), 0, dp(16)))
    }

    private fun showSettings() {
        clear()
        pageTitle("Settings")

        settingsGroup(
            "Voice & Sound",
            listOf(
                settingSwitch("🔊", "Voice Alerts", "voice_enabled"),
                voiceVolumeSlider(),
                settingSwitch("📳", "Vibrate Alerts", "vibrate_alerts")
            )
        )

        content.addView(blueButton("Open Notification Access") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }, margin(0, dp(12), 0, dp(20)))

        content.addView(blueButton("Test Voice Now") {
            speak("DriverMate PH voice test. Pickup Cavite. Drop off Manila. Fare 500 pesos. Distance 20 kilometers.")
        }, margin(0, dp(4), 0, dp(20)))
    }

    private fun voiceVolumeSlider(): LinearLayout {
        val savedVolume = prefs.getFloat("voice_volume", 1.0f)
        val currentProgress = (savedVolume * 100).toInt().coerceIn(10, 100)

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, dp(14))
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val volumeLabel = TextView(this).apply {
            text = "🔔 Alert Volume"
            textSize = 15f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        }

        val percentLabel = TextView(this).apply {
            text = "$currentProgress%"
            textSize = 13f
            setTextColor(blue)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.END
        }

        topRow.addView(volumeLabel, LinearLayout.LayoutParams(0, -2, 1f))
        topRow.addView(percentLabel, LinearLayout.LayoutParams(dp(60), -2))

        val seekBar = SeekBar(this).apply {
            max = 100
            progress = currentProgress

            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    val safeProgress = progress.coerceAtLeast(10)
                    val volume = safeProgress / 100f
                    prefs.edit().putFloat("voice_volume", volume).apply()
                    percentLabel.text = "$safeProgress%"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    speak("Voice volume test.")
                }
            })
        }

        row.addView(topRow)
        row.addView(seekBar, LinearLayout.LayoutParams(-1, dp(45)))

        row.addView(TextView(this).apply {
            text = "Minimum is 10% so voice will not accidentally mute."
            textSize = 11f
            setTextColor(gray)
        })

        return row
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
        val row = settingBase(icon, label)
        row.addView(Switch(this).apply {
            isChecked = prefs.getBoolean(key, true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(key, checked).apply()
            }
        })
        return row
    }

    private fun settingBase(icon: String, label: String): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }

        row.addView(TextView(this).apply {
            text = icon
            textSize = 18f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(38), dp(42)))

        row.addView(TextView(this).apply {
            text = label
            textSize = 15f
            setTextColor(dark)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        return row
    }

    private fun speakBookingRoute(appName: String, route: String, fare: String = "", distance: String = "") {
        val cleanRoute = route
            .replace(" to ", " → ", ignoreCase = true)
            .replace("-", " → ")

        val parts = cleanRoute.split("→").map { it.trim() }

        val pickup = parts.getOrNull(0).orEmpty()
        val dropoff = parts.getOrNull(1).orEmpty()

        val message = buildString {
            append("$appName booking detected. ")
            if (pickup.isNotBlank()) append("Pickup, $pickup. ")
            if (dropoff.isNotBlank()) append("Drop off, $dropoff. ")
            if (fare.isNotBlank()) append("Fare, $fare. ")
            if (distance.isNotBlank()) append("Distance, $distance. ")
        }

        speak(message)
    }

    private fun speak(message: String) {
        if (!prefs.getBoolean("voice_enabled", true)) {
            Toast.makeText(this, "Voice Alerts are OFF", Toast.LENGTH_SHORT).show()
            return
        }

        if (!ttsReady || tts == null) {
            Toast.makeText(this, "Voice is loading. Try again in 2 seconds.", Toast.LENGTH_SHORT).show()
            setupVoice()
            return
        }

        val volume = prefs.getFloat("voice_volume", 1.0f).coerceIn(0.1f, 1.0f)

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, volume)
        }

        val result = tts?.speak(
            message,
            TextToSpeech.QUEUE_FLUSH,
            params,
            "voice_${System.currentTimeMillis()}"
        )

        if (result == TextToSpeech.ERROR || result == null) {
            Toast.makeText(this, "Voice failed. Check phone media volume or TTS engine.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getPriorityRoute(): String {
        return prefs.getString("first_priority_route", "Kawit → General Trias") ?: "Kawit → General Trias"
    }

    private fun getSavedRoutes(): Set<String> {
        return prefs.getStringSet("saved_routes", defaultSavedRoutes) ?: defaultSavedRoutes
    }

    private fun saveRoutes(routes: Set<String>) {
        prefs.edit().putStringSet("saved_routes", routes).apply()
    }

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
            setTypeface(null, Typeface.BOLD)
            setBackgroundColor(blue)
            setOnClickListener { action() }
        }
    }

    private fun outlineButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(blue)
            setTypeface(null, Typeface.BOLD)
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
