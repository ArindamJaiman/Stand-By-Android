package com.standbypro.alarm

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RingState(val label: String, val snoozeMinutes: Int, val startedAtMillis: Long)

/**
 * Plays the alarm: the system alarm sound on the alarm stream, optionally fading in over a
 * minute ("gentle wake"), with a soft vibration. Stops by itself after ten minutes.
 */
object AlarmRinger {

    private const val RAMP_SECONDS = 60
    private const val AUTO_SILENCE_MS = 10 * 60_000L

    private val _ringing = MutableStateFlow<RingState?>(null)
    val ringing: StateFlow<RingState?> = _ringing.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var rampJob: Job? = null
    private var timeoutJob: Job? = null

    /** Volume for second [elapsed] of a gentle wake: a slow start that reaches full after a minute. */
    fun rampVolume(elapsed: Int): Float {
        val f = (elapsed.toFloat() / RAMP_SECONDS).coerceIn(0f, 1f)
        return (0.05f + 0.95f * f * f).coerceIn(0.05f, 1f)
    }

    fun start(context: Context, alarm: BedsideAlarm) {
        if (_ringing.value != null) return
        val app = context.applicationContext
        _ringing.value = RingState(alarm.label, alarm.snoozeMinutes, System.currentTimeMillis())

        wakeLock = (app.getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "standbypro:alarm")
            .apply { acquire(AUTO_SILENCE_MS + 30_000L) }

        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        player = runCatching {
            MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
                setDataSource(app, uri)
                isLooping = true
                prepare()
                val v = if (alarm.gentleWake) rampVolume(0) else 1f
                setVolume(v, v)
                start()
            }
        }.getOrNull()

        if (alarm.gentleWake) {
            rampJob = scope.launch {
                for (second in 1..RAMP_SECONDS) {
                    delay(1000L)
                    val v = rampVolume(second)
                    runCatching { player?.setVolume(v, v) }
                }
            }
        }

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        runCatching { vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 900, 500, 1800), 1)) }

        timeoutJob = scope.launch {
            delay(AUTO_SILENCE_MS)
            AlarmController.dismiss(app)
        }
    }

    fun stop() {
        rampJob?.cancel()
        timeoutJob?.cancel()
        runCatching { player?.stop() }
        player?.release()
        player = null
        runCatching { vibrator?.cancel() }
        vibrator = null
        if (wakeLock?.isHeld == true) runCatching { wakeLock?.release() }
        wakeLock = null
        _ringing.value = null
    }
}
