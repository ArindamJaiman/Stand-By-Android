package com.standbypro.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.data.ProductivityRepository
import com.standbypro.domain.FaceContext
import com.standbypro.domain.NotificationItem
import com.standbypro.domain.NotificationPrivacy
import com.standbypro.domain.RainSoon
import com.standbypro.domain.SmartStackBrain
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WidgetRegistry
import com.standbypro.media.NowPlayingRepository
import com.standbypro.power.AutoDimController
import com.standbypro.settings.StandByColorTheme
import com.standbypro.settings.StandByPage
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.compositor.AmbientCompositor
import com.standbypro.ui.design.BackdropLayer
import com.standbypro.ui.design.Ink
import com.standbypro.ui.faces.PhotoClockFace
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StandByScreen(
    viewModel: StandByViewModel,
    onExit: () -> Unit,
    onOpenStudio: () -> Unit = onExit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val ctx by viewModel.faceContext.collectAsState()
    val ambient by viewModel.ambientDisplayState.collectAsState()
    val systemHaptics = LocalHapticFeedback.current
    val haptics = remember(settings.hapticsEnabled, systemHaptics) {
        if (settings.hapticsEnabled) systemHaptics else NoHaptics
    }
    val scope = rememberCoroutineScope()
    val speaker = rememberTalkingClock()

    var controlsVisible by remember { mutableStateOf(true) }
    var pickerVisible by remember { mutableStateOf(false) }
    var lastTouch by remember { mutableIntStateOf(0) }
    var chargingSplash by remember { mutableStateOf(settings.showChargingAnimation && ambient.isCharging) }
    val sound by viewModel.sound.collectAsState()
    val sunrise by viewModel.sunriseProgress.collectAsState()
    var sleepVisible by remember { mutableStateOf(false) }
    var nightLight by remember { mutableStateOf(false) }
    var lightLevel by remember { mutableFloatStateOf(0.6f) }
    var lightWarmth by remember { mutableFloatStateOf(0.25f) }
    var sunriseDismissedFor by remember { mutableStateOf<Long?>(null) }
    var locked by remember { mutableStateOf(false) }
    var peek by remember { mutableStateOf<NotificationItem?>(null) }
    var lastNotificationId by remember { mutableStateOf(ambient.notificationState.notifications.firstOrNull()?.id) }
    var lockHint by remember { mutableIntStateOf(0) }
    val sunriseLevel = if (sunriseDismissedFor != null && sunriseDismissedFor == ctx.nextAlarmMillis) 0f else sunrise

    // Night light and sunrise light take over the window brightness.
    LaunchedEffect(nightLight, lightLevel, sunriseLevel) {
        viewModel.setBrightnessOverride(
            when {
                nightLight -> lightLevel
                sunriseLevel > 0f -> 0.02f + 0.98f * sunriseLevel * sunriseLevel
                else -> null
            }
        )
    }
    // Priority-only Do Not Disturb while the display is in night mode.
    LaunchedEffect(ambient.isNight, settings.autoDnd) {
        viewModel.applyNightDnd(ambient.isNight && settings.autoDnd)
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.setBrightnessOverride(null)
            viewModel.applyNightDnd(false)
        }
    }

    // Auto-hide chrome a few seconds after the last touch.
    LaunchedEffect(controlsVisible, lastTouch) {
        if (controlsVisible) {
            delay(5000L)
            controlsVisible = false
        }
    }
    // Celebrate plugging in while the display is showing.
    LaunchedEffect(Unit) {
        snapshotFlow { ambient.isCharging }.distinctUntilChanged().drop(1).collect { charging ->
            if (charging && settings.showChargingAnimation) chargingSplash = true
        }
    }

    // Face shuffle: rotate through favourites while the display is untouched.
    LaunchedEffect(settings.faceShuffleMinutes, lastTouch) {
        val minutes = settings.faceShuffleMinutes
        if (minutes <= 0) return@LaunchedEffect
        while (true) {
            delay(minutes * 60_000L)
            viewModel.cycleWatchFace()
        }
    }
    // Hourly chime (never during night mode).
    LaunchedEffect(ctx.time.hour) {
        if (settings.hourlyChime && !ambient.isNight && ctx.time.minute == 0 && ctx.time.second < 5) {
            viewModel.hourlyChime()
        }
    }

    val pages = StandByPage.entries
    val pagerState = rememberPagerState(initialPage = settings.startPage.ordinal) { pages.size }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.distinctUntilChanged().drop(1).collect { page ->
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            viewModel.setStartPage(pages[page])
            controlsVisible = true
            lastTouch++
        }
    }

    val nightStrength by animateFloatAsState(if (ambient.isNight) 1f else 0f, tween(1500), label = "night")
    val dimTarget = when {
        ambient.isNight && settings.nightBlackout && !controlsVisible -> 0.82f
        ambient.isDimmed && !controlsVisible -> 0.55f
        else -> 0f
    }
    val dimAlpha by animateFloatAsState(dimTarget, tween(1500), label = "dim")
    val offset = IntOffset(ambient.burnInOffset.x.roundToInt(), ambient.burnInOffset.y.roundToInt())

    fun touched() {
        viewModel.reportInteraction()
        lastTouch++
    }

    // Notification banners: a new notification slides in for a few seconds (never when hidden).
    val topNotification = ambient.notificationState.notifications.firstOrNull()
    LaunchedEffect(topNotification?.id) {
        val top = topNotification ?: return@LaunchedEffect
        if (top.id == lastNotificationId) return@LaunchedEffect
        lastNotificationId = top.id
        if (!settings.notificationBanners || ambient.notificationState.privacyMode == NotificationPrivacy.HIDDEN) return@LaunchedEffect
        peek = top
        delay(6000L)
        peek = null
    }

    // Wave to wake: a hand over the proximity sensor counts as a touch and shows the controls.
    LaunchedEffect(settings.waveToWake) {
        if (!settings.waveToWake) return@LaunchedEffect
        viewModel.waves.collect {
            touched()
            if (locked) lockHint++ else controlsVisible = true
        }
    }

    Box(modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .nightFilter(ambient.nightStyle, nightStrength)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            touched()
                            if (locked) return@detectTapGestures
                            if (pickerVisible || sleepVisible) {
                                pickerVisible = false
                                sleepVisible = false
                            } else controlsVisible = !controlsVisible
                        },
                        onDoubleTap = {
                            touched()
                            if (locked) return@detectTapGestures
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.cycleWatchFace()
                        },
                        onLongPress = {
                            touched()
                            if (locked) return@detectTapGestures
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            pickerVisible = true
                            controlsVisible = false
                        }
                    )
                }
        ) {
            BackdropLayer(ambient.backdrop, ambient.activeThemeColor, animate = !ambient.isNight, modifier = Modifier.fillMaxSize())
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), userScrollEnabled = !locked) { page ->
                Box(
                    Modifier.fillMaxSize().offset { offset }.padding(horizontal = 22.dp, vertical = 14.dp)
                ) {
                    when (pages[page]) {
                        StandByPage.DASHBOARD -> AmbientCompositor(ambient, ctx, Modifier.fillMaxSize())
                        StandByPage.STACKS -> SmartStacks(ctx, settings.leftStack, settings.rightStack, settings.smartRotate, Modifier.fillMaxSize())
                        StandByPage.PHOTOS -> PhotoClockFace(ctx, Modifier.fillMaxSize())
                        StandByPage.CLOCK -> if (ambient.isNight && WatchFaceRegistry.contains(settings.nightFaceId)) {
                            WatchFaceRegistry.Render(settings.nightFaceId, ctx, Modifier.fillMaxSize())
                        } else {
                            FaceCarousel(viewModel, ctx, settings.activeWatchFaceId, Modifier.fillMaxSize())
                        }
                    }
                }
            }

            LiveActivityPill(ambient.liveActivity, ambient.activeThemeColor, Modifier.align(Alignment.TopCenter).padding(top = 10.dp))
            NotificationPeek(peek, ambient.notificationState.privacyMode, ambient.activeThemeColor, Modifier.align(Alignment.TopCenter).padding(top = 10.dp))
            NotificationBadge(ambient.notificationState, Modifier.align(Alignment.BottomStart).padding(start = 22.dp, bottom = 10.dp))

            // Dim overlay (inactivity)
            if (dimAlpha > 0.01f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = dimAlpha)))

            // Page dots
            AnimatedVisibility(controlsVisible, Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp), enter = fadeIn(), exit = fadeOut()) {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                    pages.forEachIndexed { i, _ ->
                        val selected = pagerState.currentPage == i
                        Box(Modifier.height(7.dp).width(if (selected) 20.dp else 7.dp).clip(RoundedCornerShape(50)).background(if (selected) Color.White else Color.White.copy(alpha = 0.3f)))
                    }
                }
            }

            // Top controls
            AnimatedVisibility(
                controlsVisible,
                Modifier.align(Alignment.TopCenter).fillMaxWidth(),
                enter = fadeIn() + slideInVertically { -it / 2 },
                exit = fadeOut() + slideOutVertically { -it / 2 }
            ) {
                ControlsBar(
                    pageLabel = pages[pagerState.currentPage].label,
                    faceName = WatchFaceRegistry.getById(settings.activeWatchFaceId).name,
                    brightness = settings.brightnessLevel,
                    isNight = ambient.isNight,
                    accent = ambient.activeThemeColor,
                    onExit = onExit,
                    onStudio = onOpenStudio,
                    onBrightness = { touched(); viewModel.setBrightnessLevel(it) },
                    onToggleNight = {
                        touched()
                        if (settings.demoModeEnabled) viewModel.setSimulateNight(!settings.simulateNight)
                        else viewModel.setNightModeEnabled(!settings.nightModeEnabled)
                    },
                    onCycleTheme = {
                        touched()
                        val all = StandByColorTheme.entries
                        val idx = all.indexOfFirst { it.id == settings.colorThemeId }
                        viewModel.setColorTheme(all[(idx + 1) % all.size].id)
                    },
                    onFaces = { touched(); pickerVisible = true; controlsVisible = false },
                    onSleep = { touched(); sleepVisible = true; pickerVisible = false; controlsVisible = false },
                    onSpeak = { touched(); speaker.speakText(viewModel.spokenText()) },
                    onLock = {
                        touched()
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        locked = true
                        controlsVisible = false
                        pickerVisible = false
                        sleepVisible = false
                        lockHint++
                    }
                )
            }

            // Long-press favourites picker
            AnimatedVisibility(pickerVisible, Modifier.align(Alignment.BottomCenter), enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it }) {
                FavoritesPicker(
                    ctx = ctx,
                    activeId = settings.activeWatchFaceId,
                    favorites = viewModel.favoriteFaces(settings),
                    onPick = { id ->
                        touched()
                        viewModel.setActiveWatchFaceId(id)
                        scope.launch {
                            delay(250L)
                            pickerVisible = false
                        }
                    }
                )
            }

            // Bedtime sheet: sounds, sleep timer, night light
            AnimatedVisibility(sleepVisible, Modifier.align(Alignment.BottomCenter), enter = fadeIn() + slideInVertically { it }, exit = fadeOut() + slideOutVertically { it }) {
                SleepPanel(
                    ctx = ctx,
                    sound = sound,
                    sunriseMinutes = settings.sunriseMinutes,
                    nowMillis = System.currentTimeMillis(),
                    onSound = { touched(); viewModel.playSound(it) },
                    onVolume = { touched(); viewModel.setSoundVolume(it) },
                    onSleepTimer = { touched(); viewModel.startSleepTimer(it) },
                    onNightLight = { touched(); sleepVisible = false; nightLight = true },
                    onClose = { touched(); sleepVisible = false }
                )
            }

            // Display lock: swallows every touch until a long press.
            if (locked) {
                LockOverlay(lockHint, ambient.activeThemeColor, onTap = { touched(); lockHint++ }) {
                    touched()
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    locked = false
                    controlsVisible = true
                }
            }

            ChargingOverlay(chargingSplash, ctx.battery) { chargingSplash = false }
        }

        // Outside the night filter so their colours stay true.
        SunriseOverlay(sunriseLevel, ctx) { sunriseDismissedFor = ctx.nextAlarmMillis }
        if (nightLight) {
            NightLightOverlay(lightLevel, lightWarmth, onChange = { l, w -> lightLevel = l; lightWarmth = w }) {
                touched()
                nightLight = false
            }
        }
    }
}

