package com.standbypro.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.standbypro.MainActivity

/**
 * Registers the alarm with [AlarmManager.setAlarmClock], so it fires exactly even in Doze,
 * shows in the status bar and becomes the system "next alarm" (which also drives the sunrise
 * wake light).
 */
object AlarmScheduler {

    private const val REQUEST_RING = 41
    private const val REQUEST_SHOW = 42

    fun canSchedule(context: Context): Boolean {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()
    }

    fun set(context: Context, triggerAtMillis: Long) {
        val manager = context.getSystemService(AlarmManager::class.java) ?: return
        val show = PendingIntent.getActivity(
            context, REQUEST_SHOW,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            manager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAtMillis, show), ringIntent(context))
        } catch (e: SecurityException) {
            Log.w("AlarmScheduler", "Exact alarms not allowed", e)
        }
    }

    fun cancel(context: Context) {
        context.getSystemService(AlarmManager::class.java)?.cancel(ringIntent(context))
    }

    private fun ringIntent(context: Context): PendingIntent = PendingIntent.getBroadcast(
        context, REQUEST_RING,
        Intent(context, AlarmReceiver::class.java).setAction(AlarmController.ACTION_RING),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
