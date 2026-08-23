package com.standbypro.widgets

enum class WidgetType {
    CLOCK,
    WEATHER,
    CALENDAR,
    MEDIA,
    BATTERY,
    UNKNOWN
}

data class WidgetConfig(
    val id: String,
    val type: WidgetType,
    val isSystemWidget: Boolean = false,
    val appWidgetId: Int? = null
)

class WidgetRepository {
    // In a real app this would read from DataStore or Room
    
    fun getLeftWidgets(): List<WidgetConfig> {
        return listOf(
            WidgetConfig("clock_1", WidgetType.CLOCK)
        )
    }
    
    fun getRightWidgets(): List<WidgetConfig> {
        return listOf(
            WidgetConfig("weather_1", WidgetType.WEATHER),
            WidgetConfig("calendar_1", WidgetType.CALENDAR)
        )
    }
}
