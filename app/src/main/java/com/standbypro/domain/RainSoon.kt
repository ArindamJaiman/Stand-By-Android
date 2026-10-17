package com.standbypro.domain

/** "Rain likely" heads-up for the Live Activity pill. */
object RainSoon {

    /**
     * Returns e.g. "70% at 3pm" when rain is likely in the next two hours and it isn't raining
     * yet; null otherwise.
     */
    fun outlook(weather: WeatherData?, threshold: Int = 60): String? {
        if (weather == null || weather.condition.isRainy) return null
        val next = weather.hourlyForecast.drop(1).take(2)
            .firstOrNull { it.precipitationChance >= threshold || it.condition.isRainy } ?: return null
        return "${next.precipitationChance}% at ${next.timeLabel}"
    }
}
