package com.standbypro.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.design.safeRadialGradient
import com.standbypro.ui.design.textAt
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

private const val TAU = (2 * PI).toFloat()

/** Time + date overlay for full-bleed ambient art. */
@Composable
private fun AmbientTime(
    ctx: FaceContext,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    dateColor: Color = Color.White.copy(alpha = 0.7f),
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    weight: FontWeight = FontWeight.ExtraLight,
    widthFraction: Float = 0.62f,
    shadow: Boolean = true
) {
    val textAlign = when (alignment) {
        Alignment.Start -> Alignment.CenterStart
        Alignment.End -> Alignment.CenterEnd
        else -> Alignment.Center
    }
    Column(modifier.fillMaxSize().padding(18.dp), horizontalAlignment = alignment) {
        Spacer(Modifier.weight(0.3f))
        AutoSizeText(
            ctx.timeText,
            clockStyle(weight).let { if (shadow) it.copy(shadow = Shadow(Color.Black.copy(alpha = 0.45f), Offset(0f, 3f), 18f)) else it },
            Modifier.fillMaxWidth(widthFraction).weight(0.32f), color = color, sizingText = ctx.sizingTime(), alignment = textAlign
        )
        AutoSizeText(ctx.formatDate("EEEE · MMMM d").uppercase(), labelStyle(), Modifier.fillMaxWidth(widthFraction * 0.75f).weight(0.07f), color = dateColor, alignment = textAlign)
        Spacer(Modifier.weight(0.31f))
    }
}

@Composable
private fun FullBleed(modifier: Modifier, content: @Composable () -> Unit) {
    Box(modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(26.dp))) { content() }
}

// ---------------------------------------------------------------------------------------------

@Composable
fun AuroraFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize()) { t ->
        drawRect(Brush.verticalGradient(listOf(Color(0xFF020814), Color(0xFF041A24), Color(0xFF02060C))))
        drawStars(80, 5, t / 10f)
        val bands = listOf(Color(0xFF3DFFA8), Color(0xFF2DE2E6), Color(0xFF8A5CFF), Color(0xFF3DFFA8))
        bands.forEachIndexed { i, c ->
            val path = Path()
            val baseY = size.height * (0.28f + i * 0.08f)
            path.moveTo(0f, size.height)
            var x = 0f
            while (x <= size.width + 0.5f) {
                val nx = x / size.width
                val y = baseY + sin(nx * 5f + t * 0.25f + i) * size.height * 0.07f + sin(nx * 11f - t * 0.4f + i * 2f) * size.height * 0.025f
                path.lineTo(x, y)
                x += size.width / 60f
            }
            path.lineTo(size.width, size.height)
            path.close()
            drawPath(path, Brush.verticalGradient(listOf(c.copy(alpha = 0.0f), c.copy(alpha = 0.32f - i * 0.05f), Color.Transparent), startY = baseY - size.height * 0.2f, endY = baseY + size.height * 0.5f), blendMode = BlendMode.Plus)
        }
        // Mountains
        val ridge = Path().apply {
            moveTo(0f, size.height)
            var x = 0f
            while (x <= size.width + 0.5f) {
                val nx = x / size.width
                lineTo(x, size.height * (0.82f - 0.06f * sin(nx * 7f) - 0.04f * sin(nx * 17f + 1f)))
                x += size.width / 50f
            }
            lineTo(size.width, size.height)
            close()
        }
        drawPath(ridge, Color(0xFF01040A))
    }
    AmbientTime(ctx)
}

@Composable
fun GalaxyFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize()) { t ->
        drawRect(Color(0xFF05010D))
        fun nebula(cx: Float, cy: Float, r: Float, c: Color) = drawCircle(safeRadialGradient(listOf(c, Color.Transparent), Offset(cx, cy), r), r, Offset(cx, cy))
        nebula(size.width * (0.3f + 0.03f * sin(t * 0.05f)), size.height * 0.4f, size.minDimension * 0.7f, Color(0xFF5B2A86).copy(alpha = 0.45f))
        nebula(size.width * 0.75f, size.height * (0.6f + 0.03f * cos(t * 0.04f)), size.minDimension * 0.6f, Color(0xFF1B3B8F).copy(alpha = 0.4f))
        nebula(size.width * 0.6f, size.height * 0.25f, size.minDimension * 0.35f, Color(0xFFB0306B).copy(alpha = 0.25f))
        drawStars(140, 77, t / 6f)
        // Spiral galaxy
        val c = Offset(size.width * 0.5f, size.height * 0.5f)
        val rnd = Random(9)
        val spin = t * 1.5f
        for (i in 0 until 420) {
            val arm = i % 2
            val k = rnd.nextFloat()
            val theta = k * 3.2f * PI.toFloat() + arm * PI.toFloat() + spin * PI.toFloat() / 180f
            val r = size.minDimension * 0.04f * exp(k * 2.1f)
            val jitter = (rnd.nextFloat() - 0.5f) * size.minDimension * 0.03f
            val p = Offset(c.x + cos(theta) * r + jitter, c.y + sin(theta) * r * 0.45f + jitter * 0.5f)
            drawCircle(lerpColor(Color(0xFFFFE8C2), Color(0xFF9DB8FF), k).copy(alpha = 0.5f * (1f - k) + 0.1f), size.minDimension * 0.003f, p)
        }
        drawCircle(safeRadialGradient(listOf(Color(0xFFFFF0D0), Color(0xFFFFB35C).copy(alpha = 0.3f), Color.Transparent), c, size.minDimension * 0.08f), size.minDimension * 0.08f, c)
    }
    AmbientTime(ctx, alignment = Alignment.Start, widthFraction = 0.42f)
}

