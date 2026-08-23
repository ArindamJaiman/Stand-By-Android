package com.standbypro.settings

data class StandBySettings(
    val isEnabled: Boolean = true,
    val autoStartWhileCharging: Boolean = true,
    val requireLandscape: Boolean = true,
    val nightModeEnabled: Boolean = true,
    val burnInProtectionEnabled: Boolean = true
)
