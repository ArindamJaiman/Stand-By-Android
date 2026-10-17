package com.standbypro.power

import android.app.NotificationManager
import android.content.Context

/**
 * Switches Do Not Disturb to priority-only while StandBy is in night mode, and puts it back
 * afterwards — but only if StandBy was the one that turned it on. Alarms still ring.
 */
object DndController {

    private var changedByUs = false

    fun isGranted(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java)?.isNotificationPolicyAccessGranted == true

    fun setQuiet(context: Context, quiet: Boolean) {
        val nm = context.getSystemService(NotificationManager::class.java) ?: return
        if (!nm.isNotificationPolicyAccessGranted) return
        try {
            if (quiet) {
                if (!changedByUs && nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                    changedByUs = true
                }
            } else if (changedByUs) {
                // Respect a mode the user picked manually in the meantime.
                if (nm.currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY) {
                    nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
                changedByUs = false
            }
        } catch (e: SecurityException) {
            changedByUs = false
        }
    }
}
