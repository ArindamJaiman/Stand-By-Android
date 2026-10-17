package com.standbypro.media

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.standbypro.notifications.StandByNotificationListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Snapshot of whatever is playing on the device (Spotify, YouTube Music, podcasts…). */
data class NowPlaying(
    val hasSession: Boolean = false,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val appName: String = "",
    val art: ImageBitmap? = null,
    val isPlaying: Boolean = false,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    /** elapsedRealtime at which [positionMs] was sampled. */
    val positionSampledAt: Long = 0L,
    val speed: Float = 1f
) {
    fun positionAt(elapsedRealtime: Long): Long {
        if (!isPlaying) return positionMs
        val p = positionMs + ((elapsedRealtime - positionSampledAt) * speed).toLong()
        return if (durationMs > 0) p.coerceIn(0L, durationMs) else p.coerceAtLeast(0L)
    }

    fun progressAt(elapsedRealtime: Long): Float =
        if (durationMs > 0) positionAt(elapsedRealtime).toFloat() / durationMs else 0f
}

/**
 * Real media session tracking via [MediaSessionManager]. Android only exposes active sessions
 * to apps holding notification-listener access, which the Studio asks the user to grant.
 */
object NowPlayingRepository {

    private val _state = MutableStateFlow(NowPlaying())
    val state: StateFlow<NowPlaying> = _state.asStateFlow()

    private val _accessGranted = MutableStateFlow(false)
    val accessGranted: StateFlow<Boolean> = _accessGranted.asStateFlow()

    private var sessionManager: MediaSessionManager? = null
    private var controller: MediaController? = null
    private var appContext: Context? = null
    private val handler = Handler(Looper.getMainLooper())

    private val sessionsListener = MediaSessionManager.OnActiveSessionsChangedListener { controllers ->
        bindTo(pickController(controllers.orEmpty()))
    }

    private val callback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = publish()
        override fun onPlaybackStateChanged(state: PlaybackState?) = publish()
        override fun onSessionDestroyed() {
            bindTo(null)
            sessionManager?.let { manager ->
                appContext?.let { ctx -> runCatching { bindTo(pickController(manager.getActiveSessions(listenerComponent(ctx)))) } }
            }
        }
    }

    fun hasNotificationAccess(context: Context): Boolean {
        val enabled = Settings.Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
        return enabled.contains(context.packageName)
    }

    /** Safe to call repeatedly (e.g. on every resume and when the listener connects). */
    fun start(context: Context) {
        val ctx = context.applicationContext
        appContext = ctx
        val granted = hasNotificationAccess(ctx)
        _accessGranted.value = granted
        if (!granted) return
        try {
            val manager = sessionManager ?: (ctx.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager).also {
                it.addOnActiveSessionsChangedListener(sessionsListener, listenerComponent(ctx), handler)
                sessionManager = it
            }
            bindTo(pickController(manager.getActiveSessions(listenerComponent(ctx))))
        } catch (e: SecurityException) {
            Log.w("NowPlaying", "Notification access not yet effective", e)
            _accessGranted.value = false
        }
    }

    fun playPause() {
        val c = controller ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) c.transportControls.pause() else c.transportControls.play()
    }

    fun next() { controller?.transportControls?.skipToNext() }

    fun previous() { controller?.transportControls?.skipToPrevious() }

    private fun listenerComponent(ctx: Context) = ComponentName(ctx, StandByNotificationListener::class.java)

    private fun pickController(list: List<MediaController>): MediaController? =
        list.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING } ?: list.firstOrNull()

    private fun bindTo(newController: MediaController?) {
        if (newController?.sessionToken == controller?.sessionToken && newController != null) {
            publish()
            return
        }
        controller?.unregisterCallback(callback)
        controller = newController
        newController?.registerCallback(callback, handler)
        publish()
    }

    private fun publish() {
        val c = controller
        if (c == null) {
            _state.value = NowPlaying()
            return
        }
        val meta = c.metadata
        val playback = c.playbackState
        val bitmap: Bitmap? = meta?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
        val previous = _state.value
        val art = if (bitmap != null) {
            if (previous.title == meta?.getString(MediaMetadata.METADATA_KEY_TITLE) && previous.art != null) previous.art else bitmap.asImageBitmap()
        } else null
        _state.value = NowPlaying(
            hasSession = true,
            title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE)
                ?: meta?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE) ?: "",
            artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST)
                ?: meta?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST) ?: "",
            album = meta?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: "",
            appName = appLabel(c.packageName),
            art = art,
            isPlaying = playback?.state == PlaybackState.STATE_PLAYING,
            durationMs = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION)?.coerceAtLeast(0L) ?: 0L,
            positionMs = playback?.position ?: 0L,
            positionSampledAt = playback?.lastPositionUpdateTime?.takeIf { it > 0 } ?: SystemClock.elapsedRealtime(),
            speed = playback?.playbackSpeed?.takeIf { it > 0f } ?: 1f
        )
    }

    private fun appLabel(packageName: String): String {
        val ctx = appContext ?: return packageName
        return try {
            val pm = ctx.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
    }
}
