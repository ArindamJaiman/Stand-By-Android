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
    WINDY("Windy", "Air")
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
    val dailyForecast: List<DailyForecast> = emptyList()
)
