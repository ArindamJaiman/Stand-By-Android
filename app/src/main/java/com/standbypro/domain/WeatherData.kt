package com.standbypro.domain

enum class WeatherCondition(val displayName: String, val iconName: String) {
    CLEAR_DAY("Sunny", "WbSunny"),
    CLEAR_NIGHT("Clear", "NightsStay"),
    PARTLY_CLOUDY_DAY("Partly Cloudy", "CloudQueue"),
    PARTLY_CLOUDY_NIGHT("Partly Cloudy", "NightsStay"),
    CLOUDY("Overcast", "Cloud"),
    RAIN("Rain Showers", "Grain"),
    HEAVY_RAIN("Heavy Rain", "Thunderstorm"),
    THUNDERSTORM("Thunderstorm", "FlashOn"),
    SNOW("Snow", "AcUnit"),
    FOG("Foggy", "Dehaze"),
    WINDY("Windy", "Air");

    val isRainy: Boolean get() = this == RAIN || this == HEAVY_RAIN || this == THUNDERSTORM
    val isCloudy: Boolean get() = this == CLOUDY || this == PARTLY_CLOUDY_DAY || this == PARTLY_CLOUDY_NIGHT || this == FOG

    companion object {
        /** WMO weather interpretation codes (used by Open-Meteo). */
        fun fromWmo(code: Int, isDay: Boolean): WeatherCondition = when (code) {
            0, 1 -> if (isDay) CLEAR_DAY else CLEAR_NIGHT
            2 -> if (isDay) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
            3 -> CLOUDY
            45, 48 -> FOG
            51, 53, 55, 56, 57, 61, 63, 80, 81 -> RAIN
            65, 66, 67, 82 -> HEAVY_RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 99 -> THUNDERSTORM
            else -> if (isDay) PARTLY_CLOUDY_DAY else PARTLY_CLOUDY_NIGHT
        }
    }
}

data class HourlyForecast(
    val timeLabel: String,
    val temperatureCelsius: Int,
    val condition: WeatherCondition,
    val precipitationChance: Int = 0
)

data class DailyForecast(
    val dayLabel: String,
    val highCelsius: Int,
    val lowCelsius: Int,
    val condition: WeatherCondition
)

data class WeatherData(
    val cityName: String = "San Francisco",
    val temperatureCelsius: Int = 21,
    val condition: WeatherCondition = WeatherCondition.PARTLY_CLOUDY_DAY,
    val feelsLikeCelsius: Int = 22,
    val highCelsius: Int = 24,
    val lowCelsius: Int = 16,
    val humidityPercent: Int = 58,
    val windKmh: Int = 14,
    val uvIndex: Int = 4,
    val sunriseTime: String = "6:42 AM",
    val sunsetTime: String = "7:18 PM",
    val hourlyForecast: List<HourlyForecast> = emptyList(),
    val dailyForecast: List<DailyForecast> = emptyList(),
    val isDay: Boolean = true,
    val precipitationChance: Int = 0,
    val updatedAtMillis: Long = 0L,
    val isLive: Boolean = false
)
