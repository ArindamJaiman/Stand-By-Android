package com.standbypro.ui.faces

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.standbypro.domain.FaceContext
import com.standbypro.domain.SleepMath
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.design.safeRadialGradient
import com.standbypro.ui.design.textAt
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

private const val TAU = (2 * PI).toFloat()

@Composable
private fun FullFrame(modifier: Modifier, content: @Composable BoxScope.() -> Unit) =
    Box(modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(26.dp)), content = content)

/** Time and a caption stacked on one side of a scenic face. */
@Composable
private fun SceneTime(
    ctx: FaceContext,
    caption: String,
    color: Color,
    captionColor: Color,
    alignment: Alignment.Horizontal,
    widthFraction: Float,
    topWeight: Float = 0.3f
) {
    val textAlign = when (alignment) {
        Alignment.Start -> Alignment.CenterStart
        Alignment.End -> Alignment.CenterEnd
        else -> Alignment.Center
    }
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 18.dp), horizontalAlignment = alignment) {
        Spacer(Modifier.weight(topWeight))
        AutoSizeText(
            ctx.timeText, clockStyle(FontWeight.ExtraLight).copy(shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(0f, 3f), 18f)),
            Modifier.fillMaxWidth(widthFraction).weight(0.32f), color = color, sizingText = ctx.sizingTime(), alignment = textAlign
        )
        AutoSizeText(caption, labelStyle(), Modifier.fillMaxWidth(widthFraction * 0.8f).weight(0.07f), color = captionColor, alignment = textAlign)
        Spacer(Modifier.weight(0.61f - topWeight))
    }
}

private fun FaceContext.formatTime(millis: Long): String =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern(if (use24Hour) "HH:mm" else "h:mm a", Locale.getDefault()))

// ---------------------------------------------------------------------------------------------
// Kinetic — 24 small clocks whose hands sweep into the digits, after ClockClock
// ---------------------------------------------------------------------------------------------

/** Hand angles (degrees clockwise from 12) for the 2×3 mini clocks that draw each digit. */
object Kinetic {
    private const val U = 0f
    private const val R = 90f
    private const val D = 180f
    private const val L = 270f
    private val TL = R to D
    private val TR = L to D
    private val BL = U to R
    private val BR = U to L
    private val V = U to D
    private val X = 225f to 225f
    private val END_DOWN = D to D
    private val END_UP = U to U
    private val END_RIGHT = R to R
    private val END_LEFT = L to L

    /** Six (hand1, hand2) pairs in reading order: row by row, left then right. */
    val digits: Map<Char, List<Pair<Float, Float>>> = mapOf(
        '0' to listOf(TL, TR, V, V, BL, BR),
        '1' to listOf(X, END_DOWN, X, V, X, END_UP),
        '2' to listOf(END_RIGHT, TR, TL, BR, BL, END_LEFT),
        '3' to listOf(END_RIGHT, TR, END_RIGHT, V, END_RIGHT, BR),
        '4' to listOf(END_DOWN, END_DOWN, BL, V, X, END_UP),
        '5' to listOf(TL, END_LEFT, BL, TR, END_RIGHT, BR),
        '6' to listOf(TL, END_LEFT, V, TR, BL, BR),
        '7' to listOf(END_RIGHT, TR, X, V, X, END_UP),
        // The waist of the 8 pinches inwards: ">" on the left, "<" on the right.
        '8' to listOf(TL, TR, 315f to 225f, 45f to 135f, BL, BR),
        '9' to listOf(TL, TR, BL, V, END_RIGHT, BR)
    )

    fun hands(c: Char): List<Pair<Float, Float>> = digits[c] ?: List(6) { X }

    /** Clockwise travel from [from] to [to] plus one full turn, for a theatrical sweep. */
    fun sweep(from: Float, to: Float, progress: Float): Float {
        val delta = ((to - from) % 360f + 360f) % 360f
        return from + (delta + 360f) * progress
    }
}

private fun easeInOut(x: Float): Float = if (x < 0.5f) 4 * x * x * x else 1 - (-2 * x + 2).pow(3) / 2

