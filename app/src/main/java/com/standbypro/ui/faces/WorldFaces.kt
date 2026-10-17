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
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.standbypro.data.Locations
import com.standbypro.domain.FaceContext
import com.standbypro.domain.WeatherCondition
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.Dial
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.HandStyle
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.WeatherGlyph
import com.standbypro.ui.design.appliedIndices
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.dialDisc
import com.standbypro.ui.design.dotIndices
import com.standbypro.ui.design.hand
import com.standbypro.ui.design.hub
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.design.numerals
import com.standbypro.ui.design.progressArc
import com.standbypro.ui.design.textAt
import com.standbypro.ui.design.ticks
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

private fun cityLabel(zoneId: String): String = Locations.byZone(zoneId)?.city ?: zoneId.substringAfterLast('/').replace('_', ' ')

internal fun androidx.compose.ui.graphics.drawscope.DrawScope.gmtBezel(d: Dial, day: Color, night: Color, m: androidx.compose.ui.text.TextMeasurer) {
    progressArc(d, 1f, 0.9f, 0.2f, day, startAngle = 90f, sweepTotal = 180f, cap = StrokeCap.Butt)
    progressArc(d, 1f, 0.9f, 0.2f, night, startAngle = 270f, sweepTotal = 180f, cap = StrokeCap.Butt)
    for (h in 0 until 24) {
        val a = h * 15f
        if (h == 0) {
            val tri = Path().apply {
                moveTo(d.center.x, d.center.y - d.px(0.82f))
                lineTo(d.center.x - d.px(0.06f), d.center.y - d.px(0.97f))
                lineTo(d.center.x + d.px(0.06f), d.center.y - d.px(0.97f))
                close()
            }
            drawPath(tri, Color.White)
        } else if (h % 2 == 0) {
            textAt(m, h.toString(), d.point(a, 0.9f), numeralStyle(d, 0.075f, FontWeight.Bold), Color.White)
        } else {
            drawCircle(Color.White, d.px(0.012f), d.point(a, 0.9f))
        }
    }
}

/** GMT-Master style: two-tone 24h bezel and an arrow hand tracking your second time zone. */
@Composable
fun GmtExplorerFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 48)
    val shift = rememberZoneShift(ctx.secondaryZoneId, ctx.time.hour)
    val city = cityLabel(ctx.secondaryZoneId).uppercase()
    val day = ctx.time.dayOfMonth.toString()
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            gmtBezel(d, lerpColor(ctx.accent, Color(0xFFB0151F), 0.35f), Color(0xFF0E2A6B), m)
            dialDisc(d, Color(0xFF111215), Color(0xFF060607), fraction = 0.8f)
            ticks(d, majorEvery = 5, outer = 0.78f, majorLength = 0.0f, minorLength = 0.025f, minorWidth = 0.006f, minorColor = Color.White.copy(alpha = 0.6f))
            for (i in 0 until 12) {
                val p = d.point(i * 30f, 0.66f)
                if (i == 3) continue
                if (i % 3 == 0) {
                    rotate(i * 30f, p) {
                        drawRect(Color(0xFFE8E8E8), Offset(p.x - d.px(0.03f), p.y - d.px(0.07f)), Size(d.px(0.06f), d.px(0.14f)))
                        drawRect(Ink.Lume, Offset(p.x - d.px(0.02f), p.y - d.px(0.06f)), Size(d.px(0.04f), d.px(0.12f)))
                    }
                } else {
                    drawCircle(Color(0xFFE8E8E8), d.px(0.048f), p)
                    drawCircle(Ink.Lume, d.px(0.038f), p)
                }
            }
            dateWindow(m, day, d.point(90f, 0.6f), d, width = 0.17f, height = 0.13f)
            textAt(m, "GMT · $city", d.point(180f, 0.38f), numeralStyle(d, 0.045f, FontWeight.SemiBold), ctx.accent)
        },
        handsLayer = { d, a ->
            val gmtAngle = a.hour24 + shift / 3600f * 15f
            hand(d, gmtAngle, 0.74f, 0.02f, ctx.accent, HandStyle.ARROW, tail = 0.12f)
            handSet(d, a, ctx, hourColor = Color(0xFFE8E8E8), style = HandStyle.BATON, hourWidth = 0.075f, minuteWidth = 0.055f, hourLength = 0.44f, minuteLength = 0.7f, lume = Ink.Lume, secondColor = Color(0xFFE8E8E8), secondDisc = 0.03f, secondLength = 0.76f)
        }
    )
}

