package com.standbypro.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.standbypro.data.GitHubRepository
import com.standbypro.ui.components.GitHubContributionWidget
import com.standbypro.ui.widgets.*
import java.time.LocalDateTime

/**
 * Centralized registry of all StandBy widgets with metadata and composable dispatch.
 */
object WidgetRegistry {

    private val widgets: List<StandByWidgetDefinition> = listOf(
        StandByWidgetDefinition("widget_calendar", "Month Calendar", WidgetCategory.CALENDAR, WidgetSize.MEDIUM, description = "Interactive month calendar with day highlights"),
        StandByWidgetDefinition("widget_agenda", "Upcoming Schedule", WidgetCategory.CALENDAR, WidgetSize.MEDIUM, description = "List of upcoming calendar meetings and events"),
        StandByWidgetDefinition("widget_weather", "Live Weather", WidgetCategory.WEATHER, WidgetSize.MEDIUM, description = "Current temperature, weather condition, and daily extremes"),
        StandByWidgetDefinition("widget_battery", "Power & Battery", WidgetCategory.BATTERY, WidgetSize.MEDIUM, description = "Battery percentage, charging speed, and status"),
        StandByWidgetDefinition("widget_github", "GitHub Heatmap", WidgetCategory.GITHUB, WidgetSize.MEDIUM, description = "14-week commit matrix, streaks, and annual totals"),
        StandByWidgetDefinition("widget_music", "Music Player", WidgetCategory.MUSIC, WidgetSize.MEDIUM, description = "Playback controls, album track info, and scrub bar"),
        StandByWidgetDefinition("widget_lyrics", "Live Lyrics", WidgetCategory.MUSIC, WidgetSize.MEDIUM, description = "Synchronized live lyrics line for current track"),
        StandByWidgetDefinition("widget_timer", "Countdown Timer", WidgetCategory.TIMER, WidgetSize.MEDIUM, description = "Interactive countdown timer with start/pause/reset"),
        StandByWidgetDefinition("widget_stopwatch", "Stopwatch", WidgetCategory.STOPWATCH, WidgetSize.MEDIUM, description = "Millisecond stopwatch with lap timing"),
        StandByWidgetDefinition("widget_pomodoro", "Pomodoro Focus", WidgetCategory.TODO, WidgetSize.MEDIUM, description = "25m focus & 5m break interval productivity tracker"),
        StandByWidgetDefinition("widget_world_clock", "World Clocks", WidgetCategory.WORLD_CLOCK, WidgetSize.MEDIUM, description = "Dual timezone clocks with international offset"),
        StandByWidgetDefinition("widget_todo", "To-Do Checklist", WidgetCategory.TODO, WidgetSize.MEDIUM, description = "Interactive bedside task checklist with completion toggles"),
        StandByWidgetDefinition("widget_notes", "Bedside Notes", WidgetCategory.NOTES, WidgetSize.MEDIUM, description = "Pinned quick notes and bedside thoughts"),
        StandByWidgetDefinition("widget_system", "System Status", WidgetCategory.SYSTEM, WidgetSize.MEDIUM, description = "Kernel uptime, memory headroom, and sensor status"),
        StandByWidgetDefinition("widget_alarm", "Next Alarm", WidgetCategory.ALARM, WidgetSize.MEDIUM, description = "Scheduled alarm time and countdown")
    )

    fun getAll(): List<StandByWidgetDefinition> = widgets

    fun getById(id: String): StandByWidgetDefinition {
        return widgets.firstOrNull { it.id.equals(id, ignoreCase = true) }
            ?: widgets.first { it.id == "widget_calendar" }
    }

    fun getByCategory(category: WidgetCategory): List<StandByWidgetDefinition> {
        return widgets.filter { it.category == category }
    }

    @Composable
    fun Render(
        id: String,
        time: LocalDateTime,
        chargingState: ChargingState,
        nextAlarm: String?,
        accentColor: Color,
        modifier: Modifier = Modifier
    ) {
        when (id) {
            "widget_calendar" -> CalendarStandByWidget(time, chargingState, nextAlarm, accentColor, modifier)
            "widget_agenda" -> AgendaStandByWidget(accentColor, modifier)
            "widget_weather" -> WeatherStandByWidget(accentColor, modifier)
            "widget_battery" -> BatteryStandByWidget(chargingState, accentColor, modifier)
            "widget_github" -> {
                val gitHubState = GitHubRepository.contributionsState
                GitHubContributionWidget(
                    state = gitHubState.value,
                    accentColor = accentColor,
                    modifier = modifier
                )
            }
            "widget_music" -> MusicStandByWidget(accentColor, modifier)
            "widget_lyrics" -> LyricsStandByWidget(accentColor, modifier)
            "widget_timer" -> TimerStandByWidget(accentColor, modifier)
            "widget_stopwatch" -> StopwatchStandByWidget(accentColor, modifier)
            "widget_pomodoro" -> PomodoroStandByWidget(accentColor, modifier)
            "widget_world_clock" -> WorldClockStandByWidget(accentColor, modifier)
            "widget_todo" -> TodoStandByWidget(accentColor, modifier)
            "widget_notes" -> NotesStandByWidget(accentColor, modifier)
            "widget_system" -> SystemStatusStandByWidget(accentColor, modifier)
            "widget_alarm" -> AlarmStandByWidget(nextAlarm, accentColor, modifier)
            else -> CalendarStandByWidget(time, chargingState, nextAlarm, accentColor, modifier)
        }
    }
}
