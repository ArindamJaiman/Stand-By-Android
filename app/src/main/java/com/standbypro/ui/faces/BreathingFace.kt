package com.standbypro.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.SecondsMotion
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.rememberDayClock
import com.standbypro.ui.design.safeRadialGradient
import com.standbypro.ui.design.textAt
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin

/** Box breathing (4 s in · 4 s hold · 4 s out · 4 s hold), driven by the wall clock. */
object Breathing {

    const val STEP_SECONDS = 4f
    const val CYCLE_SECONDS = STEP_SECONDS * 4

    /** [scale] is lung fullness 0..1, [progress] is how far through the current step we are. */
    data class Step(val label: String, val index: Int, val progress: Float, val scale: Float) {
        val secondsLeft: Int get() = ceil(STEP_SECONDS * (1f - progress)).toInt().coerceIn(1, STEP_SECONDS.toInt())
    }

    fun at(seconds: Float): Step {
        val t = ((seconds % CYCLE_SECONDS) + CYCLE_SECONDS) % CYCLE_SECONDS
        val index = (t / STEP_SECONDS).toInt().coerceIn(0, 3)
        val p = (t - index * STEP_SECONDS) / STEP_SECONDS
        val eased = ((1 - cos(PI * p)) / 2).toFloat()
        return when (index) {
            0 -> Step("Breathe in", 0, p, eased)
            1 -> Step("Hold", 1, p, 1f)
            2 -> Step("Breathe out", 2, p, 1f - eased)
            else -> Step("Hold", 3, p, 0f)
        }
    }
}

private val BreathMint = Color(0xFF7FF0D2)
private val BreathTeal = Color(0xFF3FB8C9)

private fun DrawScope.breathFlower(c: Offset, radius: Float, step: Breathing.Step, m: TextMeasurer) {
    val scale = step.scale
    val petalR = radius * (0.40f + 0.22f * scale)
    val dist = radius * (0.06f + 0.5f * scale)
    val rotation = scale * 45f
    drawCircle(safeRadialGradient(listOf(BreathMint.copy(alpha = 0.10f + 0.12f * scale), Color.Transparent), c, radius * 1.6f), radius * 1.6f, c)
    for (k in 0 until 6) {
        val a = (k * 60f + rotation) * PI.toFloat() / 180f
        val pc = Offset(c.x + cos(a) * dist, c.y + sin(a) * dist)
        val col = if (k % 2 == 0) BreathMint else BreathTeal
        drawCircle(safeRadialGradient(listOf(col.copy(alpha = 0.55f), col.copy(alpha = 0.18f)), pc, petalR), petalR, pc, blendMode = BlendMode.Plus)
    }
    // Four-segment ring: one segment per step, the current one filling up.
    val ringR = radius * 1.32f
    val stroke = radius * 0.035f
    val tl = Offset(c.x - ringR, c.y - ringR)
    for (i in 0 until 4) {
        val start = -90f + i * 90f + 3f
        drawArc(Color.White.copy(alpha = 0.10f), start, 84f, false, tl, Size(ringR * 2, ringR * 2), style = Stroke(stroke, cap = StrokeCap.Round))
        val fill = when {
            i < step.index -> 1f
            i == step.index -> step.progress
            else -> 0f
        }
        if (fill > 0f) drawArc(BreathMint, start, 84f * fill, false, tl, Size(ringR * 2, ringR * 2), style = Stroke(stroke, cap = StrokeCap.Round))
    }
    textAt(m, step.secondsLeft.toString(), c, clockStyle(FontWeight.Light).copy(fontSize = (radius * 0.42f).toSp()), Color.White.copy(alpha = 0.85f))
}

/** Guided breathing: a six-petal bloom that expands and contracts with box breathing. */
@Composable
fun BreathingFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 12)
    val clock = rememberDayClock(SecondsMotion.SWEEP, ctx.animate, 33L)
    val background = Modifier.background(Brush.verticalGradient(listOf(Color(0xFF041A1A), Color(0xFF020909), Color.Black)))
    if (ctx.isCompact) {
        Canvas(modifier.fillMaxSize().then(background)) {
            if (size.minDimension < 2f) return@Canvas
            val step = Breathing.at(clock.value)
            val r = size.minDimension * 0.26f
            val c = Offset(size.width / 2f, size.height * 0.44f)
            breathFlower(c, r, step, m)
            textAt(m, step.label.uppercase(), Offset(size.width / 2f, size.height * 0.92f), labelStyle(FontWeight.Bold).copy(fontSize = (size.minDimension * 0.07f).toSp()), BreathMint)
        }
        return
    }
    Row(modifier.fillMaxSize().then(background).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.weight(1.1f).fillMaxHeight()) {
            if (size.minDimension < 2f) return@Canvas
            breathFlower(center, size.minDimension * 0.3f, Breathing.at(clock.value), m)
        }
        Column(Modifier.weight(1f).fillMaxHeight().padding(start = 12.dp)) {
            Spacer(Modifier.weight(0.22f))
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.ExtraLight), Modifier.fillMaxWidth().weight(0.3f), color = Color.White, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            Box(Modifier.fillMaxWidth().weight(0.14f)) {
                Canvas(Modifier.fillMaxSize()) {
                    if (size.minDimension < 2f) return@Canvas
                    val step = Breathing.at(clock.value)
                    val style = labelStyle(FontWeight.SemiBold).copy(fontSize = (size.height * 0.5f).toSp(), letterSpacing = androidx.compose.ui.unit.TextUnit.Unspecified)
                    val layout = m.measure(step.label, style)
                    drawText(layout, color = BreathMint, topLeft = Offset(0f, (size.height - layout.size.height) / 2f))
                }
            }
            AutoSizeText("BOX BREATHING · 4 · 4 · 4 · 4", labelStyle(), Modifier.fillMaxWidth(0.8f).weight(0.06f), color = Color.White.copy(alpha = 0.45f), alignment = Alignment.CenterStart)
            Spacer(Modifier.weight(0.28f))
        }
    }
}
