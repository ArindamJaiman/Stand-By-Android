package com.standbypro.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.standbypro.astro.Astronomy
import com.standbypro.domain.FaceContext
import com.standbypro.domain.WeatherCondition
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.HandStyle
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.WeatherGlyph
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.drawWeatherGlyph
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.design.textAt
import com.standbypro.ui.design.ticks
import java.time.Duration
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Shown in place of weather data until the first fetch succeeds. */
@Composable
internal fun WeatherUnavailable(ctx: FaceContext, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        StackedTime(ctx, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.7f), date = null)
        AutoSizeText("Weather loading… check connection or set a city in Studio", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth(0.9f).weight(0.1f), color = Ink.TextTertiary)
    }
}

internal fun temperatureColor(celsius: Int): Color = when {
    celsius <= 0 -> Color(0xFF8AB4FF)
    celsius <= 10 -> Color(0xFF5AC8FA)
    celsius <= 18 -> Color(0xFF63E6BE)
    celsius <= 25 -> Color(0xFFFFD60A)
    celsius <= 32 -> Color(0xFFFF9F0A)
    else -> Color(0xFFFF453A)
}

@Composable
fun WeatherDigitalFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val w = ctx.weather ?: return WeatherUnavailable(ctx, modifier)
    Row(modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(0.9f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            WeatherGlyph(w.condition, Modifier.fillMaxWidth(0.62f).weight(0.42f))
            AutoSizeText(ctx.temperature(w.temperatureCelsius), clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.34f), color = Ink.TextPrimary, sizingText = "-88°")
            AutoSizeText(w.condition.displayName, labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth(0.8f).weight(0.1f), color = temperatureColor(w.temperatureCelsius))
        }
        Box(Modifier.width(1.dp).fillMaxHeight(0.6f).background(Ink.Hairline))
        Column(Modifier.weight(1.1f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.45f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("EEEE, MMM d").uppercase(), labelStyle(), Modifier.fillMaxWidth().weight(0.1f), color = ctx.accent, alignment = Alignment.CenterStart)
            Spacer(Modifier.weight(0.05f))
            AutoSizeText(
                "H ${ctx.temperature(w.highCelsius)}  ·  L ${ctx.temperature(w.lowCelsius)}  ·  Feels ${ctx.temperature(w.feelsLikeCelsius)}",
                labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.09f), color = Ink.TextSecondary, alignment = Alignment.CenterStart
            )
            AutoSizeText(
                "${w.cityName.ifBlank { ctx.locationName }}  ·  💧${w.humidityPercent}%  ·  ${w.windKmh} km/h",
                labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.09f), color = Ink.TextTertiary, alignment = Alignment.CenterStart
            )
        }
    }
}

/** Analog dial wrapped by a live thermometer arc from today's low to high. */
@Composable
fun WeatherAnalogFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val w = ctx.weather
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            drawCircle(Color(0xFF0E1116), d.radius, d.center)
            val start = 135f
            val sweep = 270f
            val r = d.px(0.9f)
            val tl = Offset(d.center.x - r, d.center.y - r)
            // Cold (blue) at the start of the arc (bottom-left) to hot (red) at its end (bottom-right).
            val hotCold = arrayOf(
                0f to lerpColor(Color(0xFFFF9F0A), Color(0xFFFF453A), 0.33f),
                0.125f to Color(0xFFFF453A),
                0.3f to Color(0xFFFF453A),
                0.375f to Color(0xFF5AC8FA),
                0.5625f to Color(0xFF63E6BE),
                0.75f to Color(0xFFFFD60A),
                0.9375f to Color(0xFFFF9F0A),
                1f to lerpColor(Color(0xFFFF9F0A), Color(0xFFFF453A), 0.33f)
            )
            drawArc(
                Brush.sweepGradient(*hotCold, center = d.center),
                start, sweep, false, tl, Size(r * 2, r * 2), style = Stroke(d.px(0.07f), cap = StrokeCap.Round)
            )
            ticks(d, majorEvery = 5, outer = 0.8f, majorLength = 0.07f, minorLength = 0.0f, majorWidth = 0.016f, majorColor = Color.White.copy(alpha = 0.6f))
            if (w != null) {
                val span = (w.highCelsius - w.lowCelsius).coerceAtLeast(1)
                val f = ((w.temperatureCelsius - w.lowCelsius).toFloat() / span).coerceIn(0f, 1f)
                val p = d.point(start + 90f + sweep * f, 0.9f)
                drawCircle(Color.White, d.px(0.055f), p)
                drawCircle(temperatureColor(w.temperatureCelsius), d.px(0.035f), p)
                textAt(m, ctx.temperature(w.lowCelsius), d.point(start + 90f, 0.72f), numeralStyle(d, 0.06f, FontWeight.Medium), Ink.TextSecondary)
                textAt(m, ctx.temperature(w.highCelsius), d.point(start + 90f + sweep, 0.72f), numeralStyle(d, 0.06f, FontWeight.Medium), Ink.TextSecondary)
                drawWeatherGlyph(w.condition, d.point(0f, 0.4f), d.px(0.14f))
                textAt(m, ctx.temperature(w.temperatureCelsius), d.point(180f, 0.42f), numeralStyle(d, 0.16f, FontWeight.Light), Ink.TextPrimary)
            }
        },
        handsLayer = { d, a -> handSet(d, a, ctx, style = HandStyle.BATON, hourWidth = 0.06f, minuteWidth = 0.04f, hourLength = 0.46f, minuteLength = 0.7f) }
    )
}

