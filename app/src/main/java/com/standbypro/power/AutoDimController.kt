package com.standbypro.power

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

object AutoDimController {
    private val DIM_TIMEOUT_MS = 30_000L // 30 seconds for dimming

    private val _isDimmed = MutableStateFlow(false)
    val isDimmed: Flow<Boolean> = _isDimmed.asStateFlow()

    private var lastInteractionTime = System.currentTimeMillis()

    fun reportInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        if (_isDimmed.value) {
            _isDimmed.value = false
        }
    }

    val dimStateMonitor: Flow<Boolean> = flow {
        while (true) {
            val now = System.currentTimeMillis()
            if (now - lastInteractionTime > DIM_TIMEOUT_MS && !_isDimmed.value) {
                _isDimmed.value = true
                emit(true)
            } else if (now - lastInteractionTime <= DIM_TIMEOUT_MS && _isDimmed.value) {
                _isDimmed.value = false
                emit(false)
            }
            delay(1000L) // Check every second
        }
    }
}
