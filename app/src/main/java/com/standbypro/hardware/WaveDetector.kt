package com.standbypro.hardware

/**
 * Turns proximity samples into "wave" gestures: a hand passing over the sensor (near, then far
 * again within [maxMillis]). Longer covers — a pocket, a book — are ignored.
 */
class WaveDetector(private val minMillis: Long = 60L, private val maxMillis: Long = 1500L) {

    private var nearSince: Long? = null

    /** Returns true when [near] going false completes a wave. */
    fun onSample(near: Boolean, timeMillis: Long): Boolean {
        if (near) {
            if (nearSince == null) nearSince = timeMillis
            return false
        }
        val since = nearSince ?: return false
        nearSince = null
        return timeMillis - since in minMillis..maxMillis
    }
}
