package com.standbypro.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.standbypro.data.ProductivityRepository
import com.standbypro.domain.FaceContext
import com.standbypro.domain.FocusSessionType
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.GlassIconButton
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.PillButton
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.premiumCard
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale

/** Big progress ring with content in the middle. */
@Composable
private fun ProgressRing(progress: Float, color: Color, modifier: Modifier, track: Color = color.copy(alpha = 0.15f), ticks: Int = 60, content: @Composable () -> Unit) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * 0.06f
            val d = size.minDimension - stroke * 2.2f
            val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            val c = Offset(size.width / 2f, size.height / 2f)
            for (i in 0 until ticks) {
                val a = Math.toRadians(i * 360.0 / ticks - 90.0)
                val r1 = size.minDimension / 2f
                val r0 = r1 - stroke * 0.6f
                val lit = i.toFloat() / ticks < progress
                drawLine(
                    if (lit) color.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.08f),
                    Offset(c.x + (kotlin.math.cos(a) * r0).toFloat(), c.y + (kotlin.math.sin(a) * r0).toFloat()),
                    Offset(c.x + (kotlin.math.cos(a) * r1).toFloat(), c.y + (kotlin.math.sin(a) * r1).toFloat()),
                    stroke * 0.12f, StrokeCap.Round
                )
            }
            drawArc(track, -90f, 360f, false, tl, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round))
            drawArc(Brush.sweepGradient(listOf(color.copy(alpha = 0.55f), color, color.copy(alpha = 0.55f)), c), -90f, 360f * progress.coerceIn(0f, 1f), false, tl, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round))
        }
        Box(Modifier.fillMaxSize(0.66f), contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
fun PomodoroClockFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val state by ProductivityRepository.pomodoroState.collectAsState()
    val color = if (state.currentType == FocusSessionType.POMODORO) Color(0xFFFF453A) else Ink.Green
    Row(modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ProgressRing(state.progress, color, Modifier.weight(1f).fillMaxHeight()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AutoSizeText(state.currentType.displayName.uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.8f).height(18.dp), color = color)
                AutoSizeText(state.formattedRemaining, clockStyle(FontWeight.Light, FaceFonts.Sans), Modifier.fillMaxWidth().height(80.dp), color = Ink.TextPrimary, sizingText = "88:88")
            }
        }
        Column(Modifier.weight(0.8f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextSecondary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            Row(Modifier.fillMaxWidth().weight(0.12f), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(4) { i ->
                    val done = i < state.completedCount % 4 || (state.completedCount > 0 && state.completedCount % 4 == 0 && state.currentType == FocusSessionType.LONG_BREAK)
                    Box(Modifier.weight(1f).height(6.dp).clip(RoundedCornerShape(50)).background(if (done) color else Color.White.copy(alpha = 0.12f)))
                }
            }
            AutoSizeText("${state.completedCount} sessions today", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.08f), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
            Spacer(Modifier.weight(0.05f))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassIconButton(if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, "Start or pause", { ProductivityRepository.togglePomodoro() }, background = color.copy(alpha = 0.25f), tint = color)
                GlassIconButton(Icons.Default.Refresh, "Reset", { ProductivityRepository.resetPomodoro() })
            }
            Spacer(Modifier.weight(0.05f))
            if (!ctx.isCompact) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FocusSessionType.entries.filter { it != FocusSessionType.CUSTOM }.forEach { type ->
                    PillButton(
                        "${type.defaultMinutes}m", { ProductivityRepository.setPomodoroType(type) },
                        container = if (type == state.currentType) color.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                        content = if (type == state.currentType) color else Ink.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun TimerFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val state by ProductivityRepository.timerState.collectAsState()
    val color = if (state.isCompleted) Ink.Green else ctx.accent
    Row(modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ProgressRing(1f - state.progress, color, Modifier.weight(1f).fillMaxHeight()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AutoSizeText(if (state.isCompleted) "DONE" else if (state.isRunning) "TIMER" else "PAUSED", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.6f).height(18.dp), color = color)
                AutoSizeText(state.formattedRemaining, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().height(80.dp), color = Ink.TextPrimary, sizingText = "88:88")
            }
        }
        Column(Modifier.weight(0.8f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextSecondary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassIconButton(if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, "Start or pause", { ProductivityRepository.toggleTimer() }, background = color.copy(alpha = 0.25f), tint = color)
                GlassIconButton(Icons.Default.Add, "Add a minute", { ProductivityRepository.addTimerMinute() })
                GlassIconButton(Icons.Default.Refresh, "Reset", { ProductivityRepository.resetTimer() })
            }
            Spacer(Modifier.height(10.dp))
            if (!ctx.isCompact) Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ProductivityRepository.timerPresets.chunked(4).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        row.forEach { secs ->
                            val selected = secs == state.totalSeconds
                            PillButton(
                                if (secs >= 3600) "${secs / 3600}h" else "${secs / 60}m",
                                { ProductivityRepository.setTimerDuration(secs) },
                                container = if (selected) color.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f),
                                content = if (selected) color else Ink.TextSecondary
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.weight(0.1f))
        }
    }
}