/** GMT with pointer-date ring around the dial. */
@Composable
fun GmtCalendarFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 64)
    val shift = rememberZoneShift(ctx.secondaryZoneId, ctx.time.hour)
    val dayOfMonth = ctx.time.dayOfMonth
    val month = ctx.formatDate("MMM").uppercase()
    val weekday = ctx.formatDate("EEE").uppercase()
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF1B1E24), Color(0xFF0B0C10), ring = Ink.Steel.copy(alpha = 0.5f))
            for (i in 1..31) {
                val a = (i - 1) * 360f / 31f
                textAt(m, i.toString(), d.point(a, 0.92f), numeralStyle(d, 0.05f, if (i == dayOfMonth) FontWeight.Bold else FontWeight.Normal), if (i == dayOfMonth) ctx.accent else Ink.TextTertiary)
            }
            drawCircle(Color.White.copy(alpha = 0.12f), d.px(0.85f), d.center, style = Stroke(d.px(0.005f)))
            for (h in 0 until 24) {
                val a = h * 15f
                if (h % 3 == 0) textAt(m, h.toString(), d.point(a, 0.76f), numeralStyle(d, 0.05f, FontWeight.Medium), Ink.TextSecondary)
                else drawCircle(Color.White.copy(alpha = 0.3f), d.px(0.008f), d.point(a, 0.76f))
            }
            appliedIndices(d, Ink.Steel, outer = 0.68f, length = 0.1f, width = 0.03f)
            textAt(m, "$weekday · $month", d.point(0f, 0.36f), numeralStyle(d, 0.06f, FontWeight.SemiBold), Ink.TextPrimary)
            textAt(m, cityLabel(ctx.secondaryZoneId).uppercase(), d.point(180f, 0.36f), numeralStyle(d, 0.045f, FontWeight.Medium), ctx.accent)
        },
        handsLayer = { d, a ->
            hand(d, (dayOfMonth - 1) * 360f / 31f, 0.86f, 0.012f, ctx.accent, HandStyle.NEEDLE, tail = 0f, shadow = false)
            drawCircle(ctx.accent, d.px(0.03f), d.point((dayOfMonth - 1) * 360f / 31f, 0.84f), style = Stroke(d.px(0.01f)))
            hand(d, a.hour24 + shift / 3600f * 15f, 0.74f, 0.02f, ctx.accent, HandStyle.ARROW, tail = 0.1f)
            handSet(d, a, ctx, hourColor = Ink.Steel, style = HandStyle.DAUPHINE, hourWidth = 0.065f, minuteWidth = 0.05f, hourLength = 0.42f, minuteLength = 0.66f)
        }
    )
}

private val worldTimeCities = listOf(
    "MIDWAY", "HONOLULU", "ANCHORAGE", "LOS ANGELES", "DENVER", "CHICAGO", "NEW YORK", "SANTIAGO", "RIO",
    "NORONHA", "AZORES", "LONDON", "PARIS", "CAIRO", "MOSCOW", "DUBAI", "KARACHI", "DHAKA", "BANGKOK",
    "HONG KONG", "TOKYO", "SYDNEY", "NOUMEA", "AUCKLAND"
) // offsets -11 … +12