@Composable
fun OceanFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize()) { t ->
        drawRect(Brush.verticalGradient(listOf(Color(0xFF020A1A), Color(0xFF06264A))))
        val moon = Offset(size.width * 0.78f, size.height * 0.2f)
        drawCircle(safeRadialGradient(listOf(Color(0xFFE8F1FF).copy(alpha = 0.25f), Color.Transparent), moon, size.minDimension * 0.25f), size.minDimension * 0.25f, moon)
        drawCircle(Color(0xFFEAF2FF), size.minDimension * 0.05f, moon)
        val layers = listOf(Color(0xFF0A3D6B), Color(0xFF0B4F86), Color(0xFF0E62A3), Color(0xFF1677C2))
        layers.forEachIndexed { i, c ->
            val base = size.height * (0.55f + i * 0.11f)
            val amp = size.height * (0.02f + i * 0.008f)
            val speed = 0.4f + i * 0.25f
            val path = Path().apply {
                moveTo(0f, size.height)
                var x = 0f
                while (x <= size.width + 0.5f) {
                    val nx = x / size.width
                    lineTo(x, base + sin(nx * (6f + i) * PI.toFloat() / 2f + t * speed) * amp + sin(nx * 13f - t * speed * 1.3f) * amp * 0.4f)
                    x += size.width / 70f
                }
                lineTo(size.width, size.height)
                close()
            }
            drawPath(path, Brush.verticalGradient(listOf(c, Color(0xFF020A1A)), startY = base - amp, endY = size.height))
        }
        // Moonlight glitter
        val rnd = Random(4)
        for (i in 0 until 40) {
            val y = size.height * (0.58f + rnd.nextFloat() * 0.38f)
            val x = moon.x + (rnd.nextFloat() - 0.5f) * size.width * 0.12f * (y / size.height)
            val a = 0.5f + 0.5f * sin(t * 3f + i)
            drawLine(Color.White.copy(alpha = 0.35f * a), Offset(x - size.width * 0.012f, y), Offset(x + size.width * 0.012f, y), size.height * 0.003f)
        }
    }
    AmbientTime(ctx, alignment = Alignment.Start, widthFraction = 0.45f)
}

@Composable
fun ForestFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize()) { t ->
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0B1A14), Color(0xFF14301F), Color(0xFF0A140F))))
        drawStars(40, 3, t / 12f)
        val layerColors = listOf(Color(0xFF1C3A2A), Color(0xFF12291D), Color(0xFF081610))
        layerColors.forEachIndexed { layer, color ->
            val rnd = Random(30 + layer)
            val count = 9 + layer * 4
            val baseY = size.height * (0.62f + layer * 0.12f)
            for (i in 0 until count) {
                val x = (i + rnd.nextFloat() * 0.6f) / count * size.width
                val h = size.height * (0.28f + rnd.nextFloat() * 0.18f + layer * 0.06f)
                val w = h * 0.36f
                val tree = Path().apply {
                    moveTo(x, baseY - h)
                    for (tier in 0 until 3) {
                        val ty = baseY - h + h * (tier + 1) * 0.3f
                        val tw = w * (0.45f + tier * 0.28f)
                        lineTo(x + tw, ty)
                        lineTo(x + tw * 0.4f, ty)
                    }
                    lineTo(x + w * 0.1f, baseY)
                    lineTo(x - w * 0.1f, baseY)
                    for (tier in 2 downTo 0) {
                        val ty = baseY - h + h * (tier + 1) * 0.3f
                        val tw = w * (0.45f + tier * 0.28f)
                        lineTo(x - tw * 0.4f, ty)
                        lineTo(x - tw, ty)
                    }
                    close()
                }
                drawPath(tree, color)
            }
            drawRect(color, Offset(0f, baseY), Size(size.width, size.height - baseY))
            // Fog between layers
            drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color(0xFF9FD8B5).copy(alpha = 0.05f), Color.Transparent), startY = baseY - size.height * 0.1f, endY = baseY + size.height * 0.05f))
        }
        val rnd = Random(12)
        for (i in 0 until 22) {
            val px = (rnd.nextFloat() + sin(t * 0.2f + i) * 0.03f) * size.width
            val py = (0.45f + rnd.nextFloat() * 0.5f + cos(t * 0.3f + i * 1.7f) * 0.03f) * size.height
            val a = (0.5f + 0.5f * sin(t * (1.5f + rnd.nextFloat()) + i)).coerceIn(0f, 1f)
            drawCircle(Color(0xFFDFFF7A).copy(alpha = 0.25f * a), size.minDimension * 0.02f, Offset(px, py))
            drawCircle(Color(0xFFF4FFB8).copy(alpha = a), size.minDimension * 0.005f, Offset(px, py))
        }
    }
    AmbientTime(ctx, color = Color(0xFFE6F5EA))
}

