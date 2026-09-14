package com.standbypro.domain

/**
 * Categories for grouping watch faces in the StandBy Studio browser.
 */
enum class WatchFaceCategory(val displayName: String, val iconName: String) {
    DIGITAL("Digital", "Pin"),
    ANALOG("Analog", "Schedule"),
    HYBRID("Hybrid", "DashboardCustomize"),
    GMT("GMT", "Public"),
    FLIP("Flip Clock", "FlipCameraAndroid"),
    WORLD_CLOCK("World Time", "Language"),
    WEATHER("Weather", "WbSunny"),
    CALENDAR("Calendar", "CalendarMonth"),
    PRODUCTIVITY("Productivity", "Timer"),
    PHOTO("Photo", "PhotoLibrary"),
    AMBIENT("Ambient", "NightsStay"),
    ARTISTIC("Artistic", "Palette")
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
    val isFavoriteByDefault: Boolean = false
)
