package com.standbypro.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.standbypro.MainActivity
import com.standbypro.domain.StandByController
import com.standbypro.domain.StandByState
import com.standbypro.hardware.ChargingStateMonitor
import com.standbypro.hardware.OrientationMonitor
import com.standbypro.hardware.ScreenLockMonitor
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
        const val ACTION_EXIT_STANDBY = "com.standbypro.ACTION_EXIT_STANDBY"
    }

    override fun onCreate() {
        super.onCreate()
        
        val settingsRepository = SettingsRepository(applicationContext.dataStore)
        val chargingMonitor = ChargingStateMonitor(applicationContext)
        val orientationMonitor = OrientationMonitor(applicationContext)
        val screenLockMonitor = ScreenLockMonitor(applicationContext)

        standByController = StandByController(
            scope = scope,
            chargingStateMonitor = chargingMonitor,
            orientationMonitor = orientationMonitor,
            screenLockMonitor = screenLockMonitor,
            settingsRepository = settingsRepository
        )

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())

        monitorState()
    }

    private var lastLaunchTime = 0L

    private fun monitorState() {
        scope.launch {
            standByController.standByState.collect { state ->
                if (state == StandByState.STANDBY_ACTIVE) {
                    val now = System.currentTimeMillis()
                    if (now - lastLaunchTime > 3000L) {
                        lastLaunchTime = now
                        launchStandByActivity()
                    }
                } else {
                    // Send broadcast so MainActivity can exit if it was auto-started
                    val exitIntent = Intent(ACTION_EXIT_STANDBY).apply {
                        setPackage(packageName)
                    }
                    sendBroadcast(exitIntent)
                }
            }
        }
    }

    private fun launchStandByActivity() {
        // Acquire brief wake lock to turn on screen if locked
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
                "standbypro:activation_wakeup"
            )
            wakeLock.acquire(3000L)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Full screen intent for modern Android lock-screen override
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_START_STANDBY", true)
            putExtra("EXTRA_AUTO_STARTED", true)
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
        
        // Also try direct launch
        try {
            startActivity(intent)
        } catch (e: Exception) {
            // Handled by fullScreenIntent
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("StandBy Pro is running")
            .setContentText("Monitoring charging, landscape, and lock state")
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