@Composable
fun KineticFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val hour = if (ctx.use24Hour) ctx.time.hour else ctx.hour12
    val target = "%02d%02d".format(hour, ctx.time.minute)
    var from by remember { mutableStateOf(target) }
    var to by remember { mutableStateOf(target) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(target) {
        if (target == to) return@LaunchedEffect
        from = to
        to = target
        if (ctx.animate) {
            progress.snapTo(0f)
            progress.animateTo(1f, tween(2800, easing = LinearEasing))
        } else {
            progress.snapTo(1f)
        }
    }
    FullFrame(modifier) {
        Canvas(Modifier.fillMaxSize().background(Color(0xFF060607))) {
            if (size.minDimension < 2f) return@Canvas
            val stacked = ctx.isCompact
            val cols = if (stacked) 4 else 8
            val rows = if (stacked) 6 else 3
            val digitGap = 0.25f
            val midGap = if (stacked) 0.45f else 0.6f
            val unitsW = cols + (if (stacked) digitGap else digitGap * 2 + midGap)
            val unitsH = rows + (if (stacked) midGap else 0f)
            val s = min(size.width * 0.94f / unitsW, size.height * 0.9f / unitsH)
            val left = (size.width - unitsW * s) / 2f
            val top = (size.height - unitsH * s) / 2f
            val p = progress.value
            for (digit in 0 until 4) {
                val fromHands = Kinetic.hands(from[digit])
                val toHands = Kinetic.hands(to[digit])
                val blockCol = if (stacked) digit % 2 else digit
                val blockRow = if (stacked) digit / 2 else 0
                val gapsBefore = if (stacked) blockCol * digitGap else when (digit) {
                    0 -> 0f
                    1 -> digitGap
                    2 -> digitGap + midGap
                    else -> digitGap * 2 + midGap
                }
                val bx = left + blockCol * 2 * s + gapsBefore * s
                val by = top + blockRow * (3 + midGap) * s
                for (cell in 0 until 6) {
                    val col = cell % 2
                    val row = cell / 2
                    val c = Offset(bx + (col + 0.5f) * s, by + (row + 0.5f) * s)
                    val globalCol = digit * 2 + col
                    val local = easeInOut(((p - globalCol * 0.035f) / 0.72f).coerceIn(0f, 1f))
                    val r = s * 0.46f
                    drawCircle(Color(0xFF16171B), r, c)
                    drawCircle(Color.White.copy(alpha = 0.06f), r, c, style = Stroke(s * 0.012f))
                    val a1 = Kinetic.sweep(fromHands[cell].first, toHands[cell].first, local)
                    val a2 = Kinetic.sweep(fromHands[cell].second, toHands[cell].second, local)
                    for (a in listOf(a1, a2)) {
                        val rad = (a - 90f) * PI.toFloat() / 180f
                        drawLine(Color(0xFFF2F2F2), c, Offset(c.x + cos(rad) * r * 0.9f, c.y + sin(rad) * r * 0.9f), s * 0.075f, cap = StrokeCap.Round)
                    }
                    drawCircle(ctx.accent, s * 0.035f, c)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Fireplace — a crackling hearth with rising embers
// ---------------------------------------------------------------------------------------------

private fun DrawScope.flame(x: Float, baseY: Float, w: Float, h: Float, sway: Float, colors: List<Color>) {
    val path = Path().apply {
        moveTo(x - w / 2f, baseY)
        cubicTo(x - w / 2f, baseY - h * 0.4f, x - w * 0.12f + sway * 0.5f, baseY - h * 0.7f, x + sway, baseY - h)
        cubicTo(x + w * 0.12f + sway * 0.5f, baseY - h * 0.7f, x + w / 2f, baseY - h * 0.4f, x + w / 2f, baseY)
        close()
    }
    drawPath(path, Brush.verticalGradient(colors, startY = baseY - h, endY = baseY), blendMode = BlendMode.Plus)
}

@Composable
fun FireplaceFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullFrame(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 33L) { t ->
        val w = size.width
        val h = size.height
        val flicker = 0.85f + 0.1f * sin(t * 7.3f) + 0.05f * sin(t * 13.1f)
        drawRect(Brush.verticalGradient(listOf(Color(0xFF120806), Color(0xFF1E0D08), Color(0xFF0A0504))))
        val hearthCenter = Offset(w / 2f, h * 0.86f)
        drawCircle(safeRadialGradient(listOf(Color(0xFFFF7A2A).copy(alpha = 0.32f * flicker), Color(0xFFFF4A1A).copy(alpha = 0.1f), Color.Transparent), hearthCenter, size.maxDimension * 0.6f), size.maxDimension * 0.6f, hearthCenter)
        // Logs
        val logY = h * 0.88f
        val logW = w * 0.34f
        val logH = h * 0.075f
        listOf(-0.16f to -6f, 0.16f to 6f, 0f to 0f).forEach { (dx, tilt) ->
            val cx = w / 2f + dx * w
            rotate(tilt, Offset(cx, logY)) {
                drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF4A2A18), Color(0xFF24130A)), startY = logY - logH / 2, endY = logY + logH / 2), Offset(cx - logW / 2f, logY - logH / 2f), Size(logW, logH), CornerRadius(logH / 2f))
                drawCircle(Color(0xFF6B4026), logH * 0.42f, Offset(cx + logW / 2f - logH * 0.45f, logY))
                drawCircle(Color(0xFFFF8A3A).copy(alpha = 0.35f * flicker), logH * 0.25f, Offset(cx + logW / 2f - logH * 0.45f, logY))
            }
        }
        // Flames in three layers: deep red at the back, gold at the front.
        val baseY = logY - logH * 0.2f
        val layers = listOf(
            Triple(1.0f, 11, listOf(Color.Transparent, Color(0xFFB3200A).copy(alpha = 0.6f), Color(0xFFFF4A1A).copy(alpha = 0.7f))),
            Triple(0.75f, 9, listOf(Color.Transparent, Color(0xFFFF6A1A).copy(alpha = 0.7f), Color(0xFFFFA43A).copy(alpha = 0.8f))),
            Triple(0.5f, 7, listOf(Color.Transparent, Color(0xFFFFC85A).copy(alpha = 0.8f), Color(0xFFFFF4D0)))
        )
        layers.forEachIndexed { li, (scale, count, colors) ->
            for (i in 0 until count) {
                val f = (i + 0.5f) / count
                val x = w * (0.32f + 0.36f * f)
                val height = h * 0.42f * scale * (0.55f + 0.25f * sin(t * 2.1f + i * 1.7f + li) + 0.15f * sin(t * 3.7f + i * 0.9f)) * (1f - abs(f - 0.5f))
                val sway = w * 0.025f * sin(t * 1.3f + i + li * 2f)
                flame(x, baseY, w * 0.07f * scale + w * 0.02f, height, sway, colors)
            }
        }
        // Embers
        val rnd = Random(3)
        repeat(30) { i ->
            val speed = 0.15f + rnd.nextFloat() * 0.25f
            val life = (t * speed + rnd.nextFloat()) % 1f
            val x0 = w * (0.35f + rnd.nextFloat() * 0.3f)
            val x = x0 + sin(t * 2f + i) * w * 0.02f
            val y = baseY - life * h * 0.75f
            drawCircle(Color(0xFFFFB25A).copy(alpha = (1f - life) * 0.9f), size.minDimension * (0.003f + rnd.nextFloat() * 0.003f), Offset(x, y))
        }
    }
    SceneTime(ctx, ctx.formatDate("EEEE · MMMM d").uppercase(), Color(0xFFFFEBD6), Color(0xFFFFB27A), Alignment.CenterHorizontally, if (ctx.isCompact) 0.7f else 0.42f, topWeight = 0.06f)
}

