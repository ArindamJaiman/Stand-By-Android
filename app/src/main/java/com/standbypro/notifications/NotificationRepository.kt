package com.standbypro.notifications

import com.standbypro.domain.NotificationDisplayState
import com.standbypro.domain.NotificationItem
import com.standbypro.domain.NotificationPrivacy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Live notification feed populated by [StandByNotificationListener]. */
object NotificationRepository {

    private val _notifications = MutableStateFlow(NotificationDisplayState())
    val notifications: StateFlow<NotificationDisplayState> = _notifications.asStateFlow()

    private val _listenerConnected = MutableStateFlow(false)
    val listenerConnected: StateFlow<Boolean> = _listenerConnected.asStateFlow()

    fun update(items: List<NotificationItem>) {
        _notifications.value = _notifications.value.copy(notifications = items, unreadCount = items.size)
    }

    fun setListenerConnected(connected: Boolean) {
        _listenerConnected.value = connected
        if (!connected) update(emptyList())
    }

    fun setPrivacyMode(mode: NotificationPrivacy) {
        _notifications.value = _notifications.value.copy(privacyMode = mode)
    }

    fun dismissNotification(id: String) {
        update(_notifications.value.notifications.filter { it.id != id })
    }
}
