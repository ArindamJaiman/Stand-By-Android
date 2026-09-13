package com.standbypro

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.standbypro.services.StandByService
import com.standbypro.theme.StandByProTheme
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.main.StandByScreen
import com.standbypro.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {

    private val viewModel: StandByViewModel by viewModels()
    private var isStandByActive by mutableStateOf(false)
    private var isAutoStarted = false

    private val exitReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // When user unplugs, unlocks the phone, or rotates portrait:
            // If StandBy was launched automatically over the lock screen, finish to return to normal
            if (isAutoStarted && isStandByActive) {
                configureImmersiveStandBy(false)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start background service to monitor charging and orientation
        try {
            val serviceIntent = Intent(this, StandByService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val launchStandBy = intent.getBooleanExtra("EXTRA_START_STANDBY", false)
        isAutoStarted = intent.getBooleanExtra("EXTRA_AUTO_STARTED", false)
        isStandByActive = launchStandBy

        if (launchStandBy) {
            configureImmersiveStandBy(true)
        }

        // Register receiver for exit triggers
        val filter = IntentFilter().apply {
            addAction(StandByService.ACTION_EXIT_STANDBY)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(exitReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(exitReceiver, filter)
        }

        enableEdgeToEdge()

        setContent {
            StandByProTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isStandByActive) {
                        BackHandler {
                            configureImmersiveStandBy(false)
                            if (isAutoStarted) {
                                finish()
                            } else {
                                isStandByActive = false
                            }
                        }
                        StandByScreen(
                            viewModel = viewModel,
                            onExit = {
                                configureImmersiveStandBy(false)
                                if (isAutoStarted) {
                                    finish()
                                } else {
                                    isStandByActive = false
                                }
                            }
                        )
                    } else {
                        SettingsScreen(
                            viewModel = viewModel,
                            onLaunchPreview = {
                                isAutoStarted = false
                                configureImmersiveStandBy(true)
                                isStandByActive = true
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra("EXTRA_START_STANDBY", false)) {
            isAutoStarted = intent.getBooleanExtra("EXTRA_AUTO_STARTED", false)
            configureImmersiveStandBy(true)
            isStandByActive = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(exitReceiver)
        } catch (e: Exception) {
            // Ignored
        }
    }

    private fun configureImmersiveStandBy(enable: Boolean) {
        if (enable) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                setShowWhenLocked(true)
                setTurnScreenOn(true)
            } else {
                @Suppress("DEPRECATION")
                window.addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                )
            }

            // Hide system bars for complete immersive display
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
            windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
