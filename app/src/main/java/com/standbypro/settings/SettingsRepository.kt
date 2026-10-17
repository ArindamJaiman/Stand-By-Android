package com.standbypro.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.standbypro.domain.BottomComplicationType
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.NotificationPrivacy
import com.standbypro.domain.WatchFaceType
import com.standbypro.domain.WidgetSize
import com.standbypro.domain.WidgetSlot
import com.standbypro.ui.design.SecondsMotion
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "standby_settings")

private const val LIST_SEPARATOR = "\u001F"

private inline fun <reified T : Enum<T>> enumOr(value: String?, fallback: T): T =
    value?.let { v -> enumValues<T>().firstOrNull { it.name == v } } ?: fallback

private fun String?.toList(fallback: List<String>): List<String> =
    if (this == null) fallback else if (isEmpty()) emptyList() else split(LIST_SEPARATOR)

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object Keys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val AUTO_START = booleanPreferencesKey("auto_start")
        val REQUIRE_LANDSCAPE = booleanPreferencesKey("require_landscape")
        val REQUIRE_SCREEN_LOCKED = booleanPreferencesKey("require_screen_locked")
        val NIGHT_MODE_ENABLED = booleanPreferencesKey("night_mode_enabled_v2")
        val NIGHT_TRIGGER = stringPreferencesKey("night_trigger")
        val NIGHT_START = intPreferencesKey("night_start_minutes")
        val NIGHT_END = intPreferencesKey("night_end_minutes")
        val NIGHT_MODE_STYLE = stringPreferencesKey("night_mode_style")
        val BURN_IN_PROTECTION_ENABLED = booleanPreferencesKey("burn_in_protection_enabled")
        val AUTO_DIM_ENABLED = booleanPreferencesKey("auto_dim_enabled")
        val AUTO_DIM_TIMEOUT = intPreferencesKey("auto_dim_timeout_seconds")
        val BRIGHTNESS_LEVEL = floatPreferencesKey("brightness_level")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds_v2")
        val SECONDS_MOTION = stringPreferencesKey("seconds_motion")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val COLOR_THEME_ID = stringPreferencesKey("color_theme_id")
        val WATCH_FACE_TYPE = stringPreferencesKey("watch_face_type")
        val BOTTOM_COMPLICATION = stringPreferencesKey("bottom_complication")
        val GITHUB_USERNAME = stringPreferencesKey("github_username")
        val ACTIVE_WATCH_FACE_ID = stringPreferencesKey("active_watch_face_id")
        val LAYOUT_TYPE = stringPreferencesKey("layout_type")
        val ASSIGNED_WIDGET_1 = stringPreferencesKey("assigned_widget_1")
        val ASSIGNED_WIDGET_2 = stringPreferencesKey("assigned_widget_2")
        val ASSIGNED_WIDGET_3 = stringPreferencesKey("assigned_widget_3")
        val LEFT_STACK = stringPreferencesKey("left_stack")
        val RIGHT_STACK = stringPreferencesKey("right_stack")
        val SMART_ROTATE = booleanPreferencesKey("smart_rotate")
        val START_PAGE = stringPreferencesKey("start_page")
        val ACTIVE_PROFILE = stringPreferencesKey("active_profile")
        val FAVORITE_FACES = stringSetPreferencesKey("favorite_faces_v2")
        val RECENT_FACES = stringPreferencesKey("recent_faces")
        val USE_FAHRENHEIT = booleanPreferencesKey("use_fahrenheit")
        val WEATHER_CITY = stringPreferencesKey("weather_city")
        val WEATHER_LAT = stringPreferencesKey("weather_lat")
        val WEATHER_LON = stringPreferencesKey("weather_lon")
        val WORLD_CITIES = stringPreferencesKey("world_cities")
        val SECONDARY_CITY = stringPreferencesKey("secondary_city")
        val PHOTO_URIS = stringPreferencesKey("photo_uris")
        val PHOTO_INTERVAL = intPreferencesKey("photo_interval_seconds")
        val COUNTDOWN_LABEL = stringPreferencesKey("countdown_label")
        val COUNTDOWN_DAY = longPreferencesKey("countdown_epoch_day")
        val NOTIFICATION_PRIVACY = stringPreferencesKey("notification_privacy")
        val CHARGING_ANIMATION = booleanPreferencesKey("charging_animation")
        val HAPTICS = booleanPreferencesKey("haptics")
        val FACE_SHUFFLE = intPreferencesKey("face_shuffle_minutes")
        val HOURLY_CHIME = booleanPreferencesKey("hourly_chime")
        val NIGHT_BLACKOUT = booleanPreferencesKey("night_blackout")
        val SUNRISE_MINUTES = intPreferencesKey("sunrise_minutes")
        val AUTO_DND = booleanPreferencesKey("auto_dnd")
        val CHARGE_LIMIT = intPreferencesKey("charge_limit_percent")
        val DEMO_MODE_ENABLED = booleanPreferencesKey("demo_mode_enabled")
        val SIMULATE_CHARGING = booleanPreferencesKey("simulate_charging")
        val SIMULATE_NIGHT = booleanPreferencesKey("simulate_night")
    }

    private val defaults = StandBySettings()

    val settingsFlow: Flow<StandBySettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { p -> read(p) }
        .distinctUntilChanged()

    private fun read(p: Preferences): StandBySettings {
        val d = defaults
        return StandBySettings(
            isEnabled = p[Keys.IS_ENABLED] ?: d.isEnabled,
            autoStartWhileCharging = p[Keys.AUTO_START] ?: d.autoStartWhileCharging,
            requireLandscape = p[Keys.REQUIRE_LANDSCAPE] ?: d.requireLandscape,
            requireScreenLocked = p[Keys.REQUIRE_SCREEN_LOCKED] ?: d.requireScreenLocked,
            nightModeEnabled = p[Keys.NIGHT_MODE_ENABLED] ?: d.nightModeEnabled,
            nightTrigger = enumOr(p[Keys.NIGHT_TRIGGER], d.nightTrigger),
            nightStartMinutes = p[Keys.NIGHT_START] ?: d.nightStartMinutes,
            nightEndMinutes = p[Keys.NIGHT_END] ?: d.nightEndMinutes,
            nightModeStyle = p[Keys.NIGHT_MODE_STYLE] ?: d.nightModeStyle,
            burnInProtectionEnabled = p[Keys.BURN_IN_PROTECTION_ENABLED] ?: d.burnInProtectionEnabled,
            autoDimEnabled = p[Keys.AUTO_DIM_ENABLED] ?: d.autoDimEnabled,
            autoDimTimeoutSeconds = p[Keys.AUTO_DIM_TIMEOUT] ?: d.autoDimTimeoutSeconds,
            brightnessLevel = p[Keys.BRIGHTNESS_LEVEL] ?: d.brightnessLevel,
            use24Hour = p[Keys.USE_24_HOUR] ?: d.use24Hour,
            showSeconds = p[Keys.SHOW_SECONDS] ?: d.showSeconds,
            secondsMotion = enumOr(p[Keys.SECONDS_MOTION], d.secondsMotion),
            clockStyle = enumOr(p[Keys.CLOCK_STYLE], d.clockStyle),
            colorThemeId = p[Keys.COLOR_THEME_ID] ?: d.colorThemeId,
            watchFaceType = enumOr(p[Keys.WATCH_FACE_TYPE], d.watchFaceType),
            bottomComplication = enumOr(p[Keys.BOTTOM_COMPLICATION], d.bottomComplication),
            githubUsername = p[Keys.GITHUB_USERNAME] ?: d.githubUsername,
            activeWatchFaceId = p[Keys.ACTIVE_WATCH_FACE_ID] ?: d.activeWatchFaceId,
            layoutType = enumOr(p[Keys.LAYOUT_TYPE], d.layoutType),
            assignedWidgets = listOf(
                WidgetSlot("slot_1", p[Keys.ASSIGNED_WIDGET_1] ?: d.assignedWidgets[0].widgetId, WidgetSize.MEDIUM),
                WidgetSlot("slot_2", p[Keys.ASSIGNED_WIDGET_2] ?: d.assignedWidgets[1].widgetId, WidgetSize.MEDIUM),
                WidgetSlot("slot_3", p[Keys.ASSIGNED_WIDGET_3] ?: d.assignedWidgets[2].widgetId, WidgetSize.MEDIUM)
            ),
            leftStack = p[Keys.LEFT_STACK].toList(d.leftStack),
            rightStack = p[Keys.RIGHT_STACK].toList(d.rightStack),
            smartRotate = p[Keys.SMART_ROTATE] ?: d.smartRotate,
            startPage = enumOr(p[Keys.START_PAGE], d.startPage),
            activeProfile = enumOr(p[Keys.ACTIVE_PROFILE], d.activeProfile),
            favoriteFaceIds = p[Keys.FAVORITE_FACES] ?: d.favoriteFaceIds,
            recentFaceIds = p[Keys.RECENT_FACES].toList(d.recentFaceIds),
            useFahrenheit = p[Keys.USE_FAHRENHEIT] ?: d.useFahrenheit,
            weatherCity = p[Keys.WEATHER_CITY] ?: d.weatherCity,
            weatherLatitude = p[Keys.WEATHER_LAT]?.toDoubleOrNull(),
            weatherLongitude = p[Keys.WEATHER_LON]?.toDoubleOrNull(),
            worldCities = p[Keys.WORLD_CITIES].toList(d.worldCities),
            secondaryCity = p[Keys.SECONDARY_CITY] ?: d.secondaryCity,
            photoUris = p[Keys.PHOTO_URIS].toList(d.photoUris),
            photoIntervalSeconds = p[Keys.PHOTO_INTERVAL] ?: d.photoIntervalSeconds,
            countdownLabel = p[Keys.COUNTDOWN_LABEL] ?: d.countdownLabel,
            countdownEpochDay = p[Keys.COUNTDOWN_DAY],
            notificationPrivacy = enumOr(p[Keys.NOTIFICATION_PRIVACY], d.notificationPrivacy),
            showChargingAnimation = p[Keys.CHARGING_ANIMATION] ?: d.showChargingAnimation,
            hapticsEnabled = p[Keys.HAPTICS] ?: d.hapticsEnabled,
            faceShuffleMinutes = p[Keys.FACE_SHUFFLE] ?: d.faceShuffleMinutes,
            hourlyChime = p[Keys.HOURLY_CHIME] ?: d.hourlyChime,
            nightBlackout = p[Keys.NIGHT_BLACKOUT] ?: d.nightBlackout,
            sunriseMinutes = p[Keys.SUNRISE_MINUTES] ?: d.sunriseMinutes,
            autoDnd = p[Keys.AUTO_DND] ?: d.autoDnd,
            chargeLimitPercent = p[Keys.CHARGE_LIMIT] ?: d.chargeLimitPercent,
            demoModeEnabled = p[Keys.DEMO_MODE_ENABLED] ?: d.demoModeEnabled,
            simulateCharging = p[Keys.SIMULATE_CHARGING] ?: d.simulateCharging,
            simulateNight = p[Keys.SIMULATE_NIGHT] ?: d.simulateNight
        )
    }

    private suspend fun edit(block: (MutablePreferences) -> Unit) {
        dataStore.edit { block(it) }
    }

    suspend fun setEnabled(v: Boolean) = edit { it[Keys.IS_ENABLED] = v }
    suspend fun setAutoStart(v: Boolean) = edit { it[Keys.AUTO_START] = v }
    suspend fun setRequireLandscape(v: Boolean) = edit { it[Keys.REQUIRE_LANDSCAPE] = v }
    suspend fun setRequireScreenLocked(v: Boolean) = edit { it[Keys.REQUIRE_SCREEN_LOCKED] = v }
    suspend fun setNightModeEnabled(v: Boolean) = edit { it[Keys.NIGHT_MODE_ENABLED] = v }
    suspend fun setNightTrigger(v: NightTrigger) = edit { it[Keys.NIGHT_TRIGGER] = v.name }
    suspend fun setNightSchedule(startMinutes: Int, endMinutes: Int) = edit {
        it[Keys.NIGHT_START] = startMinutes.mod(1440)
        it[Keys.NIGHT_END] = endMinutes.mod(1440)
    }
    suspend fun setNightModeStyle(v: String) = edit { it[Keys.NIGHT_MODE_STYLE] = v }
    suspend fun setBurnInProtectionEnabled(v: Boolean) = edit { it[Keys.BURN_IN_PROTECTION_ENABLED] = v }
    suspend fun setAutoDimEnabled(v: Boolean) = edit { it[Keys.AUTO_DIM_ENABLED] = v }
    suspend fun setAutoDimTimeout(seconds: Int) = edit { it[Keys.AUTO_DIM_TIMEOUT] = seconds }
    suspend fun setBrightnessLevel(v: Float) = edit { it[Keys.BRIGHTNESS_LEVEL] = v.coerceIn(0.01f, 1.0f) }
    suspend fun setUse24Hour(v: Boolean) = edit { it[Keys.USE_24_HOUR] = v }
    suspend fun setShowSeconds(v: Boolean) = edit { it[Keys.SHOW_SECONDS] = v }
    suspend fun setSecondsMotion(v: SecondsMotion) = edit { it[Keys.SECONDS_MOTION] = v.name }
    suspend fun setClockStyle(v: ClockStyle) = edit { it[Keys.CLOCK_STYLE] = v.name }
    suspend fun setColorTheme(id: String) = edit { it[Keys.COLOR_THEME_ID] = id }
    suspend fun setWatchFaceType(v: WatchFaceType) = edit { it[Keys.WATCH_FACE_TYPE] = v.name }
    suspend fun setBottomComplication(v: BottomComplicationType) = edit { it[Keys.BOTTOM_COMPLICATION] = v.name }
    suspend fun setGithubUsername(v: String) = edit { it[Keys.GITHUB_USERNAME] = v.trim().removePrefix("@") }

    suspend fun setActiveWatchFaceId(faceId: String) = edit { p ->
        p[Keys.ACTIVE_WATCH_FACE_ID] = faceId
        val recents = (listOf(faceId) + p[Keys.RECENT_FACES].toList(emptyList()).filter { it != faceId }).take(12)
        p[Keys.RECENT_FACES] = recents.joinToString(LIST_SEPARATOR)
    }

    suspend fun setLayoutType(v: DashboardLayoutType) = edit { it[Keys.LAYOUT_TYPE] = v.name }

    suspend fun setAssignedWidget(slotIndex: Int, widgetId: String) = edit { p ->
        when (slotIndex) {
            0 -> p[Keys.ASSIGNED_WIDGET_1] = widgetId
            1 -> p[Keys.ASSIGNED_WIDGET_2] = widgetId
            2 -> p[Keys.ASSIGNED_WIDGET_3] = widgetId
        }
    }

    suspend fun setStack(left: Boolean, ids: List<String>) = edit {
        it[if (left) Keys.LEFT_STACK else Keys.RIGHT_STACK] = ids.joinToString(LIST_SEPARATOR)
    }

    suspend fun setSmartRotate(v: Boolean) = edit { it[Keys.SMART_ROTATE] = v }
    suspend fun setStartPage(v: StandByPage) = edit { it[Keys.START_PAGE] = v.name }
    suspend fun setProfile(v: StandByProfile) = edit { it[Keys.ACTIVE_PROFILE] = v.name }

    suspend fun toggleFavoriteFace(faceId: String) = edit { p ->
        val current = (p[Keys.FAVORITE_FACES] ?: defaults.favoriteFaceIds).toMutableSet()
        if (!current.add(faceId)) current.remove(faceId)
        p[Keys.FAVORITE_FACES] = current
    }

    suspend fun setUseFahrenheit(v: Boolean) = edit { it[Keys.USE_FAHRENHEIT] = v }

    suspend fun setWeatherLocation(city: String, latitude: Double?, longitude: Double?) = edit { p ->
        p[Keys.WEATHER_CITY] = city
        if (latitude != null && longitude != null) {
            p[Keys.WEATHER_LAT] = latitude.toString()
            p[Keys.WEATHER_LON] = longitude.toString()
        } else {
            p.remove(Keys.WEATHER_LAT)
            p.remove(Keys.WEATHER_LON)
        }
    }

    suspend fun setWorldCities(cities: List<String>) = edit { it[Keys.WORLD_CITIES] = cities.joinToString(LIST_SEPARATOR) }
    suspend fun setSecondaryCity(city: String) = edit { it[Keys.SECONDARY_CITY] = city }
    suspend fun setPhotoUris(uris: List<String>) = edit { it[Keys.PHOTO_URIS] = uris.joinToString(LIST_SEPARATOR) }
    suspend fun setPhotoInterval(seconds: Int) = edit { it[Keys.PHOTO_INTERVAL] = seconds }

    suspend fun setCountdown(label: String, epochDay: Long?) = edit { p ->
        p[Keys.COUNTDOWN_LABEL] = label
        if (epochDay != null) p[Keys.COUNTDOWN_DAY] = epochDay else p.remove(Keys.COUNTDOWN_DAY)
    }

    suspend fun setNotificationPrivacy(v: NotificationPrivacy) = edit { it[Keys.NOTIFICATION_PRIVACY] = v.name }
    suspend fun setShowChargingAnimation(v: Boolean) = edit { it[Keys.CHARGING_ANIMATION] = v }
    suspend fun setHaptics(v: Boolean) = edit { it[Keys.HAPTICS] = v }
    suspend fun setFaceShuffle(minutes: Int) = edit { it[Keys.FACE_SHUFFLE] = minutes }
    suspend fun setHourlyChime(v: Boolean) = edit { it[Keys.HOURLY_CHIME] = v }
    suspend fun setNightBlackout(v: Boolean) = edit { it[Keys.NIGHT_BLACKOUT] = v }
    suspend fun setSunriseMinutes(v: Int) = edit { it[Keys.SUNRISE_MINUTES] = v }
    suspend fun setAutoDnd(v: Boolean) = edit { it[Keys.AUTO_DND] = v }
    suspend fun setChargeLimit(v: Int) = edit { it[Keys.CHARGE_LIMIT] = v }
    suspend fun setDemoModeEnabled(v: Boolean) = edit { it[Keys.DEMO_MODE_ENABLED] = v }
    suspend fun setSimulateCharging(v: Boolean) = edit { it[Keys.SIMULATE_CHARGING] = v }
    suspend fun setSimulateNight(v: Boolean) = edit { it[Keys.SIMULATE_NIGHT] = v }
}
