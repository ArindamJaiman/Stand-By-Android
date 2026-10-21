package com.standbypro.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.standbypro.astro.Astronomy
import com.standbypro.audio.SleepSound
import com.standbypro.audio.SoundMachine
import com.standbypro.audio.SoundState
import com.standbypro.clock.TimeProvider
import com.standbypro.data.AirQualityRepository
import com.standbypro.data.AlarmRepository
import com.standbypro.data.CalendarRepository
import com.standbypro.data.ChargeHistoryRepository
import com.standbypro.data.CityResult
import com.standbypro.data.DemoDataProvider
import com.standbypro.data.GitHubContributionsState
import com.standbypro.data.GitHubRepository
import com.standbypro.data.Locations
import com.standbypro.data.NextAlarm
import com.standbypro.data.Place
import com.standbypro.data.ProductivityRepository
import com.standbypro.data.TasksRepository
import com.standbypro.data.WeatherRepository
import com.standbypro.domain.AirQuality
import com.standbypro.domain.BatteryHealth
import com.standbypro.domain.Briefing
import com.standbypro.domain.CalendarEventItem
import com.standbypro.domain.ChargeType
import com.standbypro.domain.ChargingState
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.FaceContext
import com.standbypro.domain.LiveActivityState
import com.standbypro.domain.NotificationDisplayState
import com.standbypro.domain.NotificationPrivacy
import com.standbypro.domain.RainSoon
import com.standbypro.domain.UpNext
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WeatherData
import com.standbypro.domain.WidgetRegistry
import com.standbypro.hardware.AmbientLightMonitor
import com.standbypro.hardware.ChargingStateMonitor
import com.standbypro.hardware.ProximityMonitor
import com.standbypro.media.NowPlayingRepository
import com.standbypro.notifications.NotificationRepository
import com.standbypro.power.AutoDimController
import com.standbypro.power.BurnInOffset
import com.standbypro.power.BurnInProtectionController
import com.standbypro.power.DndController
import com.standbypro.settings.NightTrigger
import com.standbypro.settings.SettingsRepository
import com.standbypro.settings.StandByPage
import com.standbypro.settings.StandBySettings
import com.standbypro.settings.dataStore
import com.standbypro.ui.compositor.AmbientDisplayState
import com.standbypro.ui.design.Backdrop
import com.standbypro.ui.design.ClockFont
import com.standbypro.ui.design.SecondsMotion
import com.standbypro.ui.design.ThemePreset
import com.standbypro.ui.faces.FaceMath
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class StandByViewModel(application: Application) : AndroidViewModel(application) {

    private val app: Application = application
    private val settingsRepository = SettingsRepository(application.dataStore)
    private val chargingMonitor = ChargingStateMonitor(application)
    private val ambientLightMonitor = AmbientLightMonitor(application)
    private val proximityMonitor = ProximityMonitor(application)

    val settings: StateFlow<StandBySettings> = settingsRepository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, StandBySettings())

    val currentTime: StateFlow<LocalDateTime> = TimeProvider.timeFlow(1000L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocalDateTime.now())

    private val rawCharging: StateFlow<ChargingState> = chargingMonitor.chargingState
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            ChargingState(isCharging = false, batteryPercent = 100, chargeType = ChargeType.NONE, temperatureCelsius = null, health = BatteryHealth.GOOD)
        )

    val chargingState: StateFlow<ChargingState> = combine(settings, rawCharging) { s, c ->
        if (s.demoModeEnabled && s.simulateCharging && !c.isCharging) c.copy(isCharging = true, chargeType = ChargeType.AC) else c
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChargingState(batteryPercent = 100))

    /** Light sensor is only registered while the night trigger actually needs it. */
    private val sensorNight = settings
        .map { it.nightModeEnabled && it.nightTrigger == NightTrigger.LIGHT_SENSOR && !it.demoModeEnabled }
        .distinctUntilChanged()
        .flatMapLatest { needed -> if (needed) ambientLightMonitor.isNightMode else flowOf(false) }

    /** Brightness from the light sensor when adaptive brightness is on; null otherwise. */
    val adaptiveBrightness: StateFlow<Float?> = settings
        .map { it.autoBrightness }
        .distinctUntilChanged()
        .flatMapLatest { on ->
            if (!on) flowOf(null)
            else combine(ambientLightMonitor.lux, settings.map { it.brightnessLevel }.distinctUntilChanged()) { lux, level ->
                AmbientLightMonitor.adaptiveBrightness(lux, level)
            }
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** One emission per hand wave over the proximity sensor (collect only when the setting is on). */
    val waves: Flow<Unit> = proximityMonitor.waves

    /** What the speak button says: the time, or the full daily briefing. */
    fun spokenText(): String {
        val ctx = faceContext.value
        return if (settings.value.spokenBriefing) Briefing.compose(ctx, System.currentTimeMillis())
        else "It's ${FaceMath.spokenTime(ctx.time.hour, ctx.time.minute)}"
    }

    private val minuteOfDay = currentTime.map { it.hour * 60 + it.minute }.distinctUntilChanged()

    private val _bedsideSession = kotlinx.coroutines.flow.MutableStateFlow(false)
    /** True while a "Bedside" session (launcher shortcut) forces the night rendition. */
    val bedsideSession: StateFlow<Boolean> = _bedsideSession

    fun setBedsideSession(active: Boolean) {
        _bedsideSession.value = active
    }

    val isNightMode: StateFlow<Boolean> = combine(settings, sensorNight, minuteOfDay, _bedsideSession) { s, dark, minute, bedside ->
        when {
            bedside -> true
            s.demoModeEnabled -> s.simulateNight
            !s.nightModeEnabled -> false
            s.nightTrigger == NightTrigger.ALWAYS -> true
            s.nightTrigger == NightTrigger.SCHEDULE -> inWindow(minute, s.nightStartMinutes, s.nightEndMinutes)
            s.nightTrigger == NightTrigger.SUN -> resolvePlace(s).let { isSunDown(System.currentTimeMillis(), it.latitude, it.longitude) }
            else -> dark
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val isDimmed: StateFlow<Boolean> = settings
        .map { if (it.autoDimEnabled && it.autoDimTimeoutSeconds > 0) it.autoDimTimeoutSeconds * 1000L else 0L }
        .distinctUntilChanged()
        .flatMapLatest { timeout -> if (timeout > 0) AutoDimController.dimState(timeout) else flowOf(false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val burnInOffset: StateFlow<BurnInOffset> = BurnInProtectionController.burnInOffset
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BurnInOffset(0f, 0f))

    val gitHubContributionsState: StateFlow<GitHubContributionsState> = GitHubRepository.contributionsState

    val notifications: StateFlow<NotificationDisplayState> = combine(settings, NotificationRepository.notifications) { s, n ->
        if (s.demoModeEnabled && n.notifications.isEmpty()) DemoDataProvider.demoNotifications.copy(privacyMode = s.notificationPrivacy)
        else n.copy(privacyMode = s.notificationPrivacy)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NotificationDisplayState())

    val weather: StateFlow<WeatherData?> = combine(settings, WeatherRepository.weather) { s, w ->
        if (s.demoModeEnabled && w == null) DemoDataProvider.demoWeather else w
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val events: StateFlow<List<CalendarEventItem>> = combine(settings, CalendarRepository.events) { s, e ->
        if (s.demoModeEnabled && e.isEmpty()) DemoDataProvider.demoCalendarEvents else e
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val nextAlarm: StateFlow<NextAlarm?> = AlarmRepository.nextAlarm

    val airQuality: StateFlow<AirQuality?> = combine(settings, AirQualityRepository.airQuality) { s, a ->
        if (s.demoModeEnabled && a == null) DemoDataProvider.demoAirQuality else a
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val sound: StateFlow<SoundState> = SoundMachine.state

    /** 0..1 while the sunrise wake light is ramping up before the next alarm. */
    val sunriseProgress: StateFlow<Float> = combine(settings, currentTime, nextAlarm) { s, _, alarm ->
        sunriseProgress(System.currentTimeMillis(), alarm?.triggerAtMillis, s.sunriseMinutes)
    }.distinctUntilChanged().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    private val _brightnessOverride = kotlinx.coroutines.flow.MutableStateFlow<Float?>(null)
    /** Window brightness forced by the night light or sunrise light; null = normal rules. */
    val brightnessOverride: StateFlow<Float?> = _brightnessOverride

    fun setBrightnessOverride(level: Float?) {
        _brightnessOverride.value = level
    }

    val calendarPermission: StateFlow<Boolean> = CalendarRepository.hasPermission
    val notificationAccess: StateFlow<Boolean> = NowPlayingRepository.accessGranted
    val weatherRefreshing: StateFlow<Boolean> = WeatherRepository.isRefreshing

    private val productivityActivity = combine(
        ProductivityRepository.timerState,
        ProductivityRepository.pomodoroState,
        ProductivityRepository.stopwatchState
    ) { timer, pomodoro, stopwatch ->
        when {
            timer.isRunning -> LiveActivityState.TimerActivity(subtitle = "${timer.formattedRemaining} remaining", progress = timer.progress, status = "Running")
            pomodoro.isRunning -> LiveActivityState.TimerActivity(
                id = "pomodoro_active",
                title = pomodoro.currentType.displayName,
                subtitle = "${pomodoro.formattedRemaining} left",
                progress = pomodoro.progress,
                status = "Focus"
            )
            stopwatch.isRunning -> LiveActivityState.TimerActivity(
                id = "stopwatch_active",
                title = "Stopwatch",
                subtitle = stopwatch.formattedElapsed.substringBeforeLast('.'),
                progress = null,
                status = "Running"
            )
            else -> null
        }
    }

    private val eventActivity = combine(events, currentTime.map { it.withSecond(0).withNano(0) }.distinctUntilChanged()) { ev, _ ->
        val now = System.currentTimeMillis()
        UpNext.eventFor(ev, now)?.let { e ->
            LiveActivityState.EventActivity(title = e.title, subtitle = UpNext.startsIn(e, now), colorArgb = e.colorArgb)
        }
    }

    private val rainActivity = weather.map { w -> RainSoon.outlook(w)?.let { LiveActivityState.WeatherActivity(subtitle = it) } }.distinctUntilChanged()

    /** Timers first, then a meeting that's about to start, then media, then a rain heads-up. */
    private val liveActivity: StateFlow<LiveActivityState?> = combine(productivityActivity, eventActivity, NowPlayingRepository.state, rainActivity) { work, event, media, rain ->
        work ?: event ?: if (media.hasSession && media.isPlaying) {
            LiveActivityState.MusicActivity(
                title = media.title.ifBlank { "Now Playing" },
                subtitle = listOf(media.artist, media.appName).filter { it.isNotBlank() }.joinToString(" • "),
                progress = null
            )
        } else rain
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Single source of truth for every face and widget. */
    val faceContext: StateFlow<FaceContext> = combine(
        combine(settings, currentTime, chargingState, weather, isNightMode) { s, t, c, w, n -> Base(s, t, c, w, n) },
        events,
        nextAlarm,
        airQuality
    ) { base, ev, alarm, air ->
        val s = base.settings
        val place = resolvePlace(s)
        FaceContext(
            time = base.time,
            accent = s.activeColorTheme.color,
            use24Hour = s.use24Hour,
            showSeconds = s.showSeconds && s.secondsMotion != SecondsMotion.OFF,
            secondsMotion = s.secondsMotion,
            battery = base.charging,
            weather = base.weather,
            airQuality = air,
            useFahrenheit = s.useFahrenheit,
            nextAlarm = alarm?.label,
            nextAlarmTime = alarm?.timeLabel,
            nextAlarmMillis = alarm?.triggerAtMillis,
            events = ev,
            worldClocks = s.worldCities.mapNotNull { Locations.byCity(it)?.toClock() }
                .ifEmpty { Locations.defaultWorldCities.mapNotNull { Locations.byCity(it)?.toClock() } },
            secondaryZoneId = Locations.byCity(s.secondaryCity)?.zoneId ?: "Europe/London",
            photos = s.photoUris,
            photoIntervalSeconds = s.photoIntervalSeconds,
            notificationPrivacy = s.notificationPrivacy,
            latitude = place.latitude,
            longitude = place.longitude,
            locationName = place.city,
            countdownLabel = s.countdownLabel,
            countdownDate = s.countdownEpochDay?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.of(base.time.year + 1, 1, 1),
            isNight = base.night
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FaceContext())

    val ambientDisplayState: StateFlow<AmbientDisplayState> = combine(
        combine(settings, chargingState, isNightMode, isDimmed, burnInOffset) { s, c, n, d, b -> Chrome(s, c, n, d, b) },
        notifications,
        liveActivity
    ) { chrome, notif, live ->
        val s = chrome.settings
        AmbientDisplayState(
            isCharging = chrome.charging.isCharging,
            isNight = chrome.night,
            nightStyle = s.nightStyle,
            brightness = s.brightnessLevel,
            watchFaceId = if (chrome.night && WatchFaceRegistry.contains(s.nightFaceId)) s.nightFaceId else s.activeWatchFaceId,
            layout = s.layoutType,
            widgets = s.assignedWidgets,
            liveActivity = live,
            notificationState = notif,
            burnInOffset = if (s.burnInProtectionEnabled) Offset(chrome.burnIn.x, chrome.burnIn.y) else Offset.Zero,
            activeThemeColor = s.activeColorTheme.color,
            isDimmed = chrome.dimmed,
            backdrop = s.backdrop
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AmbientDisplayState())

    private data class Base(val settings: StandBySettings, val time: LocalDateTime, val charging: ChargingState, val weather: WeatherData?, val night: Boolean)
    private data class Chrome(val settings: StandBySettings, val charging: ChargingState, val night: Boolean, val dimmed: Boolean, val burnIn: BurnInOffset)

    init {
        TasksRepository.init(application)
        ChargeHistoryRepository.load(application)
        WeatherRepository.loadCache(application)

        viewModelScope.launch {
            settings.map { it.githubUsername }.distinctUntilChanged().collect { GitHubRepository.fetchContributions(it) }
        }
        // Weather: refresh on location change and every 30 minutes.
        viewModelScope.launch {
            settings.map { resolvePlace(it) }.distinctUntilChanged().collect { place ->
                WeatherRepository.refresh(app, place.city, place.latitude, place.longitude, force = true)
                AirQualityRepository.refresh(place.latitude, place.longitude, force = true)
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(30 * 60 * 1000L)
                val place = resolvePlace(settings.value)
                WeatherRepository.refresh(app, place.city, place.latitude, place.longitude)
                AirQualityRepository.refresh(place.latitude, place.longitude)
            }
        }
        // Calendar + alarm: refresh every few minutes and whenever 12/24h changes.
        viewModelScope.launch {
            settings.map { it.use24Hour }.distinctUntilChanged().collect { refreshDeviceData() }
        }
        viewModelScope.launch {
            while (true) {
                delay(5 * 60 * 1000L)
                refreshDeviceData()
            }
        }
        viewModelScope.launch {
            ProductivityRepository.finished.collect { chime() }
        }
    }

    private val _resumeTick = kotlinx.coroutines.flow.MutableStateFlow(0)
    /** Increments on every activity resume so permission status cards can re-check. */
    val resumeTick: StateFlow<Int> = _resumeTick

    /** Called from the activity on every resume (permissions may have changed). */
    fun onResume() {
        _resumeTick.value = _resumeTick.value + 1
        refreshDeviceData()
        NowPlayingRepository.start(app)
    }

    private fun refreshDeviceData() {
        viewModelScope.launch {
            val use24 = settings.value.use24Hour
            AlarmRepository.refresh(app, use24)
            CalendarRepository.refresh(app, use24)
        }
    }

    fun refreshWeatherNow() {
        viewModelScope.launch {
            val place = resolvePlace(settings.value)
            WeatherRepository.refresh(app, place.city, place.latitude, place.longitude, force = true)
            AirQualityRepository.refresh(place.latitude, place.longitude, force = true)
        }
    }

    suspend fun searchCities(query: String): List<CityResult> = WeatherRepository.searchCities(query)

    fun resolvePlace(s: StandBySettings): Place {
        val lat = s.weatherLatitude
        val lon = s.weatherLongitude
        return if (lat != null && lon != null && s.weatherCity.isNotBlank()) {
            Place(s.weatherCity, "", java.time.ZoneId.systemDefault().id, lat, lon)
        } else {
            Locations.defaultPlace()
        }
    }

    private fun chime() {
        try {
            val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(app, uri)
            ringtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone?.play()
            viewModelScope.launch {
                delay(6000L)
                ringtone?.stop()
            }
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 400, 200, 400, 200, 600), -1))
        } catch (e: Exception) {
            // Sound is best-effort.
        }
    }

    fun reportInteraction() = AutoDimController.reportInteraction()

    // ---------------- Face & layout ----------------

    fun setActiveWatchFaceId(faceId: String) = launch { settingsRepository.setActiveWatchFaceId(faceId) }

    /** Cycles through favourites (or all faces when there are fewer than two favourites). */
    fun cycleWatchFace(direction: Int = 1) = launch {
        val s = settings.value
        val pool = favoriteFaces(s).ifEmpty { WatchFaceRegistry.getAll().map { it.id } }
        val index = pool.indexOf(s.activeWatchFaceId)
        val next = pool[Math.floorMod(index + direction, pool.size)]
        settingsRepository.setActiveWatchFaceId(next)
    }

    fun favoriteFaces(s: StandBySettings = settings.value): List<String> {
        val favs = WatchFaceRegistry.getAll().map { it.id }.filter { it in s.favoriteFaceIds }
        return if (favs.size >= 2) favs else WatchFaceRegistry.getAll().map { it.id }
    }

    fun cycleRightWidget() = launch {
        val all = WidgetRegistry.getAll()
        val current = settings.value.assignedWidgets.firstOrNull()?.widgetId ?: "widget_calendar"
        val next = all[Math.floorMod(all.indexOfFirst { it.id == current } + 1, all.size)]
        settingsRepository.setAssignedWidget(0, next.id)
    }

    fun setLayoutType(layout: DashboardLayoutType) = launch { settingsRepository.setLayoutType(layout) }
    fun setAssignedWidget(slotIndex: Int, widgetId: String) = launch { settingsRepository.setAssignedWidget(slotIndex, widgetId) }
    fun setStack(left: Boolean, ids: List<String>) = launch { settingsRepository.setStack(left, ids) }

    fun toggleStackWidget(left: Boolean, widgetId: String) = launch {
        val s = settings.value
        val current = if (left) s.leftStack else s.rightStack
        val updated = if (widgetId in current) current - widgetId else current + widgetId
        settingsRepository.setStack(left, updated.ifEmpty { listOf(widgetId) })
    }

    fun setSmartRotate(v: Boolean) = launch { settingsRepository.setSmartRotate(v) }
    fun setStartPage(v: StandByPage) = launch { settingsRepository.setStartPage(v) }
    fun toggleFavoriteFace(faceId: String) = launch { settingsRepository.toggleFavoriteFace(faceId) }

    // ---------------- Display ----------------

    fun setColorTheme(id: String) = launch { settingsRepository.setColorTheme(id) }
    fun setClockFont(v: ClockFont) = launch { settingsRepository.setClockFont(v) }
    fun setBackdrop(v: Backdrop) = launch { settingsRepository.setBackdrop(v) }
    fun setNightFace(id: String) = launch { settingsRepository.setNightFace(id) }
    fun applyTheme(preset: ThemePreset) = launch { settingsRepository.applyTheme(preset) }
    fun setBrightnessLevel(v: Float) = launch { settingsRepository.setBrightnessLevel(v) }
    fun setUse24Hour(v: Boolean) = launch { settingsRepository.setUse24Hour(v) }
    fun setAutoBrightness(v: Boolean) = launch { settingsRepository.setAutoBrightness(v) }
    fun setWaveToWake(v: Boolean) = launch { settingsRepository.setWaveToWake(v) }
    fun setSpokenBriefing(v: Boolean) = launch { settingsRepository.setSpokenBriefing(v) }
    fun setShowSeconds(v: Boolean) = launch { settingsRepository.setShowSeconds(v) }
    fun setSecondsMotion(v: SecondsMotion) = launch { settingsRepository.setSecondsMotion(v) }
    fun setNightModeEnabled(v: Boolean) = launch { settingsRepository.setNightModeEnabled(v) }
    fun setNightTrigger(v: NightTrigger) = launch { settingsRepository.setNightTrigger(v) }
    fun setNightSchedule(start: Int, end: Int) = launch { settingsRepository.setNightSchedule(start, end) }
    fun setNightModeStyle(style: String) = launch { settingsRepository.setNightModeStyle(style) }
    fun setBurnInProtectionEnabled(v: Boolean) = launch { settingsRepository.setBurnInProtectionEnabled(v) }
    fun setAutoDimEnabled(v: Boolean) = launch { settingsRepository.setAutoDimEnabled(v) }
    fun setAutoDimTimeout(seconds: Int) = launch { settingsRepository.setAutoDimTimeout(seconds) }
    fun setShowChargingAnimation(v: Boolean) = launch { settingsRepository.setShowChargingAnimation(v) }
    fun setHaptics(v: Boolean) = launch { settingsRepository.setHaptics(v) }
    fun setFaceShuffle(minutes: Int) = launch { settingsRepository.setFaceShuffle(minutes) }
    fun setHourlyChime(v: Boolean) = launch { settingsRepository.setHourlyChime(v) }
    fun setNightBlackout(v: Boolean) = launch { settingsRepository.setNightBlackout(v) }

    /** Soft single chime for the top of the hour. */
    fun hourlyChime() {
        try {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) ?: return
            RingtoneManager.getRingtone(app, uri)?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                play()
            }
        } catch (e: Exception) {
            // Best effort.
        }
    }

    // ---------------- Bedtime ----------------

    fun playSound(sound: SleepSound?) = if (sound == null) SoundMachine.stop() else SoundMachine.play(sound)
    fun setSoundVolume(v: Float) = SoundMachine.setVolume(v)
    fun startSleepTimer(minutes: Int) = SoundMachine.startSleepTimer(minutes)
    fun setSunriseMinutes(v: Int) = launch { settingsRepository.setSunriseMinutes(v) }
    fun setAutoDnd(v: Boolean) = launch { settingsRepository.setAutoDnd(v) }
    fun setChargeLimit(v: Int) = launch { settingsRepository.setChargeLimit(v) }
    fun isDndGranted(): Boolean = DndController.isGranted(app)

    /** Priority-only Do Not Disturb while the StandBy display is in night mode. */
    fun applyNightDnd(quiet: Boolean) = DndController.setQuiet(app, quiet)

    /** Leaving StandBy: silence the sound machine and hand Do Not Disturb back. */
    fun endStandBySession() {
        SoundMachine.stop()
        SoundMachine.cancelSleepTimer()
        DndController.setQuiet(app, false)
        _brightnessOverride.value = null
    }

    override fun onCleared() {
        endStandBySession()
        super.onCleared()
    }

    // ---------------- Backup ----------------

    private val _backupStatus = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    /** Last export/import result, shown under the Backup section. */
    val backupStatus: StateFlow<String?> = _backupStatus

    fun exportSettings(uri: Uri) = launch {
        _backupStatus.value = try {
            val json = settingsRepository.exportJson()
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                app.contentResolver.openOutputStream(uri, "wt")?.use { it.write(json.toByteArray()) } ?: error("Couldn't open the file")
            }
            "Settings saved"
        } catch (e: Exception) {
            "Export failed: ${e.message}"
        }
    }

    fun importSettings(uri: Uri) = launch {
        _backupStatus.value = try {
            val json = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                app.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() } ?: error("Couldn't open the file")
            }
            settingsRepository.importJson(json)
            "Settings restored"
        } catch (e: Exception) {
            "Import failed: ${e.message}"
        }
    }

    // ---------------- Behaviour ----------------

    fun setEnabled(v: Boolean) = launch { settingsRepository.setEnabled(v) }
    fun setAutoStart(v: Boolean) = launch { settingsRepository.setAutoStart(v) }
    fun setRequireLandscape(v: Boolean) = launch { settingsRepository.setRequireLandscape(v) }
    fun setRequireScreenLocked(v: Boolean) = launch { settingsRepository.setRequireScreenLocked(v) }
    fun setDemoModeEnabled(v: Boolean) = launch { settingsRepository.setDemoModeEnabled(v) }
    fun setSimulateCharging(v: Boolean) = launch { settingsRepository.setSimulateCharging(v) }
    fun setSimulateNight(v: Boolean) = launch { settingsRepository.setSimulateNight(v) }

    // ---------------- Data sources ----------------

    fun setUseFahrenheit(v: Boolean) = launch { settingsRepository.setUseFahrenheit(v) }

    fun setWeatherCity(result: CityResult?) = launch {
        if (result == null) settingsRepository.setWeatherLocation("", null, null)
        else settingsRepository.setWeatherLocation(result.name, result.latitude, result.longitude)
    }

    fun toggleWorldCity(city: String) = launch {
        val current = settings.value.worldCities
        val updated = if (city in current) current - city else (current + city).take(8)
        settingsRepository.setWorldCities(updated)
    }

    fun setSecondaryCity(city: String) = launch { settingsRepository.setSecondaryCity(city) }

    fun addPhotos(uris: List<Uri>) = launch {
        val resolver = app.contentResolver
        uris.forEach { uri ->
            try {
                resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (e: SecurityException) {
                // Picker URIs without persistable grants still work for this session.
            }
        }
        val merged = (settings.value.photoUris + uris.map { it.toString() }).distinct().takeLast(60)
        settingsRepository.setPhotoUris(merged)
    }

    fun clearPhotos() = launch { settingsRepository.setPhotoUris(emptyList()) }
    fun setPhotoInterval(seconds: Int) = launch { settingsRepository.setPhotoInterval(seconds) }
    fun setCountdown(label: String, epochDay: Long?) = launch { settingsRepository.setCountdown(label, epochDay) }
    fun setNotificationPrivacy(v: NotificationPrivacy) = launch { settingsRepository.setNotificationPrivacy(v) }

    fun setGithubUsername(username: String) = launch {
        settingsRepository.setGithubUsername(username)
        GitHubRepository.fetchContributions(username, forceRefresh = true)
    }

    fun fetchGitHubContributions(username: String, forceRefresh: Boolean = false) = launch {
        GitHubRepository.fetchContributions(username, forceRefresh = forceRefresh)
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }

    companion object {
        /** True between sunset and sunrise (sun's upper limb below the horizon). */
        fun isSunDown(nowMillis: Long, latitude: Double, longitude: Double): Boolean =
            Astronomy.sunElevation(nowMillis, latitude, longitude) < -0.833

        /**
         * Sunrise wake light: ramps 0→1 over the [windowMinutes] before the alarm, then holds full
         * daylight for 15 minutes after it rings.
         */
        fun sunriseProgress(nowMillis: Long, alarmMillis: Long?, windowMinutes: Int): Float {
            if (alarmMillis == null || windowMinutes <= 0) return 0f
            val remaining = alarmMillis - nowMillis
            val window = windowMinutes * 60_000L
            return when {
                remaining > window -> 0f
                remaining > 0L -> 1f - remaining.toFloat() / window
                remaining > -15 * 60_000L -> 1f
                else -> 0f
            }
        }

        fun inWindow(minute: Int, start: Int, end: Int): Boolean =
            if (start == end) false else if (start < end) minute in start until end else minute >= start || minute < end
    }
}
