package com.standbypro.media

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class MediaState(
    val isPlaying: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val albumArtUri: String? = null
)

class MediaControllerManager(private val context: Context) {
    // In a real app this would use MediaSessionManager and require NotificationListener access
    
    val mediaState: Flow<MediaState> = flow {
        // Mock state
        emit(MediaState(
            isPlaying = false,
            title = "Nothing playing",
            artist = "No Artist"
        ))
    }
}
