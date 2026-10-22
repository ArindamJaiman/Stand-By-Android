package com.standbypro

import android.app.KeyguardManager
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
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
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
import com.standbypro.ui.design.DynamicAccent
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.main.AlarmRingingScreen
import com.standbypro.ui.main.StandByScreen
import com.standbypro.ui.settings.SettingsScreen

class MainActivity : ComponentActivity() {

    private val viewModel: StandByViewModel by viewModels()
    private var isStandByActive by mutableStateOf(false)
    private var isAutoStarted = false

    private val exitReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // Unplugged, unlocked or rotated to portrait: an auto-started StandBy steps aside.
            if (isAutoStarted && isStandByActive) {
                configureImmersiveStandBy(false)
                finish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        allowOverLockScreen(true)
        DynamicAccent.refresh(this)

        try {
            val serviceIntent = Intent(this, StandByService::class.java)
            startForegroundService(serviceIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val launchStandBy = intent.getBooleanExtra(EXTRA_START_STANDBY, false) || intent.isStandByAction()
        if (intent.action == ACTION_START_BEDSIDE) viewModel.setBedsideSession(true)
        isAutoStarted = intent.getBooleanExtra(EXTRA_AUTO_STARTED, false)
        isStandByActive = launchStandBy
        if (launchStandBy) configureImmersiveStandBy(true)

        val filter = IntentFilter().apply {
            addAction(StandByService.ACTION_EXIT_STANDBY)
            addAction(Intent.ACTION_USER_PRESENT)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(exitReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(exitReceiver, filter)
        }

        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()
            val ambient by viewModel.ambientDisplayState.collectAsState()
            val brightnessOverride by viewModel.brightnessOverride.collectAsState()
            val adaptive by viewModel.adaptiveBrightness.collectAsState()
            val ringing by viewModel.alarmRinging.collectAsState()
            SideEffect { FaceFonts.clock = settings.clockFont.family() }

            // A ringing alarm keeps the screen on over the lock screen until it's stopped.
            LaunchedEffect(ringing != null) {
                if (ringing != null) {
                    allowOverLockScreen(true)
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else if (!isStandByActive) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }

            LaunchedEffect(isStandByActive, settings.brightnessLevel, ambient.isNight, brightnessOverride, adaptive, ringing != null) {
                val lp = window.attributes
                val forced = brightnessOverride
                lp.screenBrightness = if (ringing != null) {
                    0.8f
                } else if (isStandByActive) {
                    val level = (adaptive ?: settings.brightnessLevel).coerceIn(0.01f, 1.0f)
                    if (forced != null) forced.coerceIn(0.01f, 1.0f)
                    else if (ambient.isNight) level.coerceAtMost(0.06f) else level
                } else {
                    WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                }
                window.attributes = lp
            }

            StandByProTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Box(Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = isStandByActive,
                            transitionSpec = { fadeIn(tween(450)) togetherWith fadeOut(tween(300)) },
                            label = "mode"
                        ) { standBy ->
                            if (standBy) {
                                BackHandler { leaveStandBy() }
                                StandByScreen(
                                    viewModel = viewModel,
                                    onExit = { leaveStandBy() },
                                    onOpenStudio = { openStudio() }
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
                        AnimatedVisibility(ringing != null, enter = fadeIn(tween(600)), exit = fadeOut(tween(400))) {
                            AlarmRingingScreen(viewModel, ringing)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        DynamicAccent.refresh(this)
        viewModel.onResume()
    }

    private fun Intent.isStandByAction(): Boolean = action == ACTION_START_STANDBY || action == ACTION_START_BEDSIDE

    private fun leaveStandBy() {
        viewModel.setBedsideSession(false)
        viewModel.endStandBySession()
        configureImmersiveStandBy(false)
        if (isAutoStarted) finish() else isStandByActive = false
    }

    /** From an auto-started (lock screen) session, ask the user to unlock before showing Studio. */
    private fun openStudio() {
        val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (isAutoStarted && keyguard?.isKeyguardLocked == true) {
            keyguard.requestDismissKeyguard(this, object : KeyguardManager.KeyguardDismissCallback() {
                override fun onDismissSucceeded() {
                    viewModel.endStandBySession()
                    isAutoStarted = false
                    configureImmersiveStandBy(false)
                    isStandByActive = false
                }
            })
        } else {
            viewModel.endStandBySession()
            isAutoStarted = false
            configureImmersiveStandBy(false)
            isStandByActive = false
        }
    }

    private fun allowOverLockScreen(enable: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(enable)
            setTurnScreenOn(enable)
        } else {
            @Suppress("DEPRECATION")
            if (enable) {
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        allowOverLockScreen(true)
        if (intent.getBooleanExtra(EXTRA_START_STANDBY, false) || intent.isStandByAction()) {
            viewModel.setBedsideSession(intent.action == ACTION_START_BEDSIDE)
            isAutoStarted = intent.getBooleanExtra(EXTRA_AUTO_STARTED, false)
            configureImmersiveStandBy(true)
            isStandByActive = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(exitReceiver)
        } catch (e: Exception) {
            // Already unregistered.
        }
    }

    private fun configureImmersiveStandBy(enable: Boolean) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (enable) {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            allowOverLockScreen(true)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    companion object {
        const val EXTRA_START_STANDBY = "EXTRA_START_STANDBY"
        const val EXTRA_AUTO_STARTED = "EXTRA_AUTO_STARTED"
        const val ACTION_START_STANDBY = "com.standbypro.action.START_STANDBY"
        const val ACTION_START_BEDSIDE = "com.standbypro.action.START_BEDSIDE"
        const val ACTION_ALARM_RING = "com.standbypro.action.ALARM_RING"
    }
}