@Composable
fun MountainFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    val hour = ctx.time.hour + ctx.time.minute / 60f
    val (skyTop, skyBottom, tint) = when {
        hour < 5f || hour >= 21f -> Triple(Color(0xFF070B1D), Color(0xFF1B1F3B), Color(0xFF2A2F55))
        hour < 8f -> Triple(Color(0xFF2B2A5E), Color(0xFFFF9E7A), Color(0xFF6A4C7A))
        hour < 17f -> Triple(Color(0xFF3C7DD9), Color(0xFFBEE0FF), Color(0xFF5A7FA8))
        else -> Triple(Color(0xFF3A2357), Color(0xFFFF8155), Color(0xFF7A4A6A))
    }
    AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 100L) { t ->
        drawRect(Brush.verticalGradient(listOf(skyTop, skyBottom)))
        val dayF = ((hour - 6f) / 12f).coerceIn(0f, 1f)
        val isDay = hour in 6f..18f
        val body = Offset(size.width * (0.15f + 0.7f * dayF), size.height * (0.55f - 0.35f * sin(dayF * PI.toFloat())))
        if (isDay) {
            drawCircle(Color(0xFFFFE9B0).copy(alpha = 0.25f), size.minDimension * 0.16f, body)
            drawCircle(Color(0xFFFFF4D6), size.minDimension * 0.06f, body)
        } else {
            drawStars(60, 15, t / 10f)
            drawMoon(Offset(size.width * 0.75f, size.height * 0.22f), size.minDimension * 0.05f, com.standbypro.astro.Astronomy.moonPhase().fraction)
        }
        for (layer in 0 until 4) {
            val color = lerpColor(tint, Color(0xFF05060C), layer / 3.5f)
            val base = size.height * (0.55f + layer * 0.11f)
            val rnd = Random(layer * 7 + 1)
            val f1 = 2f + rnd.nextFloat() * 3f
            val f2 = 6f + rnd.nextFloat() * 6f
            val ph = rnd.nextFloat() * TAU
            val ridge = Path().apply {
                moveTo(0f, size.height)
                var x = 0f
                while (x <= size.width + 0.5f) {
                    val nx = x / size.width
                    val y = base - (abs(sin(nx * f1 + ph)) * 0.16f + abs(sin(nx * f2 + ph * 2f)) * 0.05f) * size.height * (1.2f - layer * 0.2f)
                    lineTo(x, y)
                    x += size.width / 80f
                }
                lineTo(size.width, size.height)
                close()
            }
            drawPath(ridge, color)
        }
    }
    AmbientTime(ctx, alignment = Alignment.End, widthFraction = 0.45f)
}

