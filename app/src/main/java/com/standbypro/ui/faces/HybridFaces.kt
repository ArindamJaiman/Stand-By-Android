package com.standbypro.ui.faces

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.standbypro.astro.Astronomy
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.ComplicationCard
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.HandStyle
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.Metric
import com.standbypro.ui.design.RingGauge
import com.standbypro.ui.design.WeatherGlyph
import com.standbypro.ui.design.appliedIndices
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.dialDisc
import com.standbypro.ui.design.hand
import com.standbypro.ui.design.hub
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.numerals
import com.standbypro.ui.design.premiumCard
import com.standbypro.ui.design.progressArc
import com.standbypro.ui.design.textAt
import com.standbypro.ui.design.sunburst
import com.standbypro.ui.design.ticks
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HybridAnalogDigitalFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val lcdText = ctx.timeText + if (ctx.showSeconds) ":" + ctx.secondText else ""
    val dateText = ctx.formatDate("EEE dd").uppercase()
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF26282C), Color(0xFF0F1012), ring = Color(0xFF3A3D42))
            ticks(d, majorEvery = 5, outer = 0.96f, majorLength = 0.1f, minorLength = 0.035f, majorWidth = 0.022f, minorWidth = 0.006f)
            numerals(d, m, listOf("12", null, null, "3", null, null, "6", null, null, "9", null, null), 0.74f, numeralStyle(d, 0.14f, FontWeight.Medium), Ink.TextPrimary)
            fun lcd(center: Offset, text: String) {
                val w = d.px(0.62f)
                val h = d.px(0.17f)
                drawRoundRect(Color(0xFF8FA07E), Offset(center.x - w / 2, center.y - h / 2), Size(w, h), CornerRadius(d.px(0.03f)))
                drawRoundRect(Color(0xFF2B2E33), Offset(center.x - w / 2, center.y - h / 2), Size(w, h), CornerRadius(d.px(0.03f)), style = Stroke(d.px(0.015f)))
                textAt(m, text, center, TextStyle(fontFamily = FaceFonts.Mono, fontWeight = FontWeight.Bold, fontSize = (h * 0.62f).toSp()), Color(0xFF1C2418))
            }
            lcd(d.point(0f, 0.38f), lcdText)
            lcd(d.point(180f, 0.38f), dateText)
        },
        handsLayer = { d, a -> handSet(d, a, ctx, style = HandStyle.BATON, hourWidth = 0.065f, minuteWidth = 0.045f, lume = Ink.Lume) }
    )
}

@Composable
fun HybridAnalogBatteryFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val pct = ctx.battery.batteryPercent
    val charging = ctx.battery.isCharging
    val temp = ctx.battery.temperatureCelsius
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF1B1D20), Color(0xFF0B0C0E), ring = Color(0xFF2E3136))
            ticks(d, majorEvery = 5, outer = 0.96f, majorLength = 0.1f, minorLength = 0.035f, majorWidth = 0.02f, minorWidth = 0.006f)
            val sd = d.sub(270f, 0.42f, 0.3f)
            drawCircle(Color(0xFF0A0B0C), sd.radius, sd.center)
            progressArc(sd, 1f, 0.82f, 0.1f, Color.White.copy(alpha = 0.1f), startAngle = 225f, sweepTotal = 270f)
            progressArc(sd, pct / 100f, 0.82f, 0.1f, if (pct < 20) Ink.Red else Ink.Green, startAngle = 225f, sweepTotal = 270f)
            listOf(0 to "E", 50 to "½", 100 to "F").forEach { (v, l) ->
                textAt(m, l, sd.point(225f + v * 2.7f, 0.55f), numeralStyle(d, 0.05f, FontWeight.Bold), Ink.TextSecondary)
            }
            textAt(m, "${if (charging) "⚡" else ""}$pct%", Offset(sd.center.x, sd.center.y + sd.px(0.62f)), numeralStyle(d, 0.06f, FontWeight.Bold), Ink.TextPrimary)
            textAt(m, temp?.let { "%.0f°C".format(it) } ?: "", d.point(90f, 0.45f), numeralStyle(d, 0.06f, FontWeight.Medium), Ink.TextSecondary)
        },
        handsLayer = { d, a ->
            val sd = d.sub(270f, 0.42f, 0.3f)
            hand(sd, 225f + pct * 2.7f, 0.7f, 0.05f, Ink.TextPrimary, HandStyle.NEEDLE, tail = 0.15f, shadow = false)
            hub(sd, Ink.TextPrimary, 0.08f)
            handSet(d, a, ctx, style = HandStyle.SWORD, hourWidth = 0.07f, minuteWidth = 0.05f)
        }
    )
}

