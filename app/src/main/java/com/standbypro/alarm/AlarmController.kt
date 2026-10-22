package com.standbypro.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.standbypro.MainActivity
import com.standbypro.settings.SettingsRepository
import com.standbypro.settings.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

/** Ties scheduling, ringing, snoozing and the ringing notification together. */
object AlarmController {

    const val ACTION_RING = "com.standbypro.alarm.RING"
    const val ACTION_SNOOZE = "com.standbypro.alarm.SNOOZE"
    const val ACTION_DISMISS = "com.standbypro.alarm.DISMISS"

    private const val CHANNEL_ID = "standby_alarm_channel"
    private const val NOTIFICATION_ID = 7

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _snoozedUntil = MutableStateFlow<Long?>(null)
    /** Wall-clock time a snoozed alarm will ring again (kept in memory). */
    val snoozedUntil: StateFlow<Long?> = _snoozedUntil.asStateFlow()

    private fun settings(context: Context) = SettingsRepository(context.applicationContext.dataStore)

    /** Registers the next ring: the regular schedule or a pending snooze, whichever comes first. */
    fun schedule(context: Context, alarm: BedsideAlarm) {
        val now = System.currentTimeMillis()
        val regular = AlarmMath.nextTrigger(alarm, ZonedDateTime.now())?.toInstant()?.toEpochMilli()
        val snooze = _snoozedUntil.value?.takeIf { it > now }
        val trigger = listOfNotNull(regular, snooze).minOrNull()
        if (trigger == null) AlarmScheduler.cancel(context) else AlarmScheduler.set(context, trigger)
    }

    fun rescheduleFromSettings(context: Context, onDone: () -> Unit = {}) {
        scope.launch {
            try {
                schedule(context, settings(context).settingsFlow.first().alarm)
            } finally {
                onDone()
            }
        }
    }

    /** Called by the alarm broadcast; [onDone] lets the receiver keep the process alive until ringing. */
    fun onRing(context: Context, onDone: () -> Unit = {}) {
        val app = context.applicationContext
        scope.launch {
            try {
                val repo = settings(app)
                val alarm = repo.settingsFlow.first().alarm
                _snoozedUntil.value = null
                ring(app, alarm)
                // One-off alarms switch themselves off; repeating ones book the next day.
                if (alarm.days.isEmpty()) repo.setAlarm(alarm.copy(enabled = false)) else schedule(app, alarm)
            } finally {
                onDone()
            }
        }
    }

    /** Rings right now without touching the schedule (Studio's "Preview"). */
    fun preview(context: Context, alarm: BedsideAlarm) = ring(context.applicationContext, alarm)

    private fun ring(context: Context, alarm: BedsideAlarm) {
        AlarmRinger.start(context, alarm)
        postRingingNotification(context, alarm)
        runCatching { context.startActivity(ringActivityIntent(context)) }
    }

    fun snooze(context: Context) {
        val app = context.applicationContext
        val minutes = AlarmRinger.ringing.value?.snoozeMinutes ?: 9
        AlarmRinger.stop()
        cancelNotification(app)
        _snoozedUntil.value = System.currentTimeMillis() + minutes * 60_000L
        rescheduleFromSettings(app)
    }

    fun dismiss(context: Context) {
        val app = context.applicationContext
        AlarmRinger.stop()
        cancelNotification(app)
        clearSnooze()
        rescheduleFromSettings(app)
    }

    fun clearSnooze() {
        _snoozedUntil.value = null
    }

    fun formatSnooze(millis: Long, use24Hour: Boolean): String =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
            .format(java.time.format.DateTimeFormatter.ofPattern(if (use24Hour) "HH:mm" else "h:mm a"))

    private fun ringActivityIntent(context: Context): Intent = Intent(context, MainActivity::class.java).apply {
        action = MainActivity.ACTION_ALARM_RING
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
    }

    private fun postRingingNotification(context: Context, alarm: BedsideAlarm) {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Alarm", NotificationManager.IMPORTANCE_HIGH).apply {
                description = "StandBy bedside alarm"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
        )
        fun action(action: String, code: Int) = PendingIntent.getBroadcast(
            context, code, Intent(context, AlarmReceiver::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val open = PendingIntent.getActivity(context, 43, ringActivityIntent(context), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarm.label)
            .setContentText("Tap to open · snooze ${alarm.snoozeMinutes} min or stop")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setContentIntent(open)
            .setFullScreenIntent(open, true)
            .addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze", action(ACTION_SNOOZE, 44))
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", action(ACTION_DISMISS, 45))
            .build()
        manager.notify(NOTIFICATION_ID, notification)
    }

    private fun cancelNotification(context: Context) {
        context.getSystemService(NotificationManager::class.java)?.cancel(NOTIFICATION_ID)
    }
}
