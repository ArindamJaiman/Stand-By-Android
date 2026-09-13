package com.standbypro.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
        val NIGHT_MODE_ENABLED = booleanPreferencesKey("night_mode_enabled")
        val BURN_IN_PROTECTION_ENABLED = booleanPreferencesKey("burn_in_protection_enabled")
        val USE_24_HOUR = booleanPreferencesKey("use_24_hour")
        val SHOW_SECONDS = booleanPreferencesKey("show_seconds")
        val CLOCK_STYLE = stringPreferencesKey("clock_style")
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
            val clockStyleStr = preferences[PreferencesKeys.CLOCK_STYLE] ?: ClockStyle.DIGITAL.name
            val clockStyle = try {
                ClockStyle.valueOf(clockStyleStr)
            } catch (e: IllegalArgumentException) {
                ClockStyle.DIGITAL
            }

            StandBySettings(
                isEnabled = preferences[PreferencesKeys.IS_ENABLED] ?: true,
                autoStartWhileCharging = preferences[PreferencesKeys.AUTO_START] ?: true,
                requireLandscape = preferences[PreferencesKeys.REQUIRE_LANDSCAPE] ?: true,
                nightModeEnabled = preferences[PreferencesKeys.NIGHT_MODE_ENABLED] ?: true,
                burnInProtectionEnabled = preferences[PreferencesKeys.BURN_IN_PROTECTION_ENABLED] ?: true,
                use24Hour = preferences[PreferencesKeys.USE_24_HOUR] ?: false,
                showSeconds = preferences[PreferencesKeys.SHOW_SECONDS] ?: false,
                clockStyle = clockStyle
            )
        }

    suspend fun setEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_ENABLED] = enabled
        }
    }

    suspend fun setAutoStart(autoStart: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_START] = autoStart
        }
    }
    
    suspend fun setRequireLandscape(requireLandscape: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.REQUIRE_LANDSCAPE] = requireLandscape
        }
    }

    suspend fun setNightModeEnabled(nightModeEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NIGHT_MODE_ENABLED] = nightModeEnabled
        }
    }

    suspend fun setBurnInProtectionEnabled(burnInProtectionEnabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.BURN_IN_PROTECTION_ENABLED] = burnInProtectionEnabled
        }
    }

    suspend fun setUse24Hour(use24Hour: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_24_HOUR] = use24Hour
        }
    }

    suspend fun setShowSeconds(showSeconds: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_SECONDS] = showSeconds
        }
    }

    suspend fun setClockStyle(clockStyle: ClockStyle) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOCK_STYLE] = clockStyle.name
        }
    }
}
