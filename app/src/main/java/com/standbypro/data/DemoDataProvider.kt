package com.standbypro.data

import com.standbypro.domain.BedsideNote
import com.standbypro.domain.CalendarEventItem
import com.standbypro.domain.DailyForecast
import com.standbypro.domain.HourlyForecast
import com.standbypro.domain.IncomingCallState
import com.standbypro.domain.LiveActivityState
import com.standbypro.domain.NotificationDisplayState
import com.standbypro.domain.NotificationItem
import com.standbypro.domain.NotificationPrivacy
import com.standbypro.domain.TimeZoneClock
import com.standbypro.domain.TodoItem
import com.standbypro.domain.WeatherCondition
import com.standbypro.domain.WeatherData

/**
 * Deterministic sample dataset provider to ensure every StandBy screen, preview,
 * and widget is richly populated and immediately demonstrable.
 */
object DemoDataProvider {

    val demoWeather = WeatherData(
        cityName = "San Francisco",
        temperatureCelsius = 22,
        condition = WeatherCondition.PARTLY_CLOUDY_DAY,
        feelsLikeCelsius = 23,
        highCelsius = 25,
        lowCelsius = 15,
        humidityPercent = 54,
        windKmh = 12,
        uvIndex = 5,
        sunriseTime = "6:38 AM",
        sunsetTime = "7:24 PM",
        hourlyForecast = listOf(
            HourlyForecast("Now", 22, WeatherCondition.PARTLY_CLOUDY_DAY),
            HourlyForecast("18:00", 21, WeatherCondition.PARTLY_CLOUDY_DAY),
            HourlyForecast("19:00", 19, WeatherCondition.CLEAR_NIGHT),
            HourlyForecast("20:00", 18, WeatherCondition.CLEAR_NIGHT),
            HourlyForecast("21:00", 17, WeatherCondition.CLEAR_NIGHT),
            HourlyForecast("22:00", 16, WeatherCondition.CLEAR_NIGHT),
            HourlyForecast("23:00", 15, WeatherCondition.CLOUDY)
        ),
        dailyForecast = listOf(
            DailyForecast("Today", 25, 15, WeatherCondition.PARTLY_CLOUDY_DAY),
            DailyForecast("Tue", 24, 14, WeatherCondition.CLEAR_DAY),
            DailyForecast("Wed", 22, 13, WeatherCondition.RAIN),
            DailyForecast("Thu", 23, 14, WeatherCondition.PARTLY_CLOUDY_DAY),
            DailyForecast("Fri", 26, 16, WeatherCondition.CLEAR_DAY)
        )
    )

    val demoCalendarEvents = listOf(
        CalendarEventItem("ev_1", "Sprint Sync & Architecture Review", "10:30 AM - 11:30 AM", "Room 4B / Meet", false, "#00E5FF"),
        CalendarEventItem("ev_2", "Design Critique: StandBy Ambient Mode", "02:00 PM - 03:00 PM", "Studio Alpha", false, "#30D158"),
        CalendarEventItem("ev_3", "Product Demo with Stakeholders", "04:30 PM - 05:15 PM", "Main Hall", false, "#FF9500"),
        CalendarEventItem("ev_4", "Gym & Evening Recovery Run", "07:00 PM - 08:00 PM", "Fitness Center", false, "#AF52DE")
    )

    val demoTodos = listOf(
        TodoItem("td_1", "Review StandBy Pro pull request", true, "Dev"),
        TodoItem("td_2", "Calibrate ambient light sensor hysteresis", false, "Hardware"),
        TodoItem("td_3", "Sync GitHub contribution graph API", false, "Integrations"),
        TodoItem("td_4", "Inspect OLED true black burn-in offsets", false, "Display")
    )

    val demoNotes = listOf(
        BedsideNote(
            id = "note_1",
            title = "Morning Priorities",
            content = "1. Test charging landscape trigger on Nothing Phone.\n2. Verify 120Hz smooth sweep second hand.\n3. Water bedside bonsai tree.",
            isPinned = true,
            timestamp = "Pinned • Today"
        ),
        BedsideNote(
            id = "note_2",
            title = "Book Recommendation",
            content = "The Design of Everyday Things — Don Norman. Chapter 4 on affordances & physical constraints.",
            isPinned = false,
            timestamp = "Yesterday"
        )
    )

    val demoNotifications = NotificationDisplayState(
        notifications = listOf(
            NotificationItem("notif_1", "Messages", "Alex Rivera", "Are we still on for the 10:30 sync?", "2m ago", "Chat"),
            NotificationItem("notif_2", "Calendar", "StandBy Team", "Reminder: Architecture Review starts in 15m", "15m ago", "Event"),
            NotificationItem("notif_3", "GitHub", "StandBy-Android", "New star on repository: 25 stars reached!", "1h ago", "Star")
        ),
        unreadCount = 3,
        privacyMode = NotificationPrivacy.ICON_ONLY
    )

    val demoLiveActivities: List<LiveActivityState> = listOf(
        LiveActivityState.TimerActivity(),
        LiveActivityState.MusicActivity(),
        LiveActivityState.DeliveryActivity(),
        LiveActivityState.SportsActivity()
    )

    val demoIncomingCall = IncomingCallState(
        callerName = "Sarah Jenkins",
        callerSubtitle = "Mobile • Work",
        isIncoming = false
    )

    val demoWorldClocks = TimeZoneClock.DEFAULT_WORLD_CLOCKS

    val demoLyrics = listOf(
        "City is my church, it wraps in the blinding twilight",
        "Waiting in a car, waiting for a ride in the dark",
        "The night city grows, look and see her eyes, they glow",
        "Waiting in a car, waiting for a ride in the dark"
    )
}