@Composable
fun StopwatchFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val state by ProductivityRepository.stopwatchState.collectAsState()
    val seconds = (state.elapsedMs % 60_000L) / 60_000f
    Row(modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        ProgressRing(seconds, ctx.accent, Modifier.weight(1f).fillMaxHeight()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AutoSizeText("STOPWATCH", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.7f).height(18.dp), color = ctx.accent)
                AutoSizeText(state.formattedElapsed, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().height(70.dp), color = Ink.TextPrimary, sizingText = "88:88.88")
            }
        }
        Column(Modifier.weight(0.8f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassIconButton(if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, "Start or pause", { ProductivityRepository.toggleStopwatch() }, background = ctx.accent.copy(alpha = 0.25f), tint = ctx.accent)
                GlassIconButton(Icons.Default.Flag, "Lap", { ProductivityRepository.recordLap() })
                GlassIconButton(Icons.Default.Refresh, "Reset", { ProductivityRepository.resetStopwatch() })
            }
            Spacer(Modifier.height(10.dp))
            val fastest = state.laps.minByOrNull { it.lapTimeMs }
            val slowest = state.laps.maxByOrNull { it.lapTimeMs }
            state.laps.take(5).forEach { lap ->
                val color = when {
                    state.laps.size < 3 -> Ink.TextSecondary
                    lap == fastest -> Ink.Green
                    lap == slowest -> Ink.Red
                    else -> Ink.TextSecondary
                }
                Row(Modifier.fillMaxWidth().height(24.dp), verticalAlignment = Alignment.CenterVertically) {
                    AutoSizeText("Lap ${lap.lapIndex}", labelStyle(FontWeight.Medium), Modifier.weight(1f).fillMaxHeight(0.7f), color = color, alignment = Alignment.CenterStart)
                    AutoSizeText(com.standbypro.domain.StopwatchState(elapsedMs = lap.lapTimeMs).formattedElapsed, clockStyle(FontWeight.Medium), Modifier.weight(1f).fillMaxHeight(0.7f), color = color, alignment = Alignment.CenterEnd)
                }
            }
            if (state.laps.isEmpty()) {
                AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().height(48.dp), color = Ink.TextTertiary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            }
        }
    }
}

/** Calm deep-work clock: time, today's focus streak and current session. */
@Composable
fun FocusClockFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val state by ProductivityRepository.pomodoroState.collectAsState()
    Column(modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(0.1f))
        AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth(0.85f).weight(0.45f), color = Ink.TextPrimary, sizingText = ctx.sizingTime())
        Box(Modifier.fillMaxWidth(0.6f).height(4.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.1f))) {
            Box(Modifier.fillMaxWidth(state.progress.coerceIn(0.001f, 1f)).fillMaxHeight().background(ctx.accent))
        }
        Spacer(Modifier.height(10.dp))
        AutoSizeText(
            if (state.isRunning) "${state.currentType.displayName} · ${state.formattedRemaining} left" else "Tap play to start a ${state.currentType.defaultMinutes}-minute ${state.currentType.displayName.lowercase()}",
            labelStyle(FontWeight.Medium), Modifier.fillMaxWidth(0.8f).weight(0.08f), color = Ink.TextSecondary
        )
        Spacer(Modifier.weight(0.04f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            GlassIconButton(if (state.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow, "Start or pause focus", { ProductivityRepository.togglePomodoro() }, background = ctx.accent.copy(alpha = 0.22f), tint = ctx.accent)
            GlassIconButton(Icons.Default.Refresh, "Reset", { ProductivityRepository.resetPomodoro() })
        }
        Spacer(Modifier.weight(0.1f))
    }
}

/** Day, week, month and year progress bars. */
@Composable
fun DayProgressFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val now = ctx.time
    val day = (now.toLocalTime().toSecondOfDay() / 86400f)
    val weekStart = now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay()
    val week = Duration.between(weekStart, now).seconds / (7 * 86400f)
    val monthStart = now.toLocalDate().withDayOfMonth(1).atStartOfDay()
    val month = Duration.between(monthStart, now).seconds / (now.toLocalDate().lengthOfMonth() * 86400f)
    val yearStart = now.toLocalDate().withDayOfYear(1).atStartOfDay()
    val year = Duration.between(yearStart, now).seconds / (now.toLocalDate().lengthOfYear() * 86400f)
    val rows = listOf(
        Triple("Today", day, Ink.Orange),
        Triple("This week", week, Ink.Yellow),
        Triple(now.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault())), month, Ink.Mint),
        Triple(now.year.toString(), year, Ink.Cyan)
    )
    Row(modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(0.75f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.35f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("EEEE d").uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.08f), color = ctx.accent, alignment = Alignment.CenterStart)
            AutoSizeText("Day ${now.dayOfYear} of ${now.toLocalDate().lengthOfYear()}", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.07f), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
        }
        Column(Modifier.weight(1.25f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.SpaceEvenly) {
            rows.forEach { (label, value, color) ->
                Column(Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.Center) {
                    Row(Modifier.fillMaxWidth().weight(0.5f)) {
                        AutoSizeText(label.uppercase(), labelStyle(FontWeight.Bold), Modifier.weight(1f).fillMaxHeight(), color = Ink.TextSecondary, sizingText = "SEPTEMBER", alignment = Alignment.CenterStart)
                        AutoSizeText("${(value * 100).toInt()}%", clockStyle(FontWeight.Medium), Modifier.weight(0.5f).fillMaxHeight(), color = color, sizingText = "100%", alignment = Alignment.CenterEnd)
                    }
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.08f))) {
                        Box(Modifier.fillMaxWidth(value.coerceIn(0.005f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), color))))
                    }
                }
            }
        }
    }
}