/** Bauhaus composition where shapes encode the time: wedge = hour, bar = minutes, square = seconds. */
@Composable
fun GeometricFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val clock = rememberFaceClock(ctx)
    Row(modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(20.dp))) {
            val s = clock.value
            drawRect(Color(0xFFF2E8D5))
            val unit = size.minDimension
            val c = Offset(size.width * 0.42f, size.height * 0.45f)
            val r = unit * 0.36f
            drawCircle(Color(0xFF1D3557), r, c)
            val hourFrac = (s / 3600f % 12f) / 12f
            drawArc(Color(0xFFE63946), -90f, 360f * hourFrac, true, Offset(c.x - r, c.y - r), Size(r * 2, r * 2))
            val minFrac = (s / 60f % 60f) / 60f
            drawRect(Color(0xFF1D1D1D), Offset(size.width * 0.08f, size.height * 0.86f), Size(size.width * 0.84f, size.height * 0.05f))
            drawRect(Color(0xFFFFB703), Offset(size.width * 0.08f, size.height * 0.86f), Size(size.width * 0.84f * minFrac, size.height * 0.05f))
            val sq = Offset(size.width * 0.8f, size.height * 0.24f)
            rotate((s % 60f) * 6f, sq) {
                drawRect(Color(0xFF2A9D8F), Offset(sq.x - unit * 0.07f, sq.y - unit * 0.07f), Size(unit * 0.14f, unit * 0.14f))
            }
            drawLine(Color(0xFF1D1D1D), Offset(size.width * 0.75f, 0f), Offset(size.width * 0.75f, size.height * 0.75f), unit * 0.012f)
        }
        Column(Modifier.weight(0.7f).fillMaxHeight().padding(start = 14.dp), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.displayHour.padStart(2, '0'), clockStyle(FontWeight.Black, FaceFonts.CondensedBold), Modifier.fillMaxWidth().weight(0.35f), color = Color(0xFFE63946), sizingText = "88", alignment = Alignment.CenterStart)
            AutoSizeText(ctx.minuteText, clockStyle(FontWeight.Black, FaceFonts.CondensedBold), Modifier.fillMaxWidth().weight(0.35f), color = Color(0xFFFFB703), sizingText = "88", alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("EEE dd MMM").uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.08f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
        }
    }
}

/** Synthwave: striped sunset over an endless neon grid. */
@Composable
fun NeonAmbientFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize()) { t ->
        val horizon = size.height * 0.58f
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0B0221), Color(0xFF2A0845), Color(0xFF6A0D5F)), endY = horizon))
        drawStars(50, 8, t / 8f)
        val sunC = Offset(size.width / 2f, horizon)
        val sunR = size.minDimension * 0.28f
        clipPath(Path().apply { addRect(androidx.compose.ui.geometry.Rect(0f, 0f, size.width, horizon)) }) {
            drawCircle(Brush.verticalGradient(listOf(Color(0xFFFFE259), Color(0xFFFF4E8E)), startY = sunC.y - sunR, endY = sunC.y), sunR, sunC)
            for (i in 0 until 6) {
                val y = sunC.y - sunR * 0.1f - i * sunR * 0.13f
                drawRect(Color(0xFF2A0845), Offset(sunC.x - sunR, y), Size(sunR * 2, sunR * (0.06f - i * 0.008f).coerceAtLeast(0.01f)))
            }
        }
        drawRect(Brush.verticalGradient(listOf(Color(0xFF1A0230), Color(0xFF05000D)), startY = horizon), Offset(0f, horizon), Size(size.width, size.height - horizon))
        val grid = Color(0xFFFF2BD6)
        val scroll = (t * 0.6f) % 1f
        for (i in 0 until 14) {
            val z = (i + scroll) / 14f
            val y = horizon + (size.height - horizon) * z * z
            drawLine(grid.copy(alpha = 0.25f + 0.6f * z), Offset(0f, y), Offset(size.width, y), size.height * 0.004f)
        }
        for (i in -12..12) {
            val xb = size.width / 2f + i * size.width * 0.12f
            drawLine(grid.copy(alpha = 0.55f), Offset(size.width / 2f + i * size.width * 0.015f, horizon), Offset(xb, size.height), size.height * 0.004f)
        }
    }
    AmbientTime(ctx, color = Color(0xFFFFF0FB), dateColor = Color(0xFF6BF7FF), weight = FontWeight.Medium)
}

@Composable
fun ZenFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 40L) { t ->
        drawRect(safeRadialGradient(listOf(Color(0xFF2A2B2E), Color(0xFF111214)), center, size.maxDimension * 0.7f))
        // Raked sand lines
        for (i in 0 until 26) {
            val y = size.height * (i + 0.5f) / 26f
            val path = Path().apply {
                moveTo(0f, y)
                var x = 0f
                while (x <= size.width + 0.5f) {
                    lineTo(x, y + sin(x / size.width * 6f + i * 0.4f) * size.height * 0.006f)
                    x += size.width / 40f
                }
            }
            drawPath(path, Color.White.copy(alpha = 0.035f), style = Stroke(size.height * 0.004f))
        }
        // Ripples
        val c = Offset(size.width * 0.72f, size.height * 0.62f)
        for (k in 0 until 5) {
            val f = ((t / 8f) + k / 5f) % 1f
            drawCircle(Color.White.copy(alpha = 0.18f * (1f - f)), size.minDimension * (0.05f + 0.55f * f), c, style = Stroke(size.minDimension * 0.004f))
        }
        drawCircle(Color(0xFF3B3C40), size.minDimension * 0.05f, c)
        drawCircle(Color(0xFF55565B), size.minDimension * 0.045f, Offset(c.x - size.minDimension * 0.008f, c.y - size.minDimension * 0.008f))
    }
    AmbientTime(ctx, alignment = Alignment.Start, widthFraction = 0.5f, color = Color(0xFFEDEBE6), shadow = false)
}

