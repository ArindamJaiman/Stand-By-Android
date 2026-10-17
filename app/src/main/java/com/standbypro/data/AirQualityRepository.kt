package com.standbypro.data

import android.util.Log
import com.standbypro.domain.AirQuality
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt

/** Live air quality and UV from the Open-Meteo air-quality API (free, no key). */
object AirQualityRepository {

    private const val REFRESH_INTERVAL_MS = 30 * 60 * 1000L

    private val _airQuality = MutableStateFlow<AirQuality?>(null)
    val airQuality: StateFlow<AirQuality?> = _airQuality.asStateFlow()

    private var lastFetchMillis = 0L
    private var lastKey = ""

    suspend fun refresh(latitude: Double, longitude: Double, force: Boolean = false) {
        val key = "${latitude.toFloat()},${longitude.toFloat()}"
        val now = System.currentTimeMillis()
        if (!force && key == lastKey && now - lastFetchMillis < REFRESH_INTERVAL_MS && _airQuality.value != null) return
        try {
            val json = withContext(Dispatchers.IO) { WeatherRepository.download(url(latitude, longitude)) }
            if (json != null) {
                _airQuality.value = parse(json)
                lastFetchMillis = now
                lastKey = key
            } else if (key != lastKey) {
                _airQuality.value = null
            }
        } catch (e: Exception) {
            Log.w("AirQualityRepository", "Air quality refresh failed: ${e.message}")
        }
    }

    private fun url(lat: Double, lon: Double): String =
        "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=${"%.4f".format(Locale.US, lat)}&longitude=${"%.4f".format(Locale.US, lon)}" +
            "&current=us_aqi,pm2_5,pm10,ozone,uv_index&timezone=auto"

    internal fun parse(json: String): AirQuality {
        val current = JSONObject(json).getJSONObject("current")
        fun value(name: String) = current.optDouble(name).takeIf { !it.isNaN() } ?: 0.0
        return AirQuality(
            usAqi = value("us_aqi").roundToInt(),
            pm25 = value("pm2_5"),
            pm10 = value("pm10"),
            ozone = value("ozone"),
            uvIndex = value("uv_index"),
            updatedAtMillis = System.currentTimeMillis()
        )
    }
}