@Composable
fun HybridAnalogWeatherFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF16202E), Color(0xFF080C12), ring = Color(0xFF2A3A50))
            ticks(d, majorEvery = 5, outer = 0.96f, majorLength = 0.1f, minorLength = 0.035f, majorWidth = 0.02f, minorWidth = 0.006f)
            numerals(d, m, listOf("12", null, null, null, null, null, "6", null, null, "9", null, null), 0.74f, numeralStyle(d, 0.14f, FontWeight.Medium), Ink.TextPrimary)
            weatherSubDial(d.sub(90f, 0.44f, 0.3f), m, d, ctx)
            ctx.weather?.let { w ->
                textAt(m, "H ${ctx.temperature(w.highCelsius)}  L ${ctx.temperature(w.lowCelsius)}", d.point(180f, 0.42f), numeralStyle(d, 0.055f, FontWeight.Medium), Ink.TextSecondary)
            }
        },
        handsLayer = { d, a -> handSet(d, a, ctx, style = HandStyle.BATON, hourWidth = 0.065f, minuteWidth = 0.045f) }
    )
}

@Composable
fun HybridAnalogCalendarFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val dayName = ctx.formatDate("EEEE").uppercase()
    val day = ctx.time.dayOfMonth.toString()
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF2B2E33), Color(0xFF111215), ring = Ink.Steel.copy(alpha = 0.5f))
            sunburst(d, Color.White, rays = 140)
            appliedIndices(d, Ink.Steel, outer = 0.92f, length = 0.14f, width = 0.04f, skip = setOf(0, 3))
            val w = d.px(0.56f)
            val h = d.px(0.15f)
            val c = d.point(0f, 0.68f)
            drawRoundRect(Ink.Steel, Offset(c.x - w / 2 - d.px(0.012f), c.y - h / 2 - d.px(0.012f)), Size(w + d.px(0.024f), h + d.px(0.024f)), CornerRadius(d.px(0.02f)))
            drawRoundRect(Color(0xFFF4F1EA), Offset(c.x - w / 2, c.y - h / 2), Size(w, h), CornerRadius(d.px(0.012f)))
            textAt(m, dayName, c, numeralStyle(d, 0.085f, FontWeight.Bold), if (ctx.time.dayOfWeek.value == 7) Ink.Red else Color(0xFF111111))
            dateWindow(m, day, d.point(90f, 0.7f), d)
        },
        handsLayer = { d, a -> handSet(d, a, ctx, hourColor = Ink.Steel, style = HandStyle.BATON, hourWidth = 0.07f, minuteWidth = 0.05f, lume = Ink.Lume) }
    )
}

@Composable
fun HybridGmtWeatherFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 48)
    val shift = rememberZoneShift(ctx.secondaryZoneId, ctx.time.hour)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            gmtBezel(d, Color(0xFF1F1F1F), Color(0xFF3A3A3A), m)
            dialDisc(d, Color(0xFF0F1A14), Color(0xFF050906), fraction = 0.8f)
            appliedIndices(d, Ink.Steel, outer = 0.76f, length = 0.12f, width = 0.035f, skip = setOf(6))
            weatherSubDial(d.sub(180f, 0.4f, 0.24f), m, d, ctx)
        },
        handsLayer = { d, a ->
            hand(d, a.hour24 + shift / 3600f * 15f, 0.72f, 0.02f, Ink.Green, HandStyle.ARROW, tail = 0.1f)
            handSet(d, a, ctx, hourColor = Ink.Steel, style = HandStyle.BATON, hourWidth = 0.07f, minuteWidth = 0.05f, hourLength = 0.42f, minuteLength = 0.68f, lume = Ink.Lume, secondColor = Ink.Green, secondLength = 0.74f)
        }
    )
}