/** Cottier world time: rotating 24h day/night ring against 24 reference cities. */
@Composable
fun WorldTimeFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 64)
    val localOffsetHours = remember(ctx.time.hour) { ZonedDateTime.now().offset.totalSeconds / 3600f }
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF15233A), Color(0xFF070C16), ring = Ink.Gold.copy(alpha = 0.7f))
            worldTimeCities.forEachIndexed { i, name ->
                val a = i * 15f
                val p = d.point(a, 0.9f)
                rotate(a, p) { textAt(m, name, p, numeralStyle(d, 0.036f, FontWeight.SemiBold), Ink.Champagne) }
            }
            drawCircle(Ink.Gold.copy(alpha = 0.4f), d.px(0.83f), d.center, style = Stroke(d.px(0.005f)))
            // Simple meridian lines behind the hands
            for (i in 0 until 12) drawLine(Color.White.copy(alpha = 0.05f), d.center, d.point(i * 30f, 0.58f), d.px(0.004f))
            dotIndices(d, 0.56f, 0.012f, Ink.Champagne.copy(alpha = 0.7f), count = 12, bigEvery = 3)
        },
        handsLayer = { d, a ->
            val utc = a.hour24 / 15f - localOffsetHours
            // Ring: label H sits at angle (H - utc) * 15 + 165°
            for (h in 0 until 24) {
                val angle = (h - utc) * 15f + 165f
                val isDay = h in 6..17
                progressArc(d, 1f, 0.735f, 0.1f, if (isDay) Color(0xFFE9D9A6) else Color(0xFF0B1630), startAngle = angle - 7.5f, sweepTotal = 15f, cap = StrokeCap.Butt)
                textAt(m, h.toString(), d.point(angle, 0.735f), numeralStyle(d, 0.042f, FontWeight.Bold), if (isDay) Color(0xFF1B2A44) else Ink.Champagne)
            }
            handSet(d, a, ctx, hourColor = Ink.GoldLight, style = HandStyle.LEAF, hourWidth = 0.06f, minuteWidth = 0.045f, hourLength = 0.4f, minuteLength = 0.62f, secondColor = Ink.GoldLight, secondLength = 0.66f)
        }
    )
}

/** Grid of mini analog clocks with day/night tinted dials. */
@Composable
fun WorldClockFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 64)
    val clocks = ctx.worldClocks.take(6)
    val shifts = remember(clocks, ctx.time.hour) { clocks.map { zoneShiftSeconds(it.zoneId) } }
    val clock = rememberFaceClock(ctx, com.standbypro.ui.design.SecondsMotion.OFF)
    Canvas(modifier.fillMaxSize().padding(8.dp).graphicsLayer()) {
        val cols = if (clocks.size <= 4) 2 else 3
        val rows = (clocks.size + cols - 1) / cols
        val cellW = size.width / cols
        val cellH = size.height / rows
        clocks.forEachIndexed { i, tz ->
            val cx = cellW * (i % cols + 0.5f)
            val cy = cellH * (i / cols + 0.42f)
            val r = minOf(cellW, cellH) * 0.34f
            val secs = clock.value + shifts[i]
            val hours = (((secs / 3600f) % 24f) + 24f) % 24f
            val isDay = hours in 6f..18f
            val d = Dial(Offset(cx, cy), r)
            drawCircle(if (isDay) Color(0xFFF2F2F2) else Color(0xFF1C1C1E), r, d.center)
            for (k in 0 until 12) drawLine(if (isDay) Color(0xFF333333) else Color(0xFF8E8E93), d.point(k * 30f, 0.8f), d.point(k * 30f, 0.92f), r * 0.05f, StrokeCap.Round)
            val ha = com.standbypro.ui.design.HandAngles.from(secs)
            val handColor = if (isDay) Color(0xFF111111) else Color.White
            hand(d, ha.hour, 0.5f, 0.1f, handColor, HandStyle.BATON, tail = 0f, shadow = false)
            hand(d, ha.minute, 0.8f, 0.07f, handColor, HandStyle.BATON, tail = 0f, shadow = false)
            hub(d, ctx.accent, 0.07f, ctx.accent)
            val dayDiff = ((secs / 86400f).let { if (it < 0f) -1 else if (it >= 1f) 1 else 0 })
            textAt(m, tz.city, Offset(cx, cy + r * 1.3f), TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.SemiBold, fontSize = (r * 0.3f).toSp()), Ink.TextPrimary)
            val sub = when (dayDiff) {
                -1 -> "Yesterday"
                1 -> "Tomorrow"
                else -> "Today"
            } + " · " + offsetLabel(shifts[i])
            textAt(m, sub, Offset(cx, cy + r * 1.62f), TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.Medium, fontSize = (r * 0.2f).toSp()), Ink.TextTertiary)
        }
    }
}