/** Two independent vertical smart stacks that surface the right widget for the moment, like iOS. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SmartStacks(ctx: FaceContext, left: List<String>, right: List<String>, smartRotate: Boolean, modifier: Modifier) {
    val media by NowPlayingRepository.state.collectAsState()
    val timer by ProductivityRepository.timerState.collectAsState()
    val pomodoro by ProductivityRepository.pomodoroState.collectAsState()
    val stopwatch by ProductivityRepository.stopwatchState.collectAsState()
    val now = System.currentTimeMillis()
    val signals = SmartStackBrain.Signals(
        hour = ctx.time.hour,
        musicPlaying = media.hasSession && media.isPlaying,
        timerRunning = timer.isRunning,
        pomodoroRunning = pomodoro.isRunning,
        stopwatchRunning = stopwatch.isRunning,
        minutesToNextEvent = ctx.events.filter { !it.isAllDay && it.startMillis > now }.minOfOrNull { ((it.startMillis - now) / 60_000L).toInt() },
        rainSoon = RainSoon.outlook(ctx.weather) != null,
        isNight = ctx.isNight,
        charging = ctx.battery.isCharging,
        batteryPercent = ctx.battery.batteryPercent
    )
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        SmartStack(left.filter { WidgetRegistry.contains(it) }.ifEmpty { listOf("widget_clock_analog") }, ctx, smartRotate, signals, 0, Modifier.weight(1f).fillMaxHeight())
        SmartStack(right.filter { WidgetRegistry.contains(it) }.ifEmpty { listOf("widget_calendar") }, ctx, smartRotate, signals, 1, Modifier.weight(1f).fillMaxHeight())
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SmartStack(ids: List<String>, ctx: FaceContext, smartRotate: Boolean, signals: SmartStackBrain.Signals, salt: Int, modifier: Modifier) {
    val state = rememberPagerState { ids.size }
    val interaction by AutoDimController.interactions.collectAsState()
    val suggestion = if (smartRotate) SmartStackBrain.suggest(ids, signals) else null
    // Turn to the suggested widget when the situation changes (music starts, a meeting nears…).
    LaunchedEffect(suggestion) {
        if (suggestion != null && suggestion != state.currentPage) state.animateScrollToPage(suggestion)
    }
    LaunchedEffect(ids, smartRotate, interaction, suggestion) {
        if (!smartRotate || ids.size < 2 || suggestion != null) return@LaunchedEffect
        delay(3 * 60 * 1000L + salt * 45_000L)
        while (true) {
            state.animateScrollToPage((state.currentPage + 1) % ids.size)
            delay(3 * 60 * 1000L)
        }
    }
    Box(modifier) {
        VerticalPager(state = state, modifier = Modifier.fillMaxSize(), pageSpacing = 12.dp) { page ->
            WidgetRegistry.Render(ids[page], ctx.copy(isCompact = true), Modifier.fillMaxSize())
        }
        if (ids.size > 1) {
            Column(Modifier.align(Alignment.CenterEnd).padding(end = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ids.indices.forEach { i ->
                    Box(Modifier.size(width = 4.dp, height = if (i == state.currentPage) 14.dp else 4.dp).clip(RoundedCornerShape(50)).background(if (i == state.currentPage) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.25f)))
                }
            }
        }
    }
}

/** Full-screen faces; swipe up/down through favourites. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun FaceCarousel(viewModel: StandByViewModel, ctx: FaceContext, activeId: String, modifier: Modifier) {
    val settings by viewModel.settings.collectAsState()
    val faces = remember(settings.favoriteFaceIds) { viewModel.favoriteFaces(settings) }
    val initial = faces.indexOf(activeId).coerceAtLeast(0)
    val state = rememberPagerState(initialPage = initial) { faces.size }
    LaunchedEffect(activeId, faces) {
        val idx = faces.indexOf(activeId)
        if (idx >= 0 && idx != state.currentPage) state.scrollToPage(idx)
    }
    LaunchedEffect(state, faces) {
        snapshotFlow { state.settledPage }.distinctUntilChanged().drop(1).collect { p ->
            faces.getOrNull(p)?.let { if (it != viewModel.settings.value.activeWatchFaceId) viewModel.setActiveWatchFaceId(it) }
        }
    }
    VerticalPager(state = state, modifier = modifier, beyondViewportPageCount = 0) { page ->
        WatchFaceRegistry.Render(faces[page], ctx, Modifier.fillMaxSize())
    }
}

@Composable
private fun ControlsBar(
    pageLabel: String,
    faceName: String,
    brightness: Float,
    isNight: Boolean,
    accent: Color,
    onExit: () -> Unit,
    onStudio: () -> Unit,
    onBrightness: (Float) -> Unit,
    onToggleNight: () -> Unit,
    onCycleTheme: () -> Unit,
    onFaces: () -> Unit,
    onSleep: () -> Unit,
    onSpeak: () -> Unit,
    onLock: () -> Unit
) {
    var local by remember(brightness) { mutableFloatStateOf(brightness) }
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RoundChip(Icons.AutoMirrored.Filled.ArrowBack, "Exit StandBy", onExit)
        Column(Modifier.weight(1f)) {
            Text(pageLabel.uppercase(), color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Text(faceName, color = Ink.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        }
        Row(
            Modifier.width(190.dp).clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.10f)).padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.BrightnessLow, null, tint = Ink.TextSecondary, modifier = Modifier.size(16.dp))
            Slider(
                value = local,
                onValueChange = { local = it; onBrightness(it) },
                valueRange = 0.01f..1f,
                modifier = Modifier.weight(1f).height(32.dp),
                colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = accent, inactiveTrackColor = Color.White.copy(alpha = 0.2f))
            )
            Icon(Icons.Default.BrightnessHigh, null, tint = Ink.TextSecondary, modifier = Modifier.size(16.dp))
        }
        RoundChip(if (isNight) Icons.Default.LightMode else Icons.Default.DarkMode, "Night mode", onToggleNight, active = isNight, accent = Ink.Red)
        RoundChip(Icons.Default.Bedtime, "Sleep sounds and night light", onSleep)
        RoundChip(Icons.Default.RecordVoiceOver, "Speak the time", onSpeak)
        RoundChip(Icons.Default.Lock, "Lock the display", onLock)
        RoundChip(Icons.Default.Palette, "Accent colour", onCycleTheme, accent = accent)
        RoundChip(Icons.Default.Star, "Favourite faces", onFaces)
        RoundChip(Icons.Default.Tune, "Open Studio", onStudio)
    }
}

@Composable
private fun RoundChip(icon: androidx.compose.ui.graphics.vector.ImageVector, description: String, onClick: () -> Unit, active: Boolean = false, accent: Color = Color.White) {
    Box(
        Modifier.size(40.dp).clip(CircleShape)
            .background(if (active) accent.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.10f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = if (active) accent else Ink.TextPrimary, modifier = Modifier.size(20.dp))
    }
}

/** Bottom sheet of live favourite faces (static previews). */
@Composable
private fun FavoritesPicker(ctx: FaceContext, activeId: String, favorites: List<String>, onPick: (String) -> Unit) {
    val preview = ctx.copy(isPreview = true, isCompact = true)
    Column(
        Modifier.fillMaxWidth().background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f)))).padding(top = 30.dp, bottom = 16.dp)
    ) {
        Text("FAVOURITES  ·  double-tap the display to cycle", color = Ink.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp, modifier = Modifier.padding(start = 24.dp, bottom = 8.dp))
        LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(favorites, key = { _, id -> id }) { _, id ->
                val selected = id == activeId
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        Modifier.size(width = 150.dp, height = 110.dp).clip(RoundedCornerShape(20.dp)).background(Ink.Graphite)
                            .border(if (selected) 2.dp else 1.dp, if (selected) ctx.accent else Ink.Hairline, RoundedCornerShape(20.dp))
                            .clickable { onPick(id) }
                    ) {
                        WatchFaceRegistry.Render(id, preview, Modifier.fillMaxSize())
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(WatchFaceRegistry.getById(id).name, color = if (selected) ctx.accent else Ink.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}

/** Haptics sink used when the user turns haptic feedback off. */
private object NoHaptics : androidx.compose.ui.hapticfeedback.HapticFeedback {
    override fun performHapticFeedback(hapticFeedbackType: HapticFeedbackType) = Unit
}
