package com.standbypro.ui.main

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WbTwilight
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.audio.SleepSound
import com.standbypro.audio.SoundState
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.design.safeRadialGradient
import kotlinx.coroutines.delay

private val SLEEP_TIMER_OPTIONS = listOf(0, 15, 30, 45, 60, 90)

/**
 * Bedtime sheet on the StandBy display: sound machine, sleep timer and the night light.
 */
@Composable
fun SleepPanel(
    ctx: FaceContext,
    sound: SoundState,
    sunriseMinutes: Int,
    nowMillis: Long,
    onSound: (SleepSound?) -> Unit,
    onVolume: (Float) -> Unit,
    onSleepTimer: (Int) -> Unit,
    onNightLight: () -> Unit,
    onClose: () -> Unit
) {
    val accent = ctx.accent
    Column(
        Modifier.fillMaxWidth()
            .background(Brush.verticalGradient(0f to Color.Transparent, 0.12f to Color.Black.copy(alpha = 0.94f), 1f to Color.Black))
            .padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("SLEEP", color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.4.sp)
            Spacer(Modifier.width(10.dp))
            val remaining = sound.sleepTimerRemainingMinutes(nowMillis)
            val status = buildList {
                sound.sound?.let { add("${it.label} playing") }
                remaining?.let { add("fades out in $it min") }
                if (sunriseMinutes > 0 && ctx.nextAlarmTime != null) add("sunrise light ${sunriseMinutes} min before ${ctx.nextAlarmTime}")
            }.joinToString("  ·  ")
            Text(status, color = Ink.TextSecondary, fontSize = 12.sp, maxLines = 1, modifier = Modifier.weight(1f))
            Box(Modifier.size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.10f)).clickable(onClick = onClose), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Close, "Close", tint = Ink.TextPrimary, modifier = Modifier.size(18.dp))
            }
        }

        // Sounds
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PanelChip("Off", sound.sound == null, accent) { onSound(null) }
            SleepSound.entries.forEach { s -> PanelChip(s.label, sound.sound == s, accent) { onSound(s) } }
        }
        if (sound.isPlaying) {
            var local by remember(sound.volume) { mutableFloatStateOf(sound.volume) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.VolumeDown, null, tint = Ink.TextSecondary, modifier = Modifier.size(18.dp))
                Slider(
                    value = local,
                    onValueChange = { local = it; onVolume(it) },
                    modifier = Modifier.weight(1f).height(30.dp),
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = accent, inactiveTrackColor = Color.White.copy(alpha = 0.2f))
                )
                Icon(Icons.Default.VolumeUp, null, tint = Ink.TextSecondary, modifier = Modifier.size(18.dp))
            }
        }

        // Sleep timer + night light
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Snooze, null, tint = Ink.TextSecondary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val active = if (sound.sleepTimerEndsAt != null) sound.sleepTimerMinutes else 0
                SLEEP_TIMER_OPTIONS.forEach { m ->
                    PanelChip(if (m == 0) "No timer" else if (m < 60) "$m min" else "${m / 60}h${if (m % 60 > 0) " ${m % 60}" else ""}", m == active, accent) { onSleepTimer(m) }
                }
            }
            Spacer(Modifier.width(12.dp))
            Row(
                Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFFFB45C).copy(alpha = 0.18f))
                    .border(1.dp, Color(0xFFFFB45C).copy(alpha = 0.6f), RoundedCornerShape(50))
                    .clickable(onClick = onNightLight).padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFFB45C), modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Night light", color = Color(0xFFFFB45C), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
        Text("The sleep timer fades sounds out and pauses your music app.", color = Ink.TextTertiary, fontSize = 11.sp)
    }
}

