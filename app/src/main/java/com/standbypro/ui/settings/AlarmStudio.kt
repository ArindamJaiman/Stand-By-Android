package com.standbypro.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.alarm.AlarmController
import com.standbypro.alarm.AlarmMath
import com.standbypro.alarm.BedsideAlarm
import com.standbypro.settings.StandBySettings
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.design.Ink
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private fun BedsideAlarm.timeLabel(use24Hour: Boolean): String =
    LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern(if (use24Hour) "HH:mm" else "h:mm a", Locale.getDefault()))

fun LazyListScope.alarmSection(viewModel: StandByViewModel, settings: StandBySettings) {
    item(key = "b_alarm") {
        val alarm = settings.alarm
        val context = LocalContext.current
        val resumeTick by viewModel.resumeTick.collectAsState()
        val canSchedule = remember(resumeTick) { viewModel.canScheduleAlarms() }
        val snoozed by viewModel.alarmSnoozedUntil.collectAsState()
        var picking by remember { mutableStateOf(false) }
        val now = ZonedDateTime.now()
        val next = AlarmMath.nextTrigger(alarm, now)
        val accent = Ink.Orange

        StudioSection(
            "Bedside alarm",
            footer = "Rings even when StandBy isn't showing, and appears as your next alarm, so the sunrise light glows before it."
        ) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f).clickable { picking = true }) {
                    Text(alarm.timeLabel(settings.use24Hour), color = if (alarm.enabled) Ink.TextPrimary else Ink.TextTertiary, fontSize = 40.sp, fontWeight = FontWeight.Light)
                    val status = when {
                        snoozed != null -> "Snoozed until ${AlarmController.formatSnooze(snoozed!!, settings.use24Hour)}"
                        next != null -> "${AlarmMath.describeDays(alarm.days)} · ${AlarmMath.ringsIn(now, next)}"
                        else -> "${AlarmMath.describeDays(alarm.days)} · Off"
                    }
                    Text(status, color = if (alarm.enabled) accent else Ink.TextTertiary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Switch(
                    checked = alarm.enabled,
                    onCheckedChange = { viewModel.setAlarm(alarm.copy(enabled = it)) },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Ink.Green, uncheckedThumbColor = Color.White, uncheckedTrackColor = Color(0xFF3A3A3C), uncheckedBorderColor = Color.Transparent)
                )
            }
            RowDivider()
            SubLabel("Repeat")
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                DayOfWeek.entries.forEach { day ->
                    val on = day in alarm.days
                    Box(
                        Modifier.size(38.dp).clip(CircleShape)
                            .background(if (on) accent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.06f))
                            .border(1.dp, if (on) accent.copy(alpha = 0.8f) else Color.Transparent, CircleShape)
                            .clickable { viewModel.setAlarm(alarm.copy(days = if (on) alarm.days - day else alarm.days + day)) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(day.getDisplayName(TextStyle.NARROW, Locale.getDefault()), color = if (on) accent else Ink.TextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            RowDivider()
            ToggleRow("Gentle wake", "Fade the sound in over a minute", alarm.gentleWake, { viewModel.setAlarm(alarm.copy(gentleWake = it)) }, icon = Icons.Default.AlarmOn, iconTint = accent)
            SubLabel("Snooze length")
            ChipRow(listOf(5, 9, 10, 15), { it == alarm.snoozeMinutes }, { "$it min" }, { viewModel.setAlarm(alarm.copy(snoozeMinutes = it)) }, accent)
            RowDivider()
            NavRow("Preview alarm", "See and hear it ring now", icon = Icons.Default.PlayArrow, iconTint = Ink.Green) { viewModel.previewAlarm() }
            if (!canSchedule && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                RowDivider()
                NavRow("Allow exact alarms", "Needed to ring at exactly the right time", icon = Icons.Default.AlarmOn, iconTint = Ink.Red) {
                    runCatching { context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) }
                }
            }
        }
        if (picking) {
            AlarmTimeDialog(alarm.hour, alarm.minute, settings.use24Hour, onDismiss = { picking = false }) { h, m ->
                viewModel.setAlarm(alarm.copy(hour = h, minute = m, enabled = true))
                picking = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmTimeDialog(hour: Int, minute: Int, is24Hour: Boolean, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = is24Hour)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("Set alarm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { TimePicker(state = state) },
        containerColor = Ink.CardHigh
    )
}
