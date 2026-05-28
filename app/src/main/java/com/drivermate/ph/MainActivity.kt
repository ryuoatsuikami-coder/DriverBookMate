package com.drivermate.ph

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {

    private val prefs by lazy { getSharedPreferences("driver_mate_settings", MODE_PRIVATE) }
    private var tts: TextToSpeech? = null

    private lateinit var content: LinearLayout
    private lateinit var scrollView: ScrollView
    private lateinit var routesContainer: LinearLayout

    private val bg = Color.rgb(5, 12, 28)
    private val card = Color.rgb(14, 27, 52)
    private val card2 = Color.rgb(18, 39, 72)
    private val green = Color.rgb(0, 220, 105)
    private val red = Color.rgb(255, 80, 65)
    private val white = Color.WHITE
    private val muted = Color.rgb(175, 190, 210)

    private val cavitePlaces = listOf(
        "Alfonso", "Amadeo", "Bacoor", "Carmona", "Cavite City", "Dasmarinas",
        "General Trias", "Imus", "Indang", "Kawit", "Magallanes Cavite",
        "Maragondon", "Mendez", "Naic", "Noveleta", "Rosario", "Silang",
        "Tagaytay", "Tanza", "Ternate", "Trece Martires"
    )

    private val manilaPlaces = listOf(
        "Manila", "Caloocan", "Las Pinas", "Makati", "Malabon", "Mandaluyong",
        "Marikina", "Muntinlupa", "Navotas", "Paranaque", "Pasay", "Pasig",
        "Pateros", "Quezon City", "San Juan", "Taguig", "Valenzuela",
        "BGC", "Alabang"
    )

    private val lagunaPlaces = listOf(
        "Alaminos Laguna", "Bay", "Binan", "Cabuyao", "Calamba", "Calauan",
        "Cavinti", "Famy", "Kalayaan", "Liliw", "Los Banos", "Luisiana",
        "Lumban", "Mabitac", "Magdalena", "Majayjay", "Nagcarlan", "Paete",
        "Pagsanjan", "Pakil", "Pangil", "Pila", "Rizal Laguna", "San Pablo",
        "San Pedro", "Santa Cruz Laguna", "Santa Maria Laguna", "Santa Rosa",
        "Siniloan", "Victoria"
    )

    private val batangasPlaces = listOf(
        "Agoncillo", "Alitagtag", "Balayan", "Balete", "Batangas City", "Bauan",
        "Calaca", "Calatagan", "Cuenca", "Ibaan", "Laurel", "Lemery", "Lian",
        "Lipa", "Lobo", "Mabini", "Malvar", "Mataasnakahoy", "Nasugbu",
        "Padre Garcia", "Rosario Batangas", "San Jose Batangas",
        "San Juan Batangas", "San Luis Batangas", "San Nicolas", "San Pascual",
        "Santa Teresita", "Santo Tomas Batangas", "Taal", "Talisay Batangas",
        "Tanauan", "Taysan", "Tingloy", "Tuy"
    )

    private val bulacanPlaces = listOf(
        "Angat", "Balagtas", "Baliwag", "Bocaue", "Bulakan", "Bustos",
        "Calumpit", "Dona Remedios Trinidad", "Guiguinto", "Hagonoy",
        "Malolos", "Marilao", "Meycauayan", "Norzagaray", "Obando",
        "Pandi", "Paombong", "Plaridel", "Pulilan", "San Ildefonso",
        "San Jose del Monte", "San Miguel", "San Rafael", "Santa Maria Bulacan"
    )

    private val pampangaPlaces = listOf(
        "Angeles", "Apalit", "Arayat", "Bacolor", "Candaba", "Floridablanca",
        "Guagua", "Lubao", "Mabalacat", "Macabebe", "Magalang", "Masantol",
        "Mexico", "Minalin", "Porac", "San Fernando Pampanga",
        "San Luis Pampanga", "San Simon", "Santa Ana Pampanga", "Santa Rita",
        "Santo Tomas Pampanga", "Sasmuan"
    )

    private val allPlaces: List<String>
        get() = (
            cavitePlaces + manilaPlaces + lagunaPlaces + batangasPlaces +
                bulacanPlaces + pampangaPlaces
            ).distinct().sorted()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale("en", "PH")
                tts?.setSpeechRate(0.85f)
                tts?.setPitch(1.03f)
            }
        }

        val root = LinearLayout(this).apply {
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
            setPadding(dp(16), dp(12), dp(16), dp(12))
            setBackgroundColor(bg)
        }

        scrollView.addView(content)
        root.addView(scrollView)
        root.addView(bottomNav())

        setContentView(root)
        showHome()
    }

    private fun showHome() {
        clear()
        addHeader()
        addHero()
        addDecisionCard()
        addFirstPriorityCard()
        addVoiceStatusCard()
    }

    private fun addHeader() {
        content.addView(TextView(this).apply {
            text = "🚘 DriverMate PH"
            textSize = 26f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        })

        content.addView(TextView(this).apply {
            text = "Driver Assistant for Booking Alerts"
            textSize = 14f
            setTextColor(muted)
            setPadding(0, 0, 0, dp(12))
        })
    }

    private fun addHero() {
        val box = roundedBox(card, 24).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(22), dp(20), dp(22))
        }

        box.addView(TextView(this).apply {
            text = "DRIVE SMARTER\nHEAR BOOKINGS\nCHOOSE ROUTES FASTER"
            textSize = 26f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD_ITALIC)
        })

        box.addView(TextView(this).apply {
            text = "Main focus: add, remove, reset, and manage preferred routes for faster booking decisions."
            textSize = 14f
            setTextColor(muted)
            setPadding(0, dp(10), 0, 0)
        })

        content.addView(box, marginParams(0, 0, 0, dp(14)))
    }

    private fun addDecisionCard() {
        val box = roundedBox(card2, 22).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        box.addView(TextView(this).apply {
            text = "🚦 Booking Decision Focus"
            textSize = 20f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        })

        box.addView(TextView(this).apply {
            text = "TAKE • WAIT • SKIP"
            textSize = 30f
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(6))
        })

        box.addView(TextView(this).apply {
            text = "The app helps drivers decide faster by matching bookings with saved preferred routes."
            textSize = 14f
            setTextColor(muted)
            gravity = Gravity.CENTER
        })

        content.addView(box, marginParams(0, 0, 0, dp(14)))
    }

    private fun addFirstPriorityCard() {
        val route = getFirstPriorityRoute().ifBlank { "No first priority route set" }

        val box = roundedBox(card, 22).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        box.addView(TextView(this).apply {
            text = "⭐ First Priority Route"
            textSize = 18f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        })

        box.addView(TextView(this).apply {
            text = route
            textSize = 20f
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(10), 0, 0)
        })

        content.addView(box, marginParams(0, 0, 0, dp(14)))
    }

    private fun addVoiceStatusCard() {
        val box = roundedBox(card, 22).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
        }

        box.addView(TextView(this).apply {
            text = "🎙️ Voice Alerts"
            textSize = 18f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        box.addView(Switch(this).apply {
            isChecked = prefs.getBoolean("voice_enabled", true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean("voice_enabled", checked).apply()
            }
        })

        content.addView(box)
    }

    private fun showRoutes() {
        clear()
        addPageTitle("Preferred Routes")

        content.addView(greenButton("+ Manual Add Route") {
            showCreateRoute()
        }, marginParams(0, dp(6), 0, dp(8)))

        content.addView(greenButton("Other Suggested Routes") {
            showOtherSuggestedRoutes()
        }, marginParams(0, dp(4), 0, dp(8)))

        content.addView(greenButton("ADD ALL GENERAL ROUTES") {
            val allRoutes = buildAreaRoutes(getGeneralLuzonPlaces(), getGeneralLuzonPlaces())
            val addedCount = saveManyRoutesNoReset(allRoutes)
            Toast.makeText(this, "$addedCount routes added", Toast.LENGTH_SHORT).show()
            showRoutes()
        }, marginParams(0, dp(4), 0, dp(8)))

        content.addView(redButton("REMOVE ALL SAVED ROUTES") {
            removeAllSavedRoutes()
            Toast.makeText(this, "All saved routes removed", Toast.LENGTH_SHORT).show()
            showRoutes()
        }, marginParams(0, dp(4), 0, dp(8)))

        content.addView(redButton("RESET ROUTES AND PRIORITY") {
            resetRoutesAndPriority()
            Toast.makeText(this, "Routes and priority reset", Toast.LENGTH_SHORT).show()
            showRoutes()
        }, marginParams(0, dp(4), 0, dp(14)))

        content.addView(TextView(this).apply {
            text = "Saved Preferred Routes"
            textSize = 18f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(10), 0, dp(8))
        })

        routesContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(routesContainer)

        val saved = getSavedRoutes()

        if (saved.isEmpty()) {
            routesContainer.addView(TextView(this).apply {
                text = "No saved routes yet. Add manually or use suggested routes."
                textSize = 14f
                setTextColor(muted)
                setPadding(0, dp(10), 0, dp(10))
            })
        } else {
            saved.forEach {
                routesContainer.addView(routeCard(it))
            }
        }
    }

    private fun showOtherSuggestedRoutes() {
        clear()
        addPageTitle("Other Suggested Routes")

        content.addView(TextView(this).apply {
            text = "Choose a category. You can add one route or add all routes in the category."
            textSize = 14f
            setTextColor(muted)
            setPadding(0, 0, 0, dp(12))
        })

        content.addView(suggestedRouteButton("Cavite Area") {
            showSuggestedRoutes("Cavite Area", buildAreaRoutes(cavitePlaces, cavitePlaces))
        })

        content.addView(suggestedRouteButton("Cavite to Manila") {
            showSuggestedRoutes("Cavite to Manila", buildAreaRoutes(cavitePlaces, manilaPlaces))
        })

        content.addView(suggestedRouteButton("Manila to Cavite") {
            showSuggestedRoutes("Manila to Cavite", buildAreaRoutes(manilaPlaces, cavitePlaces))
        })

        content.addView(suggestedRouteButton("Cavite to Laguna") {
            showSuggestedRoutes("Cavite to Laguna", buildAreaRoutes(cavitePlaces, lagunaPlaces))
        })

        content.addView(suggestedRouteButton("Laguna to Cavite") {
            showSuggestedRoutes("Laguna to Cavite", buildAreaRoutes(lagunaPlaces, cavitePlaces))
        })

        content.addView(suggestedRouteButton("Manila to Laguna") {
            showSuggestedRoutes("Manila to Laguna", buildAreaRoutes(manilaPlaces, lagunaPlaces))
        })

        content.addView(suggestedRouteButton("Cavite to Batangas") {
            showSuggestedRoutes("Cavite to Batangas", buildAreaRoutes(cavitePlaces, batangasPlaces))
        })

        content.addView(suggestedRouteButton("Batangas to Cavite") {
            showSuggestedRoutes("Batangas to Cavite", buildAreaRoutes(batangasPlaces, cavitePlaces))
        })

        content.addView(suggestedRouteButton("General Routes") {
            showSuggestedRoutes("General Routes", buildAreaRoutes(getGeneralLuzonPlaces(), getGeneralLuzonPlaces()))
        })
    }

    private fun showSuggestedRoutes(title: String, routes: List<String>) {
        clear()
        addPageTitle(title)

        content.addView(TextView(this).apply {
            text = "${routes.size} possible routes available"
            textSize = 14f
            setTextColor(muted)
            setPadding(0, 0, 0, dp(10))
        })

        content.addView(greenButton("ADD ALL IN THIS CATEGORY") {
            val before = scrollView.scrollY
            val addedCount = saveManyRoutesNoReset(routes)
            Toast.makeText(this, "$addedCount routes added", Toast.LENGTH_SHORT).show()
            scrollView.post { scrollView.scrollTo(0, before) }
        }, marginParams(0, dp(4), 0, dp(8)))

        content.addView(redButton("REMOVE ALL SAVED ROUTES") {
            removeAllSavedRoutes()
            Toast.makeText(this, "All saved routes removed", Toast.LENGTH_SHORT).show()
        }, marginParams(0, dp(4), 0, dp(12)))

        routes.forEach { route ->
            content.addView(suggestedRouteCard(route))
        }
    }

    private fun suggestedRouteCard(route: String): LinearLayout {
        val box = roundedBox(card, 18).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }

        box.addView(TextView(this).apply {
            text = route
            textSize = 15f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        box.addView(Button(this).apply {
            text = "ADD"
            textSize = 12f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
            background = roundedDrawable(green, 14)

            setOnClickListener {
                val before = scrollView.scrollY
                val saved = saveFullRouteNoReset(route, "0", "manual")

                Toast.makeText(
                    this@MainActivity,
                    if (saved) "Route added" else "Already saved",
                    Toast.LENGTH_SHORT
                ).show()

                scrollView.post { scrollView.scrollTo(0, before) }
            }
        }, LinearLayout.LayoutParams(dp(82), dp(46)))

        return box.apply {
            layoutParams = marginParams(0, dp(5), 0, dp(5))
        }
    }

    private fun routeCard(route: RouteData): LinearLayout {
        val box = roundedBox(card, 18).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }

        box.addView(TextView(this).apply {
            text = "⭐ ${route.route}"
            textSize = 17f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        })

        box.addView(TextView(this).apply {
            text = "Fare: ₱${route.fare} • Distance: ${route.distance}"
            textSize = 13f
            setTextColor(muted)
        })

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(8), 0, 0)
        }

        row.addView(greenButton("SET FIRST") {
            setFirstPriorityRoute(route.route)
            Toast.makeText(this, "Set as first priority", Toast.LENGTH_SHORT).show()
            showRoutes()
        }, LinearLayout.LayoutParams(0, dp(48), 1f))

        row.addView(redButton("REMOVE") {
            removeSavedRoute(route.route)
            Toast.makeText(this, "Route removed", Toast.LENGTH_SHORT).show()
            showRoutes()
        }, LinearLayout.LayoutParams(0, dp(48), 1f))

        box.addView(row)

        return box.apply {
            layoutParams = marginParams(0, dp(6), 0, dp(6))
        }
    }

    private fun showCreateRoute() {
        clear()
        addPageTitle("Manual Add Route")

        val fromInput = placeInput("Pickup place")
        val fromList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val toInput = placeInput("Drop-off place")
        val toList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val fareInput = placeInput("Fare e.g. 350").apply {
            inputType = InputType.TYPE_CLASS_NUMBER
        }

        fun renderPlaceList(query: String, box: LinearLayout, input: EditText) {
            box.removeAllViews()
            val q = query.trim()
            if (q.isBlank()) return

            allPlaces.filter { it.startsWith(q, true) }.take(60).forEach { place ->
                box.addView(TextView(this).apply {
                    text = place
                    textSize = 16f
                    setTextColor(white)
                    setPadding(dp(14), dp(10), dp(14), dp(10))
                    background = roundedDrawable(card2, 14)
                    setOnClickListener {
                        input.setText(place)
                        input.setSelection(input.text.length)
                        box.removeAllViews()
                    }
                }, marginParams(0, dp(4), 0, dp(4)))
            }
        }

        fromInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = renderPlaceList(s.toString(), fromList, fromInput)
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        toInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = renderPlaceList(s.toString(), toList, toInput)
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        content.addView(fromInput, marginParams(0, dp(8), 0, dp(6)))
        content.addView(fromList)
        content.addView(toInput, marginParams(0, dp(8), 0, dp(6)))
        content.addView(toList)
        content.addView(fareInput, marginParams(0, dp(8), 0, dp(12)))

        content.addView(greenButton("SAVE PREFERRED ROUTE") {
            val from = normalizePlaceName(fromInput.text.toString())
            val to = normalizePlaceName(toInput.text.toString())
            val fare = fareInput.text.toString().ifBlank { "0" }

            if (from.isBlank() || to.isBlank()) {
                Toast.makeText(this, "Enter pickup and drop-off", Toast.LENGTH_SHORT).show()
                return@greenButton
            }

            val saved = saveFullRouteNoReset("$from to $to", fare, "manual")
            Toast.makeText(this, if (saved) "Route saved" else "Already saved", Toast.LENGTH_SHORT).show()
            showRoutes()
        })
    }

    private fun showApps() {
        clear()
        addPageTitle("App Monitoring")

        content.addView(appSwitch("Lalamove", "enable_lalamove"))
        content.addView(appSwitch("Grab", "enable_grab"))
        content.addView(appSwitch("Transportify", "enable_transportify"))
        content.addView(appSwitch("Move It", "enable_moveit"))
    }

    private fun showSettings() {
        clear()
        addPageTitle("Voice Settings")

        content.addView(settingSwitch("Voice Alerts", "voice_enabled", true))
        content.addView(settingSwitch("Speak All Routes", "speak_all_routes", false))
        content.addView(settingSwitch("Auto Open Waze", "auto_open_waze", true))

        content.addView(greenButton("Test Voice") {
            tts?.speak(
                "Preferred booking detected. General Trias to Imus. Recommended. Take this booking.",
                TextToSpeech.QUEUE_FLUSH,
                null,
                "test_voice"
            )
        }, marginParams(0, dp(8), 0, dp(8)))

        content.addView(greenButton("Open Notification Access") {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        })
    }

    private fun appSwitch(name: String, key: String): LinearLayout {
        return settingSwitch(name, key, true)
    }

    private fun settingSwitch(label: String, key: String, defaultValue: Boolean): LinearLayout {
        val row = roundedBox(card, 18).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
        }

        row.addView(TextView(this).apply {
            text = label
            textSize = 17f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(Switch(this).apply {
            isChecked = prefs.getBoolean(key, defaultValue)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(key, checked).apply()
            }
        })

        return row.apply {
            layoutParams = marginParams(0, dp(6), 0, dp(6))
        }
    }

    private fun buildAreaRoutes(fromList: List<String>, toList: List<String>): List<String> {
        return fromList.distinct().flatMap { from ->
            toList.distinct().mapNotNull { to ->
                val cleanFrom = normalizePlaceName(from)
                val cleanTo = normalizePlaceName(to)

                if (!cleanFrom.equals(cleanTo, true)) {
                    "$cleanFrom to $cleanTo"
                } else null
            }
        }.distinct().sorted()
    }

    private fun getGeneralLuzonPlaces(): List<String> {
        return (
            cavitePlaces + manilaPlaces + lagunaPlaces + batangasPlaces +
                bulacanPlaces + pampangaPlaces
            ).distinct().sorted()
    }

    private fun saveManyRoutesNoReset(routes: List<String>): Int {
        val current = prefs.getString("saved_full_routes", "") ?: ""

        val existingRoutes = current.split("|")
            .mapNotNull {
                val p = it.split("~")
                if (p.isNotEmpty()) p[0].lowercase() else null
            }
            .toMutableSet()

        val newItems = mutableListOf<String>()
        var addedCount = 0

        routes.forEach { route ->
            val cleanRoute = route.trim()
            if (cleanRoute.isNotBlank() && !existingRoutes.contains(cleanRoute.lowercase())) {
                newItems.add("$cleanRoute~0~manual")
                existingRoutes.add(cleanRoute.lowercase())
                addedCount++
            }
        }

        val updated = when {
            current.isBlank() -> newItems.joinToString("|")
            newItems.isEmpty() -> current
            else -> current + "|" + newItems.joinToString("|")
        }

        prefs.edit().putString("saved_full_routes", updated).apply()
        return addedCount
    }

    private fun saveFullRouteNoReset(route: String, fare: String, distance: String): Boolean {
        val current = prefs.getString("saved_full_routes", "") ?: ""

        val exists = current.split("|").any {
            val p = it.split("~")
            p.isNotEmpty() && p[0].equals(route, true)
        }

        if (exists) return false

        val newItem = "$route~$fare~$distance"
        val updated = if (current.isBlank()) newItem else "$current|$newItem"

        prefs.edit().putString("saved_full_routes", updated).apply()
        return true
    }

    private fun getSavedRoutes(): List<RouteData> {
        val raw = prefs.getString("saved_full_routes", "") ?: ""
        if (raw.isBlank()) return emptyList()

        return raw.split("|").mapNotNull {
            val p = it.split("~")
            if (p.size >= 3) RouteData(p[0], p[1], p[2]) else null
        }
    }

    private fun removeSavedRoute(route: String) {
        val current = prefs.getString("saved_full_routes", "") ?: ""

        val updated = current.split("|")
            .filter {
                val p = it.split("~")
                p.isNotEmpty() && !p[0].equals(route, true)
            }
            .joinToString("|")

        prefs.edit().putString("saved_full_routes", updated).apply()
    }

    private fun removeAllSavedRoutes() {
        prefs.edit().remove("saved_full_routes").apply()
    }

    private fun resetRoutesAndPriority() {
        prefs.edit()
            .remove("saved_full_routes")
            .remove("first_priority_route")
            .apply()
    }

    private fun getFirstPriorityRoute(): String {
        return prefs.getString("first_priority_route", "") ?: ""
    }

    private fun setFirstPriorityRoute(route: String) {
        prefs.edit().putString("first_priority_route", route).apply()
    }

    private fun normalizePlaceName(place: String): String {
        return when (place.trim().lowercase()) {
            "gen trias" -> "General Trias"
            "dasma" -> "Dasmarinas"
            "qc" -> "Quezon City"
            "bonifacio global city" -> "BGC"
            else -> place.trim()
        }
    }

    private fun addPageTitle(title: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        row.addView(TextView(this).apply {
            text = "←"
            textSize = 28f
            setTextColor(white)
            gravity = Gravity.CENTER
            setOnClickListener { showHome() }
        }, LinearLayout.LayoutParams(dp(44), dp(44)))

        row.addView(TextView(this).apply {
            text = title
            textSize = 22f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(0, -2, 1f))

        row.addView(Space(this), LinearLayout.LayoutParams(dp(44), dp(44)))
        content.addView(row, marginParams(0, 0, 0, dp(12)))
    }

    private fun bottomNav(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(8), dp(8), dp(8), dp(8))
            background = roundedDrawable(Color.rgb(9, 20, 42), 24)

            addView(navButton("⌂", "Home") { showHome() })
            addView(navButton("☆", "Routes") { showRoutes() })
            addView(navButton("▦", "Apps") { showApps() })
            addView(navButton("⚙", "Settings") { showSettings() })
        }
    }

    private fun navButton(icon: String, label: String, action: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)

            addView(TextView(this@MainActivity).apply {
                text = icon
                textSize = 22f
                setTextColor(green)
                gravity = Gravity.CENTER
            })

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 11f
                setTextColor(muted)
                gravity = Gravity.CENTER
            })

            setOnClickListener { action() }
        }
    }

    private fun placeInput(hintText: String): EditText {
        return EditText(this).apply {
            hint = hintText
            setHintTextColor(muted)
            setTextColor(white)
            inputType = InputType.TYPE_CLASS_TEXT
            background = roundedDrawable(card, 18)
            setPadding(dp(14), dp(12), dp(14), dp(12))
        }
    }

    private fun suggestedRouteButton(title: String, action: () -> Unit): Button {
        return Button(this).apply {
            text = title
            textSize = 14f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
            background = roundedDrawable(card2, 18)
            setOnClickListener { action() }
        }.apply {
            layoutParams = marginParams(0, dp(5), 0, dp(5))
        }
    }

    private fun greenButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
            background = roundedDrawable(green, 18)
            setOnClickListener { action() }
        }
    }

    private fun greenButton(text: String, action: () -> Unit, params: LinearLayout.LayoutParams): Button {
        return greenButton(text, action).apply { layoutParams = params }
    }

    private fun redButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(white)
            setTypeface(null, Typeface.BOLD)
            background = roundedDrawable(red, 18)
            setOnClickListener { action() }
        }
    }

    private fun redButton(text: String, action: () -> Unit, params: LinearLayout.LayoutParams): Button {
        return redButton(text, action).apply { layoutParams = params }
    }

    private fun roundedBox(color: Int, radius: Int): LinearLayout {
        return LinearLayout(this).apply {
            background = roundedDrawable(color, radius)
        }
    }

    private fun roundedDrawable(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(radius).toFloat()
        }
    }

    private fun marginParams(l: Int, t: Int, r: Int, b: Int): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(-1, -2).apply {
            setMargins(l, t, r, b)
        }
    }

    private fun clear() {
        content.removeAllViews()
        scrollView.post { scrollView.scrollTo(0, 0) }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

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
