package com.standbypro.alarm

import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.Locale

/** StandBy's own bedside alarm. [days] empty means "once". */
data class BedsideAlarm(
    val enabled: Boolean = false,
    val hour: Int = 7,
    val minute: Int = 0,
    val days: Set<DayOfWeek> = DayOfWeek.entries.filter { it.value <= 5 }.toSet(),
    val gentleWake: Boolean = true,
    val snoozeMinutes: Int = 9,
    val label: String = "Alarm"
)

object AlarmMath {

    /** The next moment [alarm] should ring after [now], or null when it's off. */
    fun nextTrigger(alarm: BedsideAlarm, now: ZonedDateTime): ZonedDateTime? {
        if (!alarm.enabled) return null
        val time = LocalTime.of(alarm.hour.coerceIn(0, 23), alarm.minute.coerceIn(0, 59))
        for (offset in 0..7) {
            val candidate = ZonedDateTime.of(now.toLocalDate().plusDays(offset.toLong()), time, now.zone)
            if (!candidate.isAfter(now)) continue
            if (alarm.days.isEmpty() || candidate.dayOfWeek in alarm.days) return candidate
        }
        return null
    }

    /** "Weekdays", "Every day", "Mon, Wed, Fri" or "Once". */
    fun describeDays(days: Set<DayOfWeek>, locale: Locale = Locale.getDefault()): String {
        val weekdays = DayOfWeek.entries.filter { it.value <= 5 }.toSet()
        val weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        return when (days) {
            emptySet<DayOfWeek>() -> "Once"
            DayOfWeek.entries.toSet() -> "Every day"
            weekdays -> "Weekdays"
            weekend -> "Weekends"
            else -> days.sorted().joinToString(", ") { it.getDisplayName(TextStyle.SHORT, locale) }
        }
    }

    /** "Rings in 7 h 12 m" style countdown. */
    fun ringsIn(now: ZonedDateTime, trigger: ZonedDateTime): String {
        val minutes = Duration.between(now, trigger).toMinutes().coerceAtLeast(0) + 1
        val h = minutes / 60
        val m = minutes % 60
        return when {
            h == 0L -> "Rings in $m min"
            m == 0L -> "Rings in $h h"
            else -> "Rings in $h h $m min"
        }
    }
}