private fun offsetLabel(shiftSeconds: Float): String {
    val totalMinutes = (shiftSeconds / 60f).toInt()
    if (totalMinutes == 0) return "+0h"
    val sign = if (totalMinutes > 0) "+" else "−"
    val h = kotlin.math.abs(totalMinutes) / 60
    val mm = kotlin.math.abs(totalMinutes) % 60
    return if (mm == 0) "$sign${h}h" else "$sign$h:${"%02d".format(mm)}h"
}

/** List of cities with live times, offsets and day/night glyphs. */
@Composable
fun MultiTimezoneFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val fmt = remember(ctx.use24Hour) { DateTimeFormatter.ofPattern(if (ctx.use24Hour) "HH:mm" else "h:mm", Locale.getDefault()) }
    val clocks = ctx.worldClocks.take(5)
    Column(modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.SpaceEvenly) {
        clocks.forEach { tz ->
            val now = remember(tz.zoneId, ctx.time) { runCatching { ZonedDateTime.now(ZoneId.of(tz.zoneId)) }.getOrElse { ZonedDateTime.now() } }
            val shift = remember(tz.zoneId, ctx.time.hour) { zoneShiftSeconds(tz.zoneId) }
            val isDay = now.hour in 6..17
            Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                WeatherGlyph(if (isDay) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT, Modifier.fillMaxHeight(0.5f).width(28.dp))
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                    AutoSizeText(tz.city, labelStyle(FontWeight.SemiBold).copy(letterSpacing = TextStyle.Default.letterSpacing), Modifier.fillMaxWidth().weight(0.55f), color = Ink.TextPrimary, sizingText = "San Francisco", alignment = Alignment.CenterStart)
                    val dayLabel = when (now.toLocalDate().compareTo(ctx.time.toLocalDate())) {
                        0 -> "Today"
                        in 1..Int.MAX_VALUE -> "Tomorrow"
                        else -> "Yesterday"
                    }
                    AutoSizeText("$dayLabel, ${offsetLabel(shift)}", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.4f), color = Ink.TextTertiary, sizingText = "Yesterday, −10:30h", alignment = Alignment.CenterStart)
                }
                AutoSizeText(now.format(fmt), clockStyle(FontWeight.Light), Modifier.weight(0.7f).fillMaxHeight(0.8f), color = if (isDay) Ink.TextPrimary else ctx.accent, sizingText = "88:88", alignment = Alignment.CenterEnd)
            }
        }
    }
}

/** Equirectangular day/night map with the live terminator and your cities. */
@Composable
fun DayNightMapFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val places = remember(ctx.worldClocks) { ctx.worldClocks.mapNotNull { Locations.byCity(it.city) }.filter { it.zoneId != "UTC" } }
    val minuteKey = ctx.time.hour * 60 + ctx.time.minute
    Column(modifier.fillMaxSize().padding(10.dp)) {
        Canvas(Modifier.fillMaxWidth().weight(0.82f)) {
            val w = size.width
            val h = minOf(size.height, w / 2f)
            val top = (size.height - h) / 2f
            fun x(lon: Double) = ((lon + 180.0) / 360.0 * w).toFloat()
            fun y(lat: Double) = (top + (90.0 - lat) / 180.0 * h).toFloat()
            drawRect(Brush.verticalGradient(listOf(Color(0xFF13385C), Color(0xFF0C2440))), Offset(0f, top), Size(w, h))
            for (lon in -180..180 step 30) drawLine(Color.White.copy(alpha = 0.06f), Offset(x(lon.toDouble()), top), Offset(x(lon.toDouble()), top + h), 1f)
            for (lat in -60..60 step 30) drawLine(Color.White.copy(alpha = if (lat == 0) 0.12f else 0.06f), Offset(0f, y(lat.toDouble())), Offset(w, y(lat.toDouble())), 1f)
            // Sun position
            val nowUtc = java.time.Instant.now().atZone(ZoneId.of("UTC"))
            val utcHours = nowUtc.hour + nowUtc.minute / 60.0 + minuteKey * 0.0
            val dayOfYear = nowUtc.dayOfYear
            val decl = -23.44 * cos(2 * PI / 365.0 * (dayOfYear + 10)) * PI / 180.0
            val subLon = -(utcHours - 12.0) * 15.0
            val night = Path()
            val south = decl > 0
            night.moveTo(0f, if (south) top + h else top)
            var lon = -180.0
            while (lon <= 180.0) {
                val ha = (lon - subLon) * PI / 180.0
                val lat = atan(-cos(ha) / tan(if (kotlin.math.abs(decl) < 1e-4) 1e-4 else decl)) * 180.0 / PI
                night.lineTo(x(lon), y(lat))
                lon += 3.0
            }
            night.lineTo(w, if (south) top + h else top)
            night.close()
            drawPath(night, Color(0xFF020611).copy(alpha = 0.72f))
            drawCircle(Color(0xFFFFD36B).copy(alpha = 0.35f), h * 0.07f, Offset(x(subLon), y(decl * 180.0 / PI)))
            drawCircle(Color(0xFFFFD36B), h * 0.03f, Offset(x(subLon), y(decl * 180.0 / PI)))
            places.forEach { p ->
                val px = x(p.longitude)
                val py = y(p.latitude)
                val elevation = asin(sin(p.latitude * PI / 180) * sin(decl) + cos(p.latitude * PI / 180) * cos(decl) * cos((p.longitude - subLon) * PI / 180))
                val lit = elevation > 0
                drawCircle(ctx.accent.copy(alpha = 0.3f), h * 0.03f, Offset(px, py))
                drawCircle(if (lit) Color.White else ctx.accent, h * 0.012f, Offset(px, py))
                textAt(m, p.city, Offset(px, py - h * 0.06f), TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.SemiBold, fontSize = (h * 0.045f).toSp()), Color.White.copy(alpha = 0.85f))
            }
        }
        Row(Modifier.fillMaxWidth().weight(0.18f), verticalAlignment = Alignment.CenterVertically) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.weight(1f).fillMaxHeight(), color = Ink.TextPrimary, sizingText = "88:88", alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("EEE d MMM").uppercase(), labelStyle(), Modifier.weight(1.4f).fillMaxHeight(0.5f), color = Ink.TextSecondary, alignment = Alignment.CenterEnd)
        }
    }
}