// ---------------------------------------------------------------------------------------------
// Lighthouse — a beacon sweeping over a moonlit sea
// ---------------------------------------------------------------------------------------------

@Composable
fun LighthouseFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullFrame(modifier) {
    val towerX = if (ctx.isCompact) 0.72f else 0.8f
    Canvas(Modifier.fillMaxSize().graphicsLayer()) {
        if (size.minDimension < 2f) return@Canvas
        val w = size.width
        val h = size.height
        val horizon = h * 0.62f
        drawRect(Brush.verticalGradient(listOf(Color(0xFF050A1A), Color(0xFF0D1A36), Color(0xFF1C2D52)), endY = horizon), size = Size(w, horizon))
        val rnd = Random(12)
        repeat(90) {
            drawCircle(Color.White.copy(alpha = 0.2f + rnd.nextFloat() * 0.5f), 0.6f + rnd.nextFloat() * 1.4f, Offset(rnd.nextFloat() * w, rnd.nextFloat() * horizon * 0.9f))
        }
        val moon = Offset(w * 0.18f, h * 0.2f)
        drawCircle(safeRadialGradient(listOf(Color(0xFFDDE6FF).copy(alpha = 0.3f), Color.Transparent), moon, h * 0.22f), h * 0.22f, moon)
        drawCircle(Color(0xFFF2F4FF), h * 0.055f, moon)
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0C1A33), Color(0xFF050B17)), startY = horizon, endY = h), Offset(0f, horizon), Size(w, h - horizon))
        // Rock and tower
        val tx = w * towerX
        val rock = Path().apply {
            moveTo(tx - w * 0.16f, h)
            quadraticTo(tx - w * 0.12f, horizon + h * 0.04f, tx - w * 0.04f, horizon + h * 0.02f)
            lineTo(tx + w * 0.05f, horizon + h * 0.03f)
            quadraticTo(tx + w * 0.13f, horizon + h * 0.06f, tx + w * 0.18f, h)
            close()
        }
        drawPath(rock, Color(0xFF070B12))
        val baseY = horizon + h * 0.03f
        val topY = h * 0.3f
        val baseHalf = w * 0.028f
        val topHalf = w * 0.018f
        val bands = 6
        for (b in 0 until bands) {
            val y0 = baseY - (baseY - topY) * b / bands
            val y1 = baseY - (baseY - topY) * (b + 1) / bands
            val hw0 = baseHalf + (topHalf - baseHalf) * b / bands
            val hw1 = baseHalf + (topHalf - baseHalf) * (b + 1) / bands
            val band = Path().apply {
                moveTo(tx - hw0, y0); lineTo(tx + hw0, y0); lineTo(tx + hw1, y1); lineTo(tx - hw1, y1); close()
            }
            drawPath(band, if (b % 2 == 0) Color(0xFFD9DCE3) else Color(0xFFB0302A))
        }
        drawRect(Color(0xFF1A1E26), Offset(tx - topHalf * 1.4f, topY - h * 0.012f), Size(topHalf * 2.8f, h * 0.012f))
        drawRect(Color(0xFFFFE9A8), Offset(tx - topHalf, topY - h * 0.06f), Size(topHalf * 2f, h * 0.048f))
        drawPath(Path().apply { moveTo(tx - topHalf * 1.3f, topY - h * 0.06f); lineTo(tx, topY - h * 0.1f); lineTo(tx + topHalf * 1.3f, topY - h * 0.06f); close() }, Color(0xFF1A1E26))
    }
    AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 33L) { t ->
        val w = size.width
        val h = size.height
        val horizon = h * 0.62f
        val lamp = Offset(w * towerX, h * 0.3f - h * 0.036f)
        val a = (t * 45f % 360f) * PI.toFloat() / 180f
        val reach = w * 0.95f * abs(cos(a))
        val dir = if (cos(a) >= 0f) -1f else 1f
        val towardViewer = max(0f, sin(a))
        val spread = h * 0.05f + reach * 0.1f
        val end = lamp.x + dir * reach
        val beam = Path().apply {
            moveTo(lamp.x, lamp.y - h * 0.008f)
            lineTo(end, lamp.y - spread)
            lineTo(end, lamp.y + spread)
            lineTo(lamp.x, lamp.y + h * 0.008f)
            close()
        }
        drawPath(beam, Brush.horizontalGradient(listOf(Color(0xFFFFF2C4).copy(alpha = 0.32f + 0.2f * towardViewer), Color.Transparent), startX = lamp.x, endX = end), blendMode = BlendMode.Plus)
        val flash = h * (0.03f + 0.12f * towardViewer.pow(6))
        drawCircle(safeRadialGradient(listOf(Color(0xFFFFF6D8), Color(0xFFFFE08A).copy(alpha = 0.4f), Color.Transparent), lamp, flash * 2f), flash * 2f, lamp, blendMode = BlendMode.Plus)
        // Moon glitter and swell
        val moonX = w * 0.18f
        for (k in 0 until 14) {
            val y = horizon + (h - horizon) * (0.06f + k * 0.065f)
            val half = w * (0.012f + k * 0.006f) * (0.6f + 0.4f * sin(t * 1.5f + k))
            drawLine(Color(0xFFDDE6FF).copy(alpha = 0.35f - k * 0.02f), Offset(moonX - half, y), Offset(moonX + half, y), h * 0.004f, cap = StrokeCap.Round)
        }
        for (k in 0 until 5) {
            val y0 = horizon + (h - horizon) * (0.2f + k * 0.18f)
            val path = Path()
            var x = 0f
            path.moveTo(0f, y0)
            while (x <= w) {
                path.lineTo(x, y0 + sin(x / w * 9f + t * 0.8f + k) * h * 0.008f)
                x += w / 60f
            }
            drawPath(path, Color.White.copy(alpha = 0.05f), style = Stroke(h * 0.003f))
        }
    }
    SceneTime(ctx, ctx.formatDate("EEEE · MMMM d").uppercase(), Color(0xFFEFF3FF), Color(0xFF9FB4E0), Alignment.Start, if (ctx.isCompact) 0.52f else 0.5f, topWeight = if (ctx.isCompact) 0.48f else 0.3f)
}

