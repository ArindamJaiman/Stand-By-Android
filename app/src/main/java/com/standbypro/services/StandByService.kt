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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class StandByService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private lateinit var standByController: StandByController
    private lateinit var chargingMonitor: ChargingStateMonitor

    private var chargingWakeLock: PowerManager.WakeLock? = null
    private var isStandByActive = false
    private var exitJob: Job? = null
    private var lastLaunchTime = 0L

    companion object {
        const val CHANNEL_ID = "standby_service_channel"
        const val FULLSCREEN_CHANNEL_ID = "standby_fullscreen_channel"
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_FULLSCREEN_ID = 2
        const val ACTION_EXIT_STANDBY = "com.standbypro.ACTION_EXIT_STANDBY"
    }

    override fun onCreate() {
        super.onCreate()

        val settingsRepository = SettingsRepository(applicationContext.dataStore)
        chargingMonitor = ChargingStateMonitor(applicationContext)
        val orientationMonitor = OrientationMonitor(applicationContext)
        val screenLockMonitor = ScreenLockMonitor(applicationContext)

        standByController = StandByController(
            scope = scope,
            chargingStateMonitor = chargingMonitor,
            orientationMonitor = orientationMonitor,
            screenLockMonitor = screenLockMonitor,
            settingsRepository = settingsRepository
        )

        createNotificationChannels()
        startForeground(NOTIFICATION_ID, createNotification())

        monitorState()
    }

    private fun monitorState() {
        // 1. Maintain a partial wake lock while device is charging so CPU does not
        // suspend sensors when the user presses the power button to turn screen off.
        scope.launch {
            chargingMonitor.chargingState.collect { state ->
                updateChargingWakeLock(state.isCharging)
            }
        }

        // 2. Monitor StandBy trigger state
        scope.launch {
            standByController.standByState.collect { state ->
                if (state == StandByState.STANDBY_ACTIVE) {
                    exitJob?.cancel()
                    isStandByActive = true
                    val now = System.currentTimeMillis()
                    if (now - lastLaunchTime > 2500L) {
                        lastLaunchTime = now
                        launchStandByActivity()
                    }
                } else if (isStandByActive) {
                    // Debounce exit by 1.5 seconds to avoid momentary motion/sensor glitches
                    exitJob?.cancel()
                    exitJob = scope.launch {
                        delay(1500L)
                        if (standByController.standByState.value != StandByState.STANDBY_ACTIVE) {
                            isStandByActive = false
                            val exitIntent = Intent(ACTION_EXIT_STANDBY).apply {
                                setPackage(packageName)
                            }
                            sendBroadcast(exitIntent)
                        }
                    }
                }
            }
        }
    }

    private fun updateChargingWakeLock(isCharging: Boolean) {
        if (isCharging) {
            if (chargingWakeLock == null) {
                val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                chargingWakeLock = pm.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "standbypro:charging_cpu_monitor"
                )
            }
            if (chargingWakeLock?.isHeld == false) {
                try {
                    chargingWakeLock?.acquire()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            if (chargingWakeLock?.isHeld == true) {
                try {
                    chargingWakeLock?.release()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun launchStandByActivity() {
        // 1. Turn on the screen via wake lock with ACQUIRE_CAUSES_WAKEUP
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
                "standbypro:activation_wakeup"
            )
            wakeLock.acquire(4000L)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 2. Build full-screen intent for modern Android lock screen override
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            putExtra("EXTRA_START_STANDBY", true)
            putExtra("EXTRA_AUTO_STARTED", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Important: Full-screen intent MUST use HIGH/MAX importance channel!
        val fullScreenNotification = NotificationCompat.Builder(this, FULLSCREEN_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("StandBy Pro")
            .setContentText("Activating StandBy ambient display...")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_FULLSCREEN_ID, fullScreenNotification)

        // 3. Also trigger direct launch for active unlocked/transitional cases
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_charging)
            .setContentTitle("StandBy Pro is running")
            .setContentText("Monitoring charging, landscape & lock state")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java) ?: return

        // 1. Ongoing service channel (silent, low importance)
        val serviceChannel = NotificationChannel(
            CHANNEL_ID,
            "StandBy Background Monitor",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Maintains StandBy Pro monitoring in the background"
            setShowBadge(false)
        }
        manager.createNotificationChannel(serviceChannel)

        // 2. Full-screen lock screen activation channel (high importance, public visibility)
        val activationChannel = NotificationChannel(
            FULLSCREEN_CHANNEL_ID,
            "StandBy Screen Activation",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Awakens device screen into StandBy display over lock screen"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
            enableVibration(false)
            setShowBadge(false)
        }
        manager.createNotificationChannel(activationChannel)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        if (chargingWakeLock?.isHeld == true) {
            try {
                chargingWakeLock?.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        scope.cancel()
    }
}

