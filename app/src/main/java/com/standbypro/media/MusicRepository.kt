package com.standbypro.media

import com.standbypro.data.DemoDataProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class MusicPlayerState(
    val isPlaying: Boolean = true,
    val title: String = "Midnight City",
    val artist: String = "M83",
    val album: String = "Hurry Up, We're Dreaming",
    val currentPositionSeconds: Int = 102,
    val durationSeconds: Int = 243,
    val currentLyric: String = "♪ City is my church, it wraps in blinding twilight ♪"
) {
    val progress: Float
        get() = if (durationSeconds > 0) currentPositionSeconds.toFloat() / durationSeconds.toFloat() else 0f

    val formattedPosition: String
        get() = "%02d:%02d".format(currentPositionSeconds / 60, currentPositionSeconds % 60)

    val formattedDuration: String
        get() = "%02d:%02d".format(durationSeconds / 60, durationSeconds % 60)
}

object MusicRepository {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val _playerState = MutableStateFlow(MusicPlayerState())
    val playerState: StateFlow<MusicPlayerState> = _playerState.asStateFlow()

    private var playbackJob: Job? = null

    init {
        startPlaybackProgress()
    }

    private fun startPlaybackProgress() {
        playbackJob?.cancel()
        playbackJob = scope.launch {
            while (isActive) {
                delay(1000L)
                if (_playerState.value.isPlaying) {
                    val nextSec = (_playerState.value.currentPositionSeconds + 1) % _playerState.value.durationSeconds
                    val lyrics = DemoDataProvider.demoLyrics
                    val lyricIndex = (nextSec / 10) % lyrics.size
                    _playerState.value = _playerState.value.copy(
                        currentPositionSeconds = nextSec,
                        currentLyric = "♪ " + lyrics[lyricIndex] + " ♪"
                    )
                }
            }
        }
    }

    fun togglePlayPause() {
        val next = !_playerState.value.isPlaying
        _playerState.value = _playerState.value.copy(isPlaying = next)
    }

    fun nextTrack() {
        _playerState.value = _playerState.value.copy(
            title = "Starboy",
            artist = "The Weeknd ft. Daft Punk",
            album = "Starboy",
            currentPositionSeconds = 0,
            durationSeconds = 230,
            currentLyric = "♪ I'm tryna put you in the worst mood, ah ♪"
        )
    }

    fun previousTrack() {
        _playerState.value = _playerState.value.copy(
            title = "Midnight City",
            artist = "M83",
            album = "Hurry Up, We're Dreaming",
            currentPositionSeconds = 0,
            durationSeconds = 243,
            currentLyric = "♪ City is my church, it wraps in blinding twilight ♪"
        )
    }
}