// ---------------------------------------------------------------------------------------------
// Snowfall — a warm cabin in a snowy pine valley
// ---------------------------------------------------------------------------------------------

@Composable
fun SnowfallFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullFrame(modifier) {
    Canvas(Modifier.fillMaxSize().graphicsLayer()) {
        if (size.minDimension < 2f) return@Canvas
        val w = size.width
        val h = size.height
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0A1024), Color(0xFF1A2444), Color(0xFF3A4566))))
        fun hill(base: Float, amp: Float, freq: Float, phase: Float, color: Color) {
            val path = Path().apply {
                moveTo(0f, h)
                var x = 0f
                while (x <= w + 1f) {
                    lineTo(x, h * base - sin(x / w * freq + phase) * h * amp)
                    x += w / 40f
                }
                lineTo(w, h)
                close()
            }
            drawPath(path, color)
        }
        hill(0.62f, 0.06f, 3.1f, 0.4f, Color(0xFF8C99B8))
        // Pines on the far hill
        val rnd = Random(31)
        repeat(18) {
            val x = rnd.nextFloat() * w
            val baseY = h * (0.66f + rnd.nextFloat() * 0.06f)
            val th = h * (0.08f + rnd.nextFloat() * 0.08f)
            val tw = th * 0.42f
            drawPath(Path().apply { moveTo(x, baseY - th); lineTo(x - tw / 2, baseY); lineTo(x + tw / 2, baseY); close() }, Color(0xFF1C2A3A))
            drawPath(Path().apply { moveTo(x, baseY - th); lineTo(x - tw * 0.22f, baseY - th * 0.55f); lineTo(x + tw * 0.22f, baseY - th * 0.55f); close() }, Color(0xFFE6ECF7).copy(alpha = 0.7f))
        }
        hill(0.82f, 0.04f, 2.2f, 1.7f, Color(0xFFD9E1F0))
        // Cabin
        val cx = w * 0.3f
        val cy = h * 0.78f
        val cw = w * 0.13f
        val ch = h * 0.12f
        drawRect(Color(0xFF3A2418), Offset(cx - cw / 2, cy - ch), Size(cw, ch))
        drawPath(Path().apply { moveTo(cx - cw * 0.65f, cy - ch); lineTo(cx, cy - ch * 1.8f); lineTo(cx + cw * 0.65f, cy - ch); close() }, Color(0xFFEFF3FA))
        drawRect(Color(0xFF2A1A12), Offset(cx + cw * 0.18f, cy - ch * 1.75f), Size(cw * 0.1f, ch * 0.45f))
        val window = Offset(cx - cw * 0.18f, cy - ch * 0.55f)
        drawCircle(safeRadialGradient(listOf(Color(0xFFFFC46B).copy(alpha = 0.55f), Color.Transparent), window, cw * 0.9f), cw * 0.9f, window)
        drawRect(Color(0xFFFFC86E), Offset(window.x - cw * 0.1f, window.y - ch * 0.15f), Size(cw * 0.2f, ch * 0.3f))
        drawRect(Color(0xFF6E4A30), Offset(cx + cw * 0.12f, cy - ch * 0.62f), Size(cw * 0.2f, ch * 0.62f))
    }
    AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 33L) { t ->
        val w = size.width
        val h = size.height
        // Chimney smoke
        val chimney = Offset(w * 0.3f + w * 0.13f * 0.23f, h * 0.78f - h * 0.12f * 1.75f)
        repeat(6) { i ->
            val life = (t * 0.12f + i / 6f) % 1f
            val p = Offset(chimney.x + sin(t * 0.6f + i) * w * 0.01f + life * w * 0.04f, chimney.y - life * h * 0.25f)
            drawCircle(Color(0xFFCBD3E6).copy(alpha = 0.18f * (1f - life)), h * (0.015f + life * 0.035f), p)
        }
        // Snow in three depths
        val rnd = Random(77)
        repeat(130) { i ->
            val depth = i % 3
            val speed = 0.03f + depth * 0.025f + rnd.nextFloat() * 0.02f
            val y = ((t * speed + rnd.nextFloat()) % 1.1f) * h - h * 0.05f
            val x = (rnd.nextFloat() * w + sin(t * (0.5f + depth * 0.3f) + i) * w * 0.015f + t * w * 0.004f * (depth + 1)) % w
            drawCircle(Color.White.copy(alpha = 0.45f + depth * 0.2f), size.minDimension * (0.002f + depth * 0.0018f), Offset(x, y))
        }
    }
    SceneTime(ctx, ctx.formatDate("EEEE · MMMM d").uppercase(), Color.White, Color(0xFFC9D6F2), if (ctx.isCompact) Alignment.CenterHorizontally else Alignment.End, if (ctx.isCompact) 0.7f else 0.45f, topWeight = if (ctx.isCompact) 0.05f else 0.18f)
}

