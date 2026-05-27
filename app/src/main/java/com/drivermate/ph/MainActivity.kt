private fun addControlSection() {
    val card = whiteCard()

    val voiceRow = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
    }

    voiceRow.addView(TextView(this).apply {
        text = "🔊"
        textSize = 34f
        gravity = Gravity.CENTER
        setTextColor(green)
    }, LinearLayout.LayoutParams(dp(52), dp(58)))

    voiceRow.addView(TextView(this).apply {
        text = "Voice Message"
        textSize = 12f
        setTextColor(dark)
        setTypeface(null, Typeface.BOLD)
    }, LinearLayout.LayoutParams(0, -2, 1f))

    voiceRow.addView(Switch(this).apply {
        isChecked = prefs.getBoolean("voice_enabled", true)
        setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("voice_enabled", checked).apply()
            Toast.makeText(
                this@MainActivity,
                if (checked) "Voice Message ON" else "Voice Message OFF",
                Toast.LENGTH_SHORT
            ).show()
        }
    })

    card.addView(voiceRow)

    val speakAllRow = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(4), 0, dp(4))
    }

    speakAllRow.addView(TextView(this).apply {
        text = "🗣️"
        textSize = 30f
        gravity = Gravity.CENTER
        setTextColor(green)
    }, LinearLayout.LayoutParams(dp(52), dp(58)))

    speakAllRow.addView(TextView(this).apply {
        text = "Speak All Routes"
        textSize = 12f
        setTextColor(dark)
        setTypeface(null, Typeface.BOLD)
    }, LinearLayout.LayoutParams(0, -2, 1f))

    speakAllRow.addView(Switch(this).apply {
        isChecked = prefs.getBoolean("speak_all_routes", false)
        setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("speak_all_routes", checked).apply()
            Toast.makeText(
                this@MainActivity,
                if (checked) "Speaking all routes" else "Preferred routes only",
                Toast.LENGTH_SHORT
            ).show()
        }
    })

    card.addView(speakAllRow)

    card.addView(TextView(this).apply {
        text = "Enabled Booking Apps"
        textSize = 15f
        setTextColor(green)
        setTypeface(null, Typeface.BOLD)
        setPadding(0, dp(12), 0, dp(4))
    })

    card.addView(appSwitchRow("Lalamove", "enable_lalamove"))
    card.addView(appSwitchRow("Grab", "enable_grab"))
    card.addView(appSwitchRow("Transportify", "enable_transportify"))
    card.addView(appSwitchRow("Move It", "enable_moveit"))

    val wazeRow = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        setPadding(0, dp(8), 0, 0)
    }

    wazeRow.addView(ImageView(this).apply {
        setImageResource(R.drawable.waze_icon)
        scaleType = ImageView.ScaleType.FIT_CENTER
        setOnClickListener { openWaze("Imus, Cavite") }
    }, LinearLayout.LayoutParams(dp(76), dp(76)))

    wazeRow.addView(TextView(this).apply {
        text = "Auto Waze"
        textSize = 12f
        setTextColor(dark)
        setTypeface(null, Typeface.BOLD)
    }, LinearLayout.LayoutParams(0, -2, 1f))

    wazeRow.addView(Switch(this).apply {
        isChecked = prefs.getBoolean("auto_open_waze", true)
        setOnCheckedChangeListener { _, checked ->
            prefs.edit().putBoolean("auto_open_waze", checked).apply()
        }
    })

    card.addView(wazeRow)
    content.addView(card)
}
