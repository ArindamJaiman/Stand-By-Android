package com.standbypro.domain

enum class FocusSessionType(val displayName: String, val defaultMinutes: Int) {
    POMODORO("Deep Focus", 25),
    SHORT_BREAK("Short Break", 5),
    LONG_BREAK("Long Break", 15),
    CUSTOM("Custom Session", 30)
}

data class TimerState(
    val totalSeconds: Int = 300, // Default 5 mins
    val remainingSeconds: Int = 300,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false
) {
    val progress: Float
        get() = if (totalSeconds > 0) (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat() else 0f

    val formattedRemaining: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }
}

data class StopwatchLap(
    val lapIndex: Int,
    val lapTimeMs: Long,
    val totalTimeMs: Long
)

data class StopwatchState(
    val elapsedMs: Long = 0L,
    val isRunning: Boolean = false,
    val laps: List<StopwatchLap> = emptyList()
) {
    val formattedElapsed: String
        get() {
            val totalSeconds = elapsedMs / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val centis = (elapsedMs % 1000) / 10
            return "%02d:%02d.%02d".format(minutes, seconds, centis)
        }
}

data class PomodoroState(
    val currentType: FocusSessionType = FocusSessionType.POMODORO,
    val remainingSeconds: Int = 25 * 60,
    val isRunning: Boolean = false,
    val completedCount: Int = 3
) {
    val totalSeconds: Int
        get() = currentType.defaultMinutes * 60

    val progress: Float
        get() = if (totalSeconds > 0) (totalSeconds - remainingSeconds).toFloat() / totalSeconds.toFloat() else 0f

    val formattedRemaining: String
        get() {
            val mins = remainingSeconds / 60
            val secs = remainingSeconds % 60
            return "%02d:%02d".format(mins, secs)
        }
}
