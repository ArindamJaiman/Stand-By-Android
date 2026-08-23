package com.standbypro.clock

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime

object ClockEngine {
    
    fun timeFlow(updateIntervalMs: Long = 1000L): Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now())
            val now = System.currentTimeMillis()
            val delayMillis = updateIntervalMs - (now % updateIntervalMs)
            delay(delayMillis)
        }
    }
}
