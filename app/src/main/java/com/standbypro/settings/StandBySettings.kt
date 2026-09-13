package com.standbypro.settings

enum class ClockStyle {
    DIGITAL,
    ANALOG
}

data class StandBySettings(
    val isEnabled: Boolean = true,
    val autoStartWhileCharging: Boolean = true,
    val requireLandscape: Boolean = true,
    val requireScreenLocked: Boolean = true,
    val nightModeEnabled: Boolean = true,
    val burnInProtectionEnabled: Boolean = true,
    val use24Hour: Boolean = false,
    val showSeconds: Boolean = false,
    val clockStyle: ClockStyle = ClockStyle.DIGITAL,
    val brightnessLevel: Float = 0.05f // 0.01f (Most Dim) to 1.0f (Max)
)
