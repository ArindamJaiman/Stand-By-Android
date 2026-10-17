package com.standbypro.ui.faces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.standbypro.domain.BatteryHealth
import com.standbypro.domain.ChargeType
import com.standbypro.domain.ChargingState
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.RingGauge
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import kotlin.math.sin
import kotlin.random.Random

internal fun batteryColor(pct: Int): Color = when {
    pct < 15 -> Ink.Red
    pct < 35 -> Ink.Orange
    pct < 80 -> Ink.Green
    else -> Ink.Mint
}

internal fun ChargingState.sourceLabel(): String = when {
    !isCharging -> "On battery"
    chargeType == ChargeType.WIRELESS -> "Wireless charging"
    chargeType == ChargeType.USB -> "USB charging"
    else -> "Fast charging"
}

internal fun ChargingState.timeToFullLabel(): String? = chargeTimeRemainingMs?.let { ms ->
    val minutes = ms / 60_000
    if (minutes <= 0) null else if (minutes >= 60) "Full in ${minutes / 60}h ${minutes % 60}m" else "Full in ${minutes}m"
}

/** Liquid orb that fills to the battery level with rising bubbles while charging. */
@Composable
fun ChargingOrbFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val b = ctx.battery
    val color = batteryColor(b.batteryPercent)
    Row(modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
            AnimatedCanvas(ctx, Modifier.fillMaxSize()) { t ->
                val r = size.minDimension * 0.44f
                val c = Offset(size.width / 2f, size.height / 2f)
                drawCircle(color.copy(alpha = 0.12f), r * 1.12f, c)
                drawCircle(Color(0xFF0B0B0D), r, c)
                val level = c.y + r - 2 * r * (b.batteryPercent / 100f)
                val circle = Path().apply { addOval(androidx.compose.ui.geometry.Rect(c, r)) }
                clipPath(circle) {
                    for (layer in 0 until 2) {
                        val wave = Path().apply {
                            moveTo(c.x - r, c.y + r)
                            var x = c.x - r
                            while (x <= c.x + r + 0.5f) {
                                val nx = (x - c.x) / r
                                lineTo(x, level + sin(nx * 4f + t * (1.6f + layer * 0.7f) + layer * 2f) * r * 0.035f)
                                x += r / 25f
                            }
                            lineTo(c.x + r, c.y + r)
                            close()
                        }
                        drawPath(wave, Brush.verticalGradient(listOf(color.copy(alpha = if (layer == 0) 0.55f else 0.9f), color.copy(alpha = 0.35f)), startY = level, endY = c.y + r))
                    }
                    if (b.isCharging) {
                        val rnd = Random(3)
                        for (i in 0 until 12) {
                            val bx = c.x + (rnd.nextFloat() - 0.5f) * r * 1.4f
                            val by = c.y + r - ((rnd.nextFloat() + t * (0.08f + rnd.nextFloat() * 0.08f)) % 1f) * (c.y + r - level)
                            drawCircle(Color.White.copy(alpha = 0.45f), r * (0.015f + rnd.nextFloat() * 0.02f), Offset(bx, by), style = Stroke(r * 0.006f))
                        }
                    }
                }
                drawCircle(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.35f), Color.Transparent), Offset(c.x - r, c.y - r), c), r, c, style = Stroke(r * 0.02f))
            }
            Column(Modifier.fillMaxSize(0.5f), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                AutoSizeText("${b.batteryPercent}%", clockStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().weight(0.7f), color = Color.White, sizingText = "100%")
                AutoSizeText(if (b.isCharging) "⚡ CHARGING" else "BATTERY", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.8f).weight(0.2f), color = Color.White.copy(alpha = 0.8f))
            }
        }
        Column(Modifier.weight(0.8f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.32f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            AutoSizeText(b.sourceLabel(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.08f), color = color, alignment = Alignment.CenterStart)
            AutoSizeText(b.timeToFullLabel() ?: ctx.formatDate("EEEE, MMM d"), labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.07f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
        }
    }
}

/** Large power ring with live battery telemetry. */
@Composable
fun ChargingRingFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val b = ctx.battery
    val color = batteryColor(b.batteryPercent)
    Row(modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        RingGauge(
            b.batteryPercent / 100f, color, Modifier.weight(1f).fillMaxHeight(),
            strokeFraction = 0.09f, startAngle = -90f, sweep = 360f,
            gradient = listOf(color.copy(alpha = 0.5f), color, color)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                AutoSizeText("${b.batteryPercent}", clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.7f), color = Ink.TextPrimary, sizingText = "100")
                AutoSizeText(if (b.isCharging) "% · CHARGING" else "% BATTERY", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.8f).weight(0.15f), color = color)
            }
        }
        Column(Modifier.weight(0.9f).fillMaxHeight().padding(start = 14.dp), verticalArrangement = Arrangement.SpaceEvenly) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(1.4f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            val rows = listOf(
                "Source" to b.sourceLabel(),
                "Temperature" to (b.temperatureCelsius?.let { "%.1f °C".format(it) } ?: "—"),
                "Health" to b.health.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }.let { if (b.health == BatteryHealth.UNKNOWN) "—" else it },
                "Voltage" to (b.voltageMv?.let { "%.2f V".format(it / 1000f) } ?: "—"),
                "Estimate" to (b.timeToFullLabel() ?: if (b.batteryPercent >= 100) "Fully charged" else "—")
            )
            rows.forEach { (k, v) ->
                Row(Modifier.fillMaxWidth().weight(0.5f), verticalAlignment = Alignment.CenterVertically) {
                    AutoSizeText(k.uppercase(), labelStyle(FontWeight.Bold), Modifier.weight(1f).fillMaxHeight(0.6f), color = Ink.TextTertiary, sizingText = "TEMPERATURE", alignment = Alignment.CenterStart)
                    AutoSizeText(v, labelStyle(FontWeight.SemiBold), Modifier.weight(1.2f).fillMaxHeight(0.6f), color = Ink.TextPrimary, sizingText = "Wireless charging", alignment = Alignment.CenterEnd)
                }
            }
        }
    }
}
