package com.standbypro.ui

import android.app.AlarmManager
import android.app.Application
import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.standbypro.clock.TimeProvider
import com.standbypro.data.DemoDataProvider
import com.standbypro.data.GitHubContributionsState
import com.standbypro.data.GitHubRepository
import com.standbypro.domain.BatteryHealth
import com.standbypro.domain.BottomComplicationType
import com.standbypro.domain.ChargeType
import com.standbypro.domain.ChargingState
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.LiveActivityState
import com.standbypro.domain.NotificationDisplayState
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WatchFaceType
import com.standbypro.domain.WidgetRegistry
import com.standbypro.hardware.AmbientLightMonitor
import com.standbypro.hardware.ChargingStateMonitor
import com.standbypro.notifications.NotificationRepository
import com.standbypro.power.AutoDimController
import com.standbypro.power.BurnInOffset
import com.standbypro.power.BurnInProtectionController
import com.standbypro.settings.ClockStyle
import com.standbypro.settings.SettingsRepository
import com.standbypro.settings.StandByProfile
import com.standbypro.settings.StandBySettings
import com.standbypro.settings.dataStore
import com.standbypro.ui.compositor.AmbientDisplayState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
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

    val gitHubContributionsState: StateFlow<GitHubContributionsState> = GitHubRepository.contributionsState

    val currentTime: StateFlow<LocalDateTime> = TimeProvider.timeFlow(1000L)
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

    val notifications: StateFlow<NotificationDisplayState> = NotificationRepository.notifications

    // Unified AmbientDisplayState combining settings, hardware monitors, and layout state
    val ambientDisplayState: StateFlow<AmbientDisplayState> = combine(
        settings,
        chargingState,
        isNightMode,
        isDimmed,
        burnInOffset
    ) { set, chg, night, dim, burnIn ->
        val effectiveCharging = if (set.demoModeEnabled) set.simulateCharging else chg.isCharging
        val effectiveNight = if (set.demoModeEnabled) set.simulateNight else (set.nightModeEnabled && night)
        val offset = if (set.burnInProtectionEnabled) Offset(burnIn.x, burnIn.y) else Offset.Zero

        AmbientDisplayState(
            isCharging = effectiveCharging,
            isLandscape = true,
            isNight = effectiveNight,
            ambientLux = if (effectiveNight) 4f else 45f,
            brightness = set.brightnessLevel,
            selectedProfile = set.activeProfile.name,
            watchFaceId = set.activeWatchFaceId,
            layout = set.layoutType,
            widgets = set.assignedWidgets,
            liveActivity = if (set.demoModeEnabled) DemoDataProvider.demoLiveActivities.firstOrNull() else null,
            notificationState = NotificationRepository.notifications.value,
            burnInOffset = offset,
            activeThemeColor = set.activeColorTheme.color,
            nightModeStyle = set.nightModeStyle,
            isDimmed = set.autoDimEnabled && dim,
            showControls = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AmbientDisplayState()
    )

    init {
        // Automatically fetch GitHub contributions when username setting changes
        viewModelScope.launch {
            settingsRepository.settingsFlow
                .map { it.githubUsername }
                .distinctUntilChanged()
                .collect { username ->
                    GitHubRepository.fetchContributions(username)
                }
        }
    }

    fun reportInteraction() {
        AutoDimController.reportInteraction()
    }

    fun getNextAlarm(): String? {
        val alarmManager = getApplication<Application>().getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val nextAlarm = alarmManager?.nextAlarmClock ?: return null
        val formatter = SimpleDateFormat("EEE h:mm a", Locale.getDefault())
        return formatter.format(Date(nextAlarm.triggerTime))
    }

    fun fetchGitHubContributions(username: String, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            GitHubRepository.fetchContributions(username, forceRefresh = forceRefresh)
        }
    }

    fun setActiveWatchFaceId(faceId: String) {
        viewModelScope.launch {
            settingsRepository.setActiveWatchFaceId(faceId)
        }
    }

    fun cycleWatchFace() {
        viewModelScope.launch {
            val allFaces = WatchFaceRegistry.getAll()
            val currentId = settings.value.activeWatchFaceId
            val currentIndex = allFaces.indexOfFirst { it.id == currentId }
            val nextIndex = (currentIndex + 1) % allFaces.size
            settingsRepository.setActiveWatchFaceId(allFaces[nextIndex].id)
        }
    }

    fun cycleRightWidget() {
        viewModelScope.launch {
            val allWidgets = WidgetRegistry.getAll()
            val currentWidgetId = settings.value.assignedWidgets.firstOrNull()?.widgetId ?: "widget_calendar"
            val currentIndex = allWidgets.indexOfFirst { it.id == currentWidgetId }
            val nextIndex = (currentIndex + 1) % allWidgets.size
            settingsRepository.setAssignedWidget(0, allWidgets[nextIndex].id)
        }
    }

    fun setLayoutType(layout: DashboardLayoutType) {
        viewModelScope.launch {
            settingsRepository.setLayoutType(layout)
        }
    }

    fun setAssignedWidget(slotIndex: Int, widgetId: String) {
        viewModelScope.launch {
            settingsRepository.setAssignedWidget(slotIndex, widgetId)
        }
    }

    fun setProfile(profile: StandByProfile) {
        viewModelScope.launch {
            settingsRepository.setProfile(profile)
        }
    }

    fun toggleFavoriteFace(faceId: String) {
        viewModelScope.launch {
            settingsRepository.toggleFavoriteFace(faceId)
        }
    }

    fun setNightModeStyle(style: String) {
        viewModelScope.launch {
            settingsRepository.setNightModeStyle(style)
        }
    }

    fun setDemoModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDemoModeEnabled(enabled)
        }
    }

    fun setSimulateCharging(sim: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSimulateCharging(sim)
        }
    }

    fun setSimulateNight(sim: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSimulateNight(sim)
        }
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

    fun setColorTheme(colorThemeId: String) {
        viewModelScope.launch { settingsRepository.setColorTheme(colorThemeId) }
    }

    fun setAutoDimEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setAutoDimEnabled(enabled) }
    }

    fun setWatchFaceType(watchFaceType: WatchFaceType) {
        viewModelScope.launch { settingsRepository.setWatchFaceType(watchFaceType) }
    }

    fun setBottomComplication(bottomComplication: BottomComplicationType) {
        viewModelScope.launch { settingsRepository.setBottomComplication(bottomComplication) }
    }

    fun setGithubUsername(username: String) {
        viewModelScope.launch {
            settingsRepository.setGithubUsername(username)
            GitHubRepository.fetchContributions(username, forceRefresh = true)
        }
    }
}
