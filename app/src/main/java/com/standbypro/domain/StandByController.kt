package com.standbypro.domain

import com.standbypro.hardware.ChargingStateMonitor
import com.standbypro.hardware.OrientationMonitor
import com.standbypro.hardware.ScreenLockMonitor
import com.standbypro.settings.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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
            combine(
                chargingStateMonitor.chargingState,
                orientationMonitor.orientation,
                screenLockMonitor.isScreenLocked,
                settingsRepository.settingsFlow
            ) { chargingState, orientation, isScreenLocked, settings ->
                
                if (!settings.isEnabled || !settings.autoStartWhileCharging) {
                    return@combine StandByState.DISABLED
                }
                
                val isLandscape = orientation == PhysicalOrientation.LANDSCAPE_LEFT || 
                                  orientation == PhysicalOrientation.LANDSCAPE_RIGHT
                                  
                val requiresLandscape = settings.requireLandscape
                val isProperlyOriented = if (requiresLandscape) isLandscape else true

                val requiresScreenLocked = settings.requireScreenLocked
                val isLockedConditionMet = if (requiresScreenLocked) isScreenLocked else true

                when {
                    chargingState.isCharging && isProperlyOriented && isLockedConditionMet -> StandByState.STANDBY_ACTIVE
                    chargingState.isCharging && !isProperlyOriented -> StandByState.CHARGING
                    !chargingState.isCharging && isProperlyOriented -> StandByState.LANDSCAPE
                    else -> StandByState.ENABLED
                }
            }.collect { state ->
                _standByState.value = state
            }
        }
    }
}