/** Ensō brush circle that completes over the hour, with a hanko seal for the hour. */
@Composable
fun JapaneseFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    val clock = rememberFaceClock(ctx)
    val kanjiHour = listOf("〇", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十", "十一", "十二")[ctx.hour12]
    Row(modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.weight(1f).fillMaxHeight()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension * 0.38f
            val minuteFrac = (clock.value / 60f % 60f) / 60f
            val sweep = 20f + 330f * minuteFrac
            val steps = (sweep / 2f).toInt().coerceAtLeast(2)
            for (i in 0 until steps) {
                val f = i / steps.toFloat()
                val a = (-60f + f * sweep) * PI.toFloat() / 180f
                val width = r * (0.16f * sin(f * PI.toFloat()).coerceAtLeast(0.25f) + 0.02f * sin(i * 1.3f))
                val p = Offset(c.x + cos(a) * r, c.y + sin(a) * r)
                drawCircle(Color(0xFFF1ECE2).copy(alpha = 0.92f - 0.25f * f), width / 2f, p)
            }
            val seal = Offset(c.x + r * 0.95f, c.y + r * 0.95f)
            drawRoundRect(Color(0xFFC0292C), Offset(seal.x - r * 0.2f, seal.y - r * 0.2f), Size(r * 0.4f, r * 0.4f), CornerRadius(r * 0.04f))
            textAt(m, kanjiHour, seal, TextStyle(fontFamily = FaceFonts.Serif, fontWeight = FontWeight.Bold, fontSize = (r * if (kanjiHour.length > 1) 0.16f else 0.24f).toSp()), Color(0xFFF7E9E0))
        }
        Column(Modifier.weight(0.6f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.timeText, TextStyle(fontFamily = FaceFonts.Serif, fontWeight = FontWeight.Light), Modifier.fillMaxWidth().weight(0.3f), color = Color(0xFFF1ECE2), sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("MMMM d").lowercase(), TextStyle(fontFamily = FaceFonts.SerifItalic), Modifier.fillMaxWidth().weight(0.1f), color = Color(0xFFC0292C), alignment = Alignment.CenterStart)
        }
    }
}

@Composable
fun MinimalGradientFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 66L) { t ->
        val breathe = 0.5f + 0.5f * sin(t * TAU / 10f)
        drawRect(Color.Black)
        drawCircle(safeRadialGradient(listOf(ctx.accent.copy(alpha = 0.10f + 0.08f * breathe), Color.Transparent), center, size.maxDimension * (0.45f + 0.05f * breathe)), size.maxDimension, center)
    }
    AmbientTime(ctx, weight = FontWeight.Thin, shadow = false, color = Ink.TextPrimary, dateColor = Ink.TextTertiary)
}

@Composable
fun LavaLampFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize()) { t ->
        drawRect(Brush.verticalGradient(listOf(Color(0xFF1A0505), Color(0xFF3A0A12), Color(0xFF1A0505))))
        val rnd = Random(6)
        for (i in 0 until 9) {
            val speed = 0.02f + rnd.nextFloat() * 0.03f
            val phase = rnd.nextFloat()
            val y = (0.5f + 0.48f * sin((t * speed + phase) * TAU)) * size.height
            val x = size.width * (0.15f + rnd.nextFloat() * 0.7f) + sin(t * 0.1f + i) * size.width * 0.05f
            val r = size.minDimension * (0.12f + rnd.nextFloat() * 0.12f)
            val col = if (i % 2 == 0) Color(0xFFFF5E3A) else Color(0xFFFF2D55)
            drawCircle(safeRadialGradient(listOf(col.copy(alpha = 0.85f), col.copy(alpha = 0.35f), Color.Transparent), Offset(x, y), r), r, Offset(x, y), blendMode = BlendMode.Plus)
        }
    }
    AmbientTime(ctx, color = Color(0xFFFFEDE6), dateColor = Color(0xFFFFB199))
}

