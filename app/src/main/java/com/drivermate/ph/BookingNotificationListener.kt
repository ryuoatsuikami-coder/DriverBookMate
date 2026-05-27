private fun detectRoute(text: String): String {
    val cleanText = text
        .replace("\n", " ")
        .replace("→", " to ")
        .replace("➡", " to ")
        .replace("-", " ")
        .replace(Regex("\\s+"), " ")
        .trim()

    val lower = cleanText.lowercase()

    val allPlaces = listOf(
        "Alfonso", "Amadeo", "Bacoor", "Carmona", "Cavite City",
        "Dasmarinas", "General Trias", "Gen Trias", "Imus", "Indang",
        "Kawit", "Magallanes Cavite", "Maragondon", "Mendez", "Naic",
        "Noveleta", "Rosario", "Silang", "Tagaytay", "Tanza", "Ternate",
        "Trece Martires",

        "Manila", "Caloocan", "Las Pinas", "Makati", "Malabon",
        "Mandaluyong", "Marikina", "Muntinlupa", "Navotas", "Paranaque",
        "Pasay", "Pasig", "Pateros", "Quezon City", "San Juan",
        "Taguig", "Valenzuela", "BGC", "Alabang",

        "Binan", "Cabuyao", "Calamba", "San Pedro", "Santa Rosa",
        "Batangas City", "Lipa", "Tanauan", "Malolos", "Meycauayan",
        "San Jose del Monte", "Angeles", "Mabalacat", "San Fernando Pampanga"
    )

    val normalizedPlaces = allPlaces.map {
        val normalized = when (it.lowercase()) {
            "gen trias" -> "General Trias"
            else -> it
        }
        it to normalized
    }

    // 1. Best detection: direct "pickup to dropoff" pattern
    for ((rawFrom, cleanFrom) in normalizedPlaces) {
        for ((rawTo, cleanTo) in normalizedPlaces) {
            if (cleanFrom.equals(cleanTo, true)) continue

            val pattern1 = Regex(
                """\b${Regex.escape(rawFrom)}\b\s+(to|going to|drop.?off|destination)\s+\b${Regex.escape(rawTo)}\b""",
                RegexOption.IGNORE_CASE
            )

            if (pattern1.containsMatchIn(cleanText)) {
                return "$cleanFrom to $cleanTo"
            }
        }
    }

    // 2. Match saved routes exactly first
    for (saved in getSavedRoutes()) {
        val parts = saved.route.split(" to ", ignoreCase = true)

        if (parts.size == 2) {
            val savedFrom = normalizePlace(parts[0].trim())
            val savedTo = normalizePlace(parts[1].trim())

            if (
                lower.contains(savedFrom.lowercase()) &&
                lower.contains(savedTo.lowercase()) &&
                !savedFrom.equals(savedTo, true)
            ) {
                return "$savedFrom to $savedTo"
            }
        }
    }

    // 3. Fallback: get first two DIFFERENT places by order of appearance
    val found = normalizedPlaces
        .mapNotNull { (raw, clean) ->
            val index = lower.indexOf(raw.lowercase())
            if (index >= 0) index to clean else null
        }
        .sortedBy { it.first }
        .map { it.second }
        .distinctBy { it.lowercase() }

    return if (found.size >= 2) {
        "${found[0]} to ${found[1]}"
    } else {
        "Route not detected"
    }
}

private fun normalizePlace(place: String): String {
    return when (place.trim().lowercase()) {
        "gen trias" -> "General Trias"
        "dasma" -> "Dasmarinas"
        "qc" -> "Quezon City"
        else -> place.trim()
    }
}
