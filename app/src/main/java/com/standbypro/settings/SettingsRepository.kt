package com.standbypro.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.standbypro.domain.BottomComplicationType
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.WatchFaceType
import com.standbypro.domain.WidgetSize
import com.standbypro.domain.WidgetSlot
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "standby_settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    private object PreferencesKeys {
        val IS_ENABLED = booleanPreferencesKey("is_enabled")
        val AUTO_START = booleanPreferencesKey("auto_start")
        val REQUIRE_LANDSCAPE = booleanPreferencesKey("require_landscape")
        val REQUIRE_SCREEN_LOCKED = booleanPreferencesKey("require_screen_locked")
        val NIGHT_MODE_ENABLED = booleanPreferencesKey("night_mode_enabled")
        val NIGHT_MODE_STYLE = stringPreferencesKey("night_mode_style")
        val BURN_IN_PROTECTION_ENABLED = booleanPreferencesKey("burn_in_protection_enabled")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
        val BRIGHTNESS_LEVEL = floatPreferencesKey("brightness_level")
        val COLOR_THEME_ID = stringPreferencesKey("color_theme_id")
        val AUTO_DIM_ENABLED = booleanPreferencesKey("auto_dim_enabled")
        val WATCH_FACE_TYPE = stringPreferencesKey("watch_face_type")
        val BOTTOM_COMPLICATION = stringPreferencesKey("bottom_complication")
        val GITHUB_USERNAME = stringPreferencesKey("github_username")

        // Extended Architecture Keys
        val ACTIVE_WATCH_FACE_ID = stringPreferencesKey("active_watch_face_id")
        val LAYOUT_TYPE = stringPreferencesKey("layout_type")
        val ACTIVE_PROFILE = stringPreferencesKey("active_profile")
        val FAVORITE_FACES = stringSetPreferencesKey("favorite_faces")
        val DEMO_MODE_ENABLED = booleanPreferencesKey("demo_mode_enabled")
        val SIMULATE_CHARGING = booleanPreferencesKey("simulate_charging")
        val SIMULATE_NIGHT = booleanPreferencesKey("simulate_night")
        val ASSIGNED_WIDGET_1 = stringPreferencesKey("assigned_widget_1")
        val ASSIGNED_WIDGET_2 = stringPreferencesKey("assigned_widget_2")
        val ASSIGNED_WIDGET_3 = stringPreferencesKey("assigned_widget_3")
    }

    val settingsFlow: Flow<StandBySettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val clockStyleStr = preferences[PreferencesKeys.CLOCK_STYLE] ?: ClockStyle.ANALOG.name
            val clockStyle = try {
                ClockStyle.valueOf(clockStyleStr)
            } catch (e: IllegalArgumentException) {
                ClockStyle.ANALOG
            }

            val watchFaceTypeStr = preferences[PreferencesKeys.WATCH_FACE_TYPE] ?: WatchFaceType.GMT_CALENDAR.name
            val watchFaceType = try {
                WatchFaceType.valueOf(watchFaceTypeStr)
            } catch (e: IllegalArgumentException) {
                WatchFaceType.GMT_CALENDAR
            }

            val bottomComplicationStr = preferences[PreferencesKeys.BOTTOM_COMPLICATION] ?: BottomComplicationType.BATTERY.name
            val bottomComplication = try {
                BottomComplicationType.valueOf(bottomComplicationStr)
            } catch (e: IllegalArgumentException) {
                BottomComplicationType.BATTERY
            }

            val layoutTypeStr = preferences[PreferencesKeys.LAYOUT_TYPE] ?: DashboardLayoutType.DUO.name
            val layoutType = try {
                DashboardLayoutType.valueOf(layoutTypeStr)
            } catch (e: Exception) {
                DashboardLayoutType.DUO
            }

            val profileStr = preferences[PreferencesKeys.ACTIVE_PROFILE] ?: StandByProfile.HOME.name
            val activeProfile = try {
                StandByProfile.valueOf(profileStr)
            } catch (e: Exception) {
                StandByProfile.HOME
            }

            val activeWatchFaceId = preferences[PreferencesKeys.ACTIVE_WATCH_FACE_ID]
                ?: if (watchFaceType == WatchFaceType.DIGITAL_CALENDAR) "digital_minimal" else "gmt_explorer"

            val w1 = preferences[PreferencesKeys.ASSIGNED_WIDGET_1] ?: "widget_calendar"
            val w2 = preferences[PreferencesKeys.ASSIGNED_WIDGET_2] ?: "widget_weather"
            val w3 = preferences[PreferencesKeys.ASSIGNED_WIDGET_3] ?: "widget_battery"

            val assignedWidgets = listOf(
                WidgetSlot("slot_1", w1, WidgetSize.MEDIUM),
                WidgetSlot("slot_2", w2, WidgetSize.MEDIUM),
                WidgetSlot("slot_3", w3, WidgetSize.MEDIUM)
            )

            val githubUsername = preferences[PreferencesKeys.GITHUB_USERNAME] ?: "ArindamJaiman"
            val favorites = preferences[PreferencesKeys.FAVORITE_FACES] ?: setOf("gmt_explorer", "digital_minimal", "digital_oled", "productivity_pomodoro", "flip_amber")

            StandBySettings(
                isEnabled = preferences[PreferencesKeys.IS_ENABLED] ?: true,
                autoStartWhileCharging = preferences[PreferencesKeys.AUTO_START] ?: true,
                requireLandscape = preferences[PreferencesKeys.REQUIRE_LANDSCAPE] ?: true,
                requireScreenLocked = preferences[PreferencesKeys.REQUIRE_SCREEN_LOCKED] ?: true,
                nightModeEnabled = preferences[PreferencesKeys.NIGHT_MODE_ENABLED] ?: false,
                nightModeStyle = preferences[PreferencesKeys.NIGHT_MODE_STYLE] ?: "RED",
                burnInProtectionEnabled = preferences[PreferencesKeys.BURN_IN_PROTECTION_ENABLED] ?: true,
                autoDimEnabled = preferences[PreferencesKeys.AUTO_DIM_ENABLED] ?: true,
                use24Hour = preferences[PreferencesKeys.USE_24_HOUR] ?: false,
                showSeconds = preferences[PreferencesKeys.SHOW_SECONDS] ?: false,
                clockStyle = clockStyle,
                brightnessLevel = preferences[PreferencesKeys.BRIGHTNESS_LEVEL] ?: 0.05f,
                colorThemeId = preferences[PreferencesKeys.COLOR_THEME_ID] ?: StandByColorTheme.ORANGE.id,
                watchFaceType = watchFaceType,
                bottomComplication = bottomComplication,
                githubUsername = githubUsername,
                activeWatchFaceId = activeWatchFaceId,
                layoutType = layoutType,
                assignedWidgets = assignedWidgets,
                activeProfile = activeProfile,
                favoriteFaceIds = favorites,
                demoModeEnabled = preferences[PreferencesKeys.DEMO_MODE_ENABLED] ?: false,
                simulateCharging = preferences[PreferencesKeys.SIMULATE_CHARGING] ?: true,
                simulateNight = preferences[PreferencesKeys.SIMULATE_NIGHT] ?: false
            )
        }

    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.IS_ENABLED] = enabled }
    }

    suspend fun setAutoStart(autoStart: Boolean) {
        dataStore.edit { it[PreferencesKeys.AUTO_START] = autoStart }
    }

    suspend fun setRequireLandscape(requireLandscape: Boolean) {
        dataStore.edit { it[PreferencesKeys.REQUIRE_LANDSCAPE] = requireLandscape }
    }

    suspend fun setRequireScreenLocked(requireScreenLocked: Boolean) {
        dataStore.edit { it[PreferencesKeys.REQUIRE_SCREEN_LOCKED] = requireScreenLocked }
    }

    suspend fun setNightModeEnabled(nightModeEnabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.NIGHT_MODE_ENABLED] = nightModeEnabled }
    }

    suspend fun setNightModeStyle(style: String) {
        dataStore.edit { it[PreferencesKeys.NIGHT_MODE_STYLE] = style }
    }

    suspend fun setBurnInProtectionEnabled(burnInProtectionEnabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.BURN_IN_PROTECTION_ENABLED] = burnInProtectionEnabled }
    }

    suspend fun setUse24Hour(use24Hour: Boolean) {
        dataStore.edit { it[PreferencesKeys.USE_24_HOUR] = use24Hour }
    }

    suspend fun setShowSeconds(showSeconds: Boolean) {
        dataStore.edit { it[PreferencesKeys.SHOW_SECONDS] = showSeconds }
    }

    suspend fun setClockStyle(clockStyle: ClockStyle) {
        dataStore.edit { it[PreferencesKeys.CLOCK_STYLE] = clockStyle.name }
    }

    suspend fun setBrightnessLevel(brightness: Float) {
        dataStore.edit { it[PreferencesKeys.BRIGHTNESS_LEVEL] = brightness.coerceIn(0.01f, 1.0f) }
    }

    suspend fun setColorTheme(colorThemeId: String) {
        dataStore.edit { it[PreferencesKeys.COLOR_THEME_ID] = colorThemeId }
    }

    suspend fun setAutoDimEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.AUTO_DIM_ENABLED] = enabled }
    }

    suspend fun setWatchFaceType(watchFaceType: WatchFaceType) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.WATCH_FACE_TYPE] = watchFaceType.name
            if (watchFaceType == WatchFaceType.DIGITAL_CALENDAR) {
                preferences[PreferencesKeys.CLOCK_STYLE] = ClockStyle.DIGITAL.name
                preferences[PreferencesKeys.ACTIVE_WATCH_FACE_ID] = "digital_minimal"
            } else {
                preferences[PreferencesKeys.CLOCK_STYLE] = ClockStyle.ANALOG.name
                preferences[PreferencesKeys.ACTIVE_WATCH_FACE_ID] = "gmt_explorer"
            }
        }
    }

    suspend fun setActiveWatchFaceId(faceId: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_WATCH_FACE_ID] = faceId
            if (faceId.startsWith("digital") || faceId.startsWith("flip")) {
                preferences[PreferencesKeys.CLOCK_STYLE] = ClockStyle.DIGITAL.name
            } else {
                preferences[PreferencesKeys.CLOCK_STYLE] = ClockStyle.ANALOG.name
            }
        }
    }

    suspend fun setLayoutType(layoutType: DashboardLayoutType) {
        dataStore.edit { it[PreferencesKeys.LAYOUT_TYPE] = layoutType.name }
    }

    suspend fun setAssignedWidget(slotIndex: Int, widgetId: String) {
        dataStore.edit { preferences ->
            when (slotIndex) {
                0 -> preferences[PreferencesKeys.ASSIGNED_WIDGET_1] = widgetId
                1 -> preferences[PreferencesKeys.ASSIGNED_WIDGET_2] = widgetId
                2 -> preferences[PreferencesKeys.ASSIGNED_WIDGET_3] = widgetId
            }
        }
    }

    suspend fun setProfile(profile: StandByProfile) {
        dataStore.edit { it[PreferencesKeys.ACTIVE_PROFILE] = profile.name }
    }

    suspend fun toggleFavoriteFace(faceId: String) {
        dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.FAVORITE_FACES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(faceId)) {
                current.remove(faceId)
            } else {
                current.add(faceId)
            }
            preferences[PreferencesKeys.FAVORITE_FACES] = current
        }
    }

    suspend fun setBottomComplication(bottomComplication: BottomComplicationType) {
        dataStore.edit { it[PreferencesKeys.BOTTOM_COMPLICATION] = bottomComplication.name }
    }

    suspend fun setGithubUsername(username: String) {
        dataStore.edit { it[PreferencesKeys.GITHUB_USERNAME] = username.trim().removePrefix("@") }
    }

    suspend fun setDemoModeEnabled(enabled: Boolean) {
        dataStore.edit { it[PreferencesKeys.DEMO_MODE_ENABLED] = enabled }
    }

    suspend fun setSimulateCharging(simulate: Boolean) {
        dataStore.edit { it[PreferencesKeys.SIMULATE_CHARGING] = simulate }
    }

    suspend fun setSimulateNight(simulate: Boolean) {
        dataStore.edit { it[PreferencesKeys.SIMULATE_NIGHT] = simulate }
    }
}
