package com.standbypro.ui.widgets

import android.app.ActivityManager
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.standbypro.astro.Astronomy
import com.standbypro.data.GitHubRepository
import com.standbypro.data.TasksRepository
import com.standbypro.domain.FaceContext
import com.standbypro.domain.NotificationPrivacy
import com.standbypro.domain.WeatherCondition
import com.standbypro.media.NowPlayingRepository
import com.standbypro.notifications.NotificationRepository
import com.standbypro.ui.components.GitHubContributionWidget
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.GlassIconButton
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.RingGauge
import com.standbypro.ui.design.WeatherGlyph
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.design.premiumCard
import com.standbypro.ui.faces.AnalogStandByFace
import com.standbypro.ui.faces.CountdownFace
import com.standbypro.ui.faces.DayProgressFace
import com.standbypro.ui.faces.PhotoSlideshow
import com.standbypro.ui.faces.PomodoroClockFace
import com.standbypro.ui.faces.StackedTime
import com.standbypro.ui.faces.StopwatchFace
import com.standbypro.ui.faces.TimerFace
import com.standbypro.ui.faces.batteryColor
import com.standbypro.ui.faces.drawMoon
import com.standbypro.ui.faces.formatDate
import com.standbypro.ui.faces.sourceLabel
import com.standbypro.ui.faces.temperatureColor
import com.standbypro.ui.faces.timeToFullLabel
import com.standbypro.ui.faces.zoneShiftSeconds
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------------------------------------------------------------------------------------------
// Shared tile chrome
// ---------------------------------------------------------------------------------------------

@Composable
fun WidgetTile(
    modifier: Modifier = Modifier,
    background: Brush? = null,
    padding: Int = 16,
    content: @Composable BoxScope.() -> Unit
) {
    val base = if (background != null) {
        modifier.clip(RoundedCornerShape(28.dp)).background(background)
    } else {
        modifier.premiumCard(corner = 28.dp)
    }
    Box(base.padding(padding.dp), content = content)
}

@Composable
internal fun TileHeader(text: String, color: Color, modifier: Modifier = Modifier) {
    AutoSizeText(text.uppercase(), labelStyle(FontWeight.Bold), modifier, color = color, sizingText = "UPCOMING EVENTS", alignment = Alignment.CenterStart)
}

internal val plainLabel: TextStyle get() = labelStyle(FontWeight.SemiBold).copy(letterSpacing = TextStyle.Default.letterSpacing)

// ---------------------------------------------------------------------------------------------
// Clock widgets
// ---------------------------------------------------------------------------------------------

@Composable
fun AnalogClockWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    WidgetTile(modifier, padding = 6) { AnalogStandByFace(ctx, Modifier.fillMaxSize()) }
}

@Composable
fun DigitalClockWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    WidgetTile(modifier) { StackedTime(ctx, clockStyle(FontWeight.Light), Modifier.fillMaxSize(), timeWeight = 0.6f) }
}

