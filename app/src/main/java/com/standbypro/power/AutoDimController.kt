package com.standbypro.power

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow

/** Tracks the last touch and reports when the display should fall back to its dimmed state. */
object AutoDimController {

    @Volatile
    private var lastInteractionTime = System.currentTimeMillis()

    private val _interactions = MutableStateFlow(0L)
    /** Bumps on every interaction; used to restart the controls auto-hide timer. */
    val interactions: StateFlow<Long> = _interactions.asStateFlow()

    fun reportInteraction() {
        lastInteractionTime = System.currentTimeMillis()
        _interactions.value = lastInteractionTime
    }

    fun dimState(timeoutMs: Long): Flow<Boolean> = flow {
        emit(false)
        while (true) {
            val idle = System.currentTimeMillis() - lastInteractionTime
            emit(idle >= timeoutMs)
            delay(if (idle >= timeoutMs) 500L else (timeoutMs - idle).coerceIn(250L, 1000L))
        }
    }.distinctUntilChanged()
}
