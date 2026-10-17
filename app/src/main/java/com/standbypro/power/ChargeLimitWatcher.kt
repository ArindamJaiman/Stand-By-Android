package com.standbypro.power

/**
 * Battery-health charge limit: fires once each time the battery reaches the chosen limit while
 * charging, and re-arms after unplugging (or after the level drops a few percent).
 */
class ChargeLimitWatcher {

    private var alerted = false

    /** Returns true when the "unplug now" alert should be shown. */
    fun update(isCharging: Boolean, percent: Int, limitPercent: Int): Boolean {
        if (limitPercent <= 0 || !isCharging || percent < limitPercent - 3) {
            alerted = false
            return false
        }
        if (percent >= limitPercent && !alerted) {
            alerted = true
            return true
        }
        return false
    }
}
