package com.standbypro.data

import android.os.SystemClock
import com.standbypro.domain.FocusSessionType
import com.standbypro.domain.PomodoroState
import com.standbypro.domain.StopwatchLap
import com.standbypro.domain.StopwatchState
import com.standbypro.domain.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Timer, stopwatch and Pomodoro engines. All of them are driven by a monotonic deadline
 * (elapsedRealtime) rather than by counting ticks, so they never drift even if the process is
 * throttled while the screen is dim.
 */
object ProductivityRepository {

    enum class Finished { TIMER, POMODORO }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _finished = MutableSharedFlow<Finished>(extraBufferCapacity = 4)
    /** Emits when a countdown reaches zero so the UI can chime. */
    val finished: SharedFlow<Finished> = _finished.asSharedFlow()

    // ================= TIMER =================
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    private var timerJob: Job? = null
    private var timerDeadline = 0L

    val timerPresets = listOf(60, 3 * 60, 5 * 60, 10 * 60, 15 * 60, 25 * 60, 45 * 60, 60 * 60)

    fun setTimerDuration(seconds: Int) {
        timerJob?.cancel()
        _timerState.value = TimerState(totalSeconds = seconds, remainingSeconds = seconds, isRunning = false, isCompleted = false)
    }

    fun addTimerMinute() {
        val s = _timerState.value
        if (s.isRunning) {
            timerDeadline += 60_000L
            _timerState.value = s.copy(totalSeconds = s.totalSeconds + 60, remainingSeconds = s.remainingSeconds + 60)
        } else {
            _timerState.value = s.copy(totalSeconds = s.totalSeconds + 60, remainingSeconds = s.remainingSeconds + 60, isCompleted = false)
        }
    }

    fun toggleTimer() = if (_timerState.value.isRunning) pauseTimer() else startTimer()

    fun startTimer() {
        val current = _timerState.value
        if (current.isRunning) return
        val remaining = if (current.remainingSeconds <= 0) current.totalSeconds else current.remainingSeconds
        timerDeadline = SystemClock.elapsedRealtime() + remaining * 1000L
        _timerState.value = current.copy(remainingSeconds = remaining, isRunning = true, isCompleted = false)
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                val left = ((timerDeadline - SystemClock.elapsedRealtime() + 999) / 1000).toInt()
                if (left <= 0) {
                    _timerState.value = _timerState.value.copy(remainingSeconds = 0, isRunning = false, isCompleted = true)
                    _finished.tryEmit(Finished.TIMER)
                    break
                }
                if (left != _timerState.value.remainingSeconds) {
                    _timerState.value = _timerState.value.copy(remainingSeconds = left)
                }
                delay(200L)
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        val total = _timerState.value.totalSeconds
        _timerState.value = TimerState(totalSeconds = total, remainingSeconds = total, isRunning = false, isCompleted = false)
    }

    // ================= STOPWATCH =================
    private val _stopwatchState = MutableStateFlow(StopwatchState())
    val stopwatchState: StateFlow<StopwatchState> = _stopwatchState.asStateFlow()
    private var stopwatchJob: Job? = null
    private var stopwatchStartedAt = 0L
    private var stopwatchBase = 0L

    fun toggleStopwatch() = if (_stopwatchState.value.isRunning) pauseStopwatch() else startStopwatch()

    fun startStopwatch() {
        if (_stopwatchState.value.isRunning) return
        stopwatchBase = _stopwatchState.value.elapsedMs
        stopwatchStartedAt = SystemClock.elapsedRealtime()
        _stopwatchState.value = _stopwatchState.value.copy(isRunning = true)
        stopwatchJob?.cancel()
        stopwatchJob = scope.launch {
            while (isActive) {
                _stopwatchState.value = _stopwatchState.value.copy(
                    elapsedMs = stopwatchBase + (SystemClock.elapsedRealtime() - stopwatchStartedAt)
                )
                delay(33L)
            }
        }
    }

    fun pauseStopwatch() {
        stopwatchJob?.cancel()
        if (_stopwatchState.value.isRunning) {
            _stopwatchState.value = _stopwatchState.value.copy(
                isRunning = false,
                elapsedMs = stopwatchBase + (SystemClock.elapsedRealtime() - stopwatchStartedAt)
            )
        }
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _stopwatchState.value = StopwatchState(elapsedMs = 0L, isRunning = false, laps = emptyList())
    }

    fun recordLap() {
        val currentElapsed = _stopwatchState.value.elapsedMs
        if (currentElapsed <= 0L) return
        val laps = _stopwatchState.value.laps
        val previousTotal = laps.firstOrNull()?.totalTimeMs ?: 0L
        val newLap = StopwatchLap(lapIndex = laps.size + 1, lapTimeMs = (currentElapsed - previousTotal).coerceAtLeast(0L), totalTimeMs = currentElapsed)
        _stopwatchState.value = _stopwatchState.value.copy(laps = listOf(newLap) + laps)
    }

    // ================= POMODORO =================
    private val _pomodoroState = MutableStateFlow(PomodoroState(completedCount = 0))
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    private var pomodoroJob: Job? = null
    private var pomodoroDeadline = 0L

    fun setPomodoroType(type: FocusSessionType) {
        pomodoroJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(currentType = type, remainingSeconds = type.defaultMinutes * 60, isRunning = false)
    }

    fun togglePomodoro() = if (_pomodoroState.value.isRunning) pausePomodoro() else startPomodoro()

    fun startPomodoro() {
        val current = _pomodoroState.value
        if (current.isRunning) return
        val remaining = if (current.remainingSeconds <= 0) current.totalSeconds else current.remainingSeconds
        pomodoroDeadline = SystemClock.elapsedRealtime() + remaining * 1000L
        _pomodoroState.value = current.copy(remainingSeconds = remaining, isRunning = true)
        pomodoroJob?.cancel()
        pomodoroJob = scope.launch {
            while (isActive) {
                val left = ((pomodoroDeadline - SystemClock.elapsedRealtime() + 999) / 1000).toInt()
                if (left <= 0) {
                    val s = _pomodoroState.value
                    val focusDone = s.currentType == FocusSessionType.POMODORO
                    val completed = if (focusDone) s.completedCount + 1 else s.completedCount
                    // Auto-advance: focus → break (long break every 4th), break → focus.
                    val nextType = when {
                        focusDone && completed % 4 == 0 -> FocusSessionType.LONG_BREAK
                        focusDone -> FocusSessionType.SHORT_BREAK
                        else -> FocusSessionType.POMODORO
                    }
                    _pomodoroState.value = s.copy(
                        currentType = nextType,
                        remainingSeconds = nextType.defaultMinutes * 60,
                        isRunning = false,
                        completedCount = completed
                    )
                    _finished.tryEmit(Finished.POMODORO)
                    break
                }
                if (left != _pomodoroState.value.remainingSeconds) {
                    _pomodoroState.value = _pomodoroState.value.copy(remainingSeconds = left)
                }
                delay(200L)
            }
        }
    }

    fun pausePomodoro() {
        pomodoroJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(isRunning = false)
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(remainingSeconds = _pomodoroState.value.currentType.defaultMinutes * 60, isRunning = false)
    }
}