@Composable
fun WorldClockWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val fmt = remember(ctx.use24Hour) { DateTimeFormatter.ofPattern(if (ctx.use24Hour) "HH:mm" else "h:mm", Locale.getDefault()) }
    WidgetTile(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
            TileHeader("World clock", ctx.accent, Modifier.fillMaxWidth().weight(0.4f))
            ctx.worldClocks.take(4).forEach { tz ->
                val now = remember(tz.zoneId, ctx.time) { runCatching { ZonedDateTime.now(ZoneId.of(tz.zoneId)) }.getOrElse { ZonedDateTime.now() } }
                val shift = remember(tz.zoneId, ctx.time.hour) { zoneShiftSeconds(tz.zoneId) / 3600f }
                Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
                        AutoSizeText(tz.city, plainLabel, Modifier.fillMaxWidth().weight(0.55f), color = Ink.TextPrimary, sizingText = "San Francisco", alignment = Alignment.CenterStart)
                        AutoSizeText(
                            (if (shift >= 0) "+" else "") + (if (shift % 1f == 0f) "${shift.toInt()}h" else "%.1fh".format(shift)),
                            labelStyle(), Modifier.fillMaxWidth().weight(0.4f), color = Ink.TextTertiary, sizingText = "+10.5h", alignment = Alignment.CenterStart
                        )
                    }
                    AutoSizeText(now.format(fmt), clockStyle(FontWeight.Light), Modifier.weight(0.8f).fillMaxHeight(0.8f), color = if (now.hour in 6..17) Ink.TextPrimary else ctx.accent, sizingText = "88:88", alignment = Alignment.CenterEnd)
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Calendar
// ---------------------------------------------------------------------------------------------

@Composable
fun CalendarWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val ym = YearMonth.from(ctx.time)
    val firstDow = ym.atDay(1).dayOfWeek.value % 7 // Sunday = 0
    val days = ym.lengthOfMonth()
    val today = ctx.time.dayOfMonth
    val eventDays = remember(ctx.events, ym) {
        ctx.events.mapNotNull { ev ->
            if (ev.startMillis <= 0) null else Instant.ofEpochMilli(ev.startMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                .takeIf { YearMonth.from(it) == ym }?.dayOfMonth
        }.toSet()
    }
    WidgetTile(modifier) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().weight(0.13f), verticalAlignment = Alignment.CenterVertically) {
                TileHeader(ctx.formatDate("MMMM"), Ink.Red, Modifier.weight(1f).fillMaxHeight())
                AutoSizeText(ctx.time.year.toString(), labelStyle(), Modifier.weight(0.5f).fillMaxHeight(0.8f), color = Ink.TextTertiary, alignment = Alignment.CenterEnd)
            }
            Row(Modifier.fillMaxWidth().weight(0.1f)) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEachIndexed { i, d ->
                    AutoSizeText(d, labelStyle(FontWeight.Bold), Modifier.weight(1f).fillMaxHeight(0.8f), color = if (i == 0 || i == 6) Ink.TextTertiary else Ink.TextSecondary)
                }
            }
            val rows = (firstDow + days + 6) / 7
            for (r in 0 until rows) {
                Row(Modifier.fillMaxWidth().weight(0.77f / 6f)) {
                    for (c in 0 until 7) {
                        val day = r * 7 + c - firstDow + 1
                        Box(Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                            if (day in 1..days) {
                                val isToday = day == today
                                if (isToday) {
                                    Box(Modifier.fillMaxHeight(0.92f).aspectRatio(1f).clip(CircleShape).background(Ink.Red))
                                }
                                AutoSizeText(
                                    day.toString(), clockStyle(if (isToday) FontWeight.Bold else FontWeight.Medium),
                                    Modifier.fillMaxSize(0.62f), color = if (isToday) Color.White else if (c == 0 || c == 6) Ink.TextSecondary else Ink.TextPrimary, sizingText = "88"
                                )
                                if (!isToday && day in eventDays) {
                                    Box(Modifier.align(Alignment.BottomCenter).size(4.dp).clip(CircleShape).background(ctx.accent))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AgendaWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    WidgetTile(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
                TileHeader(ctx.formatDate("EEEE d"), Ink.Red, Modifier.weight(1f).fillMaxHeight())
            }
            val events = ctx.events.take(4)
            if (events.isEmpty()) {
                Spacer(Modifier.weight(0.3f))
                AutoSizeText("No upcoming events", plainLabel, Modifier.fillMaxWidth().height(22.dp), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
                AutoSizeText(if (ctx.nextAlarm != null) "Next alarm · ${ctx.nextAlarm}" else "Enjoy your free time", labelStyle(), Modifier.fillMaxWidth().height(16.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
                Spacer(Modifier.weight(0.7f))
            }
            events.forEach { ev ->
                Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(4.dp).fillMaxHeight(0.8f).clip(RoundedCornerShape(50)).background(ev.colorArgb?.let { Color(it) } ?: ctx.accent))
                    Column(Modifier.weight(1f).fillMaxHeight().padding(start = 10.dp), verticalArrangement = Arrangement.Center) {
                        AutoSizeText(ev.title, plainLabel, Modifier.fillMaxWidth().weight(0.55f), color = Ink.TextPrimary, sizingText = "Quarterly planning review", alignment = Alignment.CenterStart)
                        AutoSizeText(ev.timeRange + (ev.location?.let { " · $it" } ?: ""), labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.4f), color = Ink.TextSecondary, sizingText = "Tomorrow · 10:30 AM – 11:30 AM · Room 4", alignment = Alignment.CenterStart)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Weather
// ---------------------------------------------------------------------------------------------

private fun weatherBackground(condition: WeatherCondition?, isDay: Boolean): Brush = Brush.verticalGradient(
    when {
        condition == null -> listOf(Color(0xFF1C1C1E), Color(0xFF0E0E10))
        condition.isRainy -> if (isDay) listOf(Color(0xFF4A5A6E), Color(0xFF2A3442)) else listOf(Color(0xFF1C2430), Color(0xFF0D1218))
        condition.isCloudy && condition != WeatherCondition.PARTLY_CLOUDY_DAY -> if (isDay) listOf(Color(0xFF6B7B8F), Color(0xFF3E4A5A)) else listOf(Color(0xFF242A35), Color(0xFF12151B))
        isDay -> listOf(Color(0xFF3A8DF5), Color(0xFF1E5BC6))
        else -> listOf(Color(0xFF1A2550), Color(0xFF0A0F26))
    }
)

@Composable
fun WeatherWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val w = ctx.weather
    WidgetTile(modifier, background = weatherBackground(w?.condition, w?.isDay ?: true)) {
        if (w == null) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                AutoSizeText("Weather", plainLabel, Modifier.fillMaxWidth().height(24.dp), color = Color.White, alignment = Alignment.CenterStart)
                AutoSizeText("Connecting… set your city in Studio → Data", labelStyle(), Modifier.fillMaxWidth().height(16.dp), color = Color.White.copy(alpha = 0.6f), alignment = Alignment.CenterStart)
            }
            return@WidgetTile
        }
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().weight(0.5f)) {
                Column(Modifier.weight(1f).fillMaxHeight()) {
                    AutoSizeText(w.cityName.ifBlank { ctx.locationName }, plainLabel, Modifier.fillMaxWidth().weight(0.25f), color = Color.White, sizingText = "San Francisco", alignment = Alignment.CenterStart)
                    AutoSizeText(ctx.temperature(w.temperatureCelsius), clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.75f), color = Color.White, sizingText = "-88°", alignment = Alignment.CenterStart)
                }
                Column(Modifier.weight(0.8f).fillMaxHeight(), horizontalAlignment = Alignment.End) {
                    WeatherGlyph(w.condition, Modifier.weight(0.6f).aspectRatio(1f))
                    AutoSizeText(w.condition.displayName, labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().weight(0.2f), color = Color.White, alignment = Alignment.CenterEnd)
                    AutoSizeText("H:${ctx.temperature(w.highCelsius)} L:${ctx.temperature(w.lowCelsius)}", labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().weight(0.2f), color = Color.White.copy(alpha = 0.85f), alignment = Alignment.CenterEnd)
                }
            }
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.2f)))
            Row(Modifier.fillMaxWidth().weight(0.5f).padding(top = 6.dp)) {
                w.hourlyForecast.take(6).forEach { h ->
                    Column(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 3.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        AutoSizeText(h.timeLabel, labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().weight(0.25f), color = Color.White.copy(alpha = 0.85f), sizingText = "12pm")
                        WeatherGlyph(h.condition, Modifier.weight(0.45f).aspectRatio(1f))
                        AutoSizeText(ctx.temperature(h.temperatureCelsius), clockStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.3f), color = Color.White, sizingText = "-88°")
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val w = ctx.weather
    WidgetTile(modifier, background = weatherBackground(w?.condition, w?.isDay ?: true)) {
        if (w == null) {
            AutoSizeText("Forecast unavailable", plainLabel, Modifier.fillMaxWidth().height(22.dp).align(Alignment.Center), color = Color.White)
            return@WidgetTile
        }
        val days = w.dailyForecast.take(5)
        val min = days.minOfOrNull { it.lowCelsius } ?: w.lowCelsius
        val max = days.maxOfOrNull { it.highCelsius } ?: w.highCelsius
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
            TileHeader("${days.size}-day forecast", Color.White.copy(alpha = 0.8f), Modifier.fillMaxWidth().weight(0.6f))
            days.forEach { d ->
                Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    AutoSizeText(d.dayLabel, plainLabel, Modifier.weight(0.7f).fillMaxHeight(0.6f), color = Color.White, sizingText = "Today", alignment = Alignment.CenterStart)
                    WeatherGlyph(d.condition, Modifier.weight(0.4f).fillMaxHeight(0.75f))
                    AutoSizeText(ctx.temperature(d.lowCelsius), labelStyle(FontWeight.SemiBold), Modifier.weight(0.45f).fillMaxHeight(0.55f), color = Color.White.copy(alpha = 0.65f), sizingText = "-88°")
                    Canvas(Modifier.weight(1.2f).height(5.dp)) {
                        val span = (max - min).coerceAtLeast(1).toFloat()
                        drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), size.height, StrokeCap.Round)
                        val x0 = size.width * (d.lowCelsius - min) / span
                        val x1 = size.width * (d.highCelsius - min) / span
                        drawLine(Brush.horizontalGradient(listOf(temperatureColor(d.lowCelsius), temperatureColor(d.highCelsius)), x0, x1), Offset(x0, size.height / 2), Offset(x1.coerceAtLeast(x0 + 1f), size.height / 2), size.height, StrokeCap.Round)
                    }
                    AutoSizeText(ctx.temperature(d.highCelsius), labelStyle(FontWeight.Bold), Modifier.weight(0.45f).fillMaxHeight(0.55f), color = Color.White, sizingText = "-88°")
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Battery, alarm, system
// ---------------------------------------------------------------------------------------------

@Composable
fun BatteryWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val b = ctx.battery
    val color = batteryColor(b.batteryPercent)
    WidgetTile(modifier) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            RingGauge(b.batteryPercent / 100f, color, Modifier.weight(1f).fillMaxHeight(), startAngle = -90f, sweep = 360f, strokeFraction = 0.1f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AutoSizeText("${b.batteryPercent}%", clockStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().height(40.dp), color = Ink.TextPrimary, sizingText = "100%")
                    if (b.isCharging) AutoSizeText("⚡", labelStyle(), Modifier.fillMaxWidth(0.4f).height(16.dp), color = color)
                }
            }
            Column(Modifier.weight(1f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
                TileHeader("This device", color, Modifier.fillMaxWidth().height(18.dp))
                Spacer(Modifier.height(6.dp))
                AutoSizeText(b.sourceLabel(), plainLabel, Modifier.fillMaxWidth().height(20.dp), color = Ink.TextPrimary, alignment = Alignment.CenterStart)
                AutoSizeText(b.timeToFullLabel() ?: if (b.batteryPercent >= 100) "Fully charged" else " ", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(16.dp), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
                AutoSizeText(b.temperatureCelsius?.let { "%.1f °C".format(it) } ?: "", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(16.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
            }
        }
    }
}

@Composable
fun AlarmWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val alarm = ctx.nextAlarm
    val millis = ctx.nextAlarmMillis
    WidgetTile(modifier, background = Brush.verticalGradient(listOf(Color(0xFF2B1A05), Color(0xFF120B02)))) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (alarm != null) Icons.Default.Alarm else Icons.Default.AlarmOff, null, tint = Ink.Orange, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                TileHeader("Next alarm", Ink.Orange, Modifier.fillMaxWidth().height(18.dp))
            }
            Spacer(Modifier.height(8.dp))
            AutoSizeText(ctx.nextAlarmTime ?: "Off", clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.5f), color = Ink.TextPrimary, sizingText = "88:88 AM", alignment = Alignment.CenterStart)
            AutoSizeText(
                if (alarm != null && millis != null) "${alarm.substringBeforeLast(ctx.nextAlarmTime ?: "").trim().ifBlank { "Today" }} · ${com.standbypro.data.NextAlarm(millis, alarm).countdown()}" else "No alarm set",
                labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.15f), color = Ink.TextSecondary, alignment = Alignment.CenterStart
            )
        }
    }
}

