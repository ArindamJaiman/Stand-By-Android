package com.standbypro.domain

import androidx.compose.ui.graphics.Color

/** US EPA air-quality bands. */
enum class AqiCategory(val label: String, val upperBound: Int, val color: Color, val advice: String) {
    GOOD("Good", 50, Color(0xFF30D158), "Great day to open the windows"),
    MODERATE("Moderate", 100, Color(0xFFFFD60A), "Acceptable for most people"),
    SENSITIVE("Unhealthy for sensitive groups", 150, Color(0xFFFF9500), "Sensitive groups should limit time outside"),
    UNHEALTHY("Unhealthy", 200, Color(0xFFFF3B30), "Keep windows closed"),
    VERY_UNHEALTHY("Very unhealthy", 300, Color(0xFFBF5AF2), "Avoid going outside"),
    HAZARDOUS("Hazardous", Int.MAX_VALUE, Color(0xFFB0254F), "Stay indoors");

    companion object {
        fun of(aqi: Int): AqiCategory = entries.first { aqi <= it.upperBound }
    }
}

data class AirQuality(
    val usAqi: Int,
    val pm25: Double,
    val pm10: Double,
    val ozone: Double,
    val uvIndex: Double,
    val updatedAtMillis: Long = 0L
) {
    val category: AqiCategory get() = AqiCategory.of(usAqi)

    val uvLabel: String
        get() = when {
            uvIndex < 3 -> "Low"
            uvIndex < 6 -> "Moderate"
            uvIndex < 8 -> "High"
            uvIndex < 11 -> "Very high"
            else -> "Extreme"
        }
}