@Composable
fun WeatherHourlyFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val w = ctx.weather ?: return WeatherUnavailable(ctx, modifier)
    val hours = w.hourlyForecast.take(12)
    val m = rememberTextMeasurer(cacheSize = 64)
    Column(modifier.fillMaxSize().padding(14.dp)) {
        Row(Modifier.fillMaxWidth().weight(0.3f), verticalAlignment = Alignment.CenterVertically) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.weight(1f).fillMaxHeight(), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.Center) {
                AutoSizeText("${ctx.temperature(w.temperatureCelsius)} ${w.condition.displayName}", labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().weight(0.5f), color = Ink.TextPrimary, alignment = Alignment.CenterEnd)
                AutoSizeText(w.cityName.uppercase(), labelStyle(), Modifier.fillMaxWidth().weight(0.35f), color = ctx.accent, alignment = Alignment.CenterEnd)
            }
        }
        if (hours.size < 2) return@Column
        Canvas(Modifier.fillMaxWidth().weight(0.7f)) {
            val minT = hours.minOf { it.temperatureCelsius }
            val maxT = hours.maxOf { it.temperatureCelsius }
            val span = (maxT - minT).coerceAtLeast(1)
            val stepX = size.width / hours.size
            val topPad = size.height * 0.28f
            val graphH = size.height * 0.36f
            fun pt(i: Int): Offset = Offset(stepX * (i + 0.5f), topPad + graphH * (1f - (hours[i].temperatureCelsius - minT).toFloat() / span))
            val path = Path().apply {
                moveTo(pt(0).x, pt(0).y)
                for (i in 1 until hours.size) {
                    val p0 = pt(i - 1)
                    val p1 = pt(i)
                    val cx = (p0.x + p1.x) / 2f
                    cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                }
            }
            val fill = Path().apply {
                addPath(path)
                lineTo(pt(hours.size - 1).x, topPad + graphH + size.height * 0.08f)
                lineTo(pt(0).x, topPad + graphH + size.height * 0.08f)
                close()
            }
            drawPath(fill, Brush.verticalGradient(listOf(ctx.accent.copy(alpha = 0.35f), Color.Transparent), startY = topPad, endY = topPad + graphH + size.height * 0.08f))
            drawPath(path, ctx.accent, style = Stroke(size.height * 0.018f, cap = StrokeCap.Round))
            val labelStyle = TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.SemiBold, fontSize = (size.height * 0.08f).toSp())
            hours.forEachIndexed { i, h ->
                val p = pt(i)
                drawCircle(Color.Black, size.height * 0.022f, p)
                drawCircle(Color.White, size.height * 0.014f, p)
                textAt(m, ctx.temperature(h.temperatureCelsius), Offset(p.x, p.y - size.height * 0.1f), labelStyle, Ink.TextPrimary)
                drawWeatherGlyph(h.condition, Offset(p.x, size.height * 0.82f), size.height * 0.07f)
                textAt(m, h.timeLabel, Offset(p.x, size.height * 0.95f), labelStyle.copy(fontWeight = FontWeight.Medium, fontSize = (size.height * 0.065f).toSp()), Ink.TextSecondary)
                if (h.precipitationChance >= 20) {
                    textAt(m, "${h.precipitationChance}%", Offset(p.x, size.height * 0.7f), labelStyle.copy(fontSize = (size.height * 0.055f).toSp()), Color(0xFF5AC8FA))
                }
            }
        }
    }
}