@Composable
private fun PanelChip(text: String, selected: Boolean, accent: Color, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(50))
            .background(if (selected) accent.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f))
            .border(1.dp, if (selected) accent.copy(alpha = 0.7f) else Color.Transparent, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(text, color = if (selected) accent else Ink.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/**
 * Full-screen reading/night light. Drag up and down for brightness, sideways from amber to soft
 * white, tap to turn it off. [level] drives the window brightness.
 */
@Composable
fun NightLightOverlay(level: Float, warmth: Float, onChange: (level: Float, warmth: Float) -> Unit, onDismiss: () -> Unit) {
    var hintVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(3500L)
        hintVisible = false
    }
    val hintAlpha by animateFloatAsState(if (hintVisible) 1f else 0f, tween(800), label = "hint")
    var l by remember { mutableFloatStateOf(level) }
    var w by remember { mutableFloatStateOf(warmth) }
    val color = lerpColor(Color(0xFFFF8A2B), Color(0xFFFFF3E2), w)
    Box(
        Modifier.fillMaxSize()
            .background(Brush.radialGradient(listOf(color, lerpColor(color, Color.Black, 0.18f))))
            .pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) }
            .pointerInput(Unit) {
                detectDragGestures { change, drag ->
                    change.consume()
                    l = (l - drag.y / size.height).coerceIn(0.05f, 1f)
                    w = (w + drag.x / size.width).coerceIn(0f, 1f)
                    onChange(l, w)
                }
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        if (hintAlpha > 0.01f) {
            Text(
                "Tap to turn off  ·  drag up/down for brightness  ·  sideways for warmth",
                color = Color(0xFF4A2A0A).copy(alpha = 0.8f * hintAlpha), fontSize = 13.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
    }
}

/**
 * Sunrise wake light: before the alarm the screen glows from deep red to warm daylight with a sun
 * rising from the bottom edge. Drawn outside the night filter so the colours stay true.
 */
@Composable
fun SunriseOverlay(progress: Float, ctx: FaceContext, onDismiss: () -> Unit) {
    val p by animateFloatAsState(progress.coerceIn(0f, 1f), tween(1200), label = "sunrise")
    if (p <= 0.001f) return
    // White with a soft shadow on the dark sky, then a quick switch to deep brown once it's bright.
    val darkText = ((p - 0.55f) / 0.1f).coerceIn(0f, 1f)
    val textColor = lerpColor(Color.White, Color(0xFF3A1A05), darkText)
    val shadow = Shadow(Color.Black.copy(alpha = 0.35f * (1f - darkText)), Offset(0f, 2f), 12f)
    Box(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures(onTap = { onDismiss() }) }) {
        Canvas(Modifier.fillMaxSize().clipToBounds()) {
            val top = lerpColor(Color(0xFF12030A), Color(0xFFFFD9A8), p)
            val mid = lerpColor(Color(0xFF4A0A06), Color(0xFFFFB774), p)
            val bottom = lerpColor(Color(0xFFA81E08), Color(0xFFFFF1DA), p)
            // Opaque: the overlay carries its own clock, so the face underneath must not show through.
            drawRect(Brush.verticalGradient(listOf(top, mid, bottom)))
            val sunY = size.height * (1.3f - 0.42f * p)
            val sunR = size.minDimension * 0.32f
            val sunCenter = Offset(size.width / 2f, sunY)
            drawCircle(safeRadialGradient(listOf(Color(0xFFFFF4D6).copy(alpha = 0.9f * p), Color(0xFFFFB25A).copy(alpha = 0.5f * p), Color.Transparent), sunCenter, sunR * 2.2f), sunR * 2.2f, sunCenter)
            drawCircle(Color(0xFFFFF7E6).copy(alpha = 0.55f + 0.4f * p), sunR * 0.55f, sunCenter)
        }
        Column(Modifier.fillMaxSize().padding(horizontal = 40.dp, vertical = 30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.weight(0.15f))
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light).copy(shadow = shadow), Modifier.fillMaxWidth(0.5f).weight(0.35f), color = textColor, sizingText = "88:88")
            AutoSizeText(
                ctx.nextAlarmTime?.let { "Good morning  ·  alarm $it" } ?: "Good morning",
                labelStyle(FontWeight.SemiBold).copy(shadow = shadow), Modifier.fillMaxWidth(0.5f).weight(0.07f), color = textColor.copy(alpha = 0.85f)
            )
            Spacer(Modifier.weight(0.43f))
        }
        Row(Modifier.align(Alignment.TopEnd).padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.WbTwilight, null, tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Sunrise · tap to dismiss", color = textColor.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}