// ---------------------------------------------------------------------------------------------
// Modular (Apple Watch-style) faces
// ---------------------------------------------------------------------------------------------

@Composable
private fun BatteryComplication(ctx: FaceContext, modifier: Modifier) {
    val pct = ctx.battery.batteryPercent
    ComplicationCard(if (ctx.battery.isCharging) "Charging" else "Battery", if (pct < 20) Ink.Red else Ink.Green, modifier) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            RingGauge(pct / 100f, if (pct < 20) Ink.Red else Ink.Green, Modifier.fillMaxHeight(0.9f).weight(1f), startAngle = -90f, sweep = 360f) {
                AutoSizeText("$pct", clockStyle(FontWeight.SemiBold), Modifier.fillMaxSize(0.8f), color = Ink.TextPrimary, sizingText = "100")
            }
            Column(Modifier.weight(1.2f).fillMaxHeight().padding(start = 6.dp), verticalArrangement = Arrangement.Center) {
                AutoSizeText("$pct%", clockStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.5f), color = Ink.TextPrimary, sizingText = "100%", alignment = Alignment.CenterStart)
                AutoSizeText(ctx.battery.temperatureCelsius?.let { "%.1f°C".format(it) } ?: ctx.battery.chargeType.name, labelStyle(), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
            }
        }
    }
}

@Composable
private fun WeatherComplication(ctx: FaceContext, modifier: Modifier) {
    val w = ctx.weather
    ComplicationCard(w?.cityName?.ifBlank { "Weather" } ?: "Weather", Ink.Cyan, modifier) {
        if (w == null) {
            AutoSizeText("Offline", labelStyle(), Modifier.fillMaxSize(0.6f), color = Ink.TextTertiary)
        } else {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                WeatherGlyph(w.condition, Modifier.fillMaxHeight(0.8f).weight(0.8f))
                Column(Modifier.weight(1.2f).fillMaxHeight().padding(start = 6.dp), verticalArrangement = Arrangement.Center) {
                    AutoSizeText(ctx.temperature(w.temperatureCelsius), clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.55f), color = Ink.TextPrimary, sizingText = "-88°", alignment = Alignment.CenterStart)
                    AutoSizeText("H ${ctx.temperature(w.highCelsius)} L ${ctx.temperature(w.lowCelsius)}", labelStyle(), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
                }
            }
        }
    }
}

@Composable
private fun EventComplication(ctx: FaceContext, modifier: Modifier) {
    val next = ctx.events.firstOrNull { it.endMillis == 0L || it.endMillis > System.currentTimeMillis() }
    ComplicationCard("Up next", Ink.Red, modifier) {
        if (next == null) {
            AutoSizeText(if (ctx.nextAlarm != null) "Alarm ${ctx.nextAlarm}" else "No more events", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(1f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
        } else {
            AutoSizeText(next.title, labelStyle(FontWeight.SemiBold).copy(letterSpacing = TextStyle.Default.letterSpacing), Modifier.fillMaxWidth().weight(0.55f), color = Ink.TextPrimary, sizingText = "Design review with team", alignment = Alignment.CenterStart)
            AutoSizeText(next.timeRange, labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.35f), color = Ink.TextSecondary, sizingText = "Tomorrow · 10:30 AM – 11:30 AM", alignment = Alignment.CenterStart)
        }
    }
}

@Composable
fun ModularCompactFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().weight(0.38f), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(0.9f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                AutoSizeText(ctx.formatDate("EEEE").uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.3f), color = ctx.accent, alignment = Alignment.CenterStart)
                AutoSizeText(ctx.formatDate("MMMM d"), clockStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.5f), color = Ink.TextPrimary, sizingText = "September 28", alignment = Alignment.CenterStart)
            }
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light, letterSpacingEm = -0.04f), Modifier.weight(1.1f).fillMaxHeight(), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterEnd)
        }
        Row(Modifier.fillMaxWidth().weight(0.31f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            WeatherComplication(ctx, Modifier.weight(1f).fillMaxHeight())
            BatteryComplication(ctx, Modifier.weight(1f).fillMaxHeight())
        }
        EventComplication(ctx, Modifier.fillMaxWidth().weight(0.31f))
    }
}

