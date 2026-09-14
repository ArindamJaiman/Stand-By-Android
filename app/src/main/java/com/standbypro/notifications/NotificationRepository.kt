package com.standbypro.notifications

import com.standbypro.data.DemoDataProvider
import com.standbypro.domain.IncomingCallState
import com.standbypro.domain.NotificationDisplayState
import com.standbypro.domain.NotificationPrivacy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NotificationRepository {

    private val _notifications = MutableStateFlow(DemoDataProvider.demoNotifications)
    val notifications: StateFlow<NotificationDisplayState> = _notifications.asStateFlow()

    private val _incomingCall = MutableStateFlow(DemoDataProvider.demoIncomingCall)
    val incomingCall: StateFlow<IncomingCallState> = _incomingCall.asStateFlow()

    fun setPrivacyMode(mode: NotificationPrivacy) {
        _notifications.value = _notifications.value.copy(privacyMode = mode)
    }

    fun dismissNotification(id: String) {
        val updated = _notifications.value.notifications.filter { it.id != id }
        _notifications.value = _notifications.value.copy(
            notifications = updated,
            unreadCount = updated.size
        )
    }

    fun setSimulateIncomingCall(active: Boolean) {
        _incomingCall.value = _incomingCall.value.copy(isIncoming = active)
    }
}