private data class SystemStats(val ramUsedPct: Float, val ramText: String, val storageUsedPct: Float, val storageText: String, val uptime: String)

private fun readStats(context: Context): SystemStats {
    val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val mi = ActivityManager.MemoryInfo()
    am.getMemoryInfo(mi)
    val usedRam = (mi.totalMem - mi.availMem).toFloat()
    val stat = StatFs(Environment.getDataDirectory().path)
    val total = stat.totalBytes.toFloat()
    val free = stat.availableBytes.toFloat()
    val up = SystemClock.elapsedRealtime() / 60_000
    val gb = 1024f * 1024f * 1024f
    return SystemStats(
        ramUsedPct = usedRam / mi.totalMem,
        ramText = "%.1f / %.1f GB".format(usedRam / gb, mi.totalMem / gb),
        storageUsedPct = (total - free) / total,
        storageText = "%.0f GB free".format(free / gb),
        uptime = if (up >= 1440) "${up / 1440}d ${(up % 1440) / 60}h" else "${up / 60}h ${up % 60}m"
    )
}

@Composable
fun SystemWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val stats by produceState<SystemStats?>(initialValue = null) {
        while (true) {
            value = runCatching { readStats(context) }.getOrNull()
            kotlinx.coroutines.delay(15_000L)
        }
    }
    WidgetTile(modifier) {
        val s = stats
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceEvenly) {
            TileHeader("System", Ink.Teal, Modifier.fillMaxWidth().weight(0.5f))
            listOf(
                Triple("Memory", s?.ramUsedPct ?: 0f, s?.ramText ?: "—"),
                Triple("Storage", s?.storageUsedPct ?: 0f, s?.storageText ?: "—")
            ).forEach { (label, pct, text) ->
                Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center) {
                    Row(Modifier.fillMaxWidth().weight(0.55f)) {
                        AutoSizeText(label, plainLabel, Modifier.weight(1f).fillMaxHeight(), color = Ink.TextPrimary, alignment = Alignment.CenterStart)
                        AutoSizeText(text, labelStyle(FontWeight.Medium), Modifier.weight(1.2f).fillMaxHeight(), color = Ink.TextSecondary, alignment = Alignment.CenterEnd)
                    }
                    Box(Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.1f))) {
                        Box(Modifier.fillMaxWidth(pct.coerceIn(0.01f, 1f)).fillMaxHeight().background(Brush.horizontalGradient(listOf(Ink.Teal, if (pct > 0.85f) Ink.Red else Ink.Cyan))))
                    }
                }
            }
            Row(Modifier.fillMaxWidth().weight(0.7f), verticalAlignment = Alignment.CenterVertically) {
                AutoSizeText("Uptime", plainLabel, Modifier.weight(1f).fillMaxHeight(0.7f), color = Ink.TextPrimary, alignment = Alignment.CenterStart)
                AutoSizeText(s?.uptime ?: "—", clockStyle(FontWeight.Medium), Modifier.weight(1f).fillMaxHeight(0.7f), color = Ink.Teal, alignment = Alignment.CenterEnd)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Media
// ---------------------------------------------------------------------------------------------

@Composable
fun MusicWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val np by NowPlayingRepository.state.collectAsState()
    val access by NowPlayingRepository.accessGranted.collectAsState()
    val tick = remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    LaunchedEffect(np.isPlaying) {
        while (np.isPlaying) {
            tick.longValue = SystemClock.elapsedRealtime()
            kotlinx.coroutines.delay(1000L)
        }
    }
    val art = np.art
    WidgetTile(modifier, background = Brush.verticalGradient(listOf(Color(0xFF26262A), Color(0xFF111113))), padding = 0) {
        if (art != null) {
            Image(art, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize(), alpha = 0.35f)
            Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.2f), Color.Black.copy(alpha = 0.8f)))))
        }
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            if (!np.hasSession) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, null, tint = Ink.Pink, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(8.dp))
                    TileHeader("Now playing", Ink.Pink, Modifier.fillMaxWidth().height(18.dp))
                }
                Spacer(Modifier.weight(1f))
                AutoSizeText(if (access) "Nothing playing" else "Allow notification access", plainLabel, Modifier.fillMaxWidth().height(24.dp), color = Ink.TextPrimary, alignment = Alignment.CenterStart)
                AutoSizeText(if (access) "Start music in any app to control it here" else "Studio → Data → Notification access", labelStyle(), Modifier.fillMaxWidth().height(16.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
                Spacer(Modifier.weight(1f))
                return@Column
            }
            Row(Modifier.fillMaxWidth().weight(0.45f), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.fillMaxHeight().aspectRatio(1f).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    if (art != null) Image(art, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Icon(Icons.Default.MusicNote, null, tint = Ink.TextSecondary, modifier = Modifier.fillMaxSize(0.5f))
                }
                Column(Modifier.weight(1f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
                    AutoSizeText(np.title.ifBlank { "Unknown track" }, plainLabel.copy(fontWeight = FontWeight.Bold), Modifier.fillMaxWidth().weight(0.4f), color = Ink.TextPrimary, sizingText = "A fairly long song title", alignment = Alignment.CenterStart)
                    AutoSizeText(np.artist, plainLabel.copy(fontWeight = FontWeight.Medium), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextSecondary, sizingText = "Artist name here", alignment = Alignment.CenterStart)
                    AutoSizeText(np.appName.uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.2f), color = Ink.Pink, sizingText = "YOUTUBE MUSIC", alignment = Alignment.CenterStart)
                }
            }
            Spacer(Modifier.weight(0.06f))
            val now = tick.longValue
            Canvas(Modifier.fillMaxWidth().height(4.dp)) {
                val p = np.progressAt(now)
                drawLine(Color.White.copy(alpha = 0.2f), Offset(0f, size.height / 2), Offset(size.width, size.height / 2), size.height, StrokeCap.Round)
                drawLine(Color.White, Offset(0f, size.height / 2), Offset(size.width * p, size.height / 2), size.height, StrokeCap.Round)
            }
            Row(Modifier.fillMaxWidth().height(16.dp).padding(top = 2.dp)) {
                fun fmt(ms: Long) = "%d:%02d".format(ms / 60_000, (ms / 1000) % 60)
                AutoSizeText(fmt(np.positionAt(now)), labelStyle(), Modifier.weight(1f).fillMaxHeight(), color = Ink.TextTertiary, sizingText = "88:88", alignment = Alignment.CenterStart)
                if (np.durationMs > 0) AutoSizeText("-" + fmt(np.durationMs - np.positionAt(now)), labelStyle(), Modifier.weight(1f).fillMaxHeight(), color = Ink.TextTertiary, sizingText = "-88:88", alignment = Alignment.CenterEnd)
            }
            Row(Modifier.fillMaxWidth().weight(0.3f), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                GlassIconButton(Icons.Default.SkipPrevious, "Previous", { NowPlayingRepository.previous() }, background = Color.Transparent, size = 44.dp)
                GlassIconButton(if (np.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, "Play or pause", { NowPlayingRepository.playPause() }, background = Color.White.copy(alpha = 0.16f), size = 52.dp)
                GlassIconButton(Icons.Default.SkipNext, "Next", { NowPlayingRepository.next() }, background = Color.Transparent, size = 44.dp)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Productivity
// ---------------------------------------------------------------------------------------------

@Composable
fun TimerWidget(ctx: FaceContext, modifier: Modifier = Modifier) = WidgetTile(modifier, padding = 4) { TimerFace(ctx.copy(isCompact = true), Modifier.fillMaxSize()) }

@Composable
fun StopwatchWidget(ctx: FaceContext, modifier: Modifier = Modifier) = WidgetTile(modifier, padding = 4) { StopwatchFace(ctx.copy(isCompact = true), Modifier.fillMaxSize()) }

@Composable
fun PomodoroWidget(ctx: FaceContext, modifier: Modifier = Modifier) = WidgetTile(modifier, padding = 4) { PomodoroClockFace(ctx.copy(isCompact = true), Modifier.fillMaxSize()) }

@Composable
fun DayProgressWidget(ctx: FaceContext, modifier: Modifier = Modifier) = WidgetTile(modifier, padding = 4) { DayProgressFace(ctx, Modifier.fillMaxSize()) }

@Composable
fun CountdownWidget(ctx: FaceContext, modifier: Modifier = Modifier) = WidgetTile(modifier, padding = 4) { CountdownFace(ctx, Modifier.fillMaxSize()) }

@Composable
fun TodoWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val todos by TasksRepository.todos.collectAsState()
    WidgetTile(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth().height(20.dp), verticalAlignment = Alignment.CenterVertically) {
                TileHeader("Reminders", Ink.Blue, Modifier.weight(1f).fillMaxHeight())
                AutoSizeText("${todos.count { !it.isCompleted }}", clockStyle(FontWeight.Bold), Modifier.width(30.dp).fillMaxHeight(), color = Ink.Blue, alignment = Alignment.CenterEnd)
            }
            if (todos.isEmpty()) {
                AutoSizeText("All done — add reminders in Studio", labelStyle(), Modifier.fillMaxWidth().height(18.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
            }
            todos.take(5).forEach { item ->
                Row(
                    Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(10.dp)).clickable { TasksRepository.toggleTodo(item.id) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (item.isCompleted) Ink.Blue else Ink.TextTertiary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    AutoSizeText(item.title, plainLabel.copy(fontWeight = FontWeight.Medium), Modifier.weight(1f).fillMaxHeight(0.6f), color = if (item.isCompleted) Ink.TextTertiary else Ink.TextPrimary, sizingText = "Pick up groceries on the way", alignment = Alignment.CenterStart)
                }
            }
        }
    }
}

@Composable
fun NotesWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val notes by TasksRepository.notes.collectAsState()
    val note = notes.firstOrNull { it.isPinned } ?: notes.firstOrNull()
    WidgetTile(modifier, background = Brush.verticalGradient(listOf(Color(0xFF3A3110), Color(0xFF1C1808)))) {
        Column(Modifier.fillMaxSize()) {
            TileHeader("Notes", Ink.Yellow, Modifier.fillMaxWidth().height(18.dp))
            Spacer(Modifier.height(8.dp))
            if (note == null) {
                AutoSizeText("No notes yet", plainLabel, Modifier.fillMaxWidth().height(20.dp), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
            } else {
                AutoSizeText(note.title, plainLabel.copy(fontWeight = FontWeight.Bold), Modifier.fillMaxWidth().height(24.dp), color = Ink.TextPrimary, sizingText = "A long-ish note title", alignment = Alignment.CenterStart)
                Spacer(Modifier.height(4.dp))
                androidx.compose.material3.Text(
                    note.content,
                    color = Ink.TextSecondary,
                    style = TextStyle(fontFamily = FaceFonts.Sans, fontSize = androidx.compose.ui.unit.TextUnit(15f, androidx.compose.ui.unit.TextUnitType.Sp), lineHeight = androidx.compose.ui.unit.TextUnit(21f, androidx.compose.ui.unit.TextUnitType.Sp)),
                    maxLines = 6,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Sun & moon, notifications, photos, quote, GitHub
// ---------------------------------------------------------------------------------------------

@Composable
fun MoonWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val phase = remember(ctx.time.hour) { Astronomy.moonPhase() }
    val fmt = DateTimeFormatter.ofPattern("EEE d MMM", Locale.getDefault())
    val full = remember(ctx.time.toLocalDate()) { Instant.ofEpochMilli(Astronomy.nextPhase(0.5)).atZone(ZoneId.systemDefault()).format(fmt) }
    WidgetTile(modifier, background = Brush.verticalGradient(listOf(Color(0xFF0E1324), Color(0xFF04060C)))) {
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.weight(1f).fillMaxHeight()) {
                drawMoon(Offset(size.width / 2f, size.height / 2f), size.minDimension * 0.42f, phase.fraction)
            }
            Column(Modifier.weight(1.1f).fillMaxHeight().padding(start = 10.dp), verticalArrangement = Arrangement.Center) {
                TileHeader("Moon", Ink.Champagne, Modifier.fillMaxWidth().height(18.dp))
                AutoSizeText(phase.name, plainLabel.copy(fontWeight = FontWeight.Bold), Modifier.fillMaxWidth().height(26.dp), color = Ink.TextPrimary, sizingText = "Waxing Crescent", alignment = Alignment.CenterStart)
                AutoSizeText("${(phase.illumination * 100).toInt()}% illuminated", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(16.dp), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
                AutoSizeText("Full moon $full", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(16.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
            }
        }
    }
}

@Composable
fun SunWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val sun = remember(ctx.time.toLocalDate(), ctx.latitude, ctx.longitude) { Astronomy.sunTimes(ctx.time.toLocalDate(), ctx.latitude, ctx.longitude) }
    val fmt = DateTimeFormatter.ofPattern(if (ctx.use24Hour) "HH:mm" else "h:mm a", Locale.getDefault())
    val beforeSunrise = sun.sunrise?.let { ctx.time.isBefore(it) } ?: false
    val afterSunset = sun.sunset?.let { ctx.time.isAfter(it) } ?: false
    WidgetTile(modifier, background = Brush.verticalGradient(listOf(Color(0xFF3B2A12), Color(0xFF1A1206)))) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            TileHeader(if (beforeSunrise || afterSunset) "Sunrise" else "Sunset", Ink.Orange, Modifier.fillMaxWidth().height(18.dp))
            val main = if (beforeSunrise || afterSunset) sun.sunrise else sun.sunset
            AutoSizeText(main?.format(fmt) ?: "—", clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.45f), color = Ink.TextPrimary, sizingText = "88:88 AM", alignment = Alignment.CenterStart)
            Canvas(Modifier.fillMaxWidth().weight(0.3f)) {
                val h = size.height
                drawLine(Color.White.copy(alpha = 0.25f), Offset(0f, h * 0.8f), Offset(size.width, h * 0.8f), 2f)
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(0f, h * 0.8f)
                    quadraticTo(size.width / 2f, -h * 0.6f, size.width, h * 0.8f)
                }
                drawPath(path, Ink.Orange.copy(alpha = 0.6f), style = androidx.compose.ui.graphics.drawscope.Stroke(3f))
                val rise = sun.sunrise
                val set = sun.sunset
                if (rise != null && set != null) {
                    val f = (java.time.Duration.between(rise, ctx.time).seconds.toFloat() / java.time.Duration.between(rise, set).seconds).coerceIn(0f, 1f)
                    val x = size.width * f
                    val y = (1 - f) * (1 - f) * h * 0.8f + 2 * (1 - f) * f * (-h * 0.6f) + f * f * h * 0.8f
                    drawCircle(Color(0xFFFFD36B), h * 0.12f, Offset(x, y))
                }
            }
            AutoSizeText(
                "↑ ${sun.sunrise?.format(fmt) ?: "—"}   ↓ ${sun.sunset?.format(fmt) ?: "—"}",
                labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().weight(0.15f), color = Ink.TextSecondary, alignment = Alignment.CenterStart
            )
        }
    }
}

@Composable
fun NotificationsWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val state by NotificationRepository.notifications.collectAsState()
    WidgetTile(modifier) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth().height(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notifications, null, tint = ctx.accent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                TileHeader("Notifications", ctx.accent, Modifier.weight(1f).fillMaxHeight())
                AutoSizeText("${state.unreadCount}", clockStyle(FontWeight.Bold), Modifier.width(30.dp).fillMaxHeight(), color = ctx.accent, alignment = Alignment.CenterEnd)
            }
            if (state.notifications.isEmpty()) {
                AutoSizeText("You're all caught up", plainLabel, Modifier.fillMaxWidth().height(20.dp), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
            }
            val privacy = ctx.notificationPrivacy
            state.notifications.take(4).forEach { n ->
                Column(Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.05f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    AutoSizeText("${n.appName} · ${n.timestamp}", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.4f), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
                    val body = when (privacy) {
                        NotificationPrivacy.FULL -> "${n.title}: ${n.previewText}"
                        NotificationPrivacy.APP_NAME_ONLY, NotificationPrivacy.ICON_ONLY -> "New notification"
                        NotificationPrivacy.HIDDEN -> "Hidden"
                    }
                    AutoSizeText(body, plainLabel.copy(fontWeight = FontWeight.Medium), Modifier.fillMaxWidth().weight(0.6f), color = Ink.TextPrimary, sizingText = "Alex: are we still on for 10:30?", alignment = Alignment.CenterStart)
                }
            }
        }
    }
}

