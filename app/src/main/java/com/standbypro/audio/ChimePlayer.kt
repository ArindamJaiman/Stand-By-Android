package com.standbypro.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Renders bell chimes off the main thread and plays them once through a static AudioTrack. */
object ChimePlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var track: AudioTrack? = null

    fun play(strikes: List<BellSynth.Strike>, tailSeconds: Double) {
        scope.launch {
            val pcm = BellSynth.render(strikes, tailSeconds)
            val next = runCatching {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(BellSynth.SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(pcm.size * 2)
                    .build()
                    .apply { write(pcm, 0, pcm.size) }
            }.getOrNull() ?: return@launch
            swap(next)
            next.play()
            delay(pcm.size * 1000L / BellSynth.SAMPLE_RATE + 300L)
            if (track === next) swap(null)
        }
    }

    fun stop() = swap(null)

    @Synchronized
    private fun swap(next: AudioTrack?) {
        track?.let {
            runCatching { it.stop() }
            it.release()
        }
        track = next
    }
}
