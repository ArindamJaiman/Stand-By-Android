package com.standbypro.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.standbypro.media.NowPlayingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SoundState(
    val sound: SleepSound? = null,
    val volume: Float = 0.5f,
    /** Wall-clock millis when the sleep timer fades everything out, or null when off. */
    val sleepTimerEndsAt: Long? = null,
    val sleepTimerMinutes: Int = 0
) {
    val isPlaying: Boolean get() = sound != null

    fun sleepTimerRemainingMinutes(nowMillis: Long): Int? =
        sleepTimerEndsAt?.let { ((it - nowMillis + 59_999L) / 60_000L).toInt().coerceAtLeast(0) }
}

/**
 * Bedside sound machine and sleep timer. Noise is synthesised on the fly and streamed to an
 * AudioTrack; the sleep timer fades the sound out over its last 30 seconds and pauses whatever
 * media app is playing.
 */
object SoundMachine {

    private const val SAMPLE_RATE = 22050
    private const val BUFFER_SAMPLES = SAMPLE_RATE / 20
    private const val FADE_STEP = 0.025f
    private const val TIMER_FADE_MS = 30_000L

    private val _state = MutableStateFlow(SoundState())
    val state: StateFlow<SoundState> = _state.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var playJob: Job? = null
    private var timerJob: Job? = null

    /** Extra 0..1 multiplier the sleep timer lowers while fading out. */
    @Volatile
    private var timerFade = 1f

    fun play(sound: SleepSound) {
        if (_state.value.sound == sound && playJob?.isActive == true) return
        playJob?.cancel()
        _state.update { it.copy(sound = sound) }
        playJob = scope.launch(Dispatchers.IO) { stream(sound) }
    }

    /** Stops with a short fade (the stream loop ramps to silence before releasing). */
    fun stop() {
        _state.update { it.copy(sound = null) }
    }

    fun setVolume(volume: Float) {
        _state.update { it.copy(volume = volume.coerceIn(0f, 1f)) }
    }

    fun startSleepTimer(minutes: Int) {
        timerJob?.cancel()
        timerFade = 1f
        if (minutes <= 0) {
            _state.update { it.copy(sleepTimerEndsAt = null, sleepTimerMinutes = 0) }
            return
        }
        val endsAt = System.currentTimeMillis() + minutes * 60_000L
        _state.update { it.copy(sleepTimerEndsAt = endsAt, sleepTimerMinutes = minutes) }
        timerJob = scope.launch {
            delay((endsAt - TIMER_FADE_MS - System.currentTimeMillis()).coerceAtLeast(0L))
            val steps = 60
            for (i in 1..steps) {
                timerFade = 1f - i / steps.toFloat()
                delay(TIMER_FADE_MS / steps)
            }
            stop()
            withContext(Dispatchers.Main) { NowPlayingRepository.pause() }
            delay(2_000L)
            timerFade = 1f
            _state.update { it.copy(sleepTimerEndsAt = null, sleepTimerMinutes = 0) }
        }
    }

    fun cancelSleepTimer() = startSleepTimer(0)

    private fun targetGain(sound: SleepSound): Float {
        val s = _state.value
        if (s.sound != sound) return 0f
        // Squared volume feels linear to the ear.
        return s.volume * s.volume * timerFade
    }

    private suspend fun stream(sound: SleepSound) {
        val minBytes = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val track = try {
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(maxOf(minBytes, BUFFER_SAMPLES * 4))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
        } catch (e: Exception) {
            _state.update { if (it.sound == sound) it.copy(sound = null) else it }
            return
        }
        val generator = NoiseGenerator(sound, SAMPLE_RATE, System.nanoTime())
        val buffer = ShortArray(BUFFER_SAMPLES)
        var gain = 0f
        try {
            track.play()
            while (kotlinx.coroutines.currentCoroutineContext().isActive) {
                val target = targetGain(sound)
                val next = if (target > gain) minOf(target, gain + FADE_STEP) else maxOf(target, gain - FADE_STEP)
                generator.fill(buffer, gain, next)
                gain = next
                track.write(buffer, 0, buffer.size)
                if (_state.value.sound != sound && gain <= 0f) break
            }
        } finally {
            runCatching { track.stop() }
            track.release()
        }
    }
}
