package com.standbypro.ui.design

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.time.LocalTime

/** How the seconds hand (and every per-second animation) moves. */
enum class SecondsMotion(val label: String, val description: String) {
    SWEEP("Smooth sweep", "Continuous mechanical glide (capped at 60 fps)"),
    TICK("Quartz tick", "Crisp once-a-second step with a tiny overshoot"),
    OFF("Hidden", "No seconds hand – lowest power")
}

/**
 * Seconds elapsed since local midnight as a Float (e.g. 45296.42 at 12:34:56.42).
 *
 * The returned [State] must only be read inside draw lambdas (Canvas / drawBehind) so that the
 * per-frame updates invalidate drawing only — never recomposition. This is the single biggest
 * battery optimisation for an always-on display.
 *
 * @param animate false for gallery thumbnails: updates once a minute, no frame loop at all.
 * @param frameIntervalMs minimum spacing between sweep frames (15 ≈ 60 fps, 33 ≈ 30 fps).
 */
@Composable
fun rememberDayClock(
    motion: SecondsMotion,
    animate: Boolean = true,
    frameIntervalMs: Long = 15L
): State<Float> {
    val state = remember { mutableFloatStateOf(secondsOfDay(fractional = motion == SecondsMotion.SWEEP)) }
    LaunchedEffect(motion, animate, frameIntervalMs) {
        if (!animate) {
            while (isActive) {
                state.floatValue = secondsOfDay(fractional = false)
                val now = System.currentTimeMillis()
                delay(60_000L - now % 60_000L + 5L)
            }
            return@LaunchedEffect
        }
        when (motion) {
            SecondsMotion.SWEEP -> {
                var lastFrame = 0L
                while (isActive) {
                    withFrameMillis { frame ->
                        // Cap at ~60 fps even on 120 Hz panels: invisible difference, half the work.
                        if (frame - lastFrame >= frameIntervalMs) {
                            lastFrame = frame
                            state.floatValue = secondsOfDay(fractional = true)
                        }
                    }
                }
            }
            SecondsMotion.TICK -> {
                while (isActive) {
                    val within = System.currentTimeMillis() % 1000L
                    if (within < TICK_ANIM_MS + 40L) {
                        withFrameMillis {
                            val w = System.currentTimeMillis() % 1000L
                            val whole = secondsOfDay(fractional = false)
                            state.floatValue = if (w < TICK_ANIM_MS) {
                                whole - 1f + easeOutBack(w / TICK_ANIM_MS.toFloat())
                            } else whole
                        }
                    } else {
                        state.floatValue = secondsOfDay(fractional = false)
                        delay(1000L - within + 1L)
                    }
                }
            }
            SecondsMotion.OFF -> {
                while (isActive) {
                    state.floatValue = secondsOfDay(fractional = false)
                    val now = System.currentTimeMillis()
                    delay(1000L - now % 1000L + 2L)
                }
            }
        }
    }
    return state
}

private const val TICK_ANIM_MS = 160L

fun secondsOfDay(fractional: Boolean): Float {
    val now = LocalTime.now()
    val whole = now.toSecondOfDay().toFloat()
    return if (fractional) whole + now.nano / 1_000_000_000f else whole
}

fun easeOutBack(x: Float): Float {
    val c1 = 1.70158f
    val c3 = c1 + 1f
    val t = x.coerceIn(0f, 1f) - 1f
    return 1f + c3 * t * t * t + c1 * t * t
}

/** Hand angles in degrees, 0° = 12 o'clock, clockwise. */
data class HandAngles(val hour: Float, val minute: Float, val second: Float, val hour24: Float) {
    companion object {
        fun from(daySeconds: Float): HandAngles {
            val s = ((daySeconds % 86_400f) + 86_400f) % 86_400f
            return HandAngles(
                hour = (s / 3600f % 12f) * 30f,
                minute = (s / 60f % 60f) * 6f,
                second = (s % 60f) * 6f,
                hour24 = (s / 3600f % 24f) * 15f
            )
        }
    }
}

/** Continuous "infinite" phase for ambient animations, derived from wall clock (no extra loops). */
fun phase(daySeconds: Float, periodSeconds: Float): Float = (daySeconds % periodSeconds) / periodSeconds
