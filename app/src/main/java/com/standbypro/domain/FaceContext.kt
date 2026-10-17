package com.standbypro.domain

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.standbypro.ui.design.SecondsMotion
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Everything a watch face or widget may render. Built once per tick by the view model so
 * faces stay pure functions of their input and never fabricate data.
 */
@Immutable
data class FaceContext(
    val time: LocalDateTime = LocalDateTime.now(),
    val accent: Color = Color(0xFFFF9500),
    val use24Hour: Boolean = false,
    val showSeconds: Boolean = true,
    val secondsMotion: SecondsMotion = SecondsMotion.SWEEP,
    val battery: ChargingState = ChargingState(),
    val weather: WeatherData? = null,
    val useFahrenheit: Boolean = false,
    val nextAlarm: String? = null,
    val nextAlarmTime: String? = null,
    val nextAlarmMillis: Long? = null,
    val events: List<CalendarEventItem> = emptyList(),
    val worldClocks: List<TimeZoneClock> = TimeZoneClock.DEFAULT_WORLD_CLOCKS,
    val secondaryZoneId: String = "Europe/London",
    val photos: List<String> = emptyList(),
    val photoIntervalSeconds: Int = 20,
    val notificationPrivacy: NotificationPrivacy = NotificationPrivacy.ICON_ONLY,
    val latitude: Double = 28.61,
    val longitude: Double = 77.21,
    val locationName: String = "",
    val countdownLabel: String = "New Year",
    val countdownDate: LocalDate = LocalDate.of(LocalDate.now().year + 1, 1, 1),
    val isNight: Boolean = false,
    /** Gallery thumbnails: render statically, no frame loops. */
    val isPreview: Boolean = false,
    /** Compact slot (dashboard quadrant) – faces drop secondary detail. */
    val isCompact: Boolean = false
) {
    val animate: Boolean get() = !isPreview

    val effectiveMotion: SecondsMotion
        get() = when {
            !showSeconds -> SecondsMotion.OFF
            isNight && secondsMotion == SecondsMotion.SWEEP -> SecondsMotion.TICK
            else -> secondsMotion
        }

    fun temperature(celsius: Int): String =
        if (useFahrenheit) "${(celsius * 9 / 5.0 + 32).toInt()}°" else "$celsius°"

    val hour12: Int get() = if (time.hour % 12 == 0) 12 else time.hour % 12
    val displayHour: String get() = if (use24Hour) "%02d".format(time.hour) else hour12.toString()
    val minuteText: String get() = "%02d".format(time.minute)
    val secondText: String get() = "%02d".format(time.second)
    val amPm: String get() = if (time.hour < 12) "AM" else "PM"
    val timeText: String get() = "$displayHour:$minuteText"
}
