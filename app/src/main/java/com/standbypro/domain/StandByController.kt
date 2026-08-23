package com.standbypro.domain

import com.standbypro.hardware.ChargingStateMonitor
import com.standbypro.hardware.OrientationMonitor
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
    private val settingsRepository: SettingsRepository
) {

    private val _standByState = MutableStateFlow(StandByState.DISABLED)
    val standByState: StateFlow<StandByState> = _standByState.asStateFlow()

    init {
        scope.launch {
            combine(
                chargingStateMonitor.chargingState,
                orientationMonitor.orientation,
                settingsRepository.settingsFlow
            ) { chargingState, orientation, settings ->
                
                if (!settings.isEnabled) {
                    return@combine StandByState.DISABLED
                }
                
                val isLandscape = orientation == PhysicalOrientation.LANDSCAPE_LEFT || 
                                  orientation == PhysicalOrientation.LANDSCAPE_RIGHT
                                  
                val requiresLandscape = settings.requireLandscape
                val isProperlyOriented = if (requiresLandscape) isLandscape else true

                when {
                    chargingState.isCharging && isProperlyOriented -> StandByState.STANDBY_ACTIVE
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
