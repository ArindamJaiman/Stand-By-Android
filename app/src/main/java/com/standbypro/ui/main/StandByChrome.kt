package com.standbypro.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.domain.ChargingState
import com.standbypro.domain.LiveActivityState
import com.standbypro.domain.NotificationDisplayState
import com.standbypro.domain.NotificationPrivacy
import com.standbypro.settings.NightStyle
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.safeRadialGradient
import com.standbypro.ui.faces.batteryColor
import com.standbypro.ui.faces.sourceLabel
import com.standbypro.ui.faces.timeToFullLabel

/**
 * True night rendition: the whole composition is converted to luminance and re-tinted
 * (red by default) through a colour matrix, exactly like iOS StandBy's night mode.
 * [strength] animates 0..1 for a smooth transition.
 */
fun Modifier.nightFilter(style: NightStyle, strength: Float): Modifier = if (strength <= 0.001f) this else drawWithContent {
    val t = style.tint
    val dim = if (style == NightStyle.DIM) 0.55f else 0.9f
    fun mix(identity: Float, tinted: Float) = identity + (tinted - identity) * strength
    val lr = 0.299f
    val lg = 0.587f
    val lb = 0.114f
    val matrix = ColorMatrix(
        floatArrayOf(
            mix(1f, lr * t.red * dim), mix(0f, lg * t.red * dim), mix(0f, lb * t.red * dim), 0f, 0f,
            mix(0f, lr * t.green * dim), mix(1f, lg * t.green * dim), mix(0f, lb * t.green * dim), 0f, 0f,
            mix(0f, lr * t.blue * dim), mix(0f, lg * t.blue * dim), mix(1f, lb * t.blue * dim), 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        )
    )
    val paint = Paint().apply { colorFilter = ColorFilter.colorMatrix(matrix) }
    drawIntoCanvas { canvas ->
        canvas.saveLayer(Rect(Offset.Zero, size), paint)
        drawContent()
        canvas.restore()
    }
}

/** Dynamic-Island style pill for running timers and media. */
@Composable
fun LiveActivityPill(activity: LiveActivityState?, accent: Color, modifier: Modifier = Modifier) {
    AnimatedVisibility(visible = activity != null, enter = fadeIn() + scaleIn(initialScale = 0.8f), exit = fadeOut() + scaleOut(targetScale = 0.8f), modifier = modifier) {
        val a = activity ?: return@AnimatedVisibility
        Row(
            Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF0E0E10)).border(1.dp, Ink.Hairline, RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val icon = when (a) {
                is LiveActivityState.MusicActivity -> Icons.Default.MusicNote
                is LiveActivityState.EventActivity -> Icons.Default.Event
                else -> Icons.Default.Timer
            }
            val tint = (a as? LiveActivityState.EventActivity)?.colorArgb?.let { Color(it) } ?: accent
            Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
            Text(a.title, color = Ink.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(a.subtitle, color = Ink.TextSecondary, fontSize = 12.sp, maxLines = 1)
            val p = a.progress
            if (p != null) {
                Canvas(Modifier.size(16.dp)) {
                    drawArc(Color.White.copy(alpha = 0.15f), -90f, 360f, false, style = Stroke(size.minDimension * 0.18f))
                    drawArc(accent, -90f, 360f * p, false, style = Stroke(size.minDimension * 0.18f, cap = StrokeCap.Round))
                }
            }
        }
    }
}

@Composable
fun NotificationBadge(state: NotificationDisplayState, modifier: Modifier = Modifier) {
    if (state.unreadCount <= 0 || state.privacyMode == NotificationPrivacy.HIDDEN) return
    val latest = state.notifications.firstOrNull()
    Row(
        modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.08f)).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Notifications, null, tint = Ink.TextSecondary, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(6.dp))
        val label = when (state.privacyMode) {
            NotificationPrivacy.FULL -> latest?.let { "${it.appName}: ${it.title}" } ?: "${state.unreadCount}"
            NotificationPrivacy.APP_NAME_ONLY -> latest?.appName?.let { "$it · ${state.unreadCount}" } ?: "${state.unreadCount}"
            else -> "${state.unreadCount}"
        }
        Text(label, color = Ink.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/**
 * Full-screen charging moment shown when power is connected: a glowing ring sweeps up to the
 * battery level, then fades away.
 */
@Composable
fun ChargingOverlay(visible: Boolean, battery: ChargingState, onFinished: () -> Unit) {
    val sweep = remember { Animatable(0f) }
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(450), label = "chargeAlpha")
    LaunchedEffect(visible) {
        if (visible) {
            sweep.snapTo(0f)
            sweep.animateTo(battery.batteryPercent / 100f, tween(1300, easing = FastOutSlowInEasing))
            kotlinx.coroutines.delay(1400L)
            onFinished()
        }
    }
    if (alpha <= 0.01f) return
    val color = batteryColor(battery.batteryPercent)
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f * alpha)), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize(0.62f), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val d = size.minDimension * 0.86f
                val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
                val stroke = d * 0.07f
                drawCircle(safeRadialGradient(listOf(color.copy(alpha = 0.28f * alpha), Color.Transparent), center, d * 0.75f), d * 0.75f, center)
                drawArc(Color.White.copy(alpha = 0.08f * alpha), -90f, 360f, false, tl, Size(d, d), style = Stroke(stroke))
                drawArc(
                    Brush.sweepGradient(listOf(color.copy(alpha = 0.5f), color, color), center), -90f, 360f * sweep.value, false, tl, Size(d, d),
                    style = Stroke(stroke, cap = StrokeCap.Round), alpha = alpha
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AutoSizeText("⚡ ${(sweep.value * 100).toInt()}%", clockStyle(FontWeight.Light), Modifier.fillMaxWidth(0.5f).height(64.dp), color = Color.White.copy(alpha = alpha), sizingText = "⚡ 100%")
                AutoSizeText(battery.timeToFullLabel() ?: battery.sourceLabel(), labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth(0.45f).height(18.dp), color = color.copy(alpha = alpha))
            }
        }
    }
}
