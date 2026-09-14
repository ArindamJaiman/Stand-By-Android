package com.standbypro.domain

/**
 * High-priority ambient live activities shown in the header Dynamic Island / Pill.
 */
sealed interface LiveActivityState {
    val id: String
    val title: String
    val subtitle: String
    val progress: Float? // 0.0f to 1.0f or null for indeterminate
    val timestamp: String
    val status: String
    val iconName: String

    data class TimerActivity(
        override val id: String = "timer_active",
        override val title: String = "Focus Timer",
        override val subtitle: String = "14:28 remaining",
        override val progress: Float? = 0.62f,
        override val timestamp: String = "Now",
        override val status: String = "Running",
        override val iconName: String = "Timer"
    ) : LiveActivityState

    data class MusicActivity(
        override val id: String = "music_active",
        override val title: String = "Midnight City",
        override val subtitle: String = "M83 • Hurry Up, We're Dreaming",
        override val progress: Float? = 0.42f,
        override val timestamp: String = "Playing",
        override val status: String = "Playback",
        override val iconName: String = "MusicNote"
    ) : LiveActivityState

    data class SportsActivity(
        override val id: String = "sports_active",
        override val title: String = "Champions League",
        override val subtitle: String = "Real Madrid 2 - 1 Bayern Munich",
        override val progress: Float? = 0.85f,
        override val timestamp: String = "78'",
        override val status: String = "2nd Half",
        override val iconName: String = "SportsSoccer"
    ) : LiveActivityState

    data class DeliveryActivity(
        override val id: String = "delivery_active",
        override val title: String = "DoorDash Delivery",
        override val subtitle: String = "Arriving in 8 mins • Courier nearby",
        override val progress: Float? = 0.75f,
        override val timestamp: String = "8m",
        override val status: String = "On the way",
        override val iconName: String = "TwoWheeler"
    ) : LiveActivityState

    data class NavigationActivity(
        override val id: String = "nav_active",
        override val title: String = "Turn Right in 300m",
        override val subtitle: String = "Onto Grand Avenue • 12 mins to Home",
        override val progress: Float? = 0.55f,
        override val timestamp: String = "12m",
        override val status: String = "Navigating",
        override val iconName: String = "Navigation"
    ) : LiveActivityState

    data class FitnessActivity(
        override val id: String = "fitness_active",
        override val title: String = "Outdoor Run",
        override val subtitle: String = "4.82 km • 5'18\" /km • 342 kcal",
        override val progress: Float? = 0.80f,
        override val timestamp: String = "26m",
        override val status: String = "Recording",
        override val iconName: String = "DirectionsRun"
    ) : LiveActivityState
}