/** Orbit clock: hour, minute and second bodies circling with fading trails. */
@Composable
fun OrbitsFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 8)
    val timeText = ctx.timeText
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            listOf(0.42f, 0.66f, 0.9f).forEach { drawCircle(Color.White.copy(alpha = 0.08f), d.px(it), d.center, style = Stroke(d.px(0.006f))) }
            textAt(m, timeText, d.center, numeralStyle(d, 0.17f, FontWeight.Light), Ink.TextPrimary)
        },
        handsLayer = { d, a ->
            fun body(angle: Float, r: Float, size: Float, color: Color, trail: Float) {
                val steps = 18
                for (i in steps downTo 1) {
                    drawCircle(color.copy(alpha = 0.25f * (1f - i / steps.toFloat())), d.px(size * 0.5f), d.point(angle - trail * i / steps, r))
                }
                drawCircle(color.copy(alpha = 0.25f), d.px(size * 1.8f), d.point(angle, r))
                drawCircle(color, d.px(size), d.point(angle, r))
            }
            body(a.hour, 0.42f, 0.05f, ctx.accent, 20f)
            body(a.minute, 0.66f, 0.038f, Ink.Cyan, 30f)
            if (ctx.showSeconds) body(a.second, 0.9f, 0.026f, Color.White, 40f)
        }
    )
}

@Composable
fun RadarFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val green = Color(0xFF3BFF9E)
    Box(modifier.fillMaxSize()) {
        AnalogFace(
            ctx, Modifier.fillMaxSize(),
            motion = com.standbypro.ui.design.SecondsMotion.SWEEP,
            dialLayer = { d ->
                drawCircle(Color(0xFF02140B), d.radius, d.center)
                for (k in 1..4) drawCircle(green.copy(alpha = 0.18f), d.px(k / 4f), d.center, style = Stroke(d.px(0.005f)))
                drawLine(green.copy(alpha = 0.18f), d.point(0f, 1f), d.point(180f, 1f), d.px(0.005f))
                drawLine(green.copy(alpha = 0.18f), d.point(90f, 1f), d.point(270f, 1f), d.px(0.005f))
                for (i in 0 until 72) drawLine(green.copy(alpha = 0.35f), d.point(i * 5f, 0.95f), d.point(i * 5f, 1f), d.px(0.005f))
            },
            handsLayer = { d, a ->
                val sweep = (a.second * 2f) % 360f
                rotate(sweep - 90f, d.center) {
                    drawArc(Brush.sweepGradient(listOf(Color.Transparent, Color.Transparent, green.copy(alpha = 0.0f), green.copy(alpha = 0.45f)), d.center), -60f, 60f, true, Offset(d.center.x - d.radius, d.center.y - d.radius), Size(d.radius * 2, d.radius * 2))
                }
                drawLine(green, d.center, d.point(sweep, 1f), d.px(0.01f))
                val rnd = Random(ctx.time.minute)
                for (i in 0 until 6) {
                    val ang = rnd.nextFloat() * 360f
                    val dist = 0.2f + rnd.nextFloat() * 0.7f
                    val age = ((sweep - ang) + 360f) % 360f / 360f
                    drawCircle(green.copy(alpha = (1f - age).coerceIn(0f, 1f)), d.px(0.025f), d.point(ang, dist))
                }
                // Hour and minute "contacts"
                drawCircle(Color.White, d.px(0.035f), d.point(a.hour, 0.45f))
                drawCircle(green, d.px(0.03f), d.point(a.minute, 0.8f))
            }
        )
        Column(Modifier.align(Alignment.BottomStart).padding(14.dp).fillMaxWidth(0.28f).fillMaxHeight(0.2f)) {
            AutoSizeText(ctx.timeText, TextStyle(fontFamily = FaceFonts.Mono, fontWeight = FontWeight.Bold), Modifier.fillMaxWidth().weight(0.7f), color = green, alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("dd MMM").uppercase(), TextStyle(fontFamily = FaceFonts.Mono), Modifier.fillMaxWidth().weight(0.3f), color = green.copy(alpha = 0.6f), alignment = Alignment.CenterStart)
        }
    }
}

/** The Fibonacci clock (red = hours, green = minutes, blue = both). */
@Composable
fun FibonacciFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val colors = remember(ctx.time.hour, ctx.time.minute) { FaceMath.fibonacciColors(ctx.time.hour, ctx.time.minute, ctx.time.minute) }
    val palette = listOf(Color(0xFFF4F1EA), Color(0xFFE63946), Color(0xFF52B788), Color(0xFF3A86FF))
    // (x, y, size) in an 8x5 grid for squares 1,1,2,3,5
    val layout = listOf(Triple(2, 0, 1), Triple(2, 1, 1), Triple(0, 0, 2), Triple(0, 2, 3), Triple(3, 0, 5))
    Row(modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Canvas(Modifier.weight(1.4f).fillMaxHeight()) {
            val unit = minOf(size.width / 8f, size.height / 5f)
            val left = (size.width - unit * 8f) / 2f
            val top = (size.height - unit * 5f) / 2f
            val gap = unit * 0.06f
            layout.forEachIndexed { i, (x, y, s) ->
                drawRoundRect(
                    palette[colors[i]],
                    Offset(left + x * unit + gap, top + y * unit + gap),
                    Size(s * unit - gap * 2, s * unit - gap * 2),
                    CornerRadius(unit * 0.12f)
                )
            }
        }
        Column(Modifier.weight(0.6f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
            listOf("Hours" to palette[1], "Minutes" to palette[2], "Both" to palette[3]).forEach { (l, c) ->
                AutoSizeText("■ $l", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.08f), color = c, alignment = Alignment.CenterStart)
            }
            Spacer(Modifier.weight(0.05f))
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.2f), color = Ink.TextTertiary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
        }
    }
}

