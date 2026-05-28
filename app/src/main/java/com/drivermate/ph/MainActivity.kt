package com.drivermate.ph

import android.app.Activityimport android.content.Intentimport android.graphics.Colorimport android.graphics.Typefaceimport android.net.Uriimport android.os.Bundleimport android.provider.Settingsimport android.speech.tts.TextToSpeechimport android.text.Editableimport android.text.InputTypeimport android.text.TextWatcherimport android.view.Gravityimport android.view.Viewimport android.widget.*import java.util.Localeimport kotlin.math.abs

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
    "Dasmarinas", "General Trias", "Imus", "Indang", "Kawit",
    "Magallanes Cavite", "Maragondon", "Mendez", "Naic", "Noveleta",
    "Rosario", "Silang", "Tagaytay", "Tanza", "Ternate", "Trece Martires"
)

private val manilaPlaces = listOf(
    "Manila", "Caloocan", "Las Pinas", "Makati", "Malabon",
    "Mandaluyong", "Marikina", "Muntinlupa", "Navotas", "Paranaque",
    "Pasay", "Pasig", "Pateros", "Quezon City", "San Juan",
    "Taguig", "Valenzuela", "BGC", "Alabang"
)

private val lagunaPlaces = listOf(
    "Alaminos Laguna", "Bay", "Binan", "Cabuyao", "Calamba",
    "Calauan", "Cavinti", "Famy", "Kalayaan", "Liliw",
    "Los Banos", "Luisiana", "Lumban", "Mabitac", "Magdalena",
    "Majayjay", "Nagcarlan", "Paete", "Pagsanjan", "Pakil",
    "Pangil", "Pila", "Rizal Laguna", "San Pablo", "San Pedro",
    "Santa Cruz Laguna", "Santa Maria Laguna", "Santa Rosa",
    "Siniloan", "Victoria"
)

private val batangasPlaces = listOf(
    "Agoncillo", "Alitagtag", "Balayan", "Balete", "Batangas City",
    "Bauan", "Calaca", "Calatagan", "Cuenca", "Ibaan",
    "Laurel", "Lemery", "Lian", "Lipa", "Lobo", "Mabini",
    "Malvar", "Mataasnakahoy", "Nasugbu", "Padre Garcia",
    "Rosario Batangas", "San Jose Batangas", "San Juan Batangas",
    "San Luis Batangas", "San Nicolas", "San Pascual",
    "Santa Teresita", "Santo Tomas Batangas", "Taal",
    "Talisay Batangas", "Tanauan", "Taysan", "Tingloy", "Tuy"
)

private val bulacanPlaces = listOf(
    "Angat", "Balagtas", "Baliwag", "Bocaue", "Bulakan",
    "Bustos", "Calumpit", "Dona Remedios Trinidad", "Guiguinto",
    "Hagonoy", "Malolos", "Marilao", "Meycauayan", "Norzagaray",
    "Obando", "Pandi", "Paombong", "Plaridel", "Pulilan",
    "San Ildefonso", "San Jose del Monte", "San Miguel",
    "San Rafael", "Santa Maria Bulacan"
)

private val pampangaPlaces = listOf(
    "Angeles", "Apalit", "Arayat", "Bacolor", "Candaba",
    "Floridablanca", "Guagua", "Lubao", "Mabalacat", "Macabebe",
    "Magalang", "Masantol", "Mexico", "Minalin", "Porac",
    "San Fernando Pampanga", "San Luis Pampanga", "San Simon",
    "Santa Ana Pampanga", "Santa Rita", "Santo Tomas Pampanga", "Sasmuan"
)

private val allPlaces = (
    cavitePlaces +
        manilaPlaces +
        lagunaPlaces +
        batangasPlaces +
        bulacanPlaces +
        pampangaPlaces
    ).distinct().sorted()

override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    tts = TextToSpeech(this) { status ->
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("en", "PH"))
            if (
                result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
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

private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()

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
        "Cavite, Manila, Laguna, Batangas, Bulacan, and Pampanga route alerts.",
        listOf("Long-distance route suggestions", "Cavite to Manila routes", "Manila to Cavite routes", "Auto-open Waze setting")
    )

    addPreferredRoutesSection()
    addControlSection()
}

