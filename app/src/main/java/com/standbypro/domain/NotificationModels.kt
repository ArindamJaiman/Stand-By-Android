package com.standbypro.domain

enum class NotificationPrivacy(val displayName: String, val description: String) {
    FULL("Full Details", "Shows app name, contact/sender, and message preview"),
    ICON_ONLY("Icon Only (Default)", "Preserves privacy by showing only the app icon badge"),
    APP_NAME_ONLY("App Name Only", "Shows which app notified without message content"),
    HIDDEN("Completely Hidden", "Hides all notifications while StandBy is active")
}

data class NotificationItem(
    val id: String,
    val appName: String,
    val title: String,
    val previewText: String,
    val timestamp: String = "Just now",
    val iconName: String = "Notifications"
)

data class NotificationDisplayState(
    val notifications: List<NotificationItem> = emptyList(),
    val unreadCount: Int = 0,
    val privacyMode: NotificationPrivacy = NotificationPrivacy.ICON_ONLY
)

data class IncomingCallState(
    val callerName: String = "Sarah Jenkins",
    val callerSubtitle: String = "Mobile",
    val isIncoming: Boolean = false
)
