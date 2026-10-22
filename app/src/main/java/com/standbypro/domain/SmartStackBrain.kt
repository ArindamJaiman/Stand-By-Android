package com.standbypro.domain

/**
 * Smart Stack intelligence, like iOS: scores each widget against what's happening right now
 * (music playing, a timer running, a meeting soon, rain on the way, bedtime) and suggests the
 * one worth showing. Below [SUGGEST_THRESHOLD] nothing is relevant and the stack rotates as usual.
 */
object SmartStackBrain {

    const val SUGGEST_THRESHOLD = 50

    data class Signals(
        val hour: Int,
        val musicPlaying: Boolean = false,
        val timerRunning: Boolean = false,
        val pomodoroRunning: Boolean = false,
        val stopwatchRunning: Boolean = false,
        val minutesToNextEvent: Int? = null,
        val rainSoon: Boolean = false,
        val isNight: Boolean = false,
        val charging: Boolean = false,
        val batteryPercent: Int = 100
    ) {
        val morning: Boolean get() = hour in 6..9
        val bedtime: Boolean get() = hour >= 21 || hour < 2
    }

    fun score(widgetId: String, s: Signals): Int = when (widgetId) {
        "widget_music" -> if (s.musicPlaying) 100 else 0
        "widget_timer" -> if (s.timerRunning) 95 else 0
        "widget_pomodoro" -> if (s.pomodoroRunning) 95 else 0
        "widget_stopwatch" -> if (s.stopwatchRunning) 90 else 0
        "widget_agenda" -> when {
            s.minutesToNextEvent != null && s.minutesToNextEvent <= 60 -> 80
            s.morning -> 45
            else -> 0
        }
        "widget_calendar" -> if (s.minutesToNextEvent != null && s.minutesToNextEvent <= 60) 70 else 0
        "widget_weather" -> when {
            s.rainSoon -> 75
            s.morning -> 55
            else -> 0
        }
        "widget_forecast" -> when {
            s.rainSoon -> 65
            s.morning -> 50
            else -> 0
        }
        "widget_air_quality" -> if (s.morning) 45 else 0
        "widget_alarm" -> if (s.bedtime) 60 else 0
        "widget_breathe" -> if (s.bedtime || s.isNight) 55 else 0
        "widget_battery", "widget_charge_history" -> if (s.charging && s.batteryPercent < 25) 65 else 0
        "widget_sun" -> if (s.hour in 17..19) 50 else 0
        "widget_moon" -> if (s.isNight) 35 else 0
        else -> 0
    }

    /** Index of the widget to surface, or null when nothing stands out. Ties keep stack order. */
    fun suggest(ids: List<String>, s: Signals): Int? {
        var best: Int? = null
        var bestScore = SUGGEST_THRESHOLD - 1
        ids.forEachIndexed { i, id ->
            val sc = score(id, s)
            if (sc > bestScore) {
                best = i
                bestScore = sc
            }
        }
        return best
    }
}
