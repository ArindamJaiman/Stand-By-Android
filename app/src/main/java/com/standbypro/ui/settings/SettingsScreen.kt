package com.standbypro.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.settings.ClockStyle
import com.standbypro.theme.StandByAccent
import com.standbypro.theme.StandByBackground
import kotlin.math.roundToInt
import com.standbypro.theme.StandByNightRed
import com.standbypro.theme.StandByOnSurfaceDim
import com.standbypro.theme.StandBySurface
import com.standbypro.theme.StandBySurfaceVariant
import com.standbypro.ui.StandByViewModel

@Composable
fun SettingsScreen(
    viewModel: StandByViewModel,
    onLaunchPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val chargingState by viewModel.chargingState.collectAsState()
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = StandByBackground
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                text = "StandBy Pro",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Ambient Smart Display for Android",
                                fontSize = 14.sp,
                                color = StandByOnSurfaceDim
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (settings.isEnabled) StandByAccent.copy(alpha = 0.2f) else Color.Gray.copy(alpha = 0.2f))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (settings.isEnabled) "ACTIVE" else "DISABLED",
                                color = if (settings.isEnabled) StandByAccent else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Preview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StandBySurfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = StandByAccent,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Preview Ambient Display",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Test the full-screen StandBy experience immediately",
                                    fontSize = 13.sp,
                                    color = StandByOnSurfaceDim
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onLaunchPreview,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = StandByAccent,
                                contentColor = Color.Black
                            )
                        ) {
                            Text("Launch StandBy Now", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Hardware Status Pill
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StandBySurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = if (chargingState.isCharging) StandByAccent else StandByOnSurfaceDim,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Battery: ${chargingState.batteryPercent}% (${if (chargingState.isCharging) "Charging via ${chargingState.chargeType}" else "Not charging"})",
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Activation Settings
            item {
                SettingsCategoryTitle("AUTOMATION & TRIGGER")
            }

            item {
                SettingsCard {
                    SettingToggleItem(
                        icon = Icons.Default.PowerSettingsNew,
                        title = "Enable StandBy Pro",
                        subtitle = "Enable background monitoring and ambient screen",
                        checked = settings.isEnabled,
                        onCheckedChange = { viewModel.setEnabled(it) }
                    )
                    HorizontalDivider(color = StandBySurfaceVariant, thickness = 1.dp)
                    SettingToggleItem(
                        icon = Icons.Default.BatteryChargingFull,
                        title = "Auto-Start While Charging",
                        subtitle = "Launch StandBy automatically when connected to power",
                        checked = settings.autoStartWhileCharging,
                        onCheckedChange = { viewModel.setAutoStart(it) }
                    )
                    HorizontalDivider(color = StandBySurfaceVariant, thickness = 1.dp)
                    SettingToggleItem(
                        icon = Icons.Default.ScreenRotation,
                        title = "Require Landscape",
                        subtitle = "Only trigger StandBy when placed horizontally on a stand",
                        checked = settings.requireLandscape,
                        onCheckedChange = { viewModel.setRequireLandscape(it) }
                    )
                    HorizontalDivider(color = StandBySurfaceVariant, thickness = 1.dp)
                    SettingToggleItem(
                        icon = Icons.Default.Lock,
                        title = "Only When Screen Locked",
                        subtitle = "Only activate when device screen is locked or turned off",
                        checked = settings.requireScreenLocked,
                        onCheckedChange = { viewModel.setRequireScreenLocked(it) }
                    )
                }
            }

            // Clock & Aesthetics
            item {
                SettingsCategoryTitle("CLOCK & APPEARANCE")
            }

            item {
                SettingsCard {
                    SettingToggleItem(
                        icon = Icons.Default.AccessTime,
                        title = "24-Hour Format",
                        subtitle = "Display time in 24h format (e.g. 23:45) instead of 12h",
                        checked = settings.use24Hour,
                        onCheckedChange = { viewModel.setUse24Hour(it) }
                    )
                    HorizontalDivider(color = StandBySurfaceVariant, thickness = 1.dp)
                    SettingToggleItem(
                        icon = Icons.Default.AccessTime,
                        title = "Show Seconds",
                        subtitle = "Display real-time seconds ticking",
                        checked = settings.showSeconds,
                        onCheckedChange = { viewModel.setShowSeconds(it) }
                    )
                    HorizontalDivider(color = StandBySurfaceVariant, thickness = 1.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = StandByAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Clock Style",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.White
                                )
                                Text(
                                    text = "Current: ${settings.clockStyle.name}",
                                    fontSize = 12.sp,
                                    color = StandByOnSurfaceDim
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ClockStyleButton(
                                label = "Digital",
                                isSelected = settings.clockStyle == ClockStyle.DIGITAL,
                                onClick = { viewModel.setClockStyle(ClockStyle.DIGITAL) }
                            )
                            ClockStyleButton(
                                label = "Analog",
                                isSelected = settings.clockStyle == ClockStyle.ANALOG,
                                onClick = { viewModel.setClockStyle(ClockStyle.ANALOG) }
                            )
                        }
                    }
                }
            }

            // Protection & Night Mode
            item {
                SettingsCategoryTitle("OLED & AMBIENT PROTECTION")
            }

            item {
                SettingsCard {
                    SettingToggleItem(
                        icon = Icons.Default.Bedtime,
                        title = "Night Mode (Red Glow)",
                        subtitle = "Shifts colors to warm dark red in low light using ambient light sensor",
                        checked = settings.nightModeEnabled,
                        onCheckedChange = { viewModel.setNightModeEnabled(it) }
                    )
                    HorizontalDivider(color = StandBySurfaceVariant, thickness = 1.dp)
                    SettingToggleItem(
                        icon = Icons.Default.Security,
                        title = "Burn-In Protection",
                        subtitle = "Applies micro pixel shifting to preserve OLED & AMOLED panels",
                        checked = settings.burnInProtectionEnabled,
                        onCheckedChange = { viewModel.setBurnInProtectionEnabled(it) }
                    )
                }
            }

            // Brightness Notch Section
            item {
                SettingsCategoryTitle("STANDBY BRIGHTNESS & BED-SIDE NOTCH")
            }

            item {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (settings.brightnessLevel <= 0.02f) Icons.Default.BrightnessLow else Icons.Default.BrightnessMedium,
                                    contentDescription = null,
                                    tint = StandByAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = "Bedside Brightness Notch",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (settings.brightnessLevel <= 0.02f) {
                                            "Most Dim (1% — Candlelight Bedside)"
                                        } else {
                                            "${(settings.brightnessLevel * 100).roundToInt()}% Brightness"
                                        },
                                        fontSize = 12.sp,
                                        color = if (settings.brightnessLevel <= 0.02f) StandByAccent else StandByOnSurfaceDim
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Slider(
                            value = settings.brightnessLevel,
                            onValueChange = { viewModel.setBrightnessLevel(it) },
                            valueRange = 0.01f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = StandByAccent,
                                activeTrackColor = StandByAccent,
                                inactiveTrackColor = StandBySurfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BrightnessPresetChip(
                                label = "🌙 Most Dim",
                                isSelected = settings.brightnessLevel <= 0.02f,
                                onClick = { viewModel.setBrightnessLevel(0.01f) },
                                modifier = Modifier.weight(1.3f)
                            )
                            BrightnessPresetChip(
                                label = "10%",
                                isSelected = settings.brightnessLevel in 0.08f..0.14f,
                                onClick = { viewModel.setBrightnessLevel(0.10f) },
                                modifier = Modifier.weight(1f)
                            )
                            BrightnessPresetChip(
                                label = "20%",
                                isSelected = settings.brightnessLevel in 0.18f..0.24f,
                                onClick = { viewModel.setBrightnessLevel(0.20f) },
                                modifier = Modifier.weight(1f)
                            )
                            BrightnessPresetChip(
                                label = "30%",
                                isSelected = settings.brightnessLevel in 0.28f..0.34f,
                                onClick = { viewModel.setBrightnessLevel(0.30f) },
                                modifier = Modifier.weight(1f)
                            )
                            BrightnessPresetChip(
                                label = "50%",
                                isSelected = settings.brightnessLevel in 0.45f..0.55f,
                                onClick = { viewModel.setBrightnessLevel(0.50f) },
                                modifier = Modifier.weight(1f)
                            )
                            BrightnessPresetChip(
                                label = "100%",
                                isSelected = settings.brightnessLevel >= 0.95f,
                                onClick = { viewModel.setBrightnessLevel(1.00f) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // About & Socials
            item {
                SettingsCategoryTitle("ABOUT & DEVELOPER")
            }

            item {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "StandBy Pro for Android",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Crafted by Arindam Jaiman. Designed for Nothing Phone (2a) Plus & Android Tablets.",
                            fontSize = 13.sp,
                            color = StandByOnSurfaceDim
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SocialButton(
                                label = "GitHub",
                                url = "https://github.com/ArindamJaiman",
                                context = context,
                                modifier = Modifier.weight(1f)
                            )
                            SocialButton(
                                label = "LinkedIn",
                                url = "https://www.linkedin.com/in/arindamjaiman/",
                                context = context,
                                modifier = Modifier.weight(1f)
                            )
                            SocialButton(
                                label = "Instagram",
                                url = "https://www.instagram.com/thearindamjaiman",
                                context = context,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun SettingsCategoryTitle(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = StandByAccent,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = StandBySurface)
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun SettingToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StandByAccent,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = StandByOnSurfaceDim
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Black,
                checkedTrackColor = StandByAccent,
                uncheckedThumbColor = Color.Gray,
                uncheckedTrackColor = StandBySurfaceVariant
            )
        )
    }
}

@Composable
private fun ClockStyleButton(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) StandByAccent else StandBySurfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else Color.White
        )
    }
}

@Composable
private fun SocialButton(
    label: String,
    url: String,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(StandBySurfaceVariant)
            .clickable {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = StandByAccent
        )
    }
}

@Composable
private fun BrightnessPresetChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) StandByAccent else StandBySurfaceVariant)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else Color.White
        )
    }
}
