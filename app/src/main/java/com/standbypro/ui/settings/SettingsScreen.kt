package com.standbypro.ui.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.WatchFaceCategory
import com.standbypro.domain.WatchFaceDefinition
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WidgetRegistry
import com.standbypro.settings.StandByColorTheme
import com.standbypro.settings.StandByProfile
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.compositor.AmbientCompositor
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: StandByViewModel,
    onLaunchPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val ambientState by viewModel.ambientDisplayState.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val chargingState by viewModel.chargingState.collectAsState()
    val nextAlarm = remember(currentTime.minute) { viewModel.getNextAlarm() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<WatchFaceCategory?>(null) }
    var githubInput by remember(settings.githubUsername) { mutableStateOf(settings.githubUsername) }

    // Simulation overrides for preview
    var previewNight by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "StandBy Studio",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Commercial Ambient Display Engine",
                            fontSize = 12.sp,
                            color = settings.activeColorTheme.color
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onLaunchPreview,
                        colors = ButtonDefaults.buttonColors(containerColor = settings.activeColorTheme.color),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "StandBy", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0D0D0D))
            )
        },
        containerColor = Color(0xFF080808)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. LIVE MINIATURE PREVIEW (Runs exact same AmbientCompositor!)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.ViewCarousel, contentDescription = null, tint = settings.activeColorTheme.color)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "LIVE AMBIENT PREVIEW", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (previewNight) Color(0xFFFF3B30).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                                .clickable { previewNight = !previewNight }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (previewNight) "Preview Night: ON" else "Preview Night: OFF",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (previewNight) Color(0xFFFF3B30) else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Scaled Aspect Ratio Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(2.1f)
                            .clip(RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFF282828), RoundedCornerShape(16.dp))
                    ) {
                        AmbientCompositor(
                            state = ambientState.copy(isNight = previewNight),
                            currentTime = currentTime,
                            chargingState = chargingState,
                            nextAlarm = nextAlarm,
                            onUserInteraction = {},
                            onCycleFace = { viewModel.cycleWatchFace() },
                            onCycleRightWidget = { viewModel.cycleRightWidget() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // 2. WATCH FACE BROWSER (40+ Faces)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "WATCH FACE STUDIO", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = settings.activeColorTheme.color, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search 60+ watch faces...", color = Color.Gray, fontSize = 13.sp) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = settings.activeColorTheme.color,
                            unfocusedBorderColor = Color(0xFF2A2A2E),
                            focusedContainerColor = Color(0xFF0F0F12),
                            unfocusedContainerColor = Color(0xFF0F0F12),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Category Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null },
                            label = { Text("All (${WatchFaceRegistry.getAll().size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = settings.activeColorTheme.color,
                                selectedLabelColor = Color.Black
                            )
                        )
                        WatchFaceCategory.entries.forEach { cat ->
                            val count = WatchFaceRegistry.getByCategory(cat).size
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = if (selectedCategory == cat) null else cat },
                                label = { Text("${cat.displayName} ($count)") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = settings.activeColorTheme.color,
                                    selectedLabelColor = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Watch Face Cards Horizontal Carousel
                    val filteredFaces = remember(searchQuery, selectedCategory) {
                        if (searchQuery.isNotBlank()) {
                            WatchFaceRegistry.search(searchQuery)
                        } else if (selectedCategory != null) {
                            WatchFaceRegistry.getByCategory(selectedCategory!!)
                        } else {
                            WatchFaceRegistry.getAll()
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(filteredFaces, key = { it.id }) { face ->
                            WatchFaceStudioCard(
                                face = face,
                                isSelected = face.id == settings.activeWatchFaceId,
                                isFavorite = settings.favoriteFaceIds.contains(face.id),
                                accentColor = settings.activeColorTheme.color,
                                onSelect = { viewModel.setActiveWatchFaceId(face.id) },
                                onToggleFavorite = { viewModel.toggleFavoriteFace(face.id) }
                            )
                        }
                    }
                }
            }

            // 3. DASHBOARD LAYOUT SELECTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Dashboard, contentDescription = null, tint = settings.activeColorTheme.color)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "DASHBOARD LAYOUT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        DashboardLayoutType.entries.forEach { layout ->
                            val isSel = settings.layoutType == layout
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) settings.activeColorTheme.color.copy(alpha = 0.2f) else Color(0xFF0F0F12))
                                    .border(if (isSel) 1.5.dp else 1.dp, if (isSel) settings.activeColorTheme.color else Color(0xFF282828), RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setLayoutType(layout) }
                                    .padding(vertical = 12.dp, horizontal = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = layout.displayName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) settings.activeColorTheme.color else Color.White
                                    )
                                    Text(
                                        text = "${layout.slotCount} slot",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. WIDGET SLOT SELECTOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Widgets, contentDescription = null, tint = settings.activeColorTheme.color)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "PRIMARY WIDGET SLOT", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    val allWidgets = WidgetRegistry.getAll()
                    val currentWidgetId = settings.assignedWidgets.firstOrNull()?.widgetId ?: "widget_calendar"

                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allWidgets.forEach { w ->
                            val isSel = w.id == currentWidgetId
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSel) settings.activeColorTheme.color.copy(alpha = 0.2f) else Color(0xFF0F0F12))
                                    .border(if (isSel) 1.5.dp else 1.dp, if (isSel) settings.activeColorTheme.color else Color(0xFF282828), RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setAssignedWidget(0, w.id) }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = w.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isSel) settings.activeColorTheme.color else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 5. COLOR THEMES & BRIGHTNESS
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.ColorLens, contentDescription = null, tint = settings.activeColorTheme.color)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "THEME ACCENT & BRIGHTNESS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 1.sp)
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // 10 Theme Swatches
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StandByColorTheme.entries.forEach { theme ->
                            val isSelected = theme.id.equals(settings.colorThemeId, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(theme.color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.setColorTheme(theme.id) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 10-Step Brightness Notch Selector
                    Text(text = "BRIGHTNESS NOTCH: ${(settings.brightnessLevel * 100).roundToInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.01f, 0.10f, 0.20f, 0.30f, 0.40f, 0.50f, 0.60f, 0.70f, 0.80f, 0.90f, 1.00f).forEach { step ->
                            val isCurrent = (settings.brightnessLevel * 100).roundToInt() == (step * 100).roundToInt()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isCurrent) settings.activeColorTheme.color else Color(0xFF1E1E22))
                                    .clickable { viewModel.setBrightnessLevel(step) }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (step <= 0.02f) "DIM" else "${(step * 100).roundToInt()}%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrent) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 6. GITHUB INTEGRATION
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "GITHUB CONTRIBUTION GRAPH", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = settings.activeColorTheme.color, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = githubInput,
                            onValueChange = { githubInput = it },
                            modifier = Modifier.weight(1f),
                            label = { Text("Username") },
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { viewModel.setGithubUsername(githubInput) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = settings.activeColorTheme.color)
                        ) {
                            Icon(imageVector = Icons.Default.Sync, contentDescription = "Sync", tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 7. HARDWARE & NIGHT MODE BEHAVIOR
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131316))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "AMBIENT HARDWARE & SENSORS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = settings.activeColorTheme.color, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(12.dp))

                    SettingToggleRow(
                        title = "Auto-Start While Charging",
                        subtitle = "Triggers automatically when plugged in and in landscape",
                        checked = settings.autoStartWhileCharging,
                        onCheckedChange = { viewModel.setAutoStart(it) }
                    )
                    SettingToggleRow(
                        title = "Require Landscape",
                        subtitle = "Only activate when mounted horizontally on a stand",
                        checked = settings.requireLandscape,
                        onCheckedChange = { viewModel.setRequireLandscape(it) }
                    )
                    SettingToggleRow(
                        title = "Ambient Light Night Mode",
                        subtitle = "Drops brightness and shifts to low-strain night hue in the dark",
                        checked = settings.nightModeEnabled,
                        onCheckedChange = { viewModel.setNightModeEnabled(it) }
                    )
                    SettingToggleRow(
                        title = "Burn-In Protection",
                        subtitle = "Subtle periodic pixel shifting to preserve OLED screens",
                        checked = settings.burnInProtectionEnabled,
                        onCheckedChange = { viewModel.setBurnInProtectionEnabled(it) }
                    )
                    SettingToggleRow(
                        title = "Auto-Dimming",
                        subtitle = "Lowers screen intensity after 30 seconds of inactivity",
                        checked = settings.autoDimEnabled,
                        onCheckedChange = { viewModel.setAutoDimEnabled(it) }
                    )
                }
            }

            // 8. DEVELOPER / DEMO SIMULATION MODE
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1414))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "DEVELOPER DEMO SIMULATOR", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF453A), letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingToggleRow(
                        title = "Enable Demo Mode",
                        subtitle = "Simulates charging, night, weather, and live activities",
                        checked = settings.demoModeEnabled,
                        onCheckedChange = { viewModel.setDemoModeEnabled(it) }
                    )
                    AnimatedVisibility(visible = settings.demoModeEnabled) {
                        Column {
                            SettingToggleRow(
                                title = "Simulate Charging ON",
                                subtitle = "Forces charging state without physical cable",
                                checked = settings.simulateCharging,
                                onCheckedChange = { viewModel.setSimulateCharging(it) }
                            )
                            SettingToggleRow(
                                title = "Simulate Dark Bedroom",
                                subtitle = "Forces night mode threshold (< 5 lux)",
                                checked = settings.simulateNight,
                                onCheckedChange = { viewModel.setSimulateNight(it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WatchFaceStudioCard(
    face: WatchFaceDefinition,
    isSelected: Boolean,
    isFavorite: Boolean,
    accentColor: Color,
    onSelect: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.15f) else Color(0xFF0E0E10))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) accentColor else Color(0xFF26262A),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onSelect() }
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = face.category.displayName.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFFFD60A) else Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = face.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = face.description,
                fontSize = 10.sp,
                color = Color.Gray,
                maxLines = 2,
                lineHeight = 12.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            if (isSelected) {
                Text(text = "✓ ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Black, color = accentColor)
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF30D158)
            )
        )
    }
}