/** Main dial with a sub-dial for the second time zone. */
@Composable
fun HybridDualTimeFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val shift = rememberZoneShift(ctx.secondaryZoneId, ctx.time.hour)
    val city = cityLabel(ctx.secondaryZoneId).uppercase()
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF202226), Color(0xFF0D0E10), ring = Color(0xFF34363C))
            ticks(d, majorEvery = 5, outer = 0.96f, majorLength = 0.1f, minorLength = 0.035f, majorWidth = 0.02f, minorWidth = 0.006f)
            numerals(d, m, listOf("12", null, null, "3", null, null, null, null, null, "9", null, null), 0.74f, numeralStyle(d, 0.15f, FontWeight.Medium), Ink.TextPrimary)
            val sd = d.sub(180f, 0.44f, 0.28f)
            drawCircle(Color(0xFF0A0A0B), sd.radius, sd.center)
            drawCircle(Color.White.copy(alpha = 0.2f), sd.radius, sd.center, style = Stroke(d.px(0.006f)))
            numerals(sd, m, listOf("12", null, null, "3", null, null, "6", null, null, "9", null, null), 0.7f, numeralStyle(d, 0.05f, FontWeight.Medium), Ink.TextSecondary)
            textAt(m, city, d.point(0f, 0.42f), numeralStyle(d, 0.05f, FontWeight.SemiBold), ctx.accent)
        },
        handsLayer = { d, a ->
            val sd = d.sub(180f, 0.44f, 0.28f)
            val other = com.standbypro.ui.design.HandAngles.from((a.hour24 / 15f) * 3600f + shift)
            val otherHours = other.hour24 / 15f
            drawCircle(if (otherHours in 6f..18f) Color(0xFFFFD36B) else Color(0xFF5E5CE6), sd.px(0.08f), sd.point(0f, 0.42f))
            hand(sd, other.hour, 0.5f, 0.1f, Ink.TextPrimary, HandStyle.BATON, tail = 0.1f, shadow = false)
            hand(sd, other.minute, 0.78f, 0.07f, Ink.TextPrimary, HandStyle.BATON, tail = 0.1f, shadow = false)
            hub(sd, ctx.accent, 0.08f, ctx.accent)
            handSet(d, a, ctx, style = HandStyle.DAUPHINE, hourWidth = 0.07f, minuteWidth = 0.055f)
        }
    )
}
