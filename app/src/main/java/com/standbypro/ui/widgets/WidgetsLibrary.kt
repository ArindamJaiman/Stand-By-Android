package com.standbypro.ui.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.data.DemoDataProvider
import com.standbypro.data.ProductivityRepository
import com.standbypro.data.TasksRepository
import com.standbypro.data.WeatherRepository
import com.standbypro.domain.ChargingState
import com.standbypro.domain.FocusSessionType
import com.standbypro.media.MusicRepository
import com.standbypro.ui.components.GitHubContributionWidget
import com.standbypro.ui.components.MonthCalendarWidget
import java.time.LocalDateTime

@Composable
fun CalendarStandByWidget(
    time: LocalDateTime,
    chargingState: ChargingState,
    nextAlarm: String?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    MonthCalendarWidget(
        time = time,
        chargingState = chargingState,
        nextAlarm = nextAlarm,
        accentColor = accentColor,
        modifier = modifier
    )
}

@Composable
fun AgendaStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val events by TasksRepository.events.collectAsState()

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "UPCOMING SCHEDULE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            events.take(3).forEach { ev ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = ev.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(text = ev.timeRange + if (ev.location != null) " • ${ev.location}" else "", fontSize = 11.sp, color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val weather by WeatherRepository.weatherFlow.collectAsState(initial = DemoDataProvider.demoWeather)

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF12141A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = weather.cityName.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
                Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = accentColor, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = "${weather.temperatureCelsius}°", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = weather.condition.displayName, fontSize = 14.sp, color = Color.White.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "H:${weather.highCelsius}°  L:${weather.lowCelsius}°  •  Wind ${weather.windKmh} km/h", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun BatteryStandByWidget(
    chargingState: ChargingState,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "POWER STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
                Icon(imageVector = Icons.Default.BatteryChargingFull, contentDescription = null, tint = if (chargingState.isCharging) Color(0xFF30D158) else Color.White, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "${chargingState.batteryPercent}%", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { chargingState.batteryPercent / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = if (chargingState.isCharging) Color(0xFF30D158) else accentColor,
                trackColor = Color(0xFF222222),
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (chargingState.isCharging) "Charging (${chargingState.chargeType})" else "Discharging on battery",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun MusicStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val playerState by MusicRepository.playerState.collectAsState()

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141418))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "NOW PLAYING", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
                Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = playerState.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1)
            Text(text = playerState.artist, fontSize = 12.sp, color = Color.Gray, maxLines = 1)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { playerState.progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = Color(0xFF2B2B30)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${playerState.formattedPosition} / ${playerState.formattedDuration}", fontSize = 10.sp, color = Color.Gray)
                Row {
                    IconButton(onClick = { MusicRepository.previousTrack() }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = { MusicRepository.togglePlayPause() }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Play/Pause", tint = accentColor, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = { MusicRepository.nextTrack() }, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.FastForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LyricsStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val playerState by MusicRepository.playerState.collectAsState()

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF10121A))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "LIVE LYRICS • ${playerState.artist.uppercase()}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = playerState.currentLyric, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
fun TimerStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val timer by ProductivityRepository.timerState.collectAsState()

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "TIMER", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = timer.formattedRemaining, fontSize = 42.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
            LinearProgressIndicator(
                progress = { timer.progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = accentColor,
                trackColor = Color(0xFF222222)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor)
                        .clickable { if (timer.isRunning) ProductivityRepository.pauseTimer() else ProductivityRepository.startTimer() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = if (timer.isRunning) "PAUSE" else "START", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF222222))
                        .clickable { ProductivityRepository.resetTimer() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = "RESET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun StopwatchStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sw by ProductivityRepository.stopwatchState.collectAsState()

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "STOPWATCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = sw.formattedElapsed, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(accentColor)
                        .clickable { if (sw.isRunning) ProductivityRepository.pauseStopwatch() else ProductivityRepository.startStopwatch() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = if (sw.isRunning) "STOP" else "START", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
                if (sw.isRunning) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF222222))
                            .clickable { ProductivityRepository.recordLap() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "LAP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF222222))
                            .clickable { ProductivityRepository.resetStopwatch() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(text = "RESET", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun PomodoroStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val pomo by ProductivityRepository.pomodoroState.collectAsState()

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF181212))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = pomo.currentType.displayName.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3B30), letterSpacing = 1.5.sp)
                Text(text = "★ ${pomo.completedCount} DONE", fontSize = 10.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = pomo.formattedRemaining, fontSize = 40.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
            LinearProgressIndicator(
                progress = { pomo.progress },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFFF3B30),
                trackColor = Color(0xFF2B1616)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFF3B30))
                        .clickable { if (pomo.isRunning) ProductivityRepository.pausePomodoro() else ProductivityRepository.startPomodoro() }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = if (pomo.isRunning) "PAUSE" else "FOCUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF241A1A))
                        .clickable {
                            val nextType = if (pomo.currentType == FocusSessionType.POMODORO) FocusSessionType.SHORT_BREAK else FocusSessionType.POMODORO
                            ProductivityRepository.setPomodoroType(nextType)
                        }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(text = "SWITCH", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WorldClockStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val clocks = DemoDataProvider.demoWorldClocks.take(2)

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "WORLD CLOCKS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            clocks.forEach { clock ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = clock.city, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                        Text(text = clock.getFormattedOffset(), fontSize = 10.sp, color = Color.Gray)
                    }
                    Text(text = clock.getFormattedTime(false), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accentColor)
                }
            }
        }
    }
}

@Composable
fun TodoStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val todos by TasksRepository.todos.collectAsState()

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "TODAY'S TASKS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            todos.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { TasksRepository.toggleTodo(item.id) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (item.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (item.isCompleted) Color(0xFF30D158) else Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.title,
                        fontSize = 13.sp,
                        color = if (item.isCompleted) Color.Gray else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun NotesStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val notes by TasksRepository.notes.collectAsState()
    val pinnedNote = notes.firstOrNull { it.isPinned } ?: notes.firstOrNull()

    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF151518))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "BEDSIDE NOTE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(8.dp))
            if (pinnedNote != null) {
                Text(text = pinnedNote.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = pinnedNote.content, fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f), maxLines = 3, overflow = TextOverflow.Ellipsis)
            } else {
                Text(text = "No notes pinned", fontSize = 13.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun SystemStatusStandByWidget(
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "SYSTEM STATUS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Kernel Uptime", fontSize = 12.sp, color = Color.Gray)
                Text(text = "142h 18m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "RAM Available", fontSize = 12.sp, color = Color.Gray)
                Text(text = "5.8 GB / 12 GB", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Light Sensor", fontSize = 12.sp, color = Color.Gray)
                Text(text = "Active (18 lux)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor)
            }
        }
    }
}

@Composable
fun AlarmStandByWidget(
    nextAlarm: String?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxSize().padding(8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF141416))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "NEXT ALARM", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 1.5.sp)
                Icon(imageVector = Icons.Default.Alarm, contentDescription = null, tint = accentColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = nextAlarm ?: "No Alarm Set", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = if (nextAlarm != null) "Alarm active & scheduled" else "Set an alarm in Clock app", fontSize = 12.sp, color = Color.Gray)
        }
    }
}
