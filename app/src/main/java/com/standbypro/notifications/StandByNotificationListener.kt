package com.standbypro.notifications

import android.app.Notification
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.standbypro.domain.NotificationItem
import com.standbypro.media.NowPlayingRepository

/**
 * Feeds real notifications into [NotificationRepository] and unlocks media-session access for
 * the Now Playing widget. Ongoing, silent and group-summary notifications are skipped so the
 * StandBy badge only counts things a person would actually want to see.
 */
class StandByNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationRepository.setListenerConnected(true)
        NowPlayingRepository.start(applicationContext)
        publish()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationRepository.setListenerConnected(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        publish()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        publish()
    }

    private fun publish() {
        val active = try {
            activeNotifications ?: return
        } catch (e: Exception) {
            return
        }
        val items = active
            .filter { sbn ->
                val n = sbn.notification
                sbn.packageName != packageName &&
                    !sbn.isOngoing &&
                    (n.flags and Notification.FLAG_GROUP_SUMMARY) == 0 &&
                    n.category != Notification.CATEGORY_TRANSPORT &&
                    n.category != Notification.CATEGORY_SERVICE
            }
            .sortedByDescending { it.postTime }
            .mapNotNull { sbn ->
                val extras = sbn.notification.extras
                val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: return@mapNotNull null
                val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""
                NotificationItem(
                    id = sbn.key,
                    appName = appLabel(sbn.packageName),
                    title = title,
                    previewText = text,
                    timestamp = relativeTime(sbn.postTime),
                    iconName = sbn.packageName
                )
            }
        NotificationRepository.update(items)
    }

    private fun appLabel(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (e: PackageManager.NameNotFoundException) {
        pkg
    }

    private fun relativeTime(postTime: Long): String {
        val minutes = (System.currentTimeMillis() - postTime) / 60_000
        return when {
            minutes < 1 -> "now"
            minutes < 60 -> "${minutes}m"
            minutes < 24 * 60 -> "${minutes / 60}h"
            else -> "${minutes / (24 * 60)}d"
        }
    }
}
