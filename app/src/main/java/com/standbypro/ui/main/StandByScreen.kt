package com.standbypro.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.settings.StandByColorTheme
import com.standbypro.theme.StandByNightRed
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.compositor.AmbientCompositor
import kotlin.math.roundToInt

@Composable
fun StandByScreen(
    viewModel: StandByViewModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ambientState by viewModel.ambientDisplayState.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val chargingState by viewModel.chargingState.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val nextAlarm = remember(currentTime.minute) { viewModel.getNextAlarm() }

    Box(modifier = modifier.fillMaxSize().background(Color.Black)) {
        // Core Unified Ambient Compositor
        AmbientCompositor(
            state = ambientState,
            currentTime = currentTime,
            chargingState = chargingState,
            nextAlarm = nextAlarm,
            onUserInteraction = { viewModel.reportInteraction() },
            onCycleFace = { viewModel.cycleWatchFace() },
            onCycleRightWidget = { viewModel.cycleRightWidget() },
            modifier = Modifier.fillMaxSize()
        )

        // Top Overlay Bar: Exit button, Theme cycling swatch, 10-step Brightness Notch, Night mode pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .align(Alignment.TopStart),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onExit,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .size(34.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit StandBy",
                    tint = ambientState.activeThemeColor.copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Quick Color Theme Swatch Picker
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(settings.activeColorTheme.color)
                        .clickable {
                            viewModel.reportInteraction()
                            val allThemes = StandByColorTheme.entries
                            val currentIndex = allThemes.indexOfFirst { it.id == settings.colorThemeId }
                            val nextTheme = allThemes[(currentIndex + 1) % allThemes.size]
                            viewModel.setColorTheme(nextTheme.id)
                        }
                )

                // Interactive 10-step Brightness Notch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (settings.brightnessLevel <= 0.02f) ambientState.activeThemeColor.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.12f))
                        .clickable {
                            viewModel.reportInteraction()
                            val current = settings.brightnessLevel
                            val nextBrightness = when {
                                current < 0.05f -> 0.10f
                                current < 0.15f -> 0.20f
                                current < 0.25f -> 0.30f
                                current < 0.35f -> 0.40f
                                current < 0.45f -> 0.50f
                                current < 0.55f -> 0.60f
                                current < 0.65f -> 0.70f
                                current < 0.75f -> 0.80f
                                current < 0.85f -> 0.90f
                                current < 0.95f -> 1.00f
                                else -> 0.01f // Wrap back to Most Dim
                            }
                            viewModel.setBrightnessLevel(nextBrightness)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = if (settings.brightnessLevel <= 0.02f) Icons.Default.BrightnessLow else Icons.Default.BrightnessMedium,
                        contentDescription = "Brightness Notch",
                        tint = if (settings.brightnessLevel <= 0.02f) ambientState.activeThemeColor else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (settings.brightnessLevel <= 0.02f) "MOST DIM" else "${(settings.brightnessLevel * 100).roundToInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (settings.brightnessLevel <= 0.02f) ambientState.activeThemeColor else Color.White
                    )
                }

                if (ambientState.isNight) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(StandByNightRed.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.NightsStay,
                            contentDescription = null,
                            tint = StandByNightRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NIGHT MODE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = StandByNightRed
                        )
                    }
                }
            }
        }
    }
}
