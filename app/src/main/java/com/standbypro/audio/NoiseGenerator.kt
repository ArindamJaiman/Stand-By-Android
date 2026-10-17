package com.standbypro.audio

import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Bedside sound-machine presets, all synthesised on the device (no audio files). */
enum class SleepSound(val label: String) {
    RAIN("Rain"),
    OCEAN("Ocean"),
    FAN("Fan"),
    BROWN("Brown noise"),
    PINK("Pink noise"),
    WHITE("White noise")
}

/**
 * Procedural noise source producing mono samples in -1..1. Pure Kotlin so it can be unit tested;
 * [SoundMachine] streams it to an AudioTrack.
 */
class NoiseGenerator(private val sound: SleepSound, private val sampleRate: Int = 22050, seed: Long = 1L) {

    private val random = Random(seed)
    private var sample = 0L

    // Paul Kellet's pink-noise filter state.
    private var b0 = 0.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var b3 = 0.0
    private var b4 = 0.0
    private var b5 = 0.0
    private var b6 = 0.0
    private var brown = 0.0
    private var lowPass = 0.0
    private var dropEnvelope = 0.0
    private var dropLevel = 0.0

    private fun white(): Double = random.nextDouble() * 2.0 - 1.0

    private fun pink(white: Double): Double {
        b0 = 0.99886 * b0 + white * 0.0555179
        b1 = 0.99332 * b1 + white * 0.0750759
        b2 = 0.96900 * b2 + white * 0.1538520
        b3 = 0.86650 * b3 + white * 0.3104856
        b4 = 0.55000 * b4 + white * 0.5329522
        b5 = -0.7616 * b5 - white * 0.0168980
        val out = b0 + b1 + b2 + b3 + b4 + b5 + b6 + white * 0.5362
        b6 = white * 0.115926
        return out * 0.11
    }

    private fun brown(white: Double): Double {
        brown = (brown + 0.02 * white) / 1.02
        return brown * 3.5
    }

    /** Slow sine LFO in 0..1 with the given period in seconds. */
    private fun lfo(periodSeconds: Double, phase: Double = 0.0): Double =
        0.5 + 0.5 * sin(2.0 * PI * (sample.toDouble() / sampleRate / periodSeconds + phase))

    fun next(): Float {
        val w = white()
        val value = when (sound) {
            SleepSound.WHITE -> w * 0.35
            SleepSound.PINK -> pink(w) * 0.9
            SleepSound.BROWN -> brown(w)
            SleepSound.RAIN -> {
                // Steady hiss plus random droplets with a fast decay, swelling gently.
                if (random.nextDouble() < 55.0 / sampleRate) {
                    dropEnvelope = 1.0
                    dropLevel = 0.15 + random.nextDouble() * 0.45
                }
                dropEnvelope *= 0.9975
                val hiss = pink(w) * 0.7
                (hiss + w * dropEnvelope * dropLevel) * (0.85 + 0.15 * lfo(13.0))
            }
            SleepSound.OCEAN -> {
                // Two overlapping swells of rumble with foam on the crests.
                val swell = 0.6 * lfo(9.0) + 0.4 * lfo(14.0, 0.37)
                val crest = swell * swell
                brown(w) * (0.2 + 0.8 * crest) + pink(w) * 0.35 * crest * crest
            }
            SleepSound.FAN -> {
                lowPass += 0.06 * (w - lowPass)
                (lowPass * 2.4 + brown(w) * 0.5) * (1.0 + 0.04 * sin(2.0 * PI * 9.0 * sample / sampleRate))
            }
        }
        sample++
        return value.coerceIn(-1.0, 1.0).toFloat()
    }

    /**
     * Fills [buffer] with 16-bit PCM, ramping the gain linearly from [fromGain] to [toGain] so
     * volume changes never click.
     */
    fun fill(buffer: ShortArray, fromGain: Float, toGain: Float = fromGain) {
        val n = buffer.size
        for (i in 0 until n) {
            val gain = fromGain + (toGain - fromGain) * (i + 1) / n
            buffer[i] = (next() * gain * Short.MAX_VALUE).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
        }
    }
}
