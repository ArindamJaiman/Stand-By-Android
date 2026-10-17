package com.standbypro.data

import android.app.AlarmManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** The next system alarm (from any clock app) as reported by [AlarmManager.getNextAlarmClock]. */
data class NextAlarm(val triggerAtMillis: Long, val label: String, val timeLabel: String = label) {
    fun countdown(now: Long = System.currentTimeMillis()): String {
        val minutes = ((triggerAtMillis - now) / 60_000).coerceAtLeast(0)
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "in ${h}h ${m}m" else "in ${m}m"
    }
}

object AlarmRepository {

    private val _nextAlarm = MutableStateFlow<NextAlarm?>(null)
    val nextAlarm: StateFlow<NextAlarm?> = _nextAlarm.asStateFlow()

    fun refresh(context: Context, use24Hour: Boolean) {
        val manager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        val info = manager?.nextAlarmClock
        if (info == null) {
            _nextAlarm.value = null
            return
        }
        val zone = ZoneId.systemDefault()
        val at = Instant.ofEpochMilli(info.triggerTime).atZone(zone)
        val today = LocalDate.now(zone)
        val timeFmt = DateTimeFormatter.ofPattern(if (use24Hour) "HH:mm" else "h:mm a", Locale.getDefault())
        val prefix = when (at.toLocalDate()) {
            today -> ""
            today.plusDays(1) -> "Tomorrow "
            else -> at.format(DateTimeFormatter.ofPattern("EEE ", Locale.getDefault()))
        }
        _nextAlarm.value = NextAlarm(info.triggerTime, prefix + at.format(timeFmt), at.format(timeFmt))
    }
}
