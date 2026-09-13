package com.standbypro.settings

enum class ClockStyle {
    DIGITAL,
    ANALOG
}

data class StandBySettings(
    val isEnabled: Boolean = true,
    val autoStartWhileCharging: Boolean = true,
    val requireLandscape: Boolean = true,
    val nightModeEnabled: Boolean = true,
    val burnInProtectionEnabled: Boolean = true,
    val use24Hour: Boolean = false,
    val showSeconds: Boolean = false,
    val clockStyle: ClockStyle = ClockStyle.DIGITAL
)
