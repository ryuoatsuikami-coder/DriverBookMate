package com.drivermate.ph

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.*
import java.util.Locale
import kotlin.math.abs

class MainActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("driver_mate_settings", MODE_PRIVATE) }
    private var tts: TextToSpeech? = null

    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout

    private val green = Color.rgb(0, 150, 45)
    private val lightGreen = Color.rgb(226, 248, 232)
    private val bg = Color.rgb(247, 255, 249)
    private val dark = Color.rgb(25, 35, 30)
    private val redOrange = Color.rgb(255, 92, 35)
    private val gray = Color.rgb(110, 120, 115)

    private val cavitePlaces = listOf(
        "Alfonso", "Amadeo", "Bacoor", "Carmona", "Cavite City",
        "Dasmarinas", "General Emilio Aguinaldo", "General Mariano Alvarez",
        "General Trias", "Imus", "Indang", "Kawit", "Magallanes",
        "Maragondon", "Mendez", "Naic", "Noveleta", "Rosario",
        "Silang", "Tagaytay", "Tanza", "Ternate", "Trece Martires"
    )

    private val manilaPlaces = listOf(
        "Manila", "Pasay", "Makati", "BGC", "Taguig", "Paranaque",
        "Las Pinas", "Alabang", "Quezon City", "Mandaluyong",
        "San Juan", "Pasig", "Marikina", "Caloocan", "Malabon",
        "Navotas", "Valenzuela", "Muntinlupa"
    )

    private val allPlaces = cavitePlaces + manilaPlaces

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "PH")
                tts?.setSpeechRate(0.88f)
                tts?.setPitch(1.02f)
            }
        }

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(bg)
        }

        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 20, 22, 10)
        }

        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(bottomNav())

        setContentView(root)
        showHome()
    }

    private fun bottomNav(): LinearLayout {
        val nav = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(8, 8, 8, 8)
            setBackgroundColor(Color.WHITE)
        }

        nav.addView(navButton("🏠\nHome") { showHome() })
        nav.addView(navButton("📍\nMy Routes") { showRoutes() })
        nav.addView(navButton("🔔\nAlerts") { showAlertDemo() })
        nav.addView(navButton("⚙️\nSettings") { showSettings() })

        return nav
    }

    private fun navButton(text: String, action: () -> Unit): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(green)
            setPadding(6, 6, 6, 6)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }
    }

    private fun clear() {
        content.removeAllViews()
    }

    private fun title(text: String) {
        content.addView(TextView(this).apply {
            this.text = text
            textSize = 24f
            setTextColor(dark)
            gravity = Gravity.CENTER
            setPadding(0, 4, 0, 14)
            setTypeface(null, 1)
        })
    }

    private fun label(text: String) {
        content.addView(TextView(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(dark)
            setTypeface(null, 1)
            setPadding(0, 12, 0, 6)
        })
    }

    private fun card(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 16, 18, 16)
            setBackgroundColor(Color.WHITE)
        }
    }

    private fun greenButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 15f
            setTextColor(Color.WHITE)
            setBackgroundColor(green)
            setOnClickListener { action() }
        }
    }

    private fun showHome() {
        clear()

        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 8, 0, 16)
        }

        header.addView(TextView(this).apply {
            text = "DriverMate PH"
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(dark)
            setTypeface(null, 1)
        })

        header.addView(TextView(this).apply {
            text = "Smart booking alerts for drivers"
            textSize = 13f
            gravity = Gravity.CENTER
            setTextColor(gray)
        })

        header.addView(TextView(this).apply {
            text = "Your Rides,\nYour Way\nAnytime,\nAnywhere!"
            textSize = 26f
            setTextColor(dark)
            setTypeface(null, 1)
            setPadding(0, 24, 0, 12)
        })

        content.addView(header)

        addHomeCard("🚚", "Send Package", "Deliver your packages safely, anytime, anywhere.")
        addHomeCard("🚕", "Book Ride", "Quick and easy rides within the city.")
        addHomeCard("🚗", "Book Intercity Ride", "Seamless travel between cities, hassle-free.")

        label("Preferred Routes")
        val preferred = getSavedRoutes().firstOrNull()
        if (preferred != null) {
            content.addView(routeCard(preferred, true))
        } else {
            content.addView(routeCard(RouteData("Tanza to Imus", "200", "18"), false))
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, 12, 0, 0)
        }

        val preferredSwitch = Switch(this).apply {
            text = "Read Preferred\nRoutes Only"
            textSize = 12f
            isChecked = prefs.getBoolean("preferred_only", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("preferred_only", checked).apply()
            }
        }

        val wazeSwitch = Switch(this).apply {
            text = "Auto Open Waze\nwhen detected"
            textSize = 12f
            isChecked = prefs.getBoolean("auto_open_waze", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("auto_open_waze", checked).apply()
            }
        }

        row.addView(preferredSwitch, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(wazeSwitch, LinearLayout.LayoutParams(0, -2, 1f))
        content.addView(row)
    }

    private fun addHomeCard(icon: String, name: String, desc: String) {
        val c = card()
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }

        row.addView(TextView(this).apply {
            text = icon
            textSize = 42f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(90, 90))

        row.addView(TextView(this).apply {
            text = "$name\n$desc"
            textSize = 15f
            setTextColor(dark)
            setPadding(14, 0, 0, 0)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(TextView(this).apply {
            text = "›"
            textSize = 34f
            setTextColor(green)
        })

        c.addView(row)
        content.addView(c)
    }

    private fun showRoutes() {
        clear()
        title("Preferred Routes")

        val search = EditText(this).apply {
            hint = "Search routes..."
            inputType = InputType.TYPE_CLASS_TEXT
            setSingleLine(true)
        }

        content.addView(search)

        val listBox = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 0)
        }

        content.addView(listBox)

        fun render(query: String) {
            listBox.removeAllViews()
            val routes = generateAllRoutes()
                .filter { it.route.contains(query, true) }
                .take(80)

            listBox.addView(TextView(this).apply {
                text = "Showing ${routes.size} routes"
                textSize = 12f
                setTextColor(gray)
                setPadding(0, 0, 0, 8)
            })

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
        title("Create Route")

        label("From")
        val from = Spinner(this)
        from.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allPlaces)

        label("To")
        val to = Spinner(this)
        to.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, allPlaces)

        label("Fare (₱)")
        val fare = EditText(this).apply {
            hint = "Enter fare amount"
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        label("Distance (km)")
        val distance = EditText(this).apply {
            hint = "Enter distance"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        }

        content.addView(from)
        content.addView(to)
        content.addView(fare)
        content.addView(distance)

        content.addView(greenButton("💾 Save Route") {
            if (from.selectedItem.toString() == to.selectedItem.toString()) {
                Toast.makeText(this, "Choose different places", Toast.LENGTH_SHORT).show()
                return@greenButton
            }

            val route = "${from.selectedItem} to ${to.selectedItem}"
            saveFullRoute(route, fare.text.toString(), distance.text.toString())
            Toast.makeText(this, "Route saved", Toast.LENGTH_SHORT).show()
            showSaved()
        })

        label("Popular Locations")
        content.addView(TextView(this).apply {
            text = "CAVITE\n${cavitePlaces.joinToString("  •  ")}\n\nMANILA\n${manilaPlaces.joinToString("  •  ")}"
            textSize = 12f
            setTextColor(green)
        })
    }

    private fun showSaved() {
        clear()
        title("Saved Routes")

        val saved = getSavedRoutes()

        if (saved.isEmpty()) {
            content.addView(TextView(this).apply {
                text = "No saved routes yet."
                textSize = 15f
                setTextColor(gray)
            })
        } else {
            saved.forEach { content.addView(routeCard(it, true)) }
        }

        content.addView(greenButton("+ Create New Route") { showCreateRoute() })

        val delete = Button(this).apply {
            text = "Delete All Saved Routes"
            setTextColor(Color.RED)
            setBackgroundColor(Color.WHITE)
            setOnClickListener {
                prefs.edit().remove("saved_full_routes").apply()
                showSaved()
            }
        }

        content.addView(delete)
    }

    private fun showAlertDemo() {
        clear()
        title("Alert Received")

        val c = card()
        c.addView(TextView(this).apply {
            text = "🔔 Priority Booking Detected\nLalamove"
            textSize = 18f
            setTextColor(dark)
            setTypeface(null, 1)
        })

        c.addView(TextView(this).apply {
            text = "\nBooking Details\nType: Priority\nRoute: Tanza to Imus\nFare: ₱200\nDistance: 18 km\nStatus: Preferred Route Matched"
            textSize = 15f
            setTextColor(dark)
        })

        c.addView(greenButton("Open in Waze") {
            openWaze("Imus, Cavite")
        })

        content.addView(c)
    }

    private fun showSettings() {
        clear()
        title("Settings")

        val preferredSwitch = Switch(this).apply {
            text = "Read Preferred Routes Only"
            isChecked = prefs.getBoolean("preferred_only", false)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("preferred_only", checked).apply()
            }
        }

        val wazeSwitch = Switch(this).apply {
            text = "Auto Open Waze"
            isChecked = prefs.getBoolean("auto_open_waze", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("auto_open_waze", checked).apply()
            }
        }

        content.addView(preferredSwitch)
        content.addView(wazeSwitch)

        content.addView(greenButton("Notification Access") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        })

        content.addView(greenButton("Test Voice") {
            tts?.speak(
                "Priority booking. Tanza to Imus. Fare 200 pesos. Distance 18 kilometers.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "test_voice"
            )
        })

        content.addView(greenButton("Manage Saved Routes") { showSaved() })

        content.addView(TextView(this).apply {
            text = "\nVersion 1.2.1\nDriverMate PH"
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
            setTypeface(null, 1)
        })

        c.addView(TextView(this).apply {
            text = "₱ Fare ${route.fare}     •     ${route.distance} km"
            textSize = 14f
            setTextColor(if (preferred) green else redOrange)
            setPadding(0, 4, 0, 4)
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
        val cleanRoute = route.trim()
        val cleanFare = fare.trim().replace("Fare", "").replace("₱", "").replace("pesos", "").trim()
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
