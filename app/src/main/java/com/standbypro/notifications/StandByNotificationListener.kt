package com.standbypro.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class StandByNotification(
    val id: Int,
    val packageName: String,
    val title: String,
    val text: String,
    val postTime: Long
)

class StandByNotificationListener : NotificationListenerService() {

    companion object {
        private val _notifications = MutableStateFlow<List<StandByNotification>>(emptyList())
        val notifications: StateFlow<List<StandByNotification>> = _notifications.asStateFlow()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { updateNotifications() }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let { updateNotifications() }
    }

    private fun updateNotifications() {
        val currentNotifications = activeNotifications.mapNotNull { sbn ->
            val extras = sbn.notification.extras
            val title = extras.getString("android.title") ?: return@mapNotNull null
            val text = extras.getCharSequence("android.text")?.toString() ?: ""
            
            StandByNotification(
                id = sbn.id,
                packageName = sbn.packageName,
                title = title,
                text = text,
                postTime = sbn.postTime
            )
        }.sortedByDescending { it.postTime }
        
        _notifications.value = currentNotifications
    }
}