@Composable
fun WeatherForecastFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val w = ctx.weather ?: return WeatherUnavailable(ctx, modifier)
    val days = w.dailyForecast.take(6)
    val weekMin = days.minOfOrNull { it.lowCelsius } ?: w.lowCelsius
    val weekMax = days.maxOfOrNull { it.highCelsius } ?: w.highCelsius
    Row(modifier.fillMaxSize().padding(14.dp)) {
        Column(Modifier.weight(0.85f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
            AutoSizeText(w.cityName.ifBlank { ctx.locationName }.uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.08f), color = ctx.accent, alignment = Alignment.CenterStart)
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.32f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            Row(Modifier.fillMaxWidth().weight(0.22f), verticalAlignment = Alignment.CenterVertically) {
                WeatherGlyph(w.condition, Modifier.fillMaxHeight(0.85f).width(48.dp))
                AutoSizeText(ctx.temperature(w.temperatureCelsius), clockStyle(FontWeight.Light), Modifier.weight(1f).fillMaxHeight(), color = Ink.TextPrimary, sizingText = "-88°", alignment = Alignment.CenterStart)
            }
            AutoSizeText(w.condition.displayName, labelStyle(FontWeight.Medium), Modifier.fillMaxWidth(0.8f).weight(0.08f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
        }
        Column(Modifier.weight(1.15f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.SpaceEvenly) {
            days.forEach { day ->
                Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    AutoSizeText(day.dayLabel, labelStyle(FontWeight.SemiBold), Modifier.weight(0.6f).fillMaxHeight(0.5f), color = Ink.TextPrimary, sizingText = "Today", alignment = Alignment.CenterStart)
                    WeatherGlyph(day.condition, Modifier.weight(0.35f).fillMaxHeight(0.6f))
                    AutoSizeText(ctx.temperature(day.lowCelsius), labelStyle(FontWeight.Medium), Modifier.weight(0.4f).fillMaxHeight(0.45f), color = Ink.TextSecondary, sizingText = "-88°")
                    RangeBar(day.lowCelsius, day.highCelsius, weekMin, weekMax, w.temperatureCelsius.takeIf { day.dayLabel == "Today" }, Modifier.weight(1.2f).height(6.dp))
                    AutoSizeText(ctx.temperature(day.highCelsius), labelStyle(FontWeight.SemiBold), Modifier.weight(0.4f).fillMaxHeight(0.45f), color = Ink.TextPrimary, sizingText = "-88°")
                }
            }
        }
    }
}

@Composable
private fun RangeBar(low: Int, high: Int, min: Int, max: Int, current: Int?, modifier: Modifier) {
    Canvas(modifier.clip(RoundedCornerShape(50))) {
        val span = (max - min).coerceAtLeast(1).toFloat()
        drawRect(Color.White.copy(alpha = 0.1f))
        val x0 = size.width * (low - min) / span
        val x1 = size.width * (high - min) / span
        drawRoundRect(
            Brush.horizontalGradient(listOf(temperatureColor(low), temperatureColor(high)), startX = x0, endX = x1),
            topLeft = Offset(x0, 0f), size = Size((x1 - x0).coerceAtLeast(size.height), size.height),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(size.height / 2f)
        )
        if (current != null) {
            val cx = size.width * (current - min) / span
            drawCircle(Color.Black, size.height * 0.75f, Offset(cx, size.height / 2f))
            drawCircle(Color.White, size.height * 0.5f, Offset(cx, size.height / 2f))
        }
    }
}

/** Living sky: colour, sun/moon, clouds and precipitation driven by the real conditions. */
@Composable
fun WeatherSkyFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val w = ctx.weather
    val condition = w?.condition ?: if (ctx.time.hour in 6..18) WeatherCondition.CLEAR_DAY else WeatherCondition.CLEAR_NIGHT
    val sun = remember(ctx.time.toLocalDate(), ctx.latitude) { Astronomy.sunTimes(ctx.time.toLocalDate(), ctx.latitude, ctx.longitude) }
    val moonPhase = remember(ctx.time.hour) { Astronomy.moonPhase().fraction }
    val now = ctx.time
    val dayFraction: Float? = if (sun.sunrise != null && sun.sunset != null && now.isAfter(sun.sunrise) && now.isBefore(sun.sunset)) {
        Duration.between(sun.sunrise, now).seconds.toFloat() / Duration.between(sun.sunrise, sun.sunset).seconds.toFloat()
    } else null
    val isDay = dayFraction != null
    val top = when {
        condition.isRainy -> if (isDay) Color(0xFF3E4C5E) else Color(0xFF0E141C)
        condition == WeatherCondition.CLOUDY || condition == WeatherCondition.FOG -> if (isDay) Color(0xFF5C6B7D) else Color(0xFF12161D)
        isDay && (dayFraction!! < 0.08f || dayFraction > 0.92f) -> Color(0xFFFF8A5B)
        isDay -> Color(0xFF2F7DE1)
        else -> Color(0xFF070B1C)
    }
    val bottom = when {
        isDay && (dayFraction!! < 0.08f || dayFraction > 0.92f) -> Color(0xFF6B4FA0)
        isDay -> Color(0xFF9CD3FF)
        else -> Color(0xFF1A2244)
    }
    Box(modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(26.dp))) {
        AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 40L) { t ->
            drawRect(Brush.verticalGradient(listOf(top, lerpColor(top, bottom, 0.85f))))
            if (!isDay) drawStars(70, 7, t / 6f)
            // Celestial body along an arc
            val f = dayFraction ?: 0.5f
            val bx = size.width * (0.1f + 0.8f * f)
            val by = size.height * (0.55f - 0.4f * sin(f * PI.toFloat()))
            if (isDay && !condition.isRainy) {
                drawCircle(Color(0xFFFFE08A).copy(alpha = 0.18f), size.minDimension * 0.18f, Offset(bx, by))
                drawCircle(Color(0xFFFFE6A0), size.minDimension * 0.075f, Offset(bx, by))
            } else if (!isDay) {
                drawMoon(Offset(size.width * 0.78f, size.height * 0.24f), size.minDimension * 0.08f, moonPhase)
            }
            // Clouds
            val cloudCount = when {
                condition.isRainy || condition == WeatherCondition.CLOUDY -> 6
                condition.isCloudy -> 3
                else -> 1
            }
            val rnd = Random(3)
            for (i in 0 until cloudCount) {
                val speed = 6f + rnd.nextFloat() * 8f
                val baseY = size.height * (0.12f + rnd.nextFloat() * 0.4f)
                val scale = size.minDimension * (0.12f + rnd.nextFloat() * 0.1f)
                val x = ((rnd.nextFloat() * size.width + t * speed) % (size.width + scale * 4)) - scale * 2
                val col = (if (isDay) Color.White else Color(0xFF9AA4B8)).copy(alpha = if (condition.isRainy) 0.55f else 0.8f)
                drawCircle(col, scale * 0.6f, Offset(x, baseY))
                drawCircle(col, scale * 0.8f, Offset(x + scale * 0.7f, baseY - scale * 0.25f))
                drawCircle(col, scale * 0.55f, Offset(x + scale * 1.4f, baseY + scale * 0.05f))
            }
            if (condition.isRainy) {
                val drops = if (condition == WeatherCondition.HEAVY_RAIN || condition == WeatherCondition.THUNDERSTORM) 90 else 50
                val r2 = Random(11)
                for (i in 0 until drops) {
                    val x = r2.nextFloat() * size.width
                    val speed = 0.6f + r2.nextFloat() * 0.6f
                    val y = ((r2.nextFloat() + t * speed) % 1f) * size.height
                    drawLine(Color(0xFFB3D9FF).copy(alpha = 0.55f), Offset(x, y), Offset(x - size.height * 0.01f, y + size.height * 0.04f), size.minDimension * 0.004f)
                }
                if (condition == WeatherCondition.THUNDERSTORM && (t % 7f) < 0.15f) drawRect(Color.White.copy(alpha = 0.25f))
            }
            if (condition == WeatherCondition.SNOW) {
                val r3 = Random(5)
                for (i in 0 until 70) {
                    val x = (r3.nextFloat() * size.width + sin((t + i) * 0.8f) * size.width * 0.02f)
                    val y = ((r3.nextFloat() + t * (0.05f + r3.nextFloat() * 0.06f)) % 1f) * size.height
                    drawCircle(Color.White.copy(alpha = 0.85f), size.minDimension * (0.004f + r3.nextFloat() * 0.005f), Offset(x, y))
                }
            }
            if (condition == WeatherCondition.FOG) {
                for (i in 0 until 4) {
                    val y = size.height * (0.5f + i * 0.12f)
                    drawRect(Color.White.copy(alpha = 0.08f), Offset(0f, y), Size(size.width, size.height * 0.06f))
                }
            }
        }
        Column(Modifier.fillMaxSize().padding(18.dp)) {
            Spacer(Modifier.weight(0.45f))
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light).copy(shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.35f), Offset(0f, 4f), 16f)), Modifier.fillMaxWidth(0.62f).weight(0.32f), color = Color.White, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            AutoSizeText(
                w?.let { "${ctx.temperature(it.temperatureCelsius)}  ${it.condition.displayName}  ·  ${it.cityName}" } ?: ctx.formatDate("EEEE, MMMM d"),
                labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth(0.7f).weight(0.08f), color = Color.White.copy(alpha = 0.9f), alignment = Alignment.CenterStart
            )
            Spacer(Modifier.weight(0.06f))
        }
    }
}