// ---------------------------------------------------------------------------------------------
// Year in Dots — every day of the year, one row per month
// ---------------------------------------------------------------------------------------------

@Composable
fun YearDotsFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    val today = ctx.time.toLocalDate()
    val daysInYear = today.lengthOfYear()
    val dayOfYear = today.dayOfYear
    val percent = (dayOfYear * 100f / daysInYear).toInt()
    val accent = ctx.accent
    val grid: @Composable (Modifier) -> Unit = { mod ->
        Canvas(mod) {
            if (size.minDimension < 2f) return@Canvas
            val labelCols = 2.2f
            val cell = min(size.width / (31 + labelCols), size.height / 12f)
            val left = (size.width - cell * (31 + labelCols)) / 2f
            val top = (size.height - cell * 12f) / 2f
            val labelStyle = TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.Bold, fontSize = (cell * 0.55f).toSp())
            for (month in 1..12) {
                val ym = java.time.YearMonth.of(today.year, month)
                val y = top + (month - 0.5f) * cell
                textAt(m, ym.month.getDisplayName(java.time.format.TextStyle.NARROW, Locale.getDefault()), Offset(left + cell * 0.8f, y), labelStyle, if (month == today.monthValue) accent else Ink.TextTertiary)
                for (day in 1..ym.lengthOfMonth()) {
                    val date = ym.atDay(day)
                    val c = Offset(left + (labelCols + day - 0.5f) * cell, y)
                    val weekend = date.dayOfWeek.value >= 6
                    when {
                        date == today -> {
                            drawCircle(accent.copy(alpha = 0.3f), cell * 0.5f, c)
                            drawCircle(accent, cell * 0.32f, c)
                        }
                        date.isBefore(today) -> drawCircle(Color.White.copy(alpha = if (weekend) 0.45f else 0.7f), cell * 0.28f, c)
                        else -> drawCircle(Color.White.copy(alpha = if (weekend) 0.07f else 0.13f), cell * 0.28f, c)
                    }
                }
            }
        }
    }
    val caption = "DAY $dayOfYear OF $daysInYear  ·  ${daysInYear - dayOfYear} LEFT"
    if (ctx.isCompact) {
        Column(modifier.fillMaxSize().padding(12.dp)) {
            grid(Modifier.fillMaxWidth().weight(0.62f))
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.26f), color = Ink.TextPrimary, sizingText = ctx.sizingTime())
            AutoSizeText(caption, labelStyle(), Modifier.fillMaxWidth().weight(0.07f), color = accent)
        }
    } else {
        Row(modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            grid(Modifier.weight(1.45f).fillMaxHeight(0.92f))
            Column(Modifier.weight(1f).fillMaxHeight().padding(start = 18.dp)) {
                Spacer(Modifier.weight(0.24f))
                AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.28f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
                AutoSizeText("${today.year} IS $percent% COMPLETE", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.07f), color = accent, sizingText = "2026 IS 100% COMPLETE", alignment = Alignment.CenterStart)
                AutoSizeText(caption, labelStyle(FontWeight.Medium), Modifier.fillMaxWidth(0.9f).weight(0.05f), color = Ink.TextTertiary, sizingText = "DAY 365 OF 366  ·  365 LEFT", alignment = Alignment.CenterStart)
                Spacer(Modifier.weight(0.36f))
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Sleep Cycles — how much sleep is left before the alarm, in 90-minute cycles
// ---------------------------------------------------------------------------------------------

private val SleepIndigo = Color(0xFF7C7CFF)
private val SleepViolet = Color(0xFFC08BFF)

@Composable
fun SleepCyclesFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val now = ctx.time.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    val plan = SleepMath.planToAlarm(now, ctx.nextAlarmMillis)
    val background = Modifier.background(Brush.verticalGradient(listOf(Color(0xFF07071A), Color(0xFF0D0B26), Color(0xFF05050F))))
    Column(modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(26.dp)).then(background).padding(horizontal = 22.dp, vertical = 16.dp)) {
        Row(Modifier.fillMaxWidth().weight(0.5f), verticalAlignment = Alignment.CenterVertically) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.ExtraLight), Modifier.weight(1f).fillMaxHeight(), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            Column(Modifier.weight(1f).fillMaxHeight().padding(start = 16.dp), verticalArrangement = Arrangement.Center) {
                if (plan != null) {
                    AutoSizeText("SLEEP LEFT", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.16f), color = SleepViolet, alignment = Alignment.CenterStart)
                    AutoSizeText(SleepMath.durationLabel(plan.sleepMinutes), clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.42f), color = Ink.TextPrimary, sizingText = "88 h 88 m", alignment = Alignment.CenterStart)
                    AutoSizeText("%.1f cycles  ·  alarm %s".format(Locale.US, plan.cycles, ctx.nextAlarmTime ?: ctx.formatTime(plan.wakeMillis)), labelStyle(FontWeight.Medium).copy(letterSpacing = TextStyle.Default.letterSpacing), Modifier.fillMaxWidth().weight(0.16f), color = Ink.TextSecondary, sizingText = "8.8 cycles  ·  alarm 88:88 AM", alignment = Alignment.CenterStart)
                } else {
                    AutoSizeText("FALL ASLEEP NOW, WAKE AT", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.16f), color = SleepViolet, alignment = Alignment.CenterStart)
                    SleepMath.wakeSuggestions(now).forEach { (cycles, wake) ->
                        AutoSizeText("${ctx.formatTime(wake)}  ·  $cycles cycles", labelStyle(FontWeight.SemiBold).copy(letterSpacing = TextStyle.Default.letterSpacing), Modifier.fillMaxWidth().weight(0.16f), color = if (cycles == 5) Ink.TextPrimary else Ink.TextSecondary, sizingText = "88:88 AM  ·  8 cycles", alignment = Alignment.CenterStart)
                    }
                }
            }
        }
        Canvas(Modifier.fillMaxWidth().weight(0.42f).padding(top = 10.dp)) {
            if (size.minDimension < 2f) return@Canvas
            val totalMinutes = plan?.sleepMinutes ?: (5 * SleepMath.CYCLE_MINUTES)
            val cycles = totalMinutes / SleepMath.CYCLE_MINUTES.toFloat()
            val baseY = size.height * 0.2f
            val depthH = size.height * 0.62f
            val path = Path().apply { moveTo(0f, baseY) }
            val steps = 160
            for (k in 1..steps) {
                val f = k / steps.toFloat()
                val cyclePos = f * cycles
                val ci = cyclePos.toInt()
                val inCycle = cyclePos - ci
                // Deep sleep dominates early cycles; REM (near the surface) grows towards morning.
                val depth = (1f - ci * 0.13f).coerceAtLeast(0.35f) * sin(inCycle * PI.toFloat()).pow(0.8f)
                path.lineTo(f * size.width, baseY + depth * depthH)
            }
            drawPath(path, Brush.horizontalGradient(listOf(SleepIndigo, SleepViolet, Color(0xFFFFC27A))), style = Stroke(size.height * 0.035f, cap = StrokeCap.Round))
            for (k in 1 until cycles.toInt() + 1) {
                val x = k / cycles * size.width
                if (x < size.width - 2f) drawLine(Color.White.copy(alpha = 0.12f), Offset(x, baseY * 0.4f), Offset(x, size.height), size.height * 0.008f)
            }
            drawLine(Color.White.copy(alpha = 0.18f), Offset(0f, baseY), Offset(size.width, baseY), size.height * 0.006f)
            drawCircle(Color(0xFFE8E6FF), size.height * 0.07f, Offset(size.height * 0.07f, baseY * 0.45f))
            drawCircle(Color(0xFF0D0B26), size.height * 0.07f, Offset(size.height * 0.11f, baseY * 0.38f))
            drawCircle(Color(0xFFFFC27A), size.height * 0.06f, Offset(size.width - size.height * 0.06f, baseY * 0.45f))
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Pixel Day — an 8-bit landscape whose sky, sun and moon follow the real time of day
// ---------------------------------------------------------------------------------------------

private data class SkyPalette(val top: Color, val bottom: Color, val ground: Color, val far: Color, val near: Color)

private fun skyFor(hour: Float): SkyPalette {
    val night = SkyPalette(Color(0xFF0B1030), Color(0xFF1E2A5A), Color(0xFF16301E), Color(0xFF1C2448), Color(0xFF141B36))
    val dawn = SkyPalette(Color(0xFF3B3F8F), Color(0xFFF59E7A), Color(0xFF2F5A2A), Color(0xFF6A4E7A), Color(0xFF3E3A5E))
    val day = SkyPalette(Color(0xFF3E8FE8), Color(0xFFA8DBFF), Color(0xFF3F8F3A), Color(0xFF6FA0C8), Color(0xFF4A7A9A))
    val dusk = SkyPalette(Color(0xFF4B2F7C), Color(0xFFFF8A5B), Color(0xFF2E4A26), Color(0xFF6E3E66), Color(0xFF3A2A4E))
    return when {
        hour < 5f || hour >= 21f -> night
        hour < 7f -> dawn
        hour < 17f -> day
        else -> dusk
    }
}

@Composable
fun PixelDayFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullFrame(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize().background(Color.Black), frameIntervalMs = 100L) { t ->
        val gw = 96
        val gh = 48
        val px = max(size.width / gw, size.height / gh)
        val ox = (size.width - gw * px) / 2f
        val oy = (size.height - gh * px) / 2f
        fun dot(x: Int, y: Int, c: Color) = drawRect(c, Offset(ox + x * px, oy + y * px), Size(px + 0.5f, px + 0.5f))
        val hour = ctx.time.hour + ctx.time.minute / 60f
        val pal = skyFor(hour)
        val horizon = 34
        for (y in 0 until horizon) {
            val band = (y / (horizon / 6f)).toInt() / 5f
            val col = lerpColor(pal.top, pal.bottom, band.coerceIn(0f, 1f))
            for (x in 0 until gw) dot(x, y, col)
        }
        val nightSky = hour < 5f || hour >= 21f
        if (nightSky || hour < 6f || hour >= 20f) {
            val rnd = Random(5)
            repeat(40) { i ->
                val sx = rnd.nextInt(gw)
                val sy = rnd.nextInt(horizon - 8)
                if (sin(t * 1.7f + i) > -0.3f) dot(sx, sy, Color.White.copy(alpha = 0.8f))
            }
        }
        // Sun by day, moon by night, travelling an arc across the sky.
        val isDay = hour in 6f..18f
        val arcF = if (isDay) (hour - 6f) / 12f else ((hour + 6f) % 24f) / 12f
        val bx = (arcF * (gw - 10) + 5).toInt()
        val by = (horizon - 4 - sin(arcF * PI.toFloat()) * (horizon - 12)).toInt()
        val bodyColor = if (isDay) Color(0xFFFFE066) else Color(0xFFF2F2FF)
        for (dy in -3..3) for (dx in -3..3) {
            if (dx * dx + dy * dy <= 9) dot(bx + dx, by + dy, bodyColor)
        }
        if (!isDay) for (dy in -3..3) for (dx in -1..3) {
            if ((dx - 2) * (dx - 2) + dy * dy <= 6) dot(bx + dx, by + dy, lerpColor(pal.top, pal.bottom, 0.3f))
        }
        // Clouds drift across.
        val clouds = listOf(Triple(10, 6, 0.6f), Triple(50, 10, 0.4f), Triple(80, 4, 0.8f))
        clouds.forEach { (cx0, cy, speed) ->
            val cx = ((cx0 + t * speed).toInt() % (gw + 20)) - 10
            val cloudColor = if (nightSky) Color(0xFF39406A) else Color.White.copy(alpha = 0.9f)
            for (dx in 0 until 9) dot(cx + dx, cy, cloudColor)
            for (dx in 2 until 7) dot(cx + dx, cy - 1, cloudColor)
        }
        // Mountains and ground.
        val far = Random(9)
        var hgt = 8
        for (x in 0 until gw) {
            hgt = (hgt + far.nextInt(3) - 1).coerceIn(4, 14)
            for (y in horizon - hgt until horizon) dot(x, y, pal.far)
        }
        val near = Random(4)
        hgt = 4
        for (x in 0 until gw) {
            hgt = (hgt + near.nextInt(3) - 1).coerceIn(2, 8)
            for (y in horizon - hgt + 2 until horizon + 2) dot(x, y, pal.near)
        }
        for (y in horizon + 2 until gh) for (x in 0 until gw) dot(x, y, if ((x + y) % 7 == 0) lerpColor(pal.ground, Color.Black, 0.2f) else pal.ground)
        listOf(44, 55, 62, 71, 80, 90).forEach { tx ->
            val ty = horizon + 3
            for (k in 0..4) for (dx in -k / 2..k / 2) dot(tx + dx, ty - 5 + k, Color(0xFF1E5A2A))
            dot(tx, ty, Color(0xFF5A3A1A))
        }
        // Time in the 5×7 dot font, set into the grass like a game's score. The grid covers the
        // frame, so anchor it to the cells that are actually visible.
        val visibleLeft = kotlin.math.ceil(-ox / px).toInt().coerceAtLeast(0)
        val visibleBottom = ((size.height - oy) / px).toInt().coerceAtMost(gh)
        val textTop = (visibleBottom - 9).coerceAtMost(horizon + 5)
        var cursor = visibleLeft + 3
        ctx.timeText.forEach { ch ->
            FaceMath.dotGlyph(ch).forEachIndexed { gy, line ->
                line.forEachIndexed { gx, on ->
                    if (on == '#') {
                        dot(cursor + gx + 1, textTop + gy + 1, Color.Black.copy(alpha = 0.35f))
                        dot(cursor + gx, textTop + gy, Color.White)
                    }
                }
            }
            cursor += if (ch == ':') 4 else 6
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Liquid Glass — a frosted pill floating over slowly moving colour
// ---------------------------------------------------------------------------------------------

@Composable
fun LiquidGlassFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullFrame(modifier) {
    val accent = ctx.accent
    val palette = listOf(accent, Color(0xFF6A5CFF), Color(0xFFFF4FA3), Color(0xFF2EE6D6))
    AnimatedCanvas(ctx, Modifier.fillMaxSize().background(Color(0xFF05050A)), frameIntervalMs = 33L) { t ->
        palette.forEachIndexed { i, col ->
            val c = Offset(
                size.width * (0.5f + 0.38f * sin(t * TAU / (40f + i * 9f) + i * 1.7f)),
                size.height * (0.5f + 0.34f * cos(t * TAU / (33f + i * 7f) + i))
            )
            val r = size.maxDimension * (0.32f + 0.05f * i)
            drawCircle(safeRadialGradient(listOf(col.copy(alpha = 0.55f), col.copy(alpha = 0.18f), Color.Transparent), c, r), r, c, blendMode = BlendMode.Plus)
        }
    }
    Box(
        Modifier.align(Alignment.Center).fillMaxWidth(if (ctx.isCompact) 0.84f else 0.62f).fillMaxHeight(if (ctx.isCompact) 0.5f else 0.56f)
            .clip(RoundedCornerShape(40.dp))
            .background(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.22f), Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.12f))))
            .border(1.5.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.35f))), RoundedCornerShape(40.dp))
    ) {
        Canvas(Modifier.fillMaxSize()) {
            drawOval(Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.28f), Color.Transparent), endY = size.height * 0.35f), Offset(size.width * 0.06f, -size.height * 0.2f), Size(size.width * 0.88f, size.height * 0.5f))
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            AutoSizeText(
                ctx.timeText, clockStyle(FontWeight.Light).copy(shadow = Shadow(Color.Black.copy(alpha = 0.25f), Offset(0f, 2f), 12f)),
                Modifier.fillMaxWidth().weight(0.76f), color = Color.White, sizingText = ctx.sizingTime()
            )
            AutoSizeText(ctx.formatDate("EEEE · MMMM d").uppercase(), labelStyle(), Modifier.fillMaxWidth(0.7f).weight(0.14f), color = Color.White.copy(alpha = 0.85f))
        }
    }
}
