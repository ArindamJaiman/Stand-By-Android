package com.standbypro.data

import android.content.Context
import android.util.Log
import com.standbypro.domain.DailyForecast
import com.standbypro.domain.HourlyForecast
import com.standbypro.domain.WeatherCondition
import com.standbypro.domain.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** City returned by the geocoding search in the Studio. */
data class CityResult(
    val name: String,
    val region: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
) {
    val label: String get() = listOf(name, region, country).filter { it.isNotBlank() }.distinct().joinToString(", ")
}

/**
 * Live weather from Open-Meteo (free, no API key). Results are cached so the display shows
 * the last known conditions instantly after a restart or while offline.
 */
object WeatherRepository {

    private const val PREFS = "standby_weather_cache"
    private const val KEY_JSON = "json"
    private const val KEY_CITY = "city"
    private const val KEY_LAT = "lat"
    private const val KEY_LON = "lon"
    private const val REFRESH_INTERVAL_MS = 30 * 60 * 1000L

    private val _weather = MutableStateFlow<WeatherData?>(null)
    val weather: StateFlow<WeatherData?> = _weather.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var lastFetchMillis = 0L
    private var lastKey = ""

    fun loadCache(context: Context) {
        if (_weather.value != null) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_JSON, null) ?: return
        val city = prefs.getString(KEY_CITY, "") ?: ""
        try {
            _weather.value = parse(json, city).copy(isLive = false)
            lastKey = "${prefs.getFloat(KEY_LAT, 0f)},${prefs.getFloat(KEY_LON, 0f)}"
        } catch (e: Exception) {
            Log.w("WeatherRepository", "Ignoring corrupt weather cache", e)
        }
    }

    /** Fetches fresh weather unless the cached data for this place is still recent. */
    suspend fun refresh(context: Context, city: String, latitude: Double, longitude: Double, force: Boolean = false) {
        val key = "${latitude.toFloat()},${longitude.toFloat()}"
        val now = System.currentTimeMillis()
        if (!force && key == lastKey && now - lastFetchMillis < REFRESH_INTERVAL_MS && _weather.value?.isLive == true) return
        _isRefreshing.value = true
        try {
            val json = withContext(Dispatchers.IO) { download(forecastUrl(latitude, longitude)) }
            if (json != null) {
                val parsed = parse(json, city)
                _weather.value = parsed
                lastFetchMillis = now
                lastKey = key
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                    .putString(KEY_JSON, json)
                    .putString(KEY_CITY, city)
                    .putFloat(KEY_LAT, latitude.toFloat())
                    .putFloat(KEY_LON, longitude.toFloat())
                    .apply()
            } else if (_weather.value?.cityName != city) {
                // Keep stale data only if it belongs to the same place.
                if (key != lastKey) _weather.value = null
            }
        } catch (e: Exception) {
            Log.w("WeatherRepository", "Weather refresh failed: ${e.message}")
        } finally {
            _isRefreshing.value = false
        }
    }

    suspend fun searchCities(query: String): List<CityResult> {
        val q = query.trim()
        if (q.length < 2) return emptyList()
        return withContext(Dispatchers.IO) {
            val url = "https://geocoding-api.open-meteo.com/v1/search?count=8&language=en&format=json&name=" +
                URLEncoder.encode(q, "UTF-8")
            val json = download(url) ?: return@withContext emptyList()
            try {
                val results = JSONObject(json).optJSONArray("results") ?: return@withContext emptyList()
                (0 until results.length()).map { i ->
                    val o = results.getJSONObject(i)
                    CityResult(
                        name = o.optString("name"),
                        region = o.optString("admin1"),
                        country = o.optString("country"),
                        latitude = o.optDouble("latitude"),
                        longitude = o.optDouble("longitude")
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun forecastUrl(lat: Double, lon: Double): String =
        "https://api.open-meteo.com/v1/forecast?latitude=${"%.4f".format(Locale.US, lat)}&longitude=${"%.4f".format(Locale.US, lon)}" +
            "&current=temperature_2m,apparent_temperature,relative_humidity_2m,weather_code,wind_speed_10m,is_day" +
            "&hourly=temperature_2m,weather_code,precipitation_probability,is_day" +
            "&daily=weather_code,temperature_2m_max,temperature_2m_min,sunrise,sunset,uv_index_max,precipitation_probability_max" +
            "&timezone=auto&forecast_days=7"

    internal fun download(url: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "StandByPro-Android")
            }
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                connection.inputStream.bufferedReader().use { it.readText() }
            } else null
        } catch (e: Exception) {
            null
        } finally {
            connection?.disconnect()
        }
    }

    internal fun parse(json: String, city: String): WeatherData {
        val root = JSONObject(json)
        val current = root.getJSONObject("current")
        val isDay = current.optInt("is_day", 1) == 1
        val code = current.optInt("weather_code", 2)
        val daily = root.optJSONObject("daily")
        val hourly = root.optJSONObject("hourly")

        val dailyList = mutableListOf<DailyForecast>()
        var sunrise = ""
        var sunset = ""
        var uv = 0
        var rainChance = 0
        if (daily != null) {
            val days = daily.getJSONArray("time")
            val codes = daily.getJSONArray("weather_code")
            val maxT = daily.getJSONArray("temperature_2m_max")
            val minT = daily.getJSONArray("temperature_2m_min")
            for (i in 0 until days.length()) {
                val date = LocalDate.parse(days.getString(i))
                val label = if (i == 0) "Today" else date.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault()))
                dailyList += DailyForecast(label, maxT.optDouble(i).roundToInt(), minT.optDouble(i).roundToInt(), WeatherCondition.fromWmo(codes.optInt(i), true))
            }
            val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
            sunrise = daily.optJSONArray("sunrise")?.optString(0)?.let { runCatching { LocalDateTime.parse(it).format(timeFmt) }.getOrNull() } ?: ""
            sunset = daily.optJSONArray("sunset")?.optString(0)?.let { runCatching { LocalDateTime.parse(it).format(timeFmt) }.getOrNull() } ?: ""
            uv = daily.optJSONArray("uv_index_max")?.optDouble(0)?.takeIf { !it.isNaN() }?.roundToInt() ?: 0
            rainChance = daily.optJSONArray("precipitation_probability_max")?.optInt(0) ?: 0
        }

        val hourlyList = mutableListOf<HourlyForecast>()
        if (hourly != null) {
            val times = hourly.getJSONArray("time")
            val temps = hourly.getJSONArray("temperature_2m")
            val codes = hourly.getJSONArray("weather_code")
            val rain = hourly.optJSONArray("precipitation_probability")
            val days = hourly.optJSONArray("is_day")
            val nowHour = LocalDateTime.now().withMinute(0).withSecond(0).withNano(0)
            val labelFmt = DateTimeFormatter.ofPattern("ha", Locale.getDefault())
            for (i in 0 until times.length()) {
                val t = runCatching { LocalDateTime.parse(times.getString(i)) }.getOrNull() ?: continue
                if (t.isBefore(nowHour)) continue
                if (hourlyList.size >= 24) break
                val label = if (hourlyList.isEmpty()) "Now" else t.format(labelFmt).lowercase(Locale.getDefault())
                hourlyList += HourlyForecast(
                    label,
                    temps.optDouble(i).roundToInt(),
                    WeatherCondition.fromWmo(codes.optInt(i), (days?.optInt(i, 1) ?: 1) == 1),
                    rain?.optInt(i, 0) ?: 0
                )
            }
        }

        val today = dailyList.firstOrNull()
        return WeatherData(
            cityName = city,
            temperatureCelsius = current.optDouble("temperature_2m").roundToInt(),
            condition = WeatherCondition.fromWmo(code, isDay),
            feelsLikeCelsius = current.optDouble("apparent_temperature").roundToInt(),
            highCelsius = today?.highCelsius ?: current.optDouble("temperature_2m").roundToInt(),
            lowCelsius = today?.lowCelsius ?: current.optDouble("temperature_2m").roundToInt(),
            humidityPercent = current.optInt("relative_humidity_2m"),
            windKmh = current.optDouble("wind_speed_10m").roundToInt(),
            uvIndex = uv,
            sunriseTime = sunrise,
            sunsetTime = sunset,
            hourlyForecast = hourlyList,
            dailyForecast = dailyList,
            isDay = isDay,
            precipitationChance = rainChance,
            updatedAtMillis = System.currentTimeMillis(),
            isLive = true
        )
    }
}
