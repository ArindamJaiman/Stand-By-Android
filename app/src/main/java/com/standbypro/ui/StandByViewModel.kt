package com.standbypro.ui

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.standbypro.clock.ClockEngine
import com.standbypro.domain.BatteryHealth
import com.standbypro.domain.ChargeType
import com.standbypro.domain.ChargingState
import com.standbypro.hardware.AmbientLightMonitor
import com.standbypro.hardware.ChargingStateMonitor
import com.standbypro.power.AutoDimController
import com.standbypro.power.BurnInOffset
import com.standbypro.power.BurnInProtectionController
import com.standbypro.settings.ClockStyle
import com.standbypro.settings.SettingsRepository
import com.standbypro.settings.StandBySettings
import com.standbypro.settings.dataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Date
import java.util.Locale

class StandByViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application.dataStore)
    private val chargingMonitor = ChargingStateMonitor(application)
    private val ambientLightMonitor = AmbientLightMonitor(application)

    val settings: StateFlow<StandBySettings> = settingsRepository.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = StandBySettings()
        )

    val currentTime: StateFlow<LocalDateTime> = ClockEngine.timeFlow(1000L)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LocalDateTime.now()
        )

    val chargingState: StateFlow<ChargingState> = chargingMonitor.chargingState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ChargingState(
                isCharging = false,
                batteryPercent = 100,
                chargeType = ChargeType.NONE,
                temperatureCelsius = null,
                health = BatteryHealth.GOOD
            )
        )

    val isNightMode: StateFlow<Boolean> = ambientLightMonitor.isNightMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val isDimmed: StateFlow<Boolean> = AutoDimController.dimStateMonitor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    val burnInOffset: StateFlow<BurnInOffset> = BurnInProtectionController.burnInOffset
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BurnInOffset(0f, 0f)
        )

    fun reportInteraction() {
        AutoDimController.reportInteraction()
    }

    fun getNextAlarm(): String? {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val nextAlarm = alarmManager?.nextAlarmClock ?: return null
        val formatter = SimpleDateFormat("EEE h:mm a", Locale.getDefault())
        return formatter.format(Date(nextAlarm.triggerTime))
    }

    fun setEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setEnabled(enabled) }
    }

    fun setAutoStart(autoStart: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoStart(autoStart) }
    }

    fun setRequireLandscape(requireLandscape: Boolean) {
        viewModelScope.launch { settingsRepository.setRequireLandscape(requireLandscape) }
    }

    fun setRequireScreenLocked(requireScreenLocked: Boolean) {
        viewModelScope.launch { settingsRepository.setRequireScreenLocked(requireScreenLocked) }
    }

    fun setNightModeEnabled(nightModeEnabled: Boolean) {
        viewModelScope.launch { settingsRepository.setNightModeEnabled(nightModeEnabled) }
    }

    fun setBurnInProtectionEnabled(burnInProtectionEnabled: Boolean) {
        viewModelScope.launch { settingsRepository.setBurnInProtectionEnabled(burnInProtectionEnabled) }
    }

    fun setUse24Hour(use24Hour: Boolean) {
        viewModelScope.launch { settingsRepository.setUse24Hour(use24Hour) }
    }

    fun setShowSeconds(showSeconds: Boolean) {
        viewModelScope.launch { settingsRepository.setShowSeconds(showSeconds) }
    }

    fun setClockStyle(clockStyle: ClockStyle) {
        viewModelScope.launch { settingsRepository.setClockStyle(clockStyle) }
    }

    fun setBrightnessLevel(brightness: Float) {
        viewModelScope.launch { settingsRepository.setBrightnessLevel(brightness) }
    }
}