/** Countdown to a configurable date (New Year by default). */
@Composable
fun CountdownFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val target = ctx.countdownDate.atStartOfDay()
    val now = ctx.time
    val total = Duration.between(now, target).seconds.coerceAtLeast(0)
    val days = total / 86400
    val hours = (total % 86400) / 3600
    val minutes = (total % 3600) / 60
    val seconds = total % 60
    Column(modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(0.08f))
        AutoSizeText(ctx.countdownLabel.uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.6f).weight(0.1f), color = ctx.accent)
        Row(Modifier.fillMaxWidth().weight(0.46f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(days to "DAYS", hours to "HRS", minutes to "MIN", seconds to "SEC").forEachIndexed { i, (v, l) ->
                Column(Modifier.weight(if (i == 0) 1.3f else 1f).fillMaxHeight().premiumCard(corner = 20.dp).padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AutoSizeText(if (i == 0) v.toString() else "%02d".format(v), clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.72f), color = Ink.TextPrimary, sizingText = if (i == 0) "888" else "88")
                    AutoSizeText(l, labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.6f).weight(0.2f), color = Ink.TextTertiary)
                }
            }
        }
        Spacer(Modifier.weight(0.05f))
        AutoSizeText(
            "${target.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))}  ·  now ${ctx.timeText}",
            labelStyle(FontWeight.Medium), Modifier.fillMaxWidth(0.8f).weight(0.08f), color = Ink.TextSecondary
        )
        Spacer(Modifier.weight(0.08f))
    }
}

/** Today's agenda next to the clock. */
@Composable
fun AgendaFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val upcoming = ctx.events.take(4)
    Row(modifier.fillMaxSize().padding(14.dp)) {
        Column(Modifier.weight(0.8f).fillMaxHeight(), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.formatDate("EEEE").uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.08f), color = Ink.Red, alignment = Alignment.CenterStart)
            AutoSizeText(ctx.time.dayOfMonth.toString(), clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextPrimary, sizingText = "88", alignment = Alignment.CenterStart)
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.2f), color = Ink.TextSecondary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
        }
        Column(Modifier.weight(1.2f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)) {
            if (upcoming.isEmpty()) {
                AutoSizeText("No upcoming events", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(24.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
                AutoSizeText("Grant calendar access in Studio", labelStyle(), Modifier.fillMaxWidth().height(18.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
            }
            upcoming.forEach { ev ->
                val color = ev.colorArgb?.let { Color(it) } ?: ctx.accent
                Row(Modifier.fillMaxWidth().height(48.dp).premiumCard(corner = 14.dp).padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(4.dp).fillMaxHeight().clip(RoundedCornerShape(50)).background(color))
                    Column(Modifier.weight(1f).padding(start = 8.dp)) {
                        AutoSizeText(ev.title, labelStyle(FontWeight.SemiBold).copy(letterSpacing = androidx.compose.ui.text.TextStyle.Default.letterSpacing), Modifier.fillMaxWidth().weight(0.55f), color = Ink.TextPrimary, sizingText = "Design review with the team", alignment = Alignment.CenterStart)
                        AutoSizeText(ev.timeRange + (ev.location?.let { " · $it" } ?: ""), labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.45f), color = Ink.TextSecondary, sizingText = "Tomorrow · 10:30 AM – 11:30 AM · Room", alignment = Alignment.CenterStart)
                    }
                    if (ev.startMillis > 0) {
                        val minutes = ChronoUnit.MINUTES.between(LocalDateTime.now(), java.time.Instant.ofEpochMilli(ev.startMillis).atZone(java.time.ZoneId.systemDefault()).toLocalDateTime())
                        if (minutes in 0..180) AutoSizeText("in ${minutes}m", labelStyle(FontWeight.Bold), Modifier.width(56.dp).fillMaxHeight(0.5f), color = color)
                    }
                }
            }
        }
    }
}
