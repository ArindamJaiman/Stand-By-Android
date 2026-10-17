package com.standbypro.dream

import android.service.dreams.DreamService
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.compositor.AmbientCompositor
import com.standbypro.ui.design.BackdropLayer
import com.standbypro.ui.design.DynamicAccent
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.main.nightFilter
import kotlin.math.roundToInt

/**
 * StandBy as the system screen saver (Settings → Display → Screen saver). Android starts it
 * while the phone is charging or docked; it shows the user's dashboard with the same night
 * rendition and burn-in drift as the full StandBy display. Any touch wakes the phone.
 */
class StandByDreamService : DreamService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false
        isFullscreen = true
        DynamicAccent.refresh(this)

        val viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))[StandByViewModel::class.java]
        val view = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@StandByDreamService)
            setViewTreeViewModelStoreOwner(this@StandByDreamService)
            setViewTreeSavedStateRegistryOwner(this@StandByDreamService)
            setContent {
                val settings by viewModel.settings.collectAsState()
                val ambient by viewModel.ambientDisplayState.collectAsState()
                val ctx by viewModel.faceContext.collectAsState()
                SideEffect { FaceFonts.clock = settings.clockFont.family() }
                // Keep the panel dim at night; Android restores brightness when the dream ends.
                LaunchedEffect(ambient.isNight) { isScreenBright = !ambient.isNight }
                val offset = IntOffset(ambient.burnInOffset.x.roundToInt(), ambient.burnInOffset.y.roundToInt())
                Box(Modifier.fillMaxSize().background(Color.Black).nightFilter(ambient.nightStyle, if (ambient.isNight) 1f else 0f)) {
                    BackdropLayer(ambient.backdrop, ambient.activeThemeColor, animate = !ambient.isNight, modifier = Modifier.fillMaxSize())
                    AmbientCompositor(ambient, ctx, Modifier.fillMaxSize().offset { offset }.padding(horizontal = 22.dp, vertical = 14.dp))
                }
            }
        }
        setContentView(view)
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    override fun onDreamingStopped() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        super.onDreamingStopped()
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
    }
}
