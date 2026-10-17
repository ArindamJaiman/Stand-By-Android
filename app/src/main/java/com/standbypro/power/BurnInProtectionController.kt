package com.standbypro.power

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.cos
import kotlin.math.sin

data class BurnInOffset(val x: Float, val y: Float)

/**
 * Moves the whole composition along a slow Lissajous orbit (±[MAX_SHIFT_PX]) so no OLED pixel
 * stays lit at the same intensity for long. Deterministic so it never "jumps" visibly.
 */
object BurnInProtectionController {

    private const val MAX_SHIFT_PX = 14f
    private const val UPDATE_INTERVAL_MS = 30_000L

    val burnInOffset: Flow<BurnInOffset> = flow {
        var step = 0
        while (true) {
            val t = step * 0.37
            emit(BurnInOffset((sin(t) * MAX_SHIFT_PX).toFloat(), (cos(t * 0.71) * MAX_SHIFT_PX).toFloat()))
            step++
            delay(UPDATE_INTERVAL_MS)
        }
    }
}
