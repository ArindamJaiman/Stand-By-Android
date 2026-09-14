package com.standbypro.data

import com.standbypro.domain.FocusSessionType
import com.standbypro.domain.PomodoroState
import com.standbypro.domain.StopwatchLap
import com.standbypro.domain.StopwatchState
import com.standbypro.domain.TimerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

object ProductivityRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    // ================= TIMER =================
    private val _timerState = MutableStateFlow(TimerState())
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    private var timerJob: Job? = null

    fun setTimerDuration(seconds: Int) {
        timerJob?.cancel()
        _timerState.value = TimerState(totalSeconds = seconds, remainingSeconds = seconds, isRunning = false, isCompleted = false)
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return
        if (_timerState.value.remainingSeconds <= 0) {
            _timerState.value = _timerState.value.copy(remainingSeconds = _timerState.value.totalSeconds, isCompleted = false)
        }
        _timerState.value = _timerState.value.copy(isRunning = true, isCompleted = false)

        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _timerState.value.remainingSeconds > 0) {
                delay(1000L)
                val remaining = _timerState.value.remainingSeconds - 1
                if (remaining <= 0) {
                    _timerState.value = _timerState.value.copy(remainingSeconds = 0, isRunning = false, isCompleted = true)
                    break
                } else {
                    _timerState.value = _timerState.value.copy(remainingSeconds = remaining)
                }
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

    fun startStopwatch() {
        if (_stopwatchState.value.isRunning) return
        _stopwatchState.value = _stopwatchState.value.copy(isRunning = true)

        stopwatchJob?.cancel()
        stopwatchJob = scope.launch {
            var lastTick = System.currentTimeMillis()
            while (isActive) {
                delay(30L)
                val now = System.currentTimeMillis()
                val delta = now - lastTick
                lastTick = now
                _stopwatchState.value = _stopwatchState.value.copy(elapsedMs = _stopwatchState.value.elapsedMs + delta)
            }
        }
    }

    fun pauseStopwatch() {
        stopwatchJob?.cancel()
        _stopwatchState.value = _stopwatchState.value.copy(isRunning = false)
    }

    fun resetStopwatch() {
        stopwatchJob?.cancel()
        _stopwatchState.value = StopwatchState(elapsedMs = 0L, isRunning = false, laps = emptyList())
    }

    fun recordLap() {
        val currentElapsed = _stopwatchState.value.elapsedMs
        val laps = _stopwatchState.value.laps
        val previousTotal = laps.firstOrNull()?.totalTimeMs ?: 0L
        val lapDelta = currentElapsed - previousTotal
        val newLap = StopwatchLap(lapIndex = laps.size + 1, lapTimeMs = lapDelta.coerceAtLeast(0L), totalTimeMs = currentElapsed)
        _stopwatchState.value = _stopwatchState.value.copy(laps = listOf(newLap) + laps)
    }

    // ================= POMODORO =================
    private val _pomodoroState = MutableStateFlow(PomodoroState())
    val pomodoroState: StateFlow<PomodoroState> = _pomodoroState.asStateFlow()
    private var pomodoroJob: Job? = null

    fun setPomodoroType(type: FocusSessionType) {
        pomodoroJob?.cancel()
        val seconds = type.defaultMinutes * 60
        _pomodoroState.value = _pomodoroState.value.copy(currentType = type, remainingSeconds = seconds, isRunning = false)
    }

    fun startPomodoro() {
        if (_pomodoroState.value.isRunning) return
        _pomodoroState.value = _pomodoroState.value.copy(isRunning = true)

        pomodoroJob?.cancel()
        pomodoroJob = scope.launch {
            while (isActive && _pomodoroState.value.remainingSeconds > 0) {
                delay(1000L)
                val remaining = _pomodoroState.value.remainingSeconds - 1
                if (remaining <= 0) {
                    val nextCount = if (_pomodoroState.value.currentType == FocusSessionType.POMODORO) {
                        _pomodoroState.value.completedCount + 1
                    } else {
                        _pomodoroState.value.completedCount
                    }
                    _pomodoroState.value = _pomodoroState.value.copy(remainingSeconds = 0, isRunning = false, completedCount = nextCount)
                    break
                } else {
                    _pomodoroState.value = _pomodoroState.value.copy(remainingSeconds = remaining)
                }
            }
        }
    }

    fun pausePomodoro() {
        pomodoroJob?.cancel()
        _pomodoroState.value = _pomodoroState.value.copy(isRunning = false)
    }

    fun resetPomodoro() {
        pomodoroJob?.cancel()
        val seconds = _pomodoroState.value.currentType.defaultMinutes * 60
        _pomodoroState.value = _pomodoroState.value.copy(remainingSeconds = seconds, isRunning = false)
    }
}