@Composable
fun PhotoWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    Box(modifier) {
        PhotoSlideshow(ctx, Modifier.fillMaxSize(), corner = 28)
        AutoSizeText(ctx.formatDate("EEE d MMM").uppercase(), labelStyle(FontWeight.Bold).copy(shadow = androidx.compose.ui.graphics.Shadow(Color.Black.copy(alpha = 0.6f), Offset(0f, 2f), 8f)), Modifier.align(Alignment.BottomStart).padding(14.dp).fillMaxWidth(0.5f).height(18.dp), color = Color.White, alignment = Alignment.CenterStart)
    }
}

private val quotes = listOf(
    "The best time to plant a tree was 20 years ago. The second best time is now." to "Chinese proverb",
    "Simplicity is the ultimate sophistication." to "Leonardo da Vinci",
    "We are what we repeatedly do. Excellence, then, is not an act, but a habit." to "Will Durant",
    "Make it work, make it right, make it fast." to "Kent Beck",
    "The secret of getting ahead is getting started." to "Mark Twain",
    "Lost time is never found again." to "Benjamin Franklin",
    "Rest is not idleness." to "John Lubbock",
    "What we fear doing most is usually what we most need to do." to "Tim Ferriss",
    "Design is not just what it looks like. Design is how it works." to "Steve Jobs",
    "Stay hungry, stay foolish." to "Whole Earth Catalog",
    "It always seems impossible until it's done." to "Nelson Mandela",
    "Well begun is half done." to "Aristotle",
    "Done is better than perfect." to "Sheryl Sandberg",
    "Be so good they can't ignore you." to "Steve Martin",
    "The only way to do great work is to love what you do." to "Steve Jobs",
    "Sleep is the best meditation." to "Dalai Lama",
    "You miss 100% of the shots you don't take." to "Wayne Gretzky",
    "Time you enjoy wasting is not wasted time." to "Marthe Troly-Curtin",
    "A year from now you may wish you had started today." to "Karen Lamb",
    "Small deeds done are better than great deeds planned." to "Peter Marshall",
    "Focus is saying no to a thousand good ideas." to "Steve Jobs",
    "Tomorrow is the first blank page of a 365-page book." to "Brad Paisley",
    "The present moment always will have been." to "Unknown",
    "Every moment is a fresh beginning." to "T. S. Eliot"
)

@Composable
fun QuoteWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val (quote, author) = quotes[ctx.time.dayOfYear % quotes.size]
    WidgetTile(modifier, background = Brush.linearGradient(listOf(lerpColor(ctx.accent, Color.Black, 0.65f), Color(0xFF0B0B0D)))) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            TileHeader("Quote of the day", ctx.accent, Modifier.fillMaxWidth().height(18.dp))
            Spacer(Modifier.height(10.dp))
            androidx.compose.material3.Text(
                "“$quote”",
                color = Ink.TextPrimary,
                style = TextStyle(fontFamily = FaceFonts.Serif, fontSize = androidx.compose.ui.unit.TextUnit(20f, androidx.compose.ui.unit.TextUnitType.Sp), lineHeight = androidx.compose.ui.unit.TextUnit(27f, androidx.compose.ui.unit.TextUnitType.Sp)),
                maxLines = 5
            )
            Spacer(Modifier.height(8.dp))
            AutoSizeText("— $author", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(16.dp), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
        }
    }
}

@Composable
fun GitHubWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val state by GitHubRepository.contributionsState.collectAsState()
    WidgetTile(modifier) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            GitHubContributionWidget(state = state, accentColor = ctx.accent, isCompact = false)
        }
    }
}
