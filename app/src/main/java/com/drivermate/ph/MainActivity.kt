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

    private val green = Color.rgb(0, 150, 45)
    private val bg = Color.rgb(247, 255, 249)
    private val dark = Color.rgb(25, 35, 30)
    private val orange = Color.rgb(255, 92, 35)
    private val gray = Color.rgb(110, 120, 115)

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
            setPadding(22, 22, 22, 22)
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

    private fun bottomNav(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(10, 10, 10, 10)
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
            setPadding(8, 8, 8, 8)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
    }

    private fun clear() {
        content.removeAllViews()
        scrollView.post { scrollView.scrollTo(0, 0) }
    }

    private fun card(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 18, 20, 18)
            setBackgroundColor(Color.WHITE)
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, 8, 0, 12)
            }
        }
    }

    private fun greenButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(green)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(-1, -2).apply {
                setMargins(0, 8, 0, 8)
            }
        }
    }

    private fun showHome() {
        clear()

        content.addView(TextView(this).apply {
            text = "DriverMate PH"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        })

        content.addView(TextView(this).apply {
            text = "Smart booking alerts for preferred routes"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(gray)
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        })

        addHeroBanner()

        addDropdownCategory(
            R.drawable.package_truck,
            "Send Package",
            "Deliver packages safely and quickly.",
            listOf("Add package route", "Suggested Cavite package routes", "Test package voice", "Open Waze")
        )

        addDropdownCategory(
            R.drawable.city_taxi,
            "Book Ride",
            "Quick rides within Cavite and Manila.",
            listOf("Search Cavite routes", "Search Manila-Cavite routes", "Save preferred ride route", "Test ride voice")
        )

        addDropdownCategory(
            R.drawable.intercity_car,
            "Book Intercity Ride",
            "Cavite to Manila route alerts.",
            listOf("Long-distance route suggestions", "Cavite to Manila routes", "Manila to Cavite routes", "Auto-open Waze setting")
        )

        addPreferredRoutesDropdown()
        addVoiceAndWazeSwitches()
    }

    private fun addHeroBanner() {
        val frame = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(-1, 360).apply {
                setMargins(0, 14, 0, 16)
            }
            setBackgroundColor(Color.rgb(225, 250, 235))
        }

        frame.addView(ImageView(this).apply {
            setImageResource(R.drawable.hero_banner)
            scaleType = ImageView.ScaleType.CENTER_CROP
            alpha = 0.95f
        }, FrameLayout.LayoutParams(-1, -1))

        frame.addView(TextView(this).apply {
            text = "Your Rides,\nYour Way\nAnytime,\nAnywhere!"
            textSize = 32f
            setTextColor(Color.WHITE)
            setTypeface(null, Typeface.BOLD)
            setShadowLayer(8f, 2f, 2f, Color.rgb(20, 60, 30))
            gravity = Gravity.BOTTOM or Gravity.START
            setPadding(26, 0, 26, 34)
        }, FrameLayout.LayoutParams(-1, -1))

        content.addView(frame)
    }

    private fun addDropdownCategory(imageRes: Int, title: String, desc: String, options: List<String>) {
        val c = card()

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }

        header.addView(ImageView(this).apply {
            setImageResource(imageRes)
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(145, 145))

        header.addView(TextView(this).apply {
            text = "$title\n$desc"
            textSize = 16f
            setTextColor(dark)
            setPadding(14, 0, 0, 0)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        val arrow = TextView(this).apply {
            text = "▼"
            textSize = 24f
            setTextColor(green)
        }

        header.addView(arrow)
        c.addView(header)

        val dropdown = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(0, 12, 0, 0)
        }

        options.forEach { option ->
            dropdown.addView(Button(this).apply {
                text = option
                setTextColor(green)
                setBackgroundColor(Color.WHITE)
                setOnClickListener {
                    when {
                        option.contains("Search", true) ||
                        option.contains("suggestions", true) ||
                        option.contains("routes", true) -> showRoutes()

                        option.contains("Save", true) ||
                        option.contains("Add", true) -> showCreateRoute()

                        option.contains("Waze", true) -> openWaze("Imus, Cavite")

                        option.contains("voice", true) -> speakTest()
                    }
                }
            })
        }

        c.addView(dropdown)

        header.setOnClickListener {
            dropdown.visibility = if (dropdown.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            arrow.text = if (dropdown.visibility == View.VISIBLE) "▲" else "▼"
        }

        content.addView(c)
    }

    private fun addPreferredRoutesDropdown() {
        val c = card()

        val title = TextView(this).apply {
            text = "Preferred Route Preview ▼"
            textSize = 17f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        }

        c.addView(title)

        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            setPadding(0, 10, 0, 0)
        }

        val saved = getSavedRoutes()

        if (saved.isEmpty()) {
            box.addView(routeCard(RouteData("No preferred route saved", "0", "0"), false))
        } else {
            saved.forEach {
                box.addView(routeCard(it, true))
            }
        }

        c.addView(box)

        title.setOnClickListener {
            box.visibility = if (box.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            title.text = if (box.visibility == View.VISIBLE) "Preferred Route Preview ▲" else "Preferred Route Preview ▼"
        }

        content.addView(c)
    }

    private fun addVoiceAndWazeSwitches() {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 8, 0, 24)
            layoutParams = LinearLayout.LayoutParams(-1, -2)
        }

        row.addView(Switch(this).apply {
            text = "Voice\nMessage"
            textSize = 12f
            isChecked = prefs.getBoolean("voice_enabled", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("voice_enabled", checked).apply()
            }
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(Switch(this).apply {
            text = "Auto\nWaze"
            textSize = 12f
            isChecked = prefs.getBoolean("auto_open_waze", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("auto_open_waze", checked).apply()
            }
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(ImageView(this).apply {
            setImageResource(R.drawable.waze_icon)
            scaleType = ImageView.ScaleType.FIT_CENTER
            setOnClickListener { openWaze("Imus, Cavite") }
        }, LinearLayout.LayoutParams(90, 90))

        content.addView(row)
    }

    private fun speakTest() {
        if (!prefs.getBoolean("voice_enabled", true)) {
            Toast.makeText(this, "Voice Message is OFF", Toast.LENGTH_SHORT).show()
            return
        }

        tts?.speak(
            "Priority booking. Tanza to Imus. Fare 200 pesos. Distance 18 kilometers.",
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
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })

        val search = EditText(this).apply {
            hint = "Search routes..."
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
        }

        content.addView(search)

        val listBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 24)
        }

        content.addView(listBox)

        fun render(query: String) {
            listBox.removeAllViews()

            val routes = generateAllRoutes()
                .filter { it.route.contains(query, true) }
                .take(100)

            routes.forEach { route ->
                val row = routeCard(route, isSaved(route.route))
                row.setOnClickListener {
                    saveFullRoute(route.route, route.fare, route.distance)
                    Toast.makeText(this, "Route saved", Toast.LENGTH_SHORT).show()
                    render(search.text.toString())
                }
                listBox.addView(row)
            }
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = render(s.toString())
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        render("")
    }

    private fun showCreateRoute() {
        clear()

        content.addView(TextView(this).apply {
            text = "Create Route"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })

        val from = Spinner(this)
        from.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allPlaces)

        val to = Spinner(this)
        to.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allPlaces)

        val fare = EditText(this).apply {
            hint = "Fare example: 200"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        val distance = EditText(this).apply {
            hint = "Distance example: 18"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        content.addView(from)
        content.addView(to)
        content.addView(fare)
        content.addView(distance)

        content.addView(greenButton("Save Route") {
            val route = "${from.selectedItem} to ${to.selectedItem}"
            saveFullRoute(route, fare.text.toString(), distance.text.toString())
            Toast.makeText(this, "Route saved", Toast.LENGTH_SHORT).show()
            showRoutes()
        })
    }

    private fun showAlertDemo() {
        clear()
        addHeroBanner()
        content.addView(routeCard(RouteData("Tanza to Imus", "200", "18"), true))
        content.addView(greenButton("Test Voice Alert") { speakTest() })
        content.addView(greenButton("Open Waze") { openWaze("Imus, Cavite") })
    }

    private fun showSettings() {
        clear()

        content.addView(TextView(this).apply {
            text = "Settings"
            textSize = 26f
            gravity = Gravity.CENTER
            setTextColor(dark)
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
            text = "\nVersion 1.2.7\nDriverMate PH"
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(gray)
        })
    }

    private fun routeCard(route: RouteData, preferred: Boolean): LinearLayout {
        val c = card()
        val badge = if (preferred) "Preferred" else "Tap to Add"

        c.addView(TextView(this).apply {
            text = "📍 ${route.route}        $badge"
            textSize = 16f
            setTextColor(dark)
            setTypeface(null, Typeface.BOLD)
        })

        c.addView(TextView(this).apply {
            text = "₱${route.fare}     •     ${route.distance} km"
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
        return RouteData("$from to $to", fare.toString(), distance.toString())
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