private fun addTopBar() {
    val top = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    top.addView(TextView(this).apply {
        text = "☰"
        textSize = 30f
        setTextColor(green)
        gravity = Gravity.CENTER
    }, LinearLayout.LayoutParams(dp(48), dp(48)))

    top.addView(TextView(this).apply {
        text = "DriverMate PH"
        textSize = 28f
        gravity = Gravity.CENTER
        setTextColor(green)
        setTypeface(null, Typeface.BOLD)
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

    card.addView(TextView(this).apply {
        text = "Preferred Route Preview"
        textSize = 18f
        setTextColor(green)
        setTypeface(null, Typeface.BOLD)
        setPadding(dp(4), 0, 0, dp(8))
    })

    card.addView(firstPriorityCard())

    card.addView(greenButton("Manual Add Preferred Route") {
        showCreateRoute()
    })

    val dropdownTitle = TextView(this).apply {
        text = "All saved preferred routes      ⌄"
        textSize = 15f
        setTextColor(dark)
        setPadding(dp(12), dp(10), dp(12), dp(10))
        setBackgroundColor(Color.WHITE)
    }

    val dropdownBox = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        visibility = View.GONE
    }

    if (saved.isEmpty()) {
        dropdownBox.addView(TextView(this).apply {
            text = "No preferred routes yet."
            textSize = 14f
            setTextColor(gray)
            setPadding(dp(12), dp(8), dp(12), dp(8))
        })
    } else {
        saved.forEach {
            dropdownBox.addView(routeCard(it))
        }
    }

    dropdownTitle.setOnClickListener {
        dropdownBox.visibility = if (dropdownBox.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        dropdownTitle.text = if (dropdownBox.visibility == View.VISIBLE) {
            "All saved preferred routes      ⌃"
        } else {
            "All saved preferred routes      ⌄"
        }
    }

    card.addView(dropdownTitle)
    card.addView(dropdownBox)
    content.addView(card)
}

private fun firstPriorityCard(): LinearLayout {
    val selectedFirstPriority = getFirstPriorityRoute()
    val displayRoute = if (selectedFirstPriority.isBlank()) {
        "No first priority route selected"
    } else {
        selectedFirstPriority
    }

    val row = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(12), dp(10), dp(12), dp(10))
        setBackgroundColor(lightGreen)
    }

    row.addView(TextView(this).apply {
        text = "🏅 MANUAL FIRST PRIORITY"
        textSize = 16f
        gravity = Gravity.CENTER
        setTextColor(green)
        setTypeface(null, Typeface.BOLD)
    })

    row.addView(TextView(this).apply {
        text = displayRoute
        textSize = 18f
        gravity = Gravity.CENTER
        setTextColor(dark)
        setTypeface(null, Typeface.BOLD)
        setPadding(0, dp(8), 0, dp(8))
    })

    row.addView(TextView(this).apply {
        text = "Only this manually selected route can auto-open Waze."
        textSize = 13f
        gravity = Gravity.CENTER
        setTextColor(gray)
    })

    row.addView(Button(this).apply {
        text = "CLEAR FIRST PRIORITY"
        textSize = 12f
        setTextColor(Color.WHITE)
        setBackgroundColor(orange)
        setOnClickListener {
            clearFirstPriorityRoute()
            Toast.makeText(this@MainActivity, "First priority cleared", Toast.LENGTH_SHORT).show()
            showHome()
        }
    })

    return row
}

private fun addControlSection() {
    val card = whiteCard().apply {
        setPadding(dp(16), dp(18), dp(16), dp(18))
    }

    card.addView(settingSwitchRow("📣", "Voice Message", "voice_enabled", true))
    card.addView(settingSwitchRow("🗣️", "Speak All Routes", "speak_all_routes", false))

    card.addView(TextView(this).apply {
        text = "Enabled Booking Apps"
        textSize = 22f
        setTextColor(green)
        setTypeface(null, Typeface.BOLD)
        setPadding(0, dp(22), 0, dp(10))
    })

    card.addView(appSwitchRow("Lalamove", "enable_lalamove"))
    card.addView(appSwitchRow("Grab", "enable_grab"))
    card.addView(appSwitchRow("Transportify", "enable_transportify"))
    card.addView(appSwitchRow("Move It", "enable_moveit"))

    content.addView(card)

    val wazeCard = whiteCard().apply {
        setPadding(dp(16), dp(16), dp(16), dp(16))
    }

    val wazeRow = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    wazeRow.addView(ImageView(this).apply {
        setImageResource(R.drawable.waze_icon)
        scaleType = ImageView.ScaleType.FIT_CENTER
    }, LinearLayout.LayoutParams(dp(90), dp(90)))

    wazeRow.addView(TextView(this).apply {
        text = "Auto Waze"
        textSize = 22f
        setTextColor(green)
        setTypeface(null, Typeface.BOLD)
        setPadding(dp(18), 0, 0, 0)
    }, LinearLayout.LayoutParams(0, -2, 1f))

    wazeRow.addView(Switch(this).apply {
        isChecked = prefs.getBoolean("auto_open_waze", true)
        setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_open_waze", checked).apply()
        }
    })

    wazeCard.addView(wazeRow)
    content.addView(wazeCard)
}

