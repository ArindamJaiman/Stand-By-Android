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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.standbypro.astro.Astronomy
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.safeRadialGradient
import com.standbypro.ui.design.textAt
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

@Composable
fun MoonFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val phase = remember(ctx.time.hour) { Astronomy.moonPhase() }
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
    val nextFull = remember(ctx.time.toLocalDate()) { Instant.ofEpochMilli(Astronomy.nextPhase(0.5)).atZone(ZoneId.systemDefault()).format(fmt) }
    val nextNew = remember(ctx.time.toLocalDate()) { Instant.ofEpochMilli(Astronomy.nextPhase(0.0)).atZone(ZoneId.systemDefault()).format(fmt) }
    Row(modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 100L) { t ->
                drawStars(40, 21, t / 8f)
                val r = size.minDimension * 0.4f
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(safeRadialGradient(listOf(Color(0xFFF1EAD2).copy(alpha = 0.18f), Color.Transparent), c, r * 1.6f), r * 1.6f, c)
                drawMoon(c, r, phase.fraction)
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight().padding(start = 8.dp), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            AutoSizeText(phase.name, clockStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.13f), color = Ink.Champagne, sizingText = "Waxing Crescent", alignment = Alignment.CenterStart)
            AutoSizeText("${(phase.illumination * 100).toInt()}% illuminated · ${"%.1f".format(phase.ageDays)} days", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.08f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
            Spacer(Modifier.weight(0.05f))
            AutoSizeText("FULL  $nextFull", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.8f).weight(0.07f), color = Ink.TextPrimary, alignment = Alignment.CenterStart)
            AutoSizeText("NEW   $nextNew", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.8f).weight(0.07f), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
        }
    }
}

/** Orrery: planets at their real heliocentric longitudes today (orbits √-scaled to fit). */
@Composable
fun OrreryFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    val dayKey = ctx.time.toLocalDate().toEpochDay() * 24 + ctx.time.hour
    val longitudes = remember(dayKey) { Astronomy.planets.map { Astronomy.meanLongitude(it, System.currentTimeMillis()) } }
    Box(modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize().padding(6.dp)) {
            drawStars(60, 99, 0.2f, maxRadius = size.minDimension * 0.004f)
            val c = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension / 2f * 0.95f
            val maxAu = sqrt(Astronomy.planets.last().orbitAu)
            drawCircle(safeRadialGradient(listOf(Color(0xFFFFE08A), Color(0xFFFF9F0A).copy(alpha = 0.4f), Color.Transparent), c, maxR * 0.14f), maxR * 0.14f, c)
            drawCircle(Color(0xFFFFE6A0), maxR * 0.05f, c)
            Astronomy.planets.forEachIndexed { i, p ->
                val r = (maxR * 0.14f) + (maxR * 0.86f) * (sqrt(p.orbitAu) / maxAu).toFloat() * 0.98f
                drawCircle(Color.White.copy(alpha = 0.09f), r, c, style = Stroke(1.2f))
                val a = (-longitudes[i] * PI / 180.0).toFloat()
                val pos = Offset(c.x + cos(a) * r, c.y + sin(a) * r)
                val pr = maxR * 0.022f * p.sizeScale
                if (p.name == "Saturn") {
                    rotate(-25f, pos) {
                        drawOval(Color(0xFFE6D3A3).copy(alpha = 0.7f), Offset(pos.x - pr * 2.2f, pos.y - pr * 0.7f), androidx.compose.ui.geometry.Size(pr * 4.4f, pr * 1.4f), style = Stroke(pr * 0.3f))
                    }
                }
                drawCircle(Color(p.colorArgb), pr, pos)
                if (p.name == "Earth") drawCircle(Color(0xFF4FA3FF).copy(alpha = 0.3f), pr * 2.2f, pos)
                textAt(m, p.name.uppercase(), Offset(pos.x, pos.y + pr + maxR * 0.035f), TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.SemiBold, fontSize = (maxR * 0.03f).toSp()), Color.White.copy(alpha = 0.55f))
            }
        }
        Column(Modifier.align(Alignment.TopStart).padding(14.dp).fillMaxWidth(0.3f).fillMaxHeight(0.22f)) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.7f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("d MMM yyyy").uppercase(), labelStyle(), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
        }
    }
}

/** Slowly turning star field around the celestial pole with the occasional meteor. */
@Composable
fun NightSkyFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize()) {
        AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 50L) { t ->
            drawRect(Brush.verticalGradient(listOf(Color(0xFF02030A), Color(0xFF0A1030), Color(0xFF1B1840))))
            val pole = Offset(size.width * 0.5f, size.height * 0.95f)
            val rot = (t / 240f) * 15f // sidereal-ish: 15° per hour, sped up 15x
            rotate(-rot, pole) {
                val rnd = Random(42)
                val maxR = size.maxDimension * 1.1f
                for (i in 0 until 180) {
                    val ang = rnd.nextFloat() * 2f * PI.toFloat()
                    val r = sqrt(rnd.nextFloat()) * maxR
                    val p = Offset(pole.x + cos(ang) * r, pole.y + sin(ang) * r)
                    val b = rnd.nextFloat()
                    val tw = 0.65f + 0.35f * sin(t * (1f + b * 3f) + i)
                    drawCircle(Color.White.copy(alpha = (0.2f + 0.8f * b) * tw), size.minDimension * (0.0015f + b * 0.003f), p)
                }
            }
            // Meteor every ~23 seconds
            val cycle = t % 23f
            if (cycle < 0.9f) {
                val k = cycle / 0.9f
                val start = Offset(size.width * 0.15f, size.height * 0.12f)
                val end = Offset(size.width * 0.55f, size.height * 0.42f)
                val head = start + (end - start) * k
                val tail = start + (end - start) * (k - 0.25f).coerceAtLeast(0f)
                drawLine(Brush.linearGradient(listOf(Color.Transparent, Color.White), tail, head), tail, head, size.minDimension * 0.004f)
            }
            // Horizon silhouette
            drawRect(Color.Black, Offset(0f, size.height * 0.9f), androidx.compose.ui.geometry.Size(size.width, size.height * 0.1f))
        }
        Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(0.25f))
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.ExtraLight), Modifier.fillMaxWidth(0.7f).weight(0.35f), color = Ink.TextPrimary, sizingText = ctx.sizingTime())
            AutoSizeText(ctx.formatDate("EEEE · MMMM d").uppercase(), labelStyle(), Modifier.fillMaxWidth(0.5f).weight(0.07f), color = Ink.TextSecondary)
            Spacer(Modifier.weight(0.33f))
        }
    }
}
