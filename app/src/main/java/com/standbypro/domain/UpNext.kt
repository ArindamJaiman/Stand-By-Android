package com.standbypro.domain

/** Picks the calendar event worth surfacing in the Live Activity pill. */
object UpNext {

    private const val MINUTE = 60_000L

    /** The soonest timed event that starts within [leadMinutes] or began under 5 minutes ago. */
    fun eventFor(events: List<CalendarEventItem>, nowMillis: Long, leadMinutes: Int = 15): CalendarEventItem? =
        events.asSequence()
            .filter { !it.isAllDay && it.startMillis > 0L }
            .filter { it.startMillis - nowMillis in -5 * MINUTE..leadMinutes * MINUTE }
            .minByOrNull { it.startMillis }

    /** "in 8 min" before the start, "Now" once it has begun. */
    fun startsIn(event: CalendarEventItem, nowMillis: Long): String {
        val minutes = (event.startMillis - nowMillis + MINUTE - 1) / MINUTE
        return if (minutes <= 0) "Now" else "in $minutes min"
    }
}
