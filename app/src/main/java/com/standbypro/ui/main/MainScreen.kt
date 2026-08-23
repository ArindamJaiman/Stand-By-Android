package com.standbypro.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.standbypro.clock.ClockEngine
import com.standbypro.domain.BatteryHealth
import com.standbypro.domain.ChargeType
import com.standbypro.domain.ChargingState
import com.standbypro.hardware.AmbientLightMonitor
import com.standbypro.power.AutoDimController
import com.standbypro.power.BurnInProtectionController
import com.standbypro.theme.StandByProTheme
import com.standbypro.ui.clock.DigitalClock
import com.standbypro.ui.components.BatteryIndicator
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }
    var burnInOffset by remember { mutableStateOf(Offset.Zero) }
    var isNightMode by remember { mutableStateOf(false) }
    var isDimmed by remember { mutableStateOf(false) }
    
    // Monitors
    val ambientLightMonitor = remember { AmbientLightMonitor(context) }
    
    // In a real implementation this would come from a ViewModel observing ChargingStateMonitor
    val chargingState = remember { 
        ChargingState(isCharging = true, batteryPercent = 82, chargeType = ChargeType.USB, health = BatteryHealth.GOOD) 
    }

    LaunchedEffect(Unit) {
        ClockEngine.timeFlow().collect { time ->
            currentTime = time
        }
    }
    
    LaunchedEffect(Unit) {
        BurnInProtectionController.burnInOffset.collect { offset ->
            burnInOffset = offset
        }
    }
    
    LaunchedEffect(Unit) {
        ambientLightMonitor.isNightMode.collect { night ->
            isNightMode = night
        }
    }
    
    LaunchedEffect(Unit) {
        AutoDimController.dimStateMonitor.collect { dimmed ->
            isDimmed = dimmed
        }
    }

    val targetColor = when {
        isNightMode -> Color(0xFFE53935) // Deep Red for Night Mode
        isDimmed -> Color.Gray
        else -> MaterialTheme.colorScheme.primary
    }
    
    val animatedAccentColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 2000),
        label = "AccentColorAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                AutoDimController.reportInteraction()
            }
    ) {
        // Apply burn in protection offset to the main content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(burnInOffset.x.roundToInt(), burnInOffset.y.roundToInt()) }
        ) {
            DigitalClock(
                time = currentTime,
                use24Hour = false,
                showSeconds = false,
                accentColor = animatedAccentColor,
                modifier = Modifier.align(Alignment.Center)
            )
            
            BatteryIndicator(
                chargingState = chargingState,
                accentColor = animatedAccentColor,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
        
        // Dimming overlay
        if (isDimmed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun StandByScreenPreview() {
    StandByProTheme {
        MainScreen()
    }
}

