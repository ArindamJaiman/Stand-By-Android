package com.standbypro.domain

import android.util.Log
import com.standbypro.hardware.ChargingStateMonitor
import com.standbypro.hardware.OrientationMonitor
import com.standbypro.hardware.ScreenLockMonitor
import com.standbypro.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class StandByController(
    private val scope: CoroutineScope,
    private val chargingStateMonitor: ChargingStateMonitor,
    private val orientationMonitor: OrientationMonitor,
    private val screenLockMonitor: ScreenLockMonitor,
    private val settingsRepository: SettingsRepository
) {

    private val _standByState = MutableStateFlow(StandByState.DISABLED)
    val standByState: StateFlow<StandByState> = _standByState.asStateFlow()

    init {
        scope.launch {
            // Tier 1: Outer Gate - Charging State & Settings Check
            // StandByPro ONLY triggers when charging is detected.
            // When NOT charging, it enters 100% deep hibernation mode.
            combine(
                settingsRepository.settingsFlow,
                chargingStateMonitor.chargingState
            ) { settings, chargingState ->
                Pair(settings, chargingState)
            }.flatMapLatest { (settings, chargingState) ->
                if (!settings.isEnabled || !settings.autoStartWhileCharging || !chargingState.isCharging) {
                    Log.i("StandByController", "Gate 1 (Charging) not met -> HIBERNATION MODE (Sensors OFF)")
                    flowOf(StandByState.DISABLED)
                } else {
                    Log.i("StandByController", "Gate 1 (Charging) MET -> Activating Screen Lock & Orientation Listeners")
                    // Tier 2 & Tier 3: Screen Off/Locked Detection & Landscape Position Detection
                    // These flows are ONLY collected while charging is active.
                    // When unplugged, flatMapLatest cancels this flow, immediately unregistering accelerometer & receivers.
                    combine(
                        screenLockMonitor.isScreenLocked,
                        orientationMonitor.orientation
                    ) { isScreenLocked, orientation ->
                        val isLandscape = orientation == PhysicalOrientation.LANDSCAPE_LEFT || 
                                          orientation == PhysicalOrientation.LANDSCAPE_RIGHT
                                          
                        val requiresLandscape = settings.requireLandscape
                        val isProperlyOriented = if (requiresLandscape) isLandscape else true

                        val requiresScreenLocked = settings.requireScreenLocked
                        val isLockedConditionMet = if (requiresScreenLocked) isScreenLocked else true

                        Log.i(
                            "StandByController",
                            "Gate Evaluation: isCharging=true, isLockedConditionMet=$isLockedConditionMet (locked/screen-off=$isScreenLocked), isProperlyOriented=$isProperlyOriented (landscape=$isLandscape)"
                        )

                        when {
                            isProperlyOriented && isLockedConditionMet -> StandByState.STANDBY_ACTIVE
                            !isProperlyOriented -> StandByState.CHARGING
                            else -> StandByState.ENABLED
                        }
                    }
                }
            }.collect { state ->
                Log.i("StandByController", "StandBy State changed to: $state")
                _standByState.value = state
            }
        }
    }
}