private fun settingSwitchRow(icon: String, label: String, key: String, defaultValue: Boolean): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(10), 0, dp(18))

        addView(TextView(this@MainActivity).apply {
            text = icon
            textSize = 38f
            gravity = Gravity.CENTER
        }, LinearLayout.LayoutParams(dp(78), dp(70)))

        addView(TextView(this@MainActivity).apply {
            text = label
            textSize = 22f
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        addView(Switch(this@MainActivity).apply {
            isChecked = prefs.getBoolean(key, defaultValue)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(key, checked).apply()
            }
        })
    }
}

private fun appSwitchRow(label: String, key: String): LinearLayout {
    return LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(8), 0, dp(8))

        addView(TextView(this@MainActivity).apply {
            text = ""
        }, LinearLayout.LayoutParams(dp(78), dp(54)))

        addView(TextView(this@MainActivity).apply {
            text = label
            textSize = 20f
            setTextColor(green)
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(0, -2, 1f))

        addView(Switch(this@MainActivity).apply {
            isChecked = prefs.getBoolean(key, true)
            setOnCheckedChangeListener { _, checked ->
                prefs.edit().putBoolean(key, checked).apply()
                Toast.makeText(
                    this@MainActivity,
                    "$label ${if (checked) "ON" else "OFF"}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }
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

        val cleanQuery = query.trim()

        val routes = if (cleanQuery.isBlank()) {
            generateVisibleRoutes().take(100)
        } else {
            generateAllRoutes()
                .filter { it.route.contains(cleanQuery, true) }
                .take(100)
        }

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

    val selectedFrom = arrayOf("")
    val selectedTo = arrayOf("")

    val fromInput = EditText(this).apply {
        hint = "Type pickup place e.g. Tanza"
        inputType = InputType.TYPE_CLASS_TEXT
        setSingleLine(true)
    }

    val fromList = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

    val toInput = EditText(this).apply {
        hint = "Type dropoff place e.g. Imus"
        inputType = InputType.TYPE_CLASS_TEXT
        setSingleLine(true)
    }

    val toList = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

    fun renderPlaceList(query: String, box: LinearLayout, input: EditText, target: Array<String>) {
        box.removeAllViews()

        val q = query.trim()
        if (q.isBlank()) return

        val results = allPlaces
            .filter { it.startsWith(q, true) }
            .take(30)

        results.forEach { place ->
            box.addView(TextView(this).apply {
                text = place
                textSize = 16f
                setTextColor(dark)
                setPadding(dp(12), dp(8), dp(12), dp(8))
                setBackgroundColor(Color.WHITE)
                setOnClickListener {
                    target[0] = place
                    input.setText(place)
                    input.setSelection(input.text.length)
                    box.removeAllViews()
                }
            })
        }
    }

    fromInput.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            selectedFrom[0] = s.toString()
            renderPlaceList(s.toString(), fromList, fromInput, selectedFrom)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })

    toInput.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            selectedTo[0] = s.toString()
            renderPlaceList(s.toString(), toList, toInput, selectedTo)
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })

    val fare = EditText(this).apply {
        hint = "Minimum fare example: 200"
        inputType = InputType.TYPE_CLASS_NUMBER
    }

    val distance = EditText(this).apply {
        hint = "Optional distance note example: manual only"
        inputType = InputType.TYPE_CLASS_TEXT
    }

    content.addView(TextView(this).apply { text = "From / Pickup" })
    content.addView(fromInput)
    content.addView(fromList)

    content.addView(TextView(this).apply { text = "To / Dropoff" })
    content.addView(toInput)
    content.addView(toList)

    content.addView(fare)
    content.addView(distance)

    content.addView(greenButton("Add Preferred Route") {
        val fromValue = selectedFrom[0].trim()
        val toValue = selectedTo[0].trim()

        if (fromValue.isBlank() || toValue.isBlank()) {
            Toast.makeText(this, "Please enter pickup and dropoff", Toast.LENGTH_SHORT).show()
            return@greenButton
        }

        val route = "$fromValue to $toValue"
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
    content.addView(firstPriorityCard())
    content.addView(greenButton("Test Voice Alert") { speakTest() })
    content.addView(greenButton("Open Waze") { openWaze("Imus, Cavite") })
}

private fun showSettings() {
    clear()

    content.addView(TextView(this).apply {
        text = "Settings"
        textSize = 30f
        gravity = Gravity.CENTER
        setTextColor(green)
        setTypeface(null, Typeface.BOLD)
    })

    addControlSection()

    content.addView(greenButton("Notification Access") {
        startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    })

    content.addView(greenButton("Test Voice") { speakTest() })
    content.addView(greenButton("Manage Routes") { showRoutes() })

    content.addView(TextView(this).apply {
        text = "\nVersion 1.4.3\nDriverMate PH"
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

private fun routeCard(route: RouteData): LinearLayout {
    val c = whiteCard()
    val firstPriority = getFirstPriorityRoute()
    val isFirstPriority = firstPriority.equals(route.route, true)

    val row = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
    }

    row.addView(TextView(this).apply {
        text = if (isFirstPriority) "🏅 ${route.route}" else "📍 ${route.route}"
        textSize = 16f
        setTextColor(dark)
        setTypeface(null, Typeface.BOLD)
    })

    row.addView(TextView(this).apply {
        text = "Fare: ₱${route.fare}     •     Distance: ${route.distance}"
        textSize = 14f
        setTextColor(green)
    })

    val buttons = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(8), 0, 0)
    }

    buttons.addView(Button(this).apply {
        text = if (isFirstPriority) "FIRST PRIORITY" else "SET FIRST"
        textSize = 11f
        setTextColor(Color.WHITE)
        setBackgroundColor(if (isFirstPriority) gray else green)
        isEnabled = !isFirstPriority
        setOnClickListener {
            setFirstPriorityRoute(route.route)
            Toast.makeText(this@MainActivity, "Set as first priority", Toast.LENGTH_SHORT).show()
            showHome()
        }
    }, LinearLayout.LayoutParams(0, -2, 1f))

    buttons.addView(Button(this).apply {
        text = "REMOVE"
        textSize = 11f
        setTextColor(Color.WHITE)
        setBackgroundColor(orange)
        setOnClickListener {
            removeSavedRoute(route.route)

            if (getFirstPriorityRoute().equals(route.route, true)) {
                clearFirstPriorityRoute()
            }

            Toast.makeText(this@MainActivity, "Preferred route removed", Toast.LENGTH_SHORT).show()
            showHome()
        }
    }, LinearLayout.LayoutParams(0, -2, 1f))

    row.addView(buttons)
    c.addView(row)

    return c
}

private fun generateVisibleRoutes(): List<RouteData> {
    val list = mutableListOf<RouteData>()

    for (from in cavitePlaces) {
        for (to in cavitePlaces) {
            list.add(makeRoute(from, to))
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

private fun generateAllRoutes(): List<RouteData> {
    val list = mutableListOf<RouteData>()
    val searchablePlaces = allPlaces.distinct()

    for (from in searchablePlaces) {
        for (to in searchablePlaces) {
            list.add(makeRoute(from, to))
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
    val intercity =
        lagunaPlaces.contains(from) || lagunaPlaces.contains(to) ||
            batangasPlaces.contains(from) || batangasPlaces.contains(to) ||
            bulacanPlaces.contains(from) || bulacanPlaces.contains(to) ||
            pampangaPlaces.contains(from) || pampangaPlaces.contains(to)

    val manilaRoute = manilaPlaces.contains(from) || manilaPlaces.contains(to)

    return when {
        intercity -> base + 45
        manilaRoute -> base + 18
        else -> base
    }
}

private fun estimateFare(distance: Int): Int {
    return ((distance * 12) + 80).coerceAtLeast(150)
}

private fun saveFullRoute(route: String, fare: String, distance: String) {
    val current = prefs.getString("saved_full_routes", "") ?: ""
    val newItem = "$route~$fare~$distance"

    if (current.lowercase().contains(route.lowercase())) {
        Toast.makeText(this, "Route already saved", Toast.LENGTH_SHORT).show()
        return
    }

    val updated = if (current.isBlank()) newItem else "$current|$newItem"
    prefs.edit().putString("saved_full_routes", updated).apply()
}

private fun removeSavedRoute(route: String) {
    val current = prefs.getString("saved_full_routes", "") ?: ""

    val updated = current.split("|")
        .filter { item ->
            val parts = item.split("~")
            parts.isNotEmpty() && !parts[0].equals(route, true)
        }
        .joinToString("|")

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

private fun getFirstPriorityRoute(): String {
    return prefs.getString("first_priority_route", "") ?: ""
}

private fun setFirstPriorityRoute(route: String) {
    prefs.edit().putString("first_priority_route", route).apply()
}

private fun clearFirstPriorityRoute() {
    prefs.edit().remove("first_priority_route").apply()
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
