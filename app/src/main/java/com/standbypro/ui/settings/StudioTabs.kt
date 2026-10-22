package com.standbypro.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Monitor
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.data.CityResult
import com.standbypro.data.Locations
import com.standbypro.data.TasksRepository
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.FaceContext
import com.standbypro.domain.NotificationPrivacy
import com.standbypro.domain.WatchFaceCategory
import com.standbypro.domain.WatchFaceDefinition
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WidgetRegistry
import com.standbypro.settings.ChimeStyle
import com.standbypro.settings.NightStyle
import com.standbypro.settings.NightTrigger
import com.standbypro.settings.StandByColorTheme
import com.standbypro.settings.StandByPage
import com.standbypro.settings.StandBySettings
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.SecondsMotion
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@Composable
private fun studioFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accent,
    unfocusedBorderColor = Ink.Hairline,
    focusedContainerColor = Color.White.copy(alpha = 0.04f),
    unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
    focusedTextColor = Ink.TextPrimary,
    unfocusedTextColor = Ink.TextPrimary,
    cursorColor = accent
)

// =============================================================================================
// FACES
// =============================================================================================

fun LazyListScope.facesTab(
    viewModel: StandByViewModel,
    settings: StandBySettings,
    ctx: FaceContext,
    query: String,
    onQuery: (String) -> Unit,
    category: WatchFaceCategory?,
    onCategory: (WatchFaceCategory?) -> Unit,
    favoritesOnly: Boolean,
    onFavoritesOnly: (Boolean) -> Unit
) {
    val accent = settings.activeColorTheme.color
    item(key = "faces_filters") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search ${WatchFaceRegistry.getAll().size} faces", color = Ink.TextTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Ink.TextTertiary) },
                trailingIcon = if (query.isNotEmpty()) ({ Icon(Icons.Default.Close, "Clear", tint = Ink.TextTertiary, modifier = Modifier.clickable { onQuery("") }) }) else null,
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = studioFieldColors(accent)
            )
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterPill("All", category == null && !favoritesOnly, accent) { onCategory(null); onFavoritesOnly(false) }
                FilterPill("★ Favourites", favoritesOnly, Ink.Yellow) { onFavoritesOnly(!favoritesOnly); onCategory(null) }
                WatchFaceRegistry.categoriesInUse().forEach { cat ->
                    FilterPill("${cat.displayName} ${WatchFaceRegistry.getByCategory(cat).size}", category == cat, accent) {
                        onFavoritesOnly(false)
                        onCategory(if (category == cat) null else cat)
                    }
                }
            }
        }
    }
    val base = when {
        query.isNotBlank() -> WatchFaceRegistry.search(query)
        favoritesOnly -> WatchFaceRegistry.getFavorites(settings.favoriteFaceIds)
        category != null -> WatchFaceRegistry.getByCategory(category)
        else -> WatchFaceRegistry.getAll()
    }
    if (base.isEmpty()) {
        item(key = "faces_empty") {
            Text("No faces match “$query”", color = Ink.TextTertiary, fontSize = 14.sp, modifier = Modifier.padding(12.dp))
        }
    }
    val previewCtx = ctx.copy(isPreview = true, isCompact = true)
    items(base.chunked(2), key = { row -> "facerow_" + row.first().id }) { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEach { face ->
                FaceCard(
                    face = face,
                    ctx = previewCtx,
                    selected = face.id == settings.activeWatchFaceId,
                    favorite = face.id in settings.favoriteFaceIds,
                    accent = accent,
                    onSelect = { viewModel.setActiveWatchFaceId(face.id) },
                    onFavorite = { viewModel.toggleFavoriteFace(face.id) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun FilterPill(text: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(50))
            .background(if (selected) accent else Color.White.copy(alpha = 0.07f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (selected) Color.Black else Ink.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

@Composable
private fun FaceCard(
    face: WatchFaceDefinition,
    ctx: FaceContext,
    selected: Boolean,
    favorite: Boolean,
    accent: Color,
    onSelect: () -> Unit,
    onFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(1.5f).clip(RoundedCornerShape(20.dp)).background(Ink.Graphite)
                .border(if (selected) 2.dp else 1.dp, if (selected) accent else Ink.Hairline, RoundedCornerShape(20.dp))
                .clickable(onClick = onSelect)
        ) {
            WatchFaceRegistry.Render(face.id, ctx, Modifier.fillMaxSize().padding(4.dp))
            if (face.isNew) {
                Text(
                    "NEW", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black,
                    modifier = Modifier.align(Alignment.TopStart).padding(8.dp).clip(RoundedCornerShape(50)).background(accent).padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
            Box(
                Modifier.align(Alignment.TopEnd).padding(6.dp).size(30.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.45f)).clickable(onClick = onFavorite),
                contentAlignment = Alignment.Center
            ) {
                Icon(if (favorite) Icons.Default.Star else Icons.Default.StarBorder, "Favourite", tint = if (favorite) Ink.Yellow else Color.White.copy(alpha = 0.8f), modifier = Modifier.size(17.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(face.name, color = if (selected) accent else Ink.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, modifier = Modifier.padding(start = 4.dp))
        Text(face.category.displayName, color = Ink.TextTertiary, fontSize = 11.sp, maxLines = 1, modifier = Modifier.padding(start = 4.dp))
    }
}

// =============================================================================================
// WIDGETS
// =============================================================================================

fun LazyListScope.widgetsTab(viewModel: StandByViewModel, settings: StandBySettings, ctx: FaceContext) {
    val accent = settings.activeColorTheme.color
    item(key = "w_layout") {
        StudioSection("Dashboard layout", footer = "The first page of StandBy. Swipe sideways on the display for Smart Stacks, Photos and Clock.") {
            Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DashboardLayoutType.entries.forEach { layout ->
                    val sel = settings.layoutType == layout
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(14.dp))
                            .background(if (sel) accent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.04f))
                            .border(1.dp, if (sel) accent else Color.Transparent, RoundedCornerShape(14.dp))
                            .clickable { viewModel.setLayoutType(layout) }.padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LayoutDiagram(layout, if (sel) accent else Ink.TextTertiary, Modifier.fillMaxWidth().aspectRatio(1.7f))
                        Spacer(Modifier.height(6.dp))
                        Text(layout.displayName, color = if (sel) accent else Ink.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                }
            }
        }
    }
    val slots = (settings.layoutType.slotCount - 1).coerceAtLeast(0)
    if (slots > 0) {
        item(key = "w_slots") {
            StudioSection("Dashboard widgets") {
                val all = WidgetRegistry.getAll()
                for (i in 0 until slots) {
                    SubLabel(if (slots == 1) "Widget" else "Slot ${i + 1}")
                    ChipRow(all, { it.id == settings.assignedWidgets.getOrNull(i)?.widgetId }, { it.name }, { viewModel.setAssignedWidget(i, it.id) }, accent)
                    if (i < slots - 1) RowDivider()
                }
            }
        }
    }
    item(key = "w_stacks") {
        StudioSection("Smart Stacks", footer = "Swipe up and down on each stack. Smart Rotate turns to the next widget every few minutes.") {
            val all = WidgetRegistry.getAll()
            SubLabel("Left stack · ${settings.leftStack.size}")
            ChipRow(all, { it.id in settings.leftStack }, { it.name }, { viewModel.toggleStackWidget(true, it.id) }, accent)
            RowDivider()
            SubLabel("Right stack · ${settings.rightStack.size}")
            ChipRow(all, { it.id in settings.rightStack }, { it.name }, { viewModel.toggleStackWidget(false, it.id) }, accent)
            RowDivider()
            ToggleRow("Smart Rotate", "Cycle stack widgets automatically", settings.smartRotate, { viewModel.setSmartRotate(it) })
        }
    }
    item(key = "w_start") {
        StudioSection("Open StandBy on") {
            Segmented(StandByPage.entries, settings.startPage, { it.label }, { viewModel.setStartPage(it) })
        }
    }
}

@Composable
private fun LayoutDiagram(layout: DashboardLayoutType, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val g = size.minDimension * 0.06f
        val r = CornerRadius(size.minDimension * 0.08f)
        fun box(x: Float, y: Float, w: Float, h: Float, filled: Boolean) =
            drawRoundRect(if (filled) color else color.copy(alpha = 0.35f), Offset(x, y), Size(w, h), r)
        when (layout) {
            DashboardLayoutType.SINGLE -> box(0f, 0f, size.width, size.height, true)
            DashboardLayoutType.DUO -> {
                box(0f, 0f, size.width / 2 - g / 2, size.height, true)
                box(size.width / 2 + g / 2, 0f, size.width / 2 - g / 2, size.height, false)
            }
            DashboardLayoutType.TRIPLE -> {
                box(0f, 0f, size.width * 0.55f - g / 2, size.height, true)
                box(size.width * 0.55f + g / 2, 0f, size.width * 0.45f - g / 2, size.height / 2 - g / 2, false)
                box(size.width * 0.55f + g / 2, size.height / 2 + g / 2, size.width * 0.45f - g / 2, size.height / 2 - g / 2, false)
            }
            DashboardLayoutType.QUAD -> {
                val w = size.width / 2 - g / 2
                val h = size.height / 2 - g / 2
                box(0f, 0f, w, h, true)
                box(w + g, 0f, w, h, false)
                box(0f, h + g, w, h, false)
                box(w + g, h + g, w, h, false)
            }
        }
    }
}

// =============================================================================================
// DISPLAY
// =============================================================================================

private fun minutesLabel(m: Int): String = "%02d:%02d".format(m / 60, m % 60)

fun LazyListScope.displayTab(viewModel: StandByViewModel, settings: StandBySettings, ctx: FaceContext) {
    val accent = settings.activeColorTheme.color
    item(key = "d_themes") {
        StudioSection("Themes", footer = "One tap sets the accent, face, typeface, backdrop and night tint together. Fine-tune anything below.") {
            ThemeGallery(settings, ctx) { viewModel.applyTheme(it) }
        }
    }
    item(key = "d_font") {
        StudioSection("Clock typeface", footer = "Used by every face that doesn't have a signature typeface of its own.") {
            ClockFontPicker(settings.clockFont, ctx.timeText, accent) { viewModel.setClockFont(it) }
        }
    }
    item(key = "d_backdrop") {
        StudioSection("Backdrop", footer = "Drawn behind the dashboard and smart stacks. Moves very slowly and pauses at night.") {
            BackdropPicker(settings.backdrop, accent) { viewModel.setBackdrop(it) }
        }
    }
    item(key = "d_shuffle") {
        StudioSection("Face shuffle", footer = "Rotates through your favourite faces while the display is untouched.") {
            ChipRow(listOf(0, 5, 15, 30, 60), { it == settings.faceShuffleMinutes }, { if (it == 0) "Off" else if (it < 60) "$it min" else "1 hour" }, { viewModel.setFaceShuffle(it) }, accent)
        }
    }
    item(key = "d_accent") {
        StudioSection("Accent colour") {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StandByColorTheme.entries.forEach { theme ->
                    ColorSwatch(theme.color, theme.id == settings.colorThemeId) { viewModel.setColorTheme(theme.id) }
                }
            }
            Text(settings.activeColorTheme.displayName, color = Ink.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, bottom = 10.dp))
        }
    }
    item(key = "d_clock") {
        StudioSection("Clock") {
            ToggleRow("24-hour time", null, settings.use24Hour, { viewModel.setUse24Hour(it) })
            RowDivider()
            ToggleRow("Spoken briefing", "The speak button also reads the weather, your next event and alarm", settings.spokenBriefing, { viewModel.setSpokenBriefing(it) })
            RowDivider()
            ToggleRow("Seconds", "Second hands, seconds digits and progress", settings.showSeconds, { viewModel.setShowSeconds(it) })
            if (settings.showSeconds) {
                SubLabel("Seconds motion")
                Segmented(SecondsMotion.entries.filter { it != SecondsMotion.OFF }, settings.secondsMotion.takeIf { it != SecondsMotion.OFF } ?: SecondsMotion.SWEEP, { it.label }, { viewModel.setSecondsMotion(it) })
                Text(settings.secondsMotion.description, color = Ink.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            }
        }
    }
    item(key = "d_brightness") {
        StudioSection("Brightness", footer = "StandBy overrides screen brightness while it is showing; night mode caps it automatically. With adaptive brightness on, the slider makes the curve brighter or dimmer.") {
            ToggleRow("Adaptive brightness", "Follow the light in the room", settings.autoBrightness, { viewModel.setAutoBrightness(it) })
            var local by remember(settings.brightnessLevel) { mutableFloatStateOf(settings.brightnessLevel) }
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                Slider(
                    value = local,
                    onValueChange = { local = it },
                    onValueChangeFinished = { viewModel.setBrightnessLevel(local) },
                    valueRange = 0.01f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = accent, inactiveTrackColor = Color.White.copy(alpha = 0.15f))
                )
                Text("${(local * 100).roundToInt()}%", color = Ink.TextSecondary, fontSize = 14.sp, modifier = Modifier.width(48.dp).padding(start = 10.dp))
            }
        }
    }
    item(key = "d_night") {
        StudioSection("Night mode", footer = "Turns everything into a dim red (or amber) rendition that won't wreck your night vision.") {
            ToggleRow("Night mode", null, settings.nightModeEnabled, { viewModel.setNightModeEnabled(it) }, icon = Icons.Default.DarkMode, iconTint = Ink.Red)
            if (settings.nightModeEnabled) {
                SubLabel("Turns on")
                Segmented(NightTrigger.entries, settings.nightTrigger, { it.label }, { viewModel.setNightTrigger(it) })
                Text(settings.nightTrigger.description, color = Ink.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp))
                if (settings.nightTrigger == NightTrigger.SCHEDULE) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TimeStepper("From", settings.nightStartMinutes, Modifier.weight(1f)) { viewModel.setNightSchedule(it, settings.nightEndMinutes) }
                        TimeStepper("To", settings.nightEndMinutes, Modifier.weight(1f)) { viewModel.setNightSchedule(settings.nightStartMinutes, it) }
                    }
                }
                SubLabel("Tint")
                if (settings.nightTrigger == NightTrigger.SUN) {
                    Text("Uses your weather location to work out sunset and sunrise each day.", color = Ink.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, top = 4.dp))
                }
                ChipRow(NightStyle.entries, { it == settings.nightStyle }, { it.label }, { viewModel.setNightModeStyle(it.id) }, Ink.Red)
                SubLabel("Night face")
                val nightChoices = (listOf("", "digital_minimal", "ambient_minimal_gradient", "digital_oled", "analog_meridian", "ambient_breathe") + viewModel.favoriteFaces(settings)).distinct()
                ChipRow(nightChoices, { it == settings.nightFaceId }, { if (it.isEmpty()) "Same as day" else WatchFaceRegistry.getById(it).name }, { viewModel.setNightFace(it) }, Ink.Red)
                RowDivider()
                ToggleRow("Bedtime blackout", "Fade almost to black at night; tap to peek", settings.nightBlackout, { viewModel.setNightBlackout(it) })
            }
        }
    }
    item(key = "d_protect") {
        StudioSection("Screen care") {
            ToggleRow("Burn-in protection", "Slowly drifts the layout a few pixels", settings.burnInProtectionEnabled, { viewModel.setBurnInProtectionEnabled(it) })
            RowDivider()
            ToggleRow("Auto-dim", "Dims after a period without touches", settings.autoDimEnabled, { viewModel.setAutoDimEnabled(it) })
            if (settings.autoDimEnabled) {
                ChipRow(listOf(30, 60, 120, 300, 600), { it == settings.autoDimTimeoutSeconds }, { if (it < 60) "${it}s" else "${it / 60} min" }, { viewModel.setAutoDimTimeout(it) }, accent)
            }
            RowDivider()
            ToggleRow("Charging animation", "Glowing ring when you plug in", settings.showChargingAnimation, { viewModel.setShowChargingAnimation(it) })
            RowDivider()
            ToggleRow("Hourly chime", "On the hour, never at night", settings.hourlyChime, { viewModel.setHourlyChime(it) })
            if (settings.hourlyChime) {
                ChipRow(ChimeStyle.entries, { it == settings.chimeStyle }, { it.label }, { viewModel.setChimeStyle(it); viewModel.playChime(it) }, accent)
                Text(settings.chimeStyle.description + " · tap a style to hear it", color = Ink.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
            }
            RowDivider()
            ToggleRow("Haptics", "Subtle feedback when swiping pages", settings.hapticsEnabled, { viewModel.setHaptics(it) })
            RowDivider()
            ToggleRow("Wave to wake", "Wave over the top of the phone to show controls and undim", settings.waveToWake, { viewModel.setWaveToWake(it) })
        }
    }
}