@Composable
fun PendulumFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 16L) { t ->
        drawRect(Brush.verticalGradient(listOf(Color(0xFF1A140E), Color(0xFF0B0907))))
        val pivot = Offset(size.width * 0.78f, size.height * 0.04f)
        val len = size.height * 0.78f
        val angle = sin(t * PI.toFloat()) * 18f // 2 s period, beats seconds
        rotate(angle, pivot) {
            drawLine(Brush.horizontalGradient(listOf(Color(0xFF8C6A1F), Ink.GoldLight, Color(0xFF8C6A1F)), pivot.x - size.width * 0.006f, pivot.x + size.width * 0.006f), pivot, Offset(pivot.x, pivot.y + len), size.width * 0.012f)
            val bob = Offset(pivot.x, pivot.y + len)
            drawCircle(safeRadialGradient(listOf(Color(0xFFFFE9A8), Ink.Gold, Color(0xFF6B4E14)), Offset(bob.x - size.minDimension * 0.03f, bob.y - size.minDimension * 0.03f), size.minDimension * 0.14f), size.minDimension * 0.1f, bob)
        }
        drawCircle(Ink.Gold, size.minDimension * 0.015f, pivot)
    }
    AmbientTime(ctx, alignment = Alignment.Start, widthFraction = 0.55f, color = Ink.Champagne, dateColor = Ink.Gold)
}

/** Hourglass whose sand tracks the current hour: top = remaining, bottom = elapsed. */
@Composable
fun HourglassFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(0.8f).fillMaxHeight()) {
            AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 33L) { t ->
                val elapsed = (t % 3600f) / 3600f
                val w = minOf(size.width * 0.8f, size.height * 0.5f)
                val h = w * 1.7f
                val cx = size.width / 2f
                val top = (size.height - h) / 2f
                val neck = w * 0.06f
                val glass = Path().apply {
                    moveTo(cx - w / 2, top)
                    lineTo(cx + w / 2, top)
                    cubicTo(cx + w / 2, top + h * 0.3f, cx + neck, top + h * 0.42f, cx + neck, top + h / 2)
                    cubicTo(cx + neck, top + h * 0.58f, cx + w / 2, top + h * 0.7f, cx + w / 2, top + h)
                    lineTo(cx - w / 2, top + h)
                    cubicTo(cx - w / 2, top + h * 0.7f, cx - neck, top + h * 0.58f, cx - neck, top + h / 2)
                    cubicTo(cx - neck, top + h * 0.42f, cx - w / 2, top + h * 0.3f, cx - w / 2, top)
                    close()
                }
                val sand = Color(0xFFE9C46A)
                clipPath(glass) {
                    // Top sand: remaining, surface drops toward the neck
                    val sandTop = top + (h * 0.5f) * elapsed
                    drawRect(sand, Offset(cx - w, sandTop), Size(w * 2, top + h / 2 - sandTop))
                    // Bottom pile
                    val pileH = h * 0.5f * elapsed
                    val pile = Path().apply {
                        moveTo(cx - w, top + h)
                        lineTo(cx - w, top + h - pileH * 0.6f)
                        quadraticTo(cx, top + h - pileH * 1.25f, cx + w, top + h - pileH * 0.6f)
                        lineTo(cx + w, top + h)
                        close()
                    }
                    drawPath(pile, sand)
                    // Falling stream
                    if (elapsed < 0.999f) {
                        drawLine(sand, Offset(cx, top + h / 2), Offset(cx, top + h - pileH * 0.9f), w * 0.015f)
                        val rnd = Random(2)
                        for (i in 0 until 8) {
                            val f = (rnd.nextFloat() + t * 1.7f) % 1f
                            drawCircle(sand, w * 0.01f, Offset(cx + (rnd.nextFloat() - 0.5f) * w * 0.02f, top + h / 2 + f * (h / 2 - pileH * 0.9f)))
                        }
                    }
                }
                drawPath(glass, Color.White.copy(alpha = 0.06f))
                drawPath(glass, Color.White.copy(alpha = 0.35f), style = Stroke(w * 0.015f))
                drawRoundRect(Color(0xFF6B4423), Offset(cx - w * 0.62f, top - w * 0.08f), Size(w * 1.24f, w * 0.08f), CornerRadius(w * 0.02f))
                drawRoundRect(Color(0xFF6B4423), Offset(cx - w * 0.62f, top + h), Size(w * 1.24f, w * 0.08f), CornerRadius(w * 0.02f))
            }
        }
        Column(Modifier.weight(1.2f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.36f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            AutoSizeText("${60 - ctx.time.minute} minutes left this hour", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.08f), color = Color(0xFFE9C46A), alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("EEEE, MMMM d").uppercase(), labelStyle(), Modifier.fillMaxWidth().weight(0.07f), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
        }
    }
}

