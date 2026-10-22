package com.standbypro.ui.main

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.alarm.RingState
import com.standbypro.domain.FaceContext
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.safeRadialGradient
import com.standbypro.ui.faces.sizingTime

/**
 * Full-screen alarm: a warm pulsing glow, the time, and two large targets — Snooze and Stop —
 * that are easy to hit half asleep. With wave to wake on, a wave over the phone snoozes.
 */
@Composable
fun AlarmRingingScreen(viewModel: StandByViewModel, ring: RingState?) {
    val ctx by viewModel.faceContext.collectAsState()
    val settings by viewModel.settings.collectAsState()
    // Keep showing the last alarm while the screen fades out after Stop.
    var shown by remember { mutableStateOf(ring) }
    LaunchedEffect(ring) { if (ring != null) shown = ring }
    val state = ring ?: shown ?: return

    LaunchedEffect(settings.waveToWake, ring != null) {
        if (settings.waveToWake && ring != null) viewModel.waves.collect { viewModel.snoozeAlarm() }
    }
    AlarmRingingContent(ctx, state, settings.waveToWake, onSnooze = { viewModel.snoozeAlarm() }, onStop = { viewModel.dismissAlarm() })
}

@Composable
fun AlarmRingingContent(ctx: FaceContext, state: RingState, waveHint: Boolean, onSnooze: () -> Unit, onStop: () -> Unit) {
    val pulse = rememberInfiniteTransition(label = "alarm")
    val wave by pulse.animateFloat(0f, 1f, infiniteRepeatable(tween(2200, easing = LinearEasing), RepeatMode.Restart), label = "wave")
    val glow by pulse.animateFloat(0.75f, 1f, infiniteRepeatable(tween(1600), RepeatMode.Reverse), label = "glow")
    val warm = Color(0xFFFF9F45)

    Box(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(Color(0xFF1A0B05), Color(0xFF2B1206), Color(0xFF0B0503))))
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val c = center.copy(y = size.height * 0.42f)
            val r = size.minDimension * 0.6f
            drawCircle(safeRadialGradient(listOf(warm.copy(alpha = 0.35f * glow), Color.Transparent), c, r), r, c)
            for (k in 0 until 3) {
                val p = (wave + k / 3f) % 1f
                drawCircle(warm.copy(alpha = 0.35f * (1f - p)), size.minDimension * (0.12f + 0.38f * p), c, style = Stroke(size.minDimension * 0.006f))
            }
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(0.08f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Alarm, null, tint = warm, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(8.dp))
                Text(state.label.uppercase(), color = warm, fontSize = 14.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            }
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.fillMaxWidth(0.6f).weight(0.42f), color = Color.White, sizingText = ctx.sizingTime())
            AutoSizeText(
                if (waveHint) "Wave over the phone to snooze" else "Good morning",
                labelStyle(), Modifier.fillMaxWidth(0.5f).weight(0.06f), color = Color.White.copy(alpha = 0.6f)
            )
            Spacer(Modifier.weight(0.08f))
            Row(Modifier.widthIn(max = 640.dp).fillMaxWidth().weight(0.2f), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    Modifier.weight(1.4f).fillMaxHeight().clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.14f))
                        .clickable(onClick = onSnooze),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Snooze, null, tint = Color.White, modifier = Modifier.size(26.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("Snooze · ${state.snoozeMinutes} min", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                }
                Box(
                    Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(50)).background(warm).clickable(onClick = onStop),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Stop", color = Color.Black, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.weight(0.06f))
        }
    }
}
