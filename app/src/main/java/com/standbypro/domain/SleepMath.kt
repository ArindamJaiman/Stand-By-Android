package com.standbypro.domain

/** Sleep-cycle maths for the Sleep Cycles face (90-minute cycles, ~15 minutes to fall asleep). */
object SleepMath {

    const val CYCLE_MINUTES = 90
    const val FALL_ASLEEP_MINUTES = 15
    private const val MINUTE = 60_000L

    data class Plan(val asleepAtMillis: Long, val wakeMillis: Long) {
        val sleepMinutes: Int get() = ((wakeMillis - asleepAtMillis) / MINUTE).toInt()
        val cycles: Float get() = sleepMinutes / CYCLE_MINUTES.toFloat()
    }

    /** Sleep left before [alarmMillis] if you settle now, or null when the alarm is too soon or far off. */
    fun planToAlarm(nowMillis: Long, alarmMillis: Long?): Plan? {
        val asleep = nowMillis + FALL_ASLEEP_MINUTES * MINUTE
        if (alarmMillis == null || alarmMillis <= asleep || alarmMillis - nowMillis > 16 * 60 * MINUTE) return null
        return Plan(asleep, alarmMillis)
    }

    /** Wake-up times that land at the end of full cycles. */
    fun wakeSuggestions(nowMillis: Long, cycles: IntRange = 4..6): List<Pair<Int, Long>> =
        cycles.map { it to nowMillis + (FALL_ASLEEP_MINUTES + it * CYCLE_MINUTES) * MINUTE }

    fun durationLabel(minutes: Int): String = "${minutes / 60} h ${"%02d".format(minutes % 60)} m"
}
