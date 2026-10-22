package com.standbypro.audio

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.min
import kotlin.math.sin

/**
 * Additive bell synthesis for the hourly chimes (pure Kotlin, so it is unit-tested). Each strike
 * sums a church bell's characteristic partials — hum, prime, minor third, fifth, nominal — with
 * higher partials dying away faster, like real bronze.
 */
object BellSynth {

    const val SAMPLE_RATE = 22050

    /** (frequency ratio, amplitude) pairs relative to the strike note. */
    private val PARTIALS = listOf(0.5 to 0.35, 1.0 to 1.0, 1.19 to 0.45, 1.5 to 0.3, 2.0 to 0.4, 2.74 to 0.18, 4.07 to 0.1)

    data class Strike(val frequency: Double, val startSeconds: Double, val amplitude: Double = 1.0)

    // Westminster Quarters, in E major.
    private const val GS4 = 415.30
    private const val FS4 = 369.99
    private const val E4 = 329.63
    private const val B3 = 246.94
    private const val HOUR_BELL = 164.81

    /** The four changes rung before the hour (changes 2–5 of the five-change cycle). */
    val HOUR_CHANGES: List<List<Double>> = listOf(
        listOf(E4, GS4, FS4, B3),
        listOf(E4, FS4, GS4, E4),
        listOf(GS4, E4, FS4, B3),
        listOf(B3, FS4, GS4, E4)
    )

    /** The full Westminster hour: the quarters melody, a pause, then one deep strike per hour. */
    fun westminsterHour(hour12: Int): List<Strike> {
        val strikes = mutableListOf<Strike>()
        var t = 0.0
        HOUR_CHANGES.forEach { change ->
            change.forEachIndexed { i, note ->
                strikes += Strike(note, t, if (i == 3) 0.9 else 0.75)
                t += if (i == 3) 1.4 else 0.7
            }
        }
        t += 1.2
        repeat(hour12.coerceIn(1, 12)) {
            strikes += Strike(HOUR_BELL, t, 1.0)
            t += 2.0
        }
        return strikes
    }

    /** A single deep temple bell. */
    fun templeBell(): List<Strike> = listOf(Strike(110.0, 0.0, 1.0))

    fun durationSeconds(strikes: List<Strike>, tailSeconds: Double): Double = (strikes.maxOfOrNull { it.startSeconds } ?: 0.0) + tailSeconds

    /** Renders [strikes] to 16-bit mono PCM, normalised so the loudest moment peaks at 80%. */
    fun render(strikes: List<Strike>, tailSeconds: Double = 3.5, sampleRate: Int = SAMPLE_RATE): ShortArray {
        val total = (durationSeconds(strikes, tailSeconds) * sampleRate).toInt().coerceAtLeast(1)
        val mix = FloatArray(total)
        val tail = (tailSeconds * sampleRate).toInt()
        for (strike in strikes) {
            val start = (strike.startSeconds * sampleRate).toInt()
            val length = min(total - start, tail)
            for (i in 0 until length) {
                val t = i.toDouble() / sampleRate
                val attack = min(1.0, t / 0.004)
                var v = 0.0
                for ((ratio, amp) in PARTIALS) {
                    v += sin(2 * PI * strike.frequency * ratio * t) * amp * exp(-t * (0.9 + ratio * 0.8))
                }
                mix[start + i] += (v * attack * strike.amplitude).toFloat()
            }
        }
        val peak = mix.maxOf { abs(it) }.takeIf { it > 0f } ?: 1f
        val gain = 0.8f / peak
        return ShortArray(total) { (mix[it] * gain * Short.MAX_VALUE).toInt().toShort() }
    }
}
