package com.standbypro.domain

/**
 * Functional category for dashboard and complication widgets.
 */
enum class WidgetCategory(val displayName: String) {
    CLOCK("Clock"),
    CALENDAR("Calendar"),
    WEATHER("Weather"),
    BATTERY("Battery"),
    MUSIC("Music"),
    GITHUB("GitHub"),
    ALARM("Alarm"),
    TIMER("Timer"),
    STOPWATCH("Stopwatch"),
    REMINDER("Reminders"),
    TODO("To-Do"),
    NOTES("Notes"),
    WORLD_CLOCK("World Clock"),
    SYSTEM("System")
}

/**
 * Size allocation for a widget in a dashboard slot.
 */
enum class WidgetSize {
    SMALL,
    MEDIUM,
    LARGE,
    FULL
}

/**
 * Represents an assigned slot on the dashboard.
 */
data class WidgetSlot(
    val slotId: String,
    val widgetId: String,
    val size: WidgetSize = WidgetSize.MEDIUM
)

/**
 * Interface contract implemented by every StandBy widget.
 */
interface StandByWidget {
    val id: String
    val name: String
    val category: WidgetCategory
    val preferredSize: WidgetSize
    val supportsDarkMode: Boolean get() = true
    val supportsNightMode: Boolean get() = true
    val supportsCompactLayout: Boolean get() = true
    val description: String get() = ""
}

/**
 * Concrete metadata container for registered widgets.
 */
data class StandByWidgetDefinition(
    override val id: String,
    override val name: String,
    override val category: WidgetCategory,
    override val preferredSize: WidgetSize = WidgetSize.MEDIUM,
    override val supportsDarkMode: Boolean = true,
    override val supportsNightMode: Boolean = true,
    override val supportsCompactLayout: Boolean = true,
    override val description: String = ""
) : StandByWidget
