package com.standbypro.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.standbypro.MainActivity
import com.standbypro.domain.StandByController
import com.standbypro.domain.StandByState
import com.standbypro.hardware.ChargingStateMonitor
import com.standbypro.hardware.OrientationMonitor
import com.standbypro.settings.SettingsRepository
import com.standbypro.settings.dataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class StandByService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var standByController: StandByController

    companion object {
        const val CHANNEL_ID = "standby_service_channel"
        const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        
        val settingsRepository = SettingsRepository(applicationContext.dataStore)
        val chargingMonitor = ChargingStateMonitor(applicationContext)
        val orientationMonitor = OrientationMonitor(applicationContext)

        standByController = StandByController(
            scope = scope,
            chargingStateMonitor = chargingMonitor,
            orientationMonitor = orientationMonitor,
            settingsRepository = settingsRepository
        )

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        monitorState()
    }

    private fun monitorState() {
        scope.launch {
            standByController.standByState.collect { state ->
                if (state == StandByState.STANDBY_ACTIVE) {
                    launchStandByActivity()
                }
            }
        }
    }

    private fun launchStandByActivity() {
        // Full screen intent for modern Android
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_START_STANDBY", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val fullScreenIntentBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("StandBy Pro")
            .setContentText("Activating StandBy...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(2, fullScreenIntentBuilder.build())
        
        // Also try direct launch if allowed
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Ignored, full screen intent will handle it if background start is blocked
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("StandBy Pro is running")
            .setContentText("Monitoring charging and orientation")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "StandBy Service",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains StandBy Pro monitoring in the background"
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
