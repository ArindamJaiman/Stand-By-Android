package com.standbypro.settings

import androidx.compose.ui.graphics.Color
import com.standbypro.domain.BottomComplicationType
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.WatchFaceType
import com.standbypro.domain.WidgetSize
import com.standbypro.domain.WidgetSlot

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
    CORAL("coral", "Sunset Coral", Color(0xFFFF6B6B)),
    BLUE("blue", "Deep Sapphire", Color(0xFF0A84FF)),
    MONOCHROME("monochrome", "OLED Slate", Color(0xFF8E8E93));

    companion object {
        fun fromId(id: String): StandByColorTheme {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ORANGE
        }
    }
}

enum class StandByProfile(val displayName: String) {
    HOME("Home"),
    WORK("Work"),
    BEDROOM("Bedroom"),
    FOCUS("Focus"),
    TRAVEL("Travel"),
    CUSTOM("Custom")
}

data class StandBySettings(
    val isEnabled: Boolean = true,
    val autoStartWhileCharging: Boolean = true,
    val requireLandscape: Boolean = true,
    val requireScreenLocked: Boolean = true,
    val nightModeEnabled: Boolean = false,
    val nightModeStyle: String = "RED",
    val burnInProtectionEnabled: Boolean = true,
    val autoDimEnabled: Boolean = true,
    val use24Hour: Boolean = false,
    val showSeconds: Boolean = false,
    val clockStyle: ClockStyle = ClockStyle.ANALOG,
    val brightnessLevel: Float = 0.05f,
    val colorThemeId: String = StandByColorTheme.ORANGE.id,
    val watchFaceType: WatchFaceType = WatchFaceType.GMT_CALENDAR,
    val bottomComplication: BottomComplicationType = BottomComplicationType.BATTERY,
    val githubUsername: String = "ArindamJaiman",

    // Extended Architecture Properties
    val activeWatchFaceId: String = "gmt_explorer",
    val layoutType: DashboardLayoutType = DashboardLayoutType.DUO,
    val assignedWidgets: List<WidgetSlot> = listOf(
        WidgetSlot("slot_1", "widget_calendar", WidgetSize.MEDIUM),
        WidgetSlot("slot_2", "widget_weather", WidgetSize.MEDIUM),
        WidgetSlot("slot_3", "widget_battery", WidgetSize.MEDIUM)
    ),
    val activeProfile: StandByProfile = StandByProfile.HOME,
    val favoriteFaceIds: Set<String> = setOf("gmt_explorer", "digital_minimal", "digital_oled", "productivity_pomodoro", "flip_amber"),
    val recentFaceIds: List<String> = listOf("gmt_explorer", "digital_minimal", "analog_luxury", "flip_amber"),

    // Developer / Demo Simulation Overrides
    val demoModeEnabled: Boolean = false,
    val simulateCharging: Boolean = true,
    val simulateNight: Boolean = false
) {
    val activeColorTheme: StandByColorTheme
        get() = StandByColorTheme.fromId(colorThemeId)
}