/** Sun path for today with the real sunrise/sunset and current solar position. */
@Composable
fun WeatherSunArcFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val sun = remember(ctx.time.toLocalDate(), ctx.latitude, ctx.longitude) { Astronomy.sunTimes(ctx.time.toLocalDate(), ctx.latitude, ctx.longitude) }
    val fmt = remember(ctx.use24Hour) { DateTimeFormatter.ofPattern(if (ctx.use24Hour) "HH:mm" else "h:mm a", Locale.getDefault()) }
    val now = ctx.time
    Column(modifier.fillMaxSize().padding(14.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().weight(0.68f)) {
            Canvas(Modifier.fillMaxSize()) {
                val horizon = size.height * 0.78f
                val left = size.width * 0.08f
                val right = size.width * 0.92f
                val peak = size.height * 0.12f
                drawLine(Color.White.copy(alpha = 0.3f), Offset(0f, horizon), Offset(size.width, horizon), size.height * 0.008f)
                val path = Path().apply {
                    moveTo(left, horizon)
                    cubicTo(left + (right - left) * 0.2f, peak - size.height * 0.1f, left + (right - left) * 0.8f, peak - size.height * 0.1f, right, horizon)
                }
                drawPath(path, Color.White.copy(alpha = 0.35f), style = Stroke(size.height * 0.008f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(size.height * 0.03f, size.height * 0.025f))))
                val rise = sun.sunrise
                val set = sun.sunset
                if (rise != null && set != null) {
                    val f = (Duration.between(rise, now).seconds.toFloat() / Duration.between(rise, set).seconds.toFloat())
                    val tt = f.coerceIn(0f, 1f)
                    // Cubic bezier point
                    val p0 = Offset(left, horizon)
                    val p1 = Offset(left + (right - left) * 0.2f, peak - size.height * 0.1f)
                    val p2 = Offset(left + (right - left) * 0.8f, peak - size.height * 0.1f)
                    val p3 = Offset(right, horizon)
                    val u = 1 - tt
                    val sp = p0 * (u * u * u) + p1 * (3 * u * u * tt) + p2 * (3 * u * tt * tt) + p3 * (tt * tt * tt)
                    val up = f in 0f..1f
                    drawCircle(Color(0xFFFFD36B).copy(alpha = if (up) 0.25f else 0.08f), size.height * 0.1f, sp)
                    drawCircle(if (up) Color(0xFFFFD36B) else Color(0xFF6E6E73), size.height * 0.045f, sp)
                    val st = TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.SemiBold, fontSize = (size.height * 0.065f).toSp())
                    textAt(m, "↑ ${rise.format(fmt)}", Offset(left + size.width * 0.04f, horizon + size.height * 0.1f), st, Color(0xFFFFD36B))
                    textAt(m, "↓ ${set.format(fmt)}", Offset(right - size.width * 0.04f, horizon + size.height * 0.1f), st, Color(0xFFFF9F0A))
                    textAt(m, "Solar noon ${sun.solarNoon.format(fmt)}", Offset(size.width / 2f, peak + size.height * 0.02f), st.copy(fontWeight = FontWeight.Medium), Ink.TextSecondary)
                }
            }
        }
        Row(Modifier.fillMaxWidth().weight(0.32f), verticalAlignment = Alignment.CenterVertically) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.weight(1f).fillMaxHeight(), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            val mins = sun.dayLengthMinutes
            AutoSizeText("Daylight ${mins / 60}h ${mins % 60}m", labelStyle(FontWeight.SemiBold), Modifier.weight(1f).fillMaxHeight(0.4f), color = ctx.accent, alignment = Alignment.CenterEnd)
        }
    }
}
