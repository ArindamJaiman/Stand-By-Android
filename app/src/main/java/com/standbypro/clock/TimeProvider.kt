package com.standbypro.clock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Centralized time engine providing discrete time flows and high-refresh mechanical sweeping.
 */
object TimeProvider {

    /**
     * Discrete time flow (default 1 second interval) for digital clocks and calendar widgets.
     */
    fun timeFlow(intervalMs: Long = 1000L): Flow<LocalDateTime> = flow {
        while (true) {
            emit(LocalDateTime.now())
            val now = System.currentTimeMillis()
            val delayMillis = (intervalMs - (now % intervalMs)).coerceAtLeast(10L)
            delay(delayMillis)
        }
    }

    /**
     * Time flow adjusted for a specific IANA ZoneId (e.g. "Asia/Tokyo", "Europe/London").
     */
    fun timeZoneFlow(zoneId: String, intervalMs: Long = 1000L): Flow<ZonedDateTime> = flow {
        val zone = try {
            ZoneId.of(zoneId)
        } catch (e: Exception) {
            ZoneId.systemDefault()
        }
        while (true) {
            emit(ZonedDateTime.now(zone))
            val now = System.currentTimeMillis()
            val delayMillis = (intervalMs - (now % intervalMs)).coerceAtLeast(10L)
            delay(delayMillis)
        }
    }
}

/**
 * Compose helper for ultra-smooth 60/120Hz continuous mechanical second hand sweeping.
 * Returns continuous seconds as a Float (e.g. 14.523f) with zero ticking stutter.
 */
@Composable
fun rememberSweepingSeconds(enabled: Boolean = true): State<Float> {
    val state = remember { mutableFloatStateOf(0f) }

    if (enabled) {
        LaunchedEffect(Unit) {
            while (isActive) {
                withFrameNanos {
                    val now = System.currentTimeMillis()
                    val second = (now / 1000L) % 60
                    val millis = now % 1000L
                    state.floatValue = second + (millis / 1000f)
                }
            }
        }
    }

    return state
}
