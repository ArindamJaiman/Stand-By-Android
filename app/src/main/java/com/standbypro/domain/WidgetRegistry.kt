package com.standbypro.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.standbypro.ui.widgets.*

/**
 * Centralized registry of all StandBy widgets with metadata and composable dispatch.
 */
object WidgetRegistry {

    private class Entry(val definition: StandByWidgetDefinition, val content: @Composable (FaceContext, Modifier) -> Unit)

    private fun widget(
        id: String,
        name: String,
        category: WidgetCategory,
        description: String,
        content: @Composable (FaceContext, Modifier) -> Unit
    ) = Entry(StandByWidgetDefinition(id, name, category, WidgetSize.MEDIUM, description = description), content)

    private val entries: List<Entry> = listOf(
        widget("widget_clock_analog", "Analog Clock", WidgetCategory.CLOCK, "StandBy analog clock tile") { c, m -> AnalogClockWidget(c, m) },
        widget("widget_clock_digital", "Digital Clock", WidgetCategory.CLOCK, "Large digital time and date") { c, m -> DigitalClockWidget(c, m) },
        widget("widget_world_clock", "World Clock", WidgetCategory.WORLD_CLOCK, "Your cities with live time and offset") { c, m -> WorldClockWidget(c, m) },
        widget("widget_calendar", "Month Calendar", WidgetCategory.CALENDAR, "This month with today and event days marked") { c, m -> CalendarWidget(c, m) },
        widget("widget_agenda", "Up Next", WidgetCategory.CALENDAR, "Upcoming events from your calendars") { c, m -> AgendaWidget(c, m) },
        widget("widget_weather", "Weather", WidgetCategory.WEATHER, "Current conditions with an hourly strip") { c, m -> WeatherWidget(c, m) },
        widget("widget_forecast", "Forecast", WidgetCategory.WEATHER, "Five-day forecast with range bars") { c, m -> ForecastWidget(c, m) },
        widget("widget_music", "Now Playing", WidgetCategory.MUSIC, "Album art, progress and controls for any media app") { c, m -> MusicWidget(c, m) },
        widget("widget_battery", "Battery", WidgetCategory.BATTERY, "Charge ring, source, temperature and time to full") { c, m -> BatteryWidget(c, m) },
        widget("widget_alarm", "Next Alarm", WidgetCategory.ALARM, "Your next system alarm with countdown") { c, m -> AlarmWidget(c, m) },
        widget("widget_timer", "Timer", WidgetCategory.TIMER, "Countdown timer with presets") { c, m -> TimerWidget(c, m) },
        widget("widget_stopwatch", "Stopwatch", WidgetCategory.STOPWATCH, "Stopwatch with laps") { c, m -> StopwatchWidget(c, m) },
        widget("widget_pomodoro", "Pomodoro", WidgetCategory.PRODUCTIVITY, "Focus sessions with automatic breaks") { c, m -> PomodoroWidget(c, m) },
        widget("widget_day_progress", "Progress", WidgetCategory.PRODUCTIVITY, "Day, week, month and year progress") { c, m -> DayProgressWidget(c, m) },
        widget("widget_countdown", "Countdown", WidgetCategory.PRODUCTIVITY, "Days until your chosen date") { c, m -> CountdownWidget(c, m) },
        widget("widget_todo", "Reminders", WidgetCategory.TODO, "Tap to tick off bedside reminders") { c, m -> TodoWidget(c, m) },
        widget("widget_notes", "Notes", WidgetCategory.NOTES, "Your pinned note") { c, m -> NotesWidget(c, m) },
        widget("widget_notifications", "Notifications", WidgetCategory.NOTIFICATIONS, "Recent notifications, privacy-aware") { c, m -> NotificationsWidget(c, m) },
        widget("widget_moon", "Moon", WidgetCategory.ASTRONOMY, "Moon phase, illumination and next full moon") { c, m -> MoonWidget(c, m) },
        widget("widget_sun", "Sun", WidgetCategory.ASTRONOMY, "Sunrise, sunset and the sun's position") { c, m -> SunWidget(c, m) },
        widget("widget_photos", "Photos", WidgetCategory.PHOTO, "Slideshow of your favourite photos") { c, m -> PhotoWidget(c, m) },
        widget("widget_quote", "Quote", WidgetCategory.INSPIRATION, "A new quote every day") { c, m -> QuoteWidget(c, m) },
        widget("widget_system", "System", WidgetCategory.SYSTEM, "Memory, storage and uptime") { c, m -> SystemWidget(c, m) },
        widget("widget_github", "GitHub", WidgetCategory.GITHUB, "Contribution heatmap, streak and yearly total") { c, m -> GitHubWidget(c, m) }
    )

    private val byId = entries.associateBy { it.definition.id }
    private val widgets: List<StandByWidgetDefinition> = entries.map { it.definition }

    const val DEFAULT_WIDGET_ID = "widget_calendar"

    fun getAll(): List<StandByWidgetDefinition> = widgets

    fun contains(id: String): Boolean = byId.containsKey(id)

    fun getById(id: String): StandByWidgetDefinition =
        byId[id]?.definition ?: widgets.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: byId.getValue(DEFAULT_WIDGET_ID).definition

    fun getByCategory(category: WidgetCategory): List<StandByWidgetDefinition> = widgets.filter { it.category == category }

    @Composable
    fun Render(id: String, ctx: FaceContext, modifier: Modifier = Modifier) {
        val entry = byId[id] ?: byId.getValue(DEFAULT_WIDGET_ID)
        entry.content(ctx, modifier)
    }
}
