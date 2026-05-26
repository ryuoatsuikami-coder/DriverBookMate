package com.drivermate.ph

object BookingParser {

    fun cleanMessage(message: String): String {
        return message
            .replace("\n", " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    fun isPossibleBooking(message: String): Boolean {
        val lower = message.lowercase()

        return lower.contains("pickup") ||
                lower.contains("dropoff") ||
                lower.contains("fare") ||
                lower.contains("booking") ||
                lower.contains("delivery")
    }
}
