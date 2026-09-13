package com.standbypro.ui.main

import android.content.res.Configuration
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.domain.ChargingState
import com.standbypro.settings.ClockStyle
import com.standbypro.theme.StandByAccent
import com.standbypro.theme.StandByNightRed
import com.standbypro.theme.StandByOnSurfaceDim
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.clock.AnalogClock
import com.standbypro.ui.clock.DigitalClock
import com.standbypro.ui.components.MonthCalendarWidget
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

enum class RightWidgetType {
    CALENDAR,
    CARDS
}

@Composable
fun StandByScreen(
    viewModel: StandByViewModel,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentTime by viewModel.currentTime.collectAsState()
    val chargingState by viewModel.chargingState.collectAsState()
    val isNightModeSensor by viewModel.isNightMode.collectAsState()
    val isDimmed by viewModel.isDimmed.collectAsState()
    val burnInOffset by viewModel.burnInOffset.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val nextAlarm = remember(currentTime.minute) { viewModel.getNextAlarm() }

    // Active night mode state considers setting
    val activeNightMode = settings.nightModeEnabled && isNightModeSensor

    // Local toggle for clock style (defaults to ANALOG like iOS StandBy in the reference photo)
    var activeClockStyle by remember(settings.clockStyle) { 
        mutableStateOf(if (settings.clockStyle == ClockStyle.DIGITAL) ClockStyle.ANALOG else settings.clockStyle) 
    }

    // Right widget style toggle
    var activeRightWidget by remember { mutableStateOf(RightWidgetType.CALENDAR) }

    // Colors
    val targetAccent = when {
        activeNightMode -> StandByNightRed
        isDimmed -> Color.Gray
        else -> Color(0xFFFF9500) // Apple StandBy Warm Orange accent by default
    }

    val animatedAccent by animateColorAsState(
        targetValue = targetAccent,
        animationSpec = tween(durationMillis = 1500),
        label = "AccentColor"
    )

    val dimAlpha by animateFloatAsState(
        targetValue = if (isDimmed) 0.65f else 0.0f,
        animationSpec = tween(durationMillis = 1000),
        label = "DimOverlayAlpha"
    )

    val appliedBurnInOffset = if (settings.burnInProtectionEnabled) {
        IntOffset(burnInOffset.x.roundToInt(), burnInOffset.y.roundToInt())
    } else {
        IntOffset.Zero
    }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                viewModel.reportInteraction()
            }
    ) {
        // Content container with burn-in shift applied
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { appliedBurnInOffset }
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (isLandscape) {
                // Two-Panel Landscape Layout: Left Clock, Right Calendar + Info
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Panel: Clock Face
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.reportInteraction()
                                activeClockStyle = if (activeClockStyle == ClockStyle.DIGITAL) {
                                    ClockStyle.ANALOG
                                } else {
                                    ClockStyle.DIGITAL
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ClockPanel(
                            clockStyle = activeClockStyle,
                            time = currentTime,
                            use24Hour = settings.use24Hour,
                            showSeconds = settings.showSeconds,
                            accentColor = animatedAccent,
                            isNightMode = activeNightMode
                        )
                    }

                    // Spacer between panels
                    Spacer(modifier = Modifier.width(32.dp))

                    // Right Panel: Calendar + Battery + Alarm
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.reportInteraction()
                                activeRightWidget = if (activeRightWidget == RightWidgetType.CALENDAR) {
                                    RightWidgetType.CARDS
                                } else {
                                    RightWidgetType.CALENDAR
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(targetState = activeRightWidget, label = "RightWidgetCrossfade") { widgetType ->
                            when (widgetType) {
                                RightWidgetType.CALENDAR -> {
                                    MonthCalendarWidget(
                                        time = currentTime,
                                        chargingState = chargingState,
                                        nextAlarm = nextAlarm,
                                        accentColor = animatedAccent
                                    )
                                }
                                RightWidgetType.CARDS -> {
                                    SmartWidgetsPanel(
                                        time = currentTime,
                                        chargingState = chargingState,
                                        accentColor = animatedAccent,
                                        isNightMode = activeNightMode,
                                        nextAlarm = nextAlarm
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Portrait Fallback
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                viewModel.reportInteraction()
                                activeClockStyle = if (activeClockStyle == ClockStyle.DIGITAL) {
                                    ClockStyle.ANALOG
                                } else {
                                    ClockStyle.DIGITAL
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        ClockPanel(
                            clockStyle = activeClockStyle,
                            time = currentTime,
                            use24Hour = settings.use24Hour,
                            showSeconds = settings.showSeconds,
                            accentColor = animatedAccent,
                            isNightMode = activeNightMode
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        MonthCalendarWidget(
                            time = currentTime,
                            chargingState = chargingState,
                            nextAlarm = nextAlarm,
                            accentColor = animatedAccent
                        )
                    }
                }
            }

            // Top-left Exit Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
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
                        tint = animatedAccent.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (activeNightMode) {
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

        // Auto-Dim overlay
        if (dimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(dimAlpha)
                    .background(Color.Black)
            )
        }
    }
}

@Composable
private fun ClockPanel(
    clockStyle: ClockStyle,
    time: LocalDateTime,
    use24Hour: Boolean,
    showSeconds: Boolean,
    accentColor: Color,
    isNightMode: Boolean
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (clockStyle == ClockStyle.ANALOG) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.92f)
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                AnalogClock(
                    time = time,
                    accentColor = accentColor,
                    showSeconds = showSeconds,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            DigitalClock(
                time = time,
                use24Hour = use24Hour,
                showSeconds = showSeconds,
                accentColor = accentColor,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SmartWidgetsPanel(
    time: LocalDateTime,
    chargingState: ChargingState,
    accentColor: Color,
    isNightMode: Boolean,
    nextAlarm: String?
) {
    val dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEEE")
    val fullDateFormatter = DateTimeFormatter.ofPattern("MMMM d, yyyy")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Date Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isNightMode) Color(0xFF1A0505) else Color(0xFF141414)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(30.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = time.format(dayOfWeekFormatter).uppercase(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = time.format(fullDateFormatter),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isNightMode) StandByNightRed else Color.White
                    )
                }
            }
        }

        // Battery & Power Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isNightMode) Color(0xFF1A0505) else Color(0xFF141414)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (chargingState.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(30.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "${chargingState.batteryPercent}% BATTERY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = if (chargingState.isCharging) "Charging (${chargingState.chargeType})" else "On Battery",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isNightMode) StandByNightRed else Color.White
                        )
                    }
                }

                chargingState.temperatureCelsius?.let { temp ->
                    Text(
                        text = "${temp.roundToInt()}°C",
                        fontSize = 13.sp,
                        color = StandByOnSurfaceDim
                    )
                }
            }
        }

        // Next Alarm Card
        if (nextAlarm != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNightMode) Color(0xFF1A0505) else Color(0xFF141414)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Alarm,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "NEXT ALARM",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = nextAlarm,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isNightMode) StandByNightRed else Color.White
                        )
                    }
                }
            }
        }
    }
}
