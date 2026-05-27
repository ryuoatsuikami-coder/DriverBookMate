package com.drivermate.ph

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.*
import java.util.Locale
import kotlin.math.abs

class MainActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("driver_mate_settings", MODE_PRIVATE) }
    private var tts: TextToSpeech? = null

    private lateinit var content: LinearLayout
    private lateinit var scrollView: ScrollView

    private val green = Color.rgb(0, 130, 45)
    private val lightGreen = Color.rgb(235, 252, 240)
    private val bg = Color.rgb(247, 255, 249)
    private val dark = Color.rgb(20, 35, 25)
    private val gray = Color.rgb(95, 105, 100)
    private val orange = Color.rgb(235, 85, 35)

    private val cavitePlaces = listOf(
        "Alfonso", "Amadeo", "Bacoor", "Carmona", "Cavite City",
        "Dasmarinas", "General Trias", "Imus", "Kawit", "Naic",
        "Rosario", "Silang", "Tagaytay", "Tanza", "Trece Martires"
    )

    private val manilaPlaces = listOf(
        "Manila", "Pasay", "Makati", "BGC", "Taguig", "Paranaque",
        "Las Pinas", "Alabang", "Quezon City", "Pasig", "Muntinlupa"
    )

    private val allPlaces = cavitePlaces + manilaPlaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale("en", "PH"))
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts?.language = Locale.US
                }
                tts?.setSpeechRate(0.88f)
                tts?.setPitch(1.02f)
            }
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }

        scrollView = ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(bg)
            layoutParams = LinearLayout.LayoutParams(-1, 0, 1f)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14), dp(10), dp(14), dp(10))
            setBackgroundColor(bg)
            layoutParams = LinearLayout.LayoutParams(-1, -2)
            minimumHeight = resources.displayMetrics.heightPixels
        }

        scrollView.addView(content)
        root.addView(scrollView)
        root.addView(bottomNav())

        setContentView(root)
        showHome()
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun clear() {
        content.removeAllViews()
        scrollView.post { scrollView.scrollTo(0, 0) }
    }

    private fun showHome() {
        clear()

        addTopBar()
        addHeroBanner()

        addCategoryCard(
            R.drawable.package_truck,
            "Send Package",
            "Deliver packages safely and quickly.",
            listOf("Add package route", "Suggested Cavite package routes", "Test package voice", "Open Waze")
        )

        addCategoryCard(
            R.drawable.city_taxi,
            "Book Ride",
            "Quick rides within Cavite and Manila.",
            listOf("Search Cavite routes", "Search Manila-Cavite routes", "Add preferred ride route", "Test ride voice")
        )

        addCategoryCard(
            R.drawable.intercity_car,
            "Book Intercity Ride",
            "Cavite to Manila route alerts.",
            listOf("Long-distance route suggestions", "Cavite to Manila routes", "Manila to Cavite routes", "Auto-open Waze setting")
        )

        addPreferredRoutesSection()
        addControlSection()
    }

    private fun addTopBar() {
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))
        }

        top.addView(TextView(this).apply {
            text = "☰"
            textSize = 30f
            setTextColor(green)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(48), dp(48)))

        top.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER

            addView(TextView(this@MainActivity).apply {
                text = "DriverMate PH"
                textSize = 28f
                gravity = Gravity.CENTER
                setTextColor(green)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = "Smart booking alerts for preferred routes"
                textSize = 14f
                gravity = Gravity.CENTER
                setTextColor(dark)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))

        top.addView(TextView(this).apply {
            text = "♧"
            textSize = 30f
            setTextColor(green)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(48), dp(48)))

        content.addView(top)
    }

    private fun addHeroBanner() {
        val frame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, dp(260)).apply {
                setMargins(0, dp(8), 0, dp(12))
            }
        }

        frame.addView(ImageView(this).apply {
            setImageResource(R.drawable.hero_banner)
            scaleType = ImageView.ScaleType.CENTER_CROP
        }, FrameLayout.LayoutParams(-1, -1))

        frame.addView(TextView(this).apply {
            text = "YOUR RIDES\nYOUR WAY\nANYTIME,\nANYWHERE!"
            textSize = 34f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setShadowLayer(8f, 2f, 2f, Color.rgb(20, 60, 30))
            gravity = Gravity.CENTER
        }, FrameLayout.LayoutParams(-1, -1))

        content.addView(frame)
    }

    private fun addCategoryCard(imageRes: Int, title: String, desc: String, options: List<String>) {
        val card = whiteCard()

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        header.addView(ImageView(this).apply {
            setImageResource(imageRes)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(dp(95), dp(95)))

        header.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)

            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 22f
                setTextColor(green)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = desc
                textSize = 14f
                setTextColor(dark)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))

        val arrow = TextView(this).apply {
            text = "⌄"
            textSize = 34f
            setTextColor(green)
            gravity = Gravity.CENTER
        }

        header.addView(arrow, LinearLayout.LayoutParams(dp(44), dp(44)))
        card.addView(header)

        val dropdown = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(0, dp(10), 0, 0)
        }

        options.forEach { option ->
            dropdown.addView(Button(this).apply {
                text = option
                textSize = 14f
                setTextColor(green)
                setBackgroundColor(Color.WHITE)
                setOnClickListener {
                    when {
                        option.contains("Search", true) ||
                        option.contains("suggested", true) ||
                        option.contains("routes", true) -> showRoutes()

                        option.contains("Add", true) -> showCreateRoute()

                        option.contains("Waze", true) -> openWaze("Imus, Cavite")

                        option.contains("voice", true) -> speakTest()
                    }
                }
            })
        }

        card.addView(dropdown)

        header.setOnClickListener {
            dropdown.visibility = if (dropdown.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            arrow.text = if (dropdown.visibility == View.VISIBLE) "⌃" else "⌄"
        }

        content.addView(card)
    }

    private fun addPreferredRoutesSection() {
        val card = whiteCard()

        val saved = getSavedRoutes()
        val first = saved.firstOrNull() ?: RouteData("No preferred route saved", "0", "manual only")

        val title = TextView(this).apply {
            text = "Preferred Route Preview⌄"
            textSize = 18f
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(4), 0, 0, dp(8))
        }

        card.addView(title)
        card.addView(firstPriorityCard(first))

        card.addView(greenButton("Manual Add Preferred Route") {
            showCreateRoute()
        })

        val dropdownTitle = TextView(this).apply {
            text = "All other preferred routes      ⌄"
            textSize = 15f
            setTextColor(dark)
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(Color.WHITE)
        }

        val dropdownBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val others = saved.drop(1)

        if (others.isEmpty()) {
            dropdownBox.addView(TextView(this).apply {
                text = "No other preferred routes yet."
                textSize = 14f
                setTextColor(gray)
                setPadding(dp(12), dp(8), dp(12), dp(8))
            })
        } else {
            others.forEach {
                dropdownBox.addView(routeCard(it, true))
            }
        }

        dropdownTitle.setOnClickListener {
            dropdownBox.visibility = if (dropdownBox.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            dropdownTitle.text = if (dropdownBox.visibility == View.VISIBLE) {
                "All other preferred routes      ⌃"
            } else {
                "All other preferred routes      ⌄"
            }
        }

        card.addView(dropdownTitle)
        card.addView(dropdownBox)

        content.addView(card)
    }

    private fun firstPriorityCard(route: RouteData): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), dp(10), dp(12), dp(10))
            setBackgroundColor(lightGreen)
        }

        row.addView(TextView(this).apply {
            text = "🏅\n1"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(green)
        }, LinearLayout.LayoutParams(dp(62), -2))

        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = route.route
                textSize = 18f
                setTextColor(dark)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = "Fare: ₱${route.fare}     •     Distance: ${route.distance}"
                textSize = 15f
                setTextColor(dark)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(TextView(this).apply {
            text = "FIRST PRIORITY"
            textSize = 12f
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setBackgroundColor(Color.rgb(210, 245, 220))
            setPadding(dp(8), dp(6), dp(8), dp(6))
        }, LinearLayout.LayoutParams(-2, -2))

        return row
    }

    private fun addControlSection() {
        val card = whiteCard()

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(TextView(this).apply {
            text = "🔊"
            textSize = 34f
            gravity = Gravity.CENTER
            setTextColor(green)
        }, LinearLayout.LayoutParams(dp(58), dp(58)))

        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = "Voice Message"
                textSize = 12f
                setTextColor(dark)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = "Voice alerts for booking messages"
                textSize = 11f
                setTextColor(gray)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(Switch(this).apply {
            isChecked = prefs.getBoolean("voice_enabled", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("voice_enabled", checked).apply()
            }
        }, LinearLayout.LayoutParams(-2, -2))

        row.addView(View(this).apply {
            setBackgroundColor(Color.rgb(210, 210, 210))
        }, LinearLayout.LayoutParams(dp(1), dp(58)).apply {
            setMargins(dp(8), 0, dp(8), 0)
        })

        row.addView(ImageView(this).apply {
            setImageResource(R.drawable.waze_icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener { openWaze("Imus, Cavite") }
        }, LinearLayout.LayoutParams(dp(76), dp(76)))

        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = "Auto Waze"
                textSize = 12f
                setTextColor(dark)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = "Open Waze on first preferred booking."
                textSize = 11f
                setTextColor(gray)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(Switch(this).apply {
            isChecked = prefs.getBoolean("auto_open_waze", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("auto_open_waze", checked).apply()
            }
        }, LinearLayout.LayoutParams(-2, -2))

        card.addView(row)
        content.addView(card)
    }

    private fun whiteCard(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, dp(6), 0, dp(8))
            }
        }
    }

    private fun bottomNav(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(-1, -2)

            addView(navButton("🏠\nHome") { showHome() })
            addView(navButton("📍\nRoutes") { showRoutes() })
            addView(navButton("🔔\nAlerts") { showAlertDemo() })
            addView(navButton("⚙️\nSettings") { showSettings() })
        }
    }

    private fun navButton(text: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(green)
            setPadding(dp(6), dp(6), dp(6), dp(6))
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
    }

    private fun speakTest() {
        if (!prefs.getBoolean("voice_enabled", true)) {
            Toast.makeText(this, "Voice Message is OFF", Toast.LENGTH_SHORT).show()
            return
        }

        tts?.speak(
            "Priority booking. Tanza to Imus. Fare 200 pesos. Distance not detected.",
            TextToSpeech.QUEUE_FLUSH,
            null,
            "test_voice_${System.currentTimeMillis()}"
        )
    }

    private fun showRoutes() {
        clear()

        content.addView(TextView(this).apply {
            text = "Preferred Routes"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
        })

        content.addView(greenButton("Manual Add Preferred Route") {
            showCreateRoute()
        })

        val search = EditText(this).apply {
            hint = "Search suggested routes..."
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
        }

        content.addView(search)

        val listBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(12), 0, dp(24))
        }

        content.addView(listBox)

        fun render(query: String) {
            listBox.removeAllViews()

            val routes = generateAllRoutes()
                .filter { it.route.contains(query, true) }
                .take(100)

            routes.forEach { route ->
                listBox.addView(suggestedRouteCard(route))
            }
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = render(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        render("")
    }

    private fun suggestedRouteCard(route: RouteData): LinearLayout {
        val c = whiteCard()

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            addView(TextView(this@MainActivity).apply {
                text = "📍 ${route.route}"
                textSize = 16f
                setTextColor(dark)
                setTypeface(null, Typeface.BOLD)
            })

            addView(TextView(this@MainActivity).apply {
                text = "Suggested fare: ₱${route.fare} • Suggested distance only"
                textSize = 13f
                setTextColor(if (isSaved(route.route)) green else orange)
            })
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(Button(this).apply {
            text = if (isSaved(route.route)) "SAVED" else "ADD"
            textSize = 12f
            setTextColor(Color.WHITE)
            setBackgroundColor(if (isSaved(route.route)) gray else green)
            isEnabled = !isSaved(route.route)
            setOnClickListener {
                saveFullRoute(route.route, route.fare, "manual only")
                Toast.makeText(this@MainActivity, "Preferred route added", Toast.LENGTH_SHORT).show()
                showRoutes()
            }
        }, LinearLayout.LayoutParams(dp(90), -2))

        c.addView(row)
        return c
    }

    private fun showCreateRoute() {
        clear()

        content.addView(TextView(this).apply {
            text = "Manual Add Preferred Route"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
        })

        val from = Spinner(this)
        from.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allPlaces)

        val to = Spinner(this)
        to.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allPlaces)

        val fare = EditText(this).apply {
            hint = "Minimum fare example: 200"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val distance = EditText(this).apply {
            hint = "Optional distance note example: manual only"
            inputType = InputType.TYPE_CLASS_TEXT
        }

        content.addView(TextView(this).apply {
            text = "From"
            textSize = 14f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })
        content.addView(from)

        content.addView(TextView(this).apply {
            text = "To"
            textSize = 14f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })
        content.addView(to)

        content.addView(fare)
        content.addView(distance)

        content.addView(greenButton("Add Preferred Route") {
            val route = "${from.selectedItem} to ${to.selectedItem}"
            val fareValue = fare.text.toString().ifBlank { "0" }
            val distanceValue = distance.text.toString().ifBlank { "manual only" }

            saveFullRoute(route, fareValue, distanceValue)
            Toast.makeText(this, "Preferred route added", Toast.LENGTH_SHORT).show()
            showRoutes()
        })
    }

    private fun showAlertDemo() {
        clear()
        addTopBar()
        addHeroBanner()
        content.addView(firstPriorityCard(RouteData("Tanza to Imus", "200", "manual only")))
        content.addView(greenButton("Test Voice Alert") { speakTest() })
        content.addView(greenButton("Open Waze") { openWaze("Imus, Cavite") })
    }

    private fun showSettings() {
        clear()

        content.addView(TextView(this).apply {
            text = "Settings"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
        })

        content.addView(greenButton("Notification Access") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        })

        content.addView(greenButton("Test Voice") {
            speakTest()
        })

        content.addView(greenButton("Manage Routes") {
            showRoutes()
        })

        content.addView(TextView(this).apply {
            text = "\nVersion 1.3.0\nDriverMate PH"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(gray)
        })
    }

    private fun greenButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(green)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, dp(8), 0, dp(8))
            }
        }
    }

    private fun routeCard(route: RouteData, preferred: Boolean): LinearLayout {
        val c = whiteCard()
        val badge = if (preferred) "Preferred" else "Tap to Add"

        c.addView(TextView(this).apply {
            text = "📍 ${route.route}        $badge"
            textSize = 16f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })

        c.addView(TextView(this).apply {
            text = "Fare: ₱${route.fare}     •     Distance: ${route.distance}"
            textSize = 14f
            setTextColor(if (preferred) green else orange)
        })

        return c
    }

    private fun generateAllRoutes(): List<RouteData> {
        val list = mutableListOf<RouteData>()

        for (from in cavitePlaces) {
            for (to in cavitePlaces) {
                if (from != to) list.add(makeRoute(from, to))
            }
        }

        for (from in cavitePlaces) {
            for (to in manilaPlaces) {
                list.add(makeRoute(from, to))
                list.add(makeRoute(to, from))
            }
        }

        return list.shuffled()
    }

    private fun makeRoute(from: String, to: String): RouteData {
        val distance = estimateDistance(from, to)
        val fare = estimateFare(distance)
        return RouteData("$from to $to", fare.toString(), "suggested only")
    }

    private fun estimateDistance(from: String, to: String): Int {
        val a = abs(from.hashCode() % 35)
        val b = abs(to.hashCode() % 35)
        val base = abs(a - b) + 8
        val manilaRoute = manilaPlaces.contains(from) || manilaPlaces.contains(to)
        return if (manilaRoute) base + 18 else base
    }

    private fun estimateFare(distance: Int): Int {
        return ((distance * 12) + 80).coerceAtLeast(150)
    }

    private fun saveFullRoute(route: String, fare: String, distance: String) {
        val current = prefs.getString("saved_full_routes", "") ?: ""
        val newItem = "$route~$fare~$distance"

        if (current.lowercase().contains(route.lowercase())) return

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

    private fun isSaved(route: String): Boolean {
        return getSavedRoutes().any { it.route.equals(route, true) }
    }

    private fun openWaze(destination: String) {
        val encoded = Uri.encode(destination)
        val uri = Uri.parse("waze://?q=$encoded&navigate=yes")

        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://waze.com/ul?q=$encoded&navigate=yes")
                )
            )
        }
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
