package com.standbypro.settings

import androidx.compose.ui.graphics.Color

enum class ClockStyle {
    DIGITAL,
    ANALOG
}

enum class StandByColorTheme(
    val id: String,
    val displayName: String,
    val color: Color
) {
    ORANGE("orange", "Amber Orange", Color(0xFFFF9500)),
    CYAN("cyan", "Neon Cyan", Color(0xFF00E5FF)),
    RED("red", "Crimson Red", Color(0xFFFF3B30)),
    GREEN("green", "Emerald Green", Color(0xFF30D158)),
    PURPLE("purple", "Royal Purple", Color(0xFFAF52DE)),
    YELLOW("yellow", "Solar Yellow", Color(0xFFFFD60A)),
    WHITE("white", "Monochrome White", Color(0xFFFFFFFF)),
    CORAL("coral", "Sunset Coral", Color(0xFFFF6B6B));

    companion object {
        fun fromId(id: String): StandByColorTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ORANGE
        }
    }
}

data class StandBySettings(
    val isEnabled: Boolean = true,
    val autoStartWhileCharging: Boolean = true,
    val requireLandscape: Boolean = true,
    val requireScreenLocked: Boolean = true,
    val nightModeEnabled: Boolean = false,
    val burnInProtectionEnabled: Boolean = true,
    val autoDimEnabled: Boolean = true,
    val use24Hour: Boolean = false,
    val showSeconds: Boolean = false,
    val clockStyle: ClockStyle = ClockStyle.DIGITAL,
    val brightnessLevel: Float = 0.05f,
    val colorThemeId: String = StandByColorTheme.ORANGE.id
) {
    val activeColorTheme: StandByColorTheme
        get() = StandByColorTheme.fromId(colorThemeId)
}