/** Water fills the frame as the hour progresses; the hour floats inside. */
@Composable
fun TideFace(ctx: FaceContext, modifier: Modifier = Modifier) = FullBleed(modifier) {
    val m = rememberTextMeasurer(cacheSize = 8)
    val hourText = ctx.displayHour.padStart(2, '0')
    val minuteText = ctx.minuteText
    AnimatedCanvas(ctx, Modifier.fillMaxSize()) { t ->
        drawRect(Color(0xFF05070C))
        val level = size.height * (1f - (t % 3600f) / 3600f * 0.9f - 0.05f)
        val style = TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.Black, fontSize = (size.height * 0.5f).toSp())
        textAt(m, hourText, Offset(size.width * 0.32f, size.height * 0.5f), style, Color.White.copy(alpha = 0.12f))
        val wave = Path().apply {
            moveTo(0f, size.height)
            var x = 0f
            while (x <= size.width + 0.5f) {
                val nx = x / size.width
                lineTo(x, level + sin(nx * 9f + t * 1.2f) * size.height * 0.012f + sin(nx * 4f - t * 0.7f) * size.height * 0.01f)
                x += size.width / 60f
            }
            lineTo(size.width, size.height)
            close()
        }
        clipPath(wave) {
            drawRect(Brush.verticalGradient(listOf(ctx.accent.copy(alpha = 0.9f), lerpColor(ctx.accent, Color.Black, 0.7f)), startY = level, endY = size.height))
            textAt(m, hourText, Offset(size.width * 0.32f, size.height * 0.5f), style, Color.White.copy(alpha = 0.92f))
            val rnd = Random(1)
            for (i in 0 until 14) {
                val x = rnd.nextFloat() * size.width
                val y = size.height - ((rnd.nextFloat() + t * (0.03f + rnd.nextFloat() * 0.05f)) % 1f) * (size.height - level)
                drawCircle(Color.White.copy(alpha = 0.35f), size.minDimension * (0.005f + rnd.nextFloat() * 0.01f), Offset(x, y), style = Stroke(size.minDimension * 0.002f))
            }
        }
        textAt(m, ":$minuteText", Offset(size.width * 0.74f, size.height * 0.5f), style.copy(fontWeight = FontWeight.Light, fontSize = (size.height * 0.2f).toSp()), Color.White.copy(alpha = 0.85f))
    }
}

/** 24-hour spiral: two turns from midnight (centre) to midnight (rim). */
@Composable
fun SpiralFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val timeText = ctx.timeText
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            val path = Path()
            val steps = 400
            for (i in 0..steps) {
                val f = i / steps.toFloat()
                val angle = f * 720f
                val r = 0.2f + 0.75f * f
                val p = d.point(angle, r)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, Color.White.copy(alpha = 0.14f), style = Stroke(d.px(0.03f), cap = StrokeCap.Round))
            for (h in 0 until 24 step 3) {
                val f = h / 24f
                textAt(m, h.toString(), d.point(f * 720f, 0.2f + 0.75f * f + 0.07f), numeralStyle(d, 0.05f, FontWeight.SemiBold), Ink.TextTertiary)
            }
            textAt(m, timeText, d.center, numeralStyle(d, 0.11f, FontWeight.Light), Ink.TextPrimary)
        },
        handsLayer = { d, a ->
            val f = a.hour24 / 360f
            val path = Path()
            val steps = (400 * f).toInt().coerceAtLeast(2)
            for (i in 0..steps) {
                val ff = f * i / steps
                val p = d.point(ff * 720f, 0.2f + 0.75f * ff)
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            drawPath(path, Brush.sweepGradient(listOf(ctx.accent.copy(alpha = 0.4f), ctx.accent), d.center), style = Stroke(d.px(0.03f), cap = StrokeCap.Round))
            val head = d.point(f * 720f, 0.2f + 0.75f * f)
            drawCircle(ctx.accent.copy(alpha = 0.3f), d.px(0.07f), head)
            drawCircle(Color.White, d.px(0.03f), head)
        }
    )
}
