package com.standbypro.domain

/**
 * Categories for grouping watch faces in the StandBy Studio browser.
 */
enum class WatchFaceCategory(val displayName: String, val iconName: String) {
    DIGITAL("Digital", "Pin"),
    ANALOG("Analog", "Schedule"),
    GMT("GMT & World", "Public"),
    HYBRID("Hybrid", "DashboardCustomize"),
    MODULAR("Modular", "GridView"),
    FLIP("Flip Clock", "FlipCameraAndroid"),
    WEATHER("Weather", "WbSunny"),
    ASTRONOMY("Astronomy", "DarkMode"),
    PRODUCTIVITY("Productivity", "Timer"),
    AMBIENT("Ambient", "NightsStay"),
    ARTISTIC("Artistic", "Palette"),
    PHOTO("Photo", "PhotoLibrary"),
    CHARGING("Charging", "BatteryChargingFull"),
    WORLD_CLOCK("World Time", "Language"),
    CALENDAR("Calendar", "CalendarMonth")
}

/**
 * Metadata definition for a watch face in StandBy Pro.
 */
data class WatchFaceDefinition(
    val id: String,
    val name: String,
    val category: WatchFaceCategory,
    val description: String,
    val supportsTheme: Boolean = true,
    val supportsWidgets: Boolean = true,
    val supportsAnimation: Boolean = true,
    val isFavoriteByDefault: Boolean = false,
    val isNew: Boolean = false,
    /** Faces with buttons (timers) that should receive taps instead of cycling. */
    val isInteractive: Boolean = false,
    /** Uses live data that needs a permission or network (weather, calendar, photos). */
    val usesLiveData: Boolean = false
)
