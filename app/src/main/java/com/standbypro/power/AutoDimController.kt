package com.standbypro.power

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

object AutoDimController {
    private const val DIM_TIMEOUT_MS = 30_000L // 30 seconds

    private val _isDimmed = MutableStateFlow(false)
    val isDimmed: StateFlow<Boolean> = _isDimmed.asStateFlow()

    @Volatile
    private var lastInteractionTime = System.currentTimeMillis()

    fun reportInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        _isDimmed.value = false
    }

    val dimStateMonitor: Flow<Boolean> = flow {
        // Always emit initial state
        emit(false)
        while (true) {
            delay(1000L)
            val elapsed = System.currentTimeMillis() - lastInteractionTime
            val shouldDim = elapsed > DIM_TIMEOUT_MS
            if (shouldDim != _isDimmed.value) {
                _isDimmed.value = shouldDim
                emit(shouldDim)
            }
        }
    }
}
