package com.standbypro.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.Dial
import com.standbypro.ui.design.HandAngles
import com.standbypro.ui.design.SecondsMotion
import com.standbypro.ui.design.dial
import com.standbypro.ui.design.rememberDayClock
import java.time.Instant
import java.time.ZoneId

/**
 * Standard analog face host. The static [dialLayer] is drawn into its own cached graphics layer;
 * only the [handsLayer] re-draws on every animation frame, and it reads the clock state inside
 * the draw phase so the composition is never invalidated by the second hand.
 *
 * @param motion overrides the user's seconds motion (e.g. ambient faces that always glide).
 */
@Composable
fun AnalogFace(
    ctx: FaceContext,
    modifier: Modifier = Modifier,
    fill: Float = 0.94f,
    padding: Dp = 8.dp,
    motion: SecondsMotion = ctx.effectiveMotion,
    dialLayer: DrawScope.(Dial) -> Unit,
    handsLayer: DrawScope.(Dial, HandAngles) -> Unit
) {
    val clock = rememberDayClock(motion, ctx.animate)
    Box(modifier = modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer()) { if (size.minDimension >= 2f) dialLayer(dial(fill)) }
        Canvas(modifier = Modifier.fillMaxSize().graphicsLayer()) {
            if (size.minDimension >= 2f) handsLayer(dial(fill), HandAngles.from(clock.value))
        }
    }
}

/** Full-bleed animated canvas (ambient faces): [draw] receives seconds-of-day. */
@Composable
fun AnimatedCanvas(
    ctx: FaceContext,
    modifier: Modifier = Modifier,
    frameIntervalMs: Long = 33L,
    motion: SecondsMotion = SecondsMotion.SWEEP,
    draw: DrawScope.(Float) -> Unit
) {
    val clock = rememberDayClock(motion, ctx.animate, frameIntervalMs)
    Canvas(modifier = modifier.fillMaxSize().graphicsLayer()) { if (size.minDimension >= 2f) draw(clock.value) }
}

/** Day clock state for faces that draw custom content. */
@Composable
fun rememberFaceClock(ctx: FaceContext, motion: SecondsMotion = ctx.effectiveMotion): State<Float> =
    rememberDayClock(motion, ctx.animate)

/** Difference in seconds between [zoneId] and the device zone; recomputed hourly. */
@Composable
fun rememberZoneShift(zoneId: String, hourKey: Int): Float = remember(zoneId, hourKey) { zoneShiftSeconds(zoneId) }

fun zoneShiftSeconds(zoneId: String): Float = try {
    val now = Instant.now()
    val other = ZoneId.of(zoneId).rules.getOffset(now).totalSeconds
    val local = ZoneId.systemDefault().rules.getOffset(now).totalSeconds
    (other - local).toFloat()
} catch (e: Exception) {
    0f
}
