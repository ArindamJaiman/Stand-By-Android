package com.standbypro.domain

enum class ChargeType {
    NONE,
    AC,
    USB,
    WIRELESS,
    UNKNOWN
}

enum class BatteryHealth {
    GOOD,
    OVERHEAT,
    DEAD,
    OVER_VOLTAGE,
    UNSPECIFIED_FAILURE,
    COLD,
    UNKNOWN
}

data class ChargingState(
    val isCharging: Boolean = false,
    val batteryPercent: Int = 0,
    val chargeType: ChargeType = ChargeType.NONE,
    val temperatureCelsius: Float? = null,
    val health: BatteryHealth = BatteryHealth.UNKNOWN,
    /** Estimated time to full in ms, or null when unknown / not charging. */
    val chargeTimeRemainingMs: Long? = null,
    val voltageMv: Int? = null
)
