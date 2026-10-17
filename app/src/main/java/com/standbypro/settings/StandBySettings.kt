package com.standbypro.settings

import androidx.compose.ui.graphics.Color
import com.standbypro.domain.BottomComplicationType
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.NotificationPrivacy
import com.standbypro.domain.WatchFaceType
import com.standbypro.domain.WidgetSize
import com.standbypro.domain.WidgetSlot
import com.standbypro.ui.design.SecondsMotion

enum class ClockStyle {
    DIGITAL,
    ANALOG
}

enum class StandByColorTheme(
    val id: String,
    val displayName: String,
    val color: Color
) {
    ORANGE("orange", "StandBy Orange", Color(0xFFFF9500)),
    RED("red", "Crimson", Color(0xFFFF3B30)),
    CORAL("coral", "Sunset Coral", Color(0xFFFF6B6B)),
    PINK("pink", "Peony", Color(0xFFFF375F)),
    YELLOW("yellow", "Solar Yellow", Color(0xFFFFD60A)),
    GOLD("gold", "Champagne Gold", Color(0xFFE9CC7A)),
    GREEN("green", "Emerald", Color(0xFF30D158)),
    MINT("mint", "Mint", Color(0xFF63E6BE)),
    CYAN("cyan", "Glacier Cyan", Color(0xFF64D2FF)),
    BLUE("blue", "Sapphire", Color(0xFF0A84FF)),
    INDIGO("indigo", "Indigo", Color(0xFF5E5CE6)),
    PURPLE("purple", "Royal Purple", Color(0xFFBF5AF2)),
    WHITE("white", "Porcelain", Color(0xFFF5F5F7)),
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

/** What switches the display into its red/amber night rendition. */
enum class NightTrigger(val label: String, val description: String) {
    LIGHT_SENSOR("Dark room", "Ambient light sensor below 5 lux"),
    SCHEDULE("Schedule", "Between the bedtime hours you choose"),
    ALWAYS("Always", "Night rendition all the time")
}

enum class NightStyle(val id: String, val label: String, val tint: Color) {
    RED("RED", "Red", Color(0xFFFF2D1E)),
    AMBER("AMBER", "Amber", Color(0xFFFF9F0A)),
    DIM("DIM", "Dim white", Color(0xFF8E8E93));

    companion object {
        fun fromId(id: String): NightStyle = entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: RED
    }
}

/** StandBy display pages, swiped horizontally like iOS. */
enum class StandByPage(val label: String) {
    DASHBOARD("Dashboard"),
    STACKS("Smart Stacks"),
    PHOTOS("Photos"),
    CLOCK("Clock")
}

data class StandBySettings(
    val isEnabled: Boolean = true,
    val autoStartWhileCharging: Boolean = true,
    val requireLandscape: Boolean = true,
    val requireScreenLocked: Boolean = true,

    // Night & power
    val nightModeEnabled: Boolean = true,
    val nightTrigger: NightTrigger = NightTrigger.LIGHT_SENSOR,
    val nightStartMinutes: Int = 22 * 60,
    val nightEndMinutes: Int = 7 * 60,
    val nightModeStyle: String = NightStyle.RED.id,
    val burnInProtectionEnabled: Boolean = true,
    val autoDimEnabled: Boolean = true,
    val autoDimTimeoutSeconds: Int = 60,
    val brightnessLevel: Float = 0.35f,

    // Clock
    val use24Hour: Boolean = false,
    val showSeconds: Boolean = true,
    val secondsMotion: SecondsMotion = SecondsMotion.SWEEP,
    val clockStyle: ClockStyle = ClockStyle.ANALOG,
    val colorThemeId: String = StandByColorTheme.ORANGE.id,
    val watchFaceType: WatchFaceType = WatchFaceType.GMT_CALENDAR,
    val bottomComplication: BottomComplicationType = BottomComplicationType.BATTERY,
    val githubUsername: String = "ArindamJaiman",

    // Layout
    val activeWatchFaceId: String = "analog_standby",
    val layoutType: DashboardLayoutType = DashboardLayoutType.DUO,
    val assignedWidgets: List<WidgetSlot> = listOf(
        WidgetSlot("slot_1", "widget_calendar", WidgetSize.MEDIUM),
        WidgetSlot("slot_2", "widget_weather", WidgetSize.MEDIUM),
        WidgetSlot("slot_3", "widget_battery", WidgetSize.MEDIUM)
    ),
    val leftStack: List<String> = listOf("widget_clock_analog", "widget_world_clock", "widget_timer"),
    val rightStack: List<String> = listOf("widget_calendar", "widget_weather", "widget_music", "widget_agenda"),
    val smartRotate: Boolean = true,
    val startPage: StandByPage = StandByPage.DASHBOARD,
    val activeProfile: StandByProfile = StandByProfile.HOME,
    val favoriteFaceIds: Set<String> = DEFAULT_FAVORITES,
    val recentFaceIds: List<String> = emptyList(),

    // Data sources
    val useFahrenheit: Boolean = false,
    val weatherCity: String = "",
    val weatherLatitude: Double? = null,
    val weatherLongitude: Double? = null,
    val worldCities: List<String> = listOf("London", "New York", "Tokyo", "Dubai", "Sydney", "San Francisco"),
    val secondaryCity: String = "London",
    val photoUris: List<String> = emptyList(),
    val photoIntervalSeconds: Int = 20,
    val countdownLabel: String = "New Year",
    val countdownEpochDay: Long? = null,
    val notificationPrivacy: NotificationPrivacy = NotificationPrivacy.ICON_ONLY,
    val showChargingAnimation: Boolean = true,
    val hapticsEnabled: Boolean = true,

    // Developer / demo simulation overrides
    val demoModeEnabled: Boolean = false,
    val simulateCharging: Boolean = true,
    val simulateNight: Boolean = false
) {
    val activeColorTheme: StandByColorTheme
        get() = StandByColorTheme.fromId(colorThemeId)

    val nightStyle: NightStyle
        get() = NightStyle.fromId(nightModeStyle)

    companion object {
        val DEFAULT_FAVORITES = setOf(
            "analog_standby", "digital_minimal", "gmt_explorer", "flip_classic",
            "analog_solar", "digital_float", "astro_moon", "world_clock_face"
        )
    }
}
