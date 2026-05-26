package com.drivermate.ph

object RouteMatcher {

    private val preferredKeywords = listOf(
        "cavite",
        "dasma",
        "dasmarinas",
        "imus",
        "bacoor",
        "general trias",
        "gentri",
        "tagaytay",
        "manila",
        "makati",
        "bgc",
        "pasay"
    )

    fun isPreferredRoute(message: String): Boolean {
        val lower = message.lowercase()
        return preferredKeywords.any { keyword ->
            lower.contains(keyword)
        }
    }
}