@Composable
fun ModularInfographFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val w = ctx.weather
    Column(modifier.fillMaxSize().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth().weight(0.36f), verticalAlignment = Alignment.CenterVertically) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.SemiBold, letterSpacingEm = -0.03f), Modifier.weight(1.2f).fillMaxHeight(), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            Column(Modifier.weight(0.8f).fillMaxHeight(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                AutoSizeText(ctx.formatDate("EEE d").uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.4f), color = ctx.accent, alignment = Alignment.CenterEnd)
                AutoSizeText(if (ctx.use24Hour) "" else ctx.amPm, labelStyle(), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextSecondary, alignment = Alignment.CenterEnd)
            }
        }
        Row(Modifier.fillMaxWidth().weight(0.34f), horizontalArrangement = Arrangement.SpaceEvenly) {
            val pct = ctx.battery.batteryPercent
            RingGauge(pct / 100f, Ink.Green, Modifier.weight(1f).fillMaxHeight(), gradient = listOf(Ink.Red, Ink.Yellow, Ink.Green, Ink.Green)) {
                AutoSizeText("$pct%", clockStyle(FontWeight.SemiBold), Modifier.fillMaxSize(0.8f), color = Ink.TextPrimary, sizingText = "100%")
            }
            if (w != null) {
                val span = (w.highCelsius - w.lowCelsius).coerceAtLeast(1)
                RingGauge((w.temperatureCelsius - w.lowCelsius).toFloat() / span, Ink.Cyan, Modifier.weight(1f).fillMaxHeight(), gradient = listOf(Ink.Cyan, Ink.Yellow, Ink.Orange, Ink.Orange)) {
                    AutoSizeText(ctx.temperature(w.temperatureCelsius), clockStyle(FontWeight.SemiBold), Modifier.fillMaxSize(0.8f), color = Ink.TextPrimary, sizingText = "-88°")
                }
                RingGauge(w.humidityPercent / 100f, Ink.Blue, Modifier.weight(1f).fillMaxHeight()) {
                    AutoSizeText("${w.humidityPercent}%", clockStyle(FontWeight.SemiBold), Modifier.fillMaxSize(0.8f), color = Ink.TextPrimary, sizingText = "100%")
                }
            } else {
                val moon = remember(ctx.time.hour) { Astronomy.moonPhase() }
                RingGauge(moon.illumination.toFloat(), Ink.Champagne, Modifier.weight(1f).fillMaxHeight()) {
                    AutoSizeText("${(moon.illumination * 100).toInt()}%", clockStyle(FontWeight.SemiBold), Modifier.fillMaxSize(0.8f), color = Ink.TextPrimary, sizingText = "100%")
                }
            }
        }
        EventComplication(ctx, Modifier.fillMaxWidth().weight(0.3f))
    }
}

@Composable
fun ModularDashboardFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val sun = remember(ctx.time.toLocalDate(), ctx.latitude, ctx.longitude) { Astronomy.sunTimes(ctx.time.toLocalDate(), ctx.latitude, ctx.longitude) }
    val fmt = DateTimeFormatter.ofPattern(if (ctx.use24Hour) "HH:mm" else "h:mm a", Locale.getDefault())
    val w = ctx.weather
    Column(modifier.fillMaxSize().padding(12.dp)) {
        Spacer(Modifier.weight(0.06f))
        StackedTime(ctx, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.6f), timeWeight = 0.7f)
        Row(Modifier.fillMaxWidth().weight(0.26f).premiumCard(corner = 18.dp).padding(8.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Metric("Sunrise", sun.sunrise?.format(fmt) ?: "—", Ink.Yellow, Modifier.weight(1f).fillMaxHeight())
            Metric("Sunset", sun.sunset?.format(fmt) ?: "—", Ink.Orange, Modifier.weight(1f).fillMaxHeight())
            Metric("Rain", w?.let { "${it.precipitationChance}%" } ?: "—", Ink.Cyan, Modifier.weight(1f).fillMaxHeight())
            Metric("Wind", w?.let { "${it.windKmh}" } ?: "—", Ink.Mint, Modifier.weight(1f).fillMaxHeight())
        }
        Spacer(Modifier.weight(0.06f))
    }
}
