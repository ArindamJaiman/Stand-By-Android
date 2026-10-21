package com.standbypro.domain

import com.standbypro.ui.faces.FaceMath

/** The spoken daily briefing: time, weather, rain, next event and alarm in plain sentences. */
object Briefing {

    fun compose(ctx: FaceContext, nowMillis: Long): String = buildList {
        add("It's ${FaceMath.spokenTime(ctx.time.hour, ctx.time.minute)}.")
        ctx.weather?.let { w ->
            add("It's ${degrees(ctx, w.temperatureCelsius)} and ${w.condition.displayName.lowercase()}, with a high of ${degrees(ctx, w.highCelsius)}.")
            RainSoon.outlook(w)?.let { outlook ->
                val (chance, time) = outlook.split("% at ").let { it[0] to it.getOrElse(1) { "" } }
                add("There's a $chance percent chance of rain at $time.")
            }
        }
        nextEvent(ctx.events, nowMillis)?.let { e ->
            val at = e.timeRange.substringBefore(" -").trim()
            add(if (at.isNotEmpty()) "Next up at $at: ${e.title}." else "Next up: ${e.title}.")
        }
        add(ctx.nextAlarmTime?.let { "Your alarm is set for $it." } ?: "No alarm is set.")
    }.joinToString(" ")

    private fun degrees(ctx: FaceContext, celsius: Int): String =
        if (ctx.useFahrenheit) "${(celsius * 9 / 5.0 + 32).toInt()} degrees" else "$celsius degrees"

    /** The next timed event that hasn't started yet; falls back to the first event without times. */
    fun nextEvent(events: List<CalendarEventItem>, nowMillis: Long): CalendarEventItem? =
        events.filter { !it.isAllDay && it.startMillis > nowMillis }.minByOrNull { it.startMillis }
            ?: events.firstOrNull { !it.isAllDay && it.startMillis == 0L }
}
