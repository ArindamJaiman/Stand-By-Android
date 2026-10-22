package com.standbypro.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Private receiver for the alarm itself and the notification's Snooze / Stop buttons. */
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            AlarmController.ACTION_RING -> {
                val pending = goAsync()
                AlarmController.onRing(context) { pending.finish() }
            }
            AlarmController.ACTION_SNOOZE -> AlarmController.snooze(context)
            AlarmController.ACTION_DISMISS -> AlarmController.dismiss(context)
        }
    }
}

/** Alarms don't survive a reboot or a clock change, so book the next ring again. */
class AlarmBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        AlarmController.rescheduleFromSettings(context) { pending.finish() }
    }
}
