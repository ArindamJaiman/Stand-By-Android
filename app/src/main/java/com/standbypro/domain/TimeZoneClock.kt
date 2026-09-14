package com.standbypro.domain

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Real timezone model supporting DST-safe calculations via Java Time APIs.
 */
data class TimeZoneClock(
    val zoneId: String,
    val city: String,
    val country: String,
    val customLabel: String? = null
) {
    fun getCurrentZonedDateTime(): ZonedDateTime {
        return try {
            ZonedDateTime.now(ZoneId.of(zoneId))
        } catch (e: Exception) {
            ZonedDateTime.now()
        }
    }

    fun getFormattedTime(use24Hour: Boolean = false): String {
        val pattern = if (use24Hour) "HH:mm" else "h:mm a"
        return getCurrentZonedDateTime().format(DateTimeFormatter.ofPattern(pattern))
    }

    fun getFormattedOffset(): String {
        val now = getCurrentZonedDateTime()
        val offsetSeconds = now.offset.totalSeconds
        val hours = offsetSeconds / 3600
        val sign = if (hours >= 0) "+" else ""
        return "UTC$sign$hours"
    }

    companion object {
        val DEFAULT_WORLD_CLOCKS = listOf(
            TimeZoneClock("Asia/Kolkata", "Delhi", "India"),
            TimeZoneClock("Europe/London", "London", "United Kingdom"),
            TimeZoneClock("America/New_York", "New York", "USA"),
            TimeZoneClock("Asia/Tokyo", "Tokyo", "Japan"),
            TimeZoneClock("Asia/Dubai", "Dubai", "UAE"),
            TimeZoneClock("UTC", "UTC", "Universal Time")
        )
    }
}