@Composable
private fun TimeStepper(label: String, minutes: Int, modifier: Modifier, onChange: (Int) -> Unit) {
    Row(
        modifier.clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = 0.06f)).padding(horizontal = 6.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("−", color = Ink.TextPrimary, fontSize = 22.sp, modifier = Modifier.clip(CircleShape).clickable { onChange(minutes - 30) }.padding(horizontal = 10.dp))
        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label.uppercase(), color = Ink.TextTertiary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text(minutesLabel(minutes), color = Ink.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
        Text("+", color = Ink.TextPrimary, fontSize = 22.sp, modifier = Modifier.clip(CircleShape).clickable { onChange(minutes + 30) }.padding(horizontal = 10.dp))
    }
}

// =============================================================================================
// DATA
// =============================================================================================

@OptIn(ExperimentalMaterial3Api::class)
fun LazyListScope.dataTab(
    viewModel: StandByViewModel,
    settings: StandBySettings,
    calendarGranted: Boolean,
    notificationAccess: Boolean,
    requestCalendar: () -> Unit
) {
    val accent = settings.activeColorTheme.color
    item(key = "data_weather") {
        val weather by viewModel.weather.collectAsState()
        val refreshing by viewModel.weatherRefreshing.collectAsState()
        var search by remember { mutableStateOf("") }
        var results by remember { mutableStateOf<List<CityResult>>(emptyList()) }
        LaunchedEffect(search) {
            if (search.length < 2) {
                results = emptyList()
                return@LaunchedEffect
            }
            delay(400L)
            results = viewModel.searchCities(search)
        }
        val place = viewModel.resolvePlace(settings)
        StudioSection("Weather", footer = "Live data from Open-Meteo. Location is used only to fetch your forecast.") {
            NavRow(
                place.city,
                if (settings.weatherLatitude == null) "Automatic from your time zone" else "Chosen city",
                value = weather?.let { "${if (settings.useFahrenheit) (it.temperatureCelsius * 9 / 5 + 32) else it.temperatureCelsius}° ${it.condition.displayName}" } ?: if (refreshing) "Updating…" else "Offline",
                icon = Icons.Default.LocationOn,
                iconTint = Ink.Blue
            ) { viewModel.refreshWeatherNow() }
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = { Text("Search for a city", color = Ink.TextTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Ink.TextTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = studioFieldColors(accent)
            )
            results.forEach { r ->
                NavRow(r.name, r.label, icon = Icons.Default.LocationOn, iconTint = Ink.Teal) {
                    viewModel.setWeatherCity(r)
                    search = ""
                }
            }
            if (settings.weatherLatitude != null) {
                RowDivider()
                NavRow("Use automatic location", "Pick a city from your device time zone", icon = Icons.Default.MyLocation, iconTint = Ink.Green) { viewModel.setWeatherCity(null) }
            }
            RowDivider()
            ToggleRow("Fahrenheit", null, settings.useFahrenheit, { viewModel.setUseFahrenheit(it) })
            RowDivider()
            NavRow("Refresh now", null, icon = Icons.Default.Refresh, iconTint = Ink.Indigo) { viewModel.refreshWeatherNow() }
        }
    }
    item(key = "data_world") {
        StudioSection("World clocks", footer = "Used by World Clocks, Time Zones, Day & Night and widgets (up to 8).") {
            ChipRow(Locations.places.map { it.city }.distinct(), { it in settings.worldCities }, { it }, { viewModel.toggleWorldCity(it) }, accent)
            RowDivider()
            SubLabel("Second time zone (GMT faces)")
            ChipRow(Locations.places.map { it.city }.distinct(), { it == settings.secondaryCity }, { it }, { viewModel.setSecondaryCity(it) }, accent)
        }
    }
    item(key = "data_photos") {
        val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(30)) { uris ->
            if (uris.isNotEmpty()) viewModel.addPhotos(uris)
        }
        StudioSection("Photos", footer = "Photos stay on your device; StandBy only keeps read access to the ones you pick.") {
            NavRow("Choose photos", "${settings.photoUris.size} selected", icon = Icons.Default.Photo, iconTint = Ink.Yellow) {
                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            if (settings.photoUris.isNotEmpty()) {
                RowDivider()
                NavRow("Remove all photos", null, valueColor = Ink.Red, icon = Icons.Default.Close, iconTint = Ink.Red) { viewModel.clearPhotos() }
            }
            SubLabel("Change every")
            ChipRow(listOf(10, 20, 60, 300), { it == settings.photoIntervalSeconds }, { if (it < 60) "${it}s" else "${it / 60} min" }, { viewModel.setPhotoInterval(it) }, accent)
        }
    }
    item(key = "data_countdown") {
        var label by remember(settings.countdownLabel) { mutableStateOf(settings.countdownLabel) }
        var showPicker by remember { mutableStateOf(false) }
        val date = settings.countdownEpochDay?.let { LocalDate.ofEpochDay(it) } ?: LocalDate.of(LocalDate.now().year + 1, 1, 1)
        StudioSection("Countdown") {
            OutlinedTextField(
                value = label,
                onValueChange = { label = it.take(28) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = { Text("Event name", color = Ink.TextTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = studioFieldColors(accent),
                trailingIcon = {
                    if (label != settings.countdownLabel) TextButton(onClick = { viewModel.setCountdown(label.ifBlank { "Countdown" }, settings.countdownEpochDay) }) { Text("Save", color = accent) }
                }
            )
            NavRow("Date", null, value = date.format(DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.getDefault())), icon = Icons.Default.Event, iconTint = Ink.Purple) { showPicker = true }
        }
        if (showPicker) {
            val state = rememberDatePickerState(initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            DatePickerDialog(
                onDismissRequest = { showPicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { ms ->
                            viewModel.setCountdown(label.ifBlank { "Countdown" }, Instant.ofEpochMilli(ms).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay())
                        }
                        showPicker = false
                    }) { Text("Set") }
                },
                dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
            ) {
                DatePicker(state = state)
            }
        }
    }
    item(key = "data_sources") {
        StudioSection("Calendar & notifications") {
            NavRow("Calendar access", if (calendarGranted) "Events appear on agenda faces" else "Tap to allow", value = if (calendarGranted) "On" else "Off", icon = Icons.Default.Event, iconTint = Ink.Red) {
                if (!calendarGranted) requestCalendar()
            }
            RowDivider()
            val context = androidx.compose.ui.platform.LocalContext.current
            NavRow("Notification access", "Now Playing and notification badge", value = if (notificationAccess) "On" else "Off", icon = Icons.Default.PowerSettingsNew, iconTint = Ink.Pink) {
                context.startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }
            RowDivider()
            ToggleRow("Notification banners", "New notifications slide in on the display for a few seconds", settings.notificationBanners, { viewModel.setNotificationBanners(it) })
            SubLabel("Notification privacy")
            ChipRow(NotificationPrivacy.entries, { it == settings.notificationPrivacy }, { it.displayName.substringBefore(" (") }, { viewModel.setNotificationPrivacy(it) }, accent)
        }
    }
    item(key = "data_tasks") {
        val todos by TasksRepository.todos.collectAsState()
        var newTodo by remember { mutableStateOf("") }
        var noteTitle by remember { mutableStateOf("") }
        var noteBody by remember { mutableStateOf("") }
        StudioSection("Reminders & notes", footer = "Shown on the Reminders and Notes widgets.") {
            OutlinedTextField(
                value = newTodo,
                onValueChange = { newTodo = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = { Text("New reminder", color = Ink.TextTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = studioFieldColors(accent),
                trailingIcon = {
                    Icon(Icons.Default.Add, "Add", tint = accent, modifier = Modifier.clickable {
                        TasksRepository.addTodo(newTodo)
                        newTodo = ""
                    })
                }
            )
            todos.take(8).forEach { t ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (t.isCompleted) "✓" else "○", color = if (t.isCompleted) Ink.Blue else Ink.TextTertiary, fontSize = 16.sp, modifier = Modifier.clickable { TasksRepository.toggleTodo(t.id) }.padding(end = 10.dp))
                    Text(t.title, color = if (t.isCompleted) Ink.TextTertiary else Ink.TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1)
                    Icon(Icons.Default.Close, "Delete", tint = Ink.TextTertiary, modifier = Modifier.size(18.dp).clickable { TasksRepository.removeTodo(t.id) })
                }
            }
            RowDivider()
            OutlinedTextField(
                value = noteTitle, onValueChange = { noteTitle = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                placeholder = { Text("Note title", color = Ink.TextTertiary) }, singleLine = true,
                shape = RoundedCornerShape(14.dp), colors = studioFieldColors(accent)
            )
            OutlinedTextField(
                value = noteBody, onValueChange = { noteBody = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                placeholder = { Text("Write a bedside note…", color = Ink.TextTertiary) }, minLines = 2,
                shape = RoundedCornerShape(14.dp), colors = studioFieldColors(accent),
                trailingIcon = {
                    Icon(Icons.Default.Add, "Save note", tint = accent, modifier = Modifier.clickable {
                        TasksRepository.addNote(noteTitle, noteBody)
                        noteTitle = ""
                        noteBody = ""
                    })
                }
            )
        }
    }
    item(key = "data_github") {
        var username by remember(settings.githubUsername) { mutableStateOf(settings.githubUsername) }
        StudioSection("GitHub") {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                placeholder = { Text("GitHub username", color = Ink.TextTertiary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = studioFieldColors(accent),
                trailingIcon = { TextButton(onClick = { viewModel.setGithubUsername(username) }) { Text("Sync", color = accent) } }
            )
        }
    }
}

// =============================================================================================
// BEHAVIOUR
// =============================================================================================

fun LazyListScope.behaviourTab(viewModel: StandByViewModel, settings: StandBySettings) {
    alarmSection(viewModel, settings)
    item(key = "b_auto") {
        StudioSection("Automatic StandBy", footer = "StandBy starts when the phone is charging, on its side and locked — just like iPhone. The monitor sleeps completely when unplugged.") {
            ToggleRow("StandBy", "Master switch", settings.isEnabled, { viewModel.setEnabled(it) })
            RowDivider()
            ToggleRow("Start while charging", null, settings.autoStartWhileCharging, { viewModel.setAutoStart(it) })
            RowDivider()
            ToggleRow("Require landscape", "Only when the phone is on its side", settings.requireLandscape, { viewModel.setRequireLandscape(it) })
            RowDivider()
            ToggleRow("Require locked", "Only when the screen is off or locked", settings.requireScreenLocked, { viewModel.setRequireScreenLocked(it) })
            RowDivider()
            val context = LocalContext.current
            NavRow("Use as screen saver", "Pick StandBy Pro in Display → Screen saver", icon = Icons.Default.Monitor, iconTint = Ink.Teal) {
                runCatching { context.startActivity(android.content.Intent(android.provider.Settings.ACTION_DREAM_SETTINGS)) }
            }
        }
    }
    item(key = "b_bedtime") {
        val context = LocalContext.current
        val resumeTick by viewModel.resumeTick.collectAsState()
        val dndGranted = remember(resumeTick) { viewModel.isDndGranted() }
        StudioSection(
            "Bedtime & battery",
            footer = "Sleep sounds, the sleep timer and the night light are behind the moon button on the StandBy display."
        ) {
            SubLabel("Sunrise wake light")
            ChipRow(listOf(0, 10, 20, 30), { it == settings.sunriseMinutes }, { if (it == 0) "Off" else "$it min before alarm" }, { viewModel.setSunriseMinutes(it) }, Ink.Orange)
            Text(
                "The display glows from deep red to warm daylight before your next alarm.",
                color = Ink.TextTertiary, fontSize = 12.sp, modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 6.dp)
            )
            RowDivider()
            ToggleRow(
                "Do Not Disturb at night", "Priority only while night mode is on; alarms still ring",
                settings.autoDnd,
                { on ->
                    viewModel.setAutoDnd(on)
                    if (on && !dndGranted) context.startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                },
                icon = Icons.Default.DoNotDisturbOn, iconTint = Ink.Indigo
            )
            if (settings.autoDnd && !dndGranted) {
                NavRow("Allow Do Not Disturb access", "Required to switch Do Not Disturb", icon = Icons.Default.Bedtime, iconTint = Ink.Red) {
                    context.startActivity(android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
                }
            }
            RowDivider()
            SubLabel("Charge limit alert")
            ChipRow(listOf(0, 80, 85, 90, 100), { it == settings.chargeLimitPercent }, { if (it == 0) "Off" else "$it%" }, { viewModel.setChargeLimit(it) }, Ink.Green)
            Row(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.BatteryChargingFull, null, tint = Ink.TextTertiary, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Notifies you to unplug when charging reaches the limit — keeping lithium cells below 100% slows wear.", color = Ink.TextTertiary, fontSize = 12.sp)
            }
        }
    }
    item(key = "b_backup") {
        val status by viewModel.backupStatus.collectAsState()
        val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) viewModel.exportSettings(uri)
        }
        val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) viewModel.importSettings(uri)
        }
        StudioSection("Backup", footer = status ?: "Saves faces, themes, widgets, cities and every preference to a file you can restore on any phone. Photos stay on this device.") {
            NavRow("Export settings", "Save a .json backup", icon = Icons.Default.Upload, iconTint = Ink.Blue) {
                exportLauncher.launch("standby-pro-settings.json")
            }
            RowDivider()
            NavRow("Import settings", "Restore from a backup file", icon = Icons.Default.Download, iconTint = Ink.Green) {
                importLauncher.launch(arrayOf("application/json", "text/plain", "application/octet-stream"))
            }
        }
    }
    item(key = "b_demo") {
        StudioSection("Demo & testing", footer = "Simulates charging and night so you can try everything at your desk.") {
            ToggleRow("Demo mode", "Fills empty data with samples", settings.demoModeEnabled, { viewModel.setDemoModeEnabled(it) })
            if (settings.demoModeEnabled) {
                RowDivider()
                ToggleRow("Simulate charging", null, settings.simulateCharging, { viewModel.setSimulateCharging(it) })
                RowDivider()
                ToggleRow("Simulate dark room", null, settings.simulateNight, { viewModel.setSimulateNight(it) })
            }
        }
    }
    item(key = "b_tips") {
        StudioSection("Gestures") {
            listOf(
                "Swipe left / right" to "Dashboard · Smart Stacks · Photos · Clock",
                "Swipe up / down" to "Next favourite face or stack widget",
                "Double-tap" to "Cycle favourite faces",
                "Long-press" to "Favourites picker",
                "Tap" to "Show or hide controls",
                "Lock button" to "Ignore touches until you press and hold"
            ).forEachIndexed { i, (g, d) ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(g, color = Ink.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium, modifier = Modifier.width(140.dp))
                    Text(d, color = Ink.TextTertiary, fontSize = 13.sp)
                }
                if (i < 5) RowDivider()
            }
        }
    }
}
