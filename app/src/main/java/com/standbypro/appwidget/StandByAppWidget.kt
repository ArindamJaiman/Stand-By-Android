package com.standbypro.appwidget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.text.format.DateFormat
import android.widget.RemoteViews
import com.standbypro.MainActivity
import com.standbypro.R
import java.util.Date

/**
 * Home-screen / lock-screen widget: a self-updating clock (TextClock, no wakeups), the next
 * alarm and battery level. Tapping it opens the StandBy display.
 */
class StandByAppWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, manager: AppWidgetManager, appWidgetIds: IntArray) {
        manager.updateAppWidget(appWidgetIds, build(context))
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED) updateAll(context)
    }

    companion object {
        /** Refreshes every placed instance; cheap no-op when none are on the home screen. */
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context) ?: return
            val ids = manager.getAppWidgetIds(ComponentName(context, StandByAppWidget::class.java))
            if (ids.isNotEmpty()) manager.updateAppWidget(ids, build(context))
        }

        private fun build(context: Context): RemoteViews {
            val views = RemoteViews(context.packageName, R.layout.widget_standby)

            val alarm = context.getSystemService(AlarmManager::class.java)?.nextAlarmClock
            val alarmText = alarm?.let { "⏰  " + DateFormat.getTimeFormat(context).format(Date(it.triggerTime)) } ?: "No alarm"
            views.setTextViewText(R.id.widget_alarm, alarmText)

            val battery = context.getSystemService(BatteryManager::class.java)
            val level = battery?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.takeIf { it in 0..100 }
            val charging = battery?.isCharging == true
            views.setTextViewText(R.id.widget_battery, level?.let { (if (charging) "⚡ " else "") + "$it%" } ?: "")

            val open = Intent(context, MainActivity::class.java).apply {
                action = MainActivity.ACTION_START_STANDBY
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
            views.setOnClickPendingIntent(
                R.id.widget_root,
                PendingIntent.getActivity(context, 11, open, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
            return views
        }
    }
}
