package com.standbypro.ui.faces

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.WeatherGlyph
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.rememberDayClock
import com.standbypro.ui.design.safeRadialGradient
import kotlin.math.sin

/**
 * Slideshow of the user's chosen photos with a slow Ken Burns drift and cross-fades.
 * Falls back to generative art with a hint when no photos are selected.
 */
@Composable
fun PhotoSlideshow(ctx: FaceContext, modifier: Modifier = Modifier, corner: Int = 26) {
    val interval = ctx.photoIntervalSeconds.coerceAtLeast(5)
    val photos = ctx.photos
    Box(modifier.clip(RoundedCornerShape(corner.dp)).background(Color(0xFF0B0B0D))) {
        if (photos.isEmpty()) {
            AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 66L) { t ->
                val p = t / 40f
                drawRect(Brush.linearGradient(listOf(Color(0xFF243B55), Color(0xFF141E30)), Offset.Zero, Offset(size.width, size.height)))
                fun blob(x: Float, y: Float, r: Float, c: Color) = drawCircle(safeRadialGradient(listOf(c, Color.Transparent), Offset(x, y), r), r, Offset(x, y))
                blob(size.width * (0.3f + 0.1f * sin(p)), size.height * 0.3f, size.minDimension * 0.7f, ctx.accent.copy(alpha = 0.45f))
                blob(size.width * 0.75f, size.height * (0.7f + 0.1f * sin(p * 1.3f)), size.minDimension * 0.6f, Color(0xFFBF5AF2).copy(alpha = 0.35f))
            }
            AutoSizeText("Add photos in Studio → Data", labelStyle(FontWeight.SemiBold), Modifier.align(Alignment.BottomEnd).padding(14.dp).fillMaxWidth(0.4f).height(14.dp), color = Color.White.copy(alpha = 0.6f))
        } else {
            val secondOfDay = ctx.time.toLocalTime().toSecondOfDay()
            val index = (secondOfDay / interval) % photos.size
            val clock = rememberDayClock(com.standbypro.ui.design.SecondsMotion.SWEEP, ctx.animate, 50L)
            Crossfade(targetState = photos[index], animationSpec = tween(1400), label = "photo") { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().graphicsLayer {
                        val f = ((clock.value % interval) / interval)
                        val s = 1.06f + 0.08f * f
                        scaleX = s
                        scaleY = s
                        translationX = (if (index % 2 == 0) -1f else 1f) * size.width * 0.03f * f
                        translationY = size.height * 0.02f * f
                    }
                )
            }
        }
    }
}

private val textShadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(0f, 3f), 14f)

@Composable
fun PhotoFrameFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(6.dp)) {
        PhotoSlideshow(ctx, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, Color.Black.copy(alpha = 0.65f)))))
        Column(Modifier.align(Alignment.BottomStart).padding(18.dp).fillMaxWidth(0.55f).fillMaxHeight(0.42f), verticalArrangement = Arrangement.Bottom) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light).copy(shadow = textShadow), Modifier.fillMaxWidth().weight(0.7f), color = Color.White, sizingText = ctx.sizingTime(), alignment = Alignment.BottomStart)
            AutoSizeText(ctx.formatDate("EEEE, MMMM d"), labelStyle(FontWeight.SemiBold).copy(shadow = textShadow), Modifier.fillMaxWidth().weight(0.18f), color = Color.White.copy(alpha = 0.9f), alignment = Alignment.CenterStart)
        }
    }
}

@Composable
fun PhotoClockFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(6.dp)) {
        PhotoSlideshow(ctx, Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent, Color.Transparent))))
        Column(Modifier.align(Alignment.TopCenter).padding(top = 14.dp).fillMaxWidth(0.7f).fillMaxHeight(0.42f), horizontalAlignment = Alignment.CenterHorizontally) {
            AutoSizeText(ctx.formatDate("EEEE d MMMM").uppercase(), labelStyle(FontWeight.Bold).copy(shadow = textShadow), Modifier.fillMaxWidth(0.6f).weight(0.15f), color = Color.White.copy(alpha = 0.9f))
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.SemiBold, letterSpacingEm = -0.04f).copy(shadow = textShadow), Modifier.fillMaxWidth().weight(0.85f), color = Color.White, sizingText = ctx.sizingTime())
        }
    }
}

@Composable
fun PhotoWeatherFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(6.dp)) {
        PhotoSlideshow(ctx, Modifier.fillMaxSize())
        Row(
            Modifier.align(Alignment.BottomStart).padding(14.dp).fillMaxWidth(0.62f).fillMaxHeight(0.26f)
                .clip(RoundedCornerShape(22.dp)).background(Color.Black.copy(alpha = 0.38f)).padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Medium), Modifier.weight(1f).fillMaxHeight(), color = Color.White, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            ctx.weather?.let { w ->
                WeatherGlyph(w.condition, Modifier.fillMaxHeight(0.75f).width(40.dp))
                AutoSizeText(ctx.temperature(w.temperatureCelsius), clockStyle(FontWeight.Light), Modifier.weight(0.6f).fillMaxHeight(0.7f), color = Color.White, sizingText = "-88°")
            }
        }
    }
}

@Composable
fun PhotoCalendarFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxSize().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        PhotoSlideshow(ctx, Modifier.weight(1.3f).fillMaxHeight())
        Column(Modifier.weight(0.7f).fillMaxHeight().padding(vertical = 8.dp), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.formatDate("EEEE").uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.08f), color = Ink.Red, alignment = Alignment.CenterStart)
            AutoSizeText(ctx.time.dayOfMonth.toString(), clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.26f), color = Ink.TextPrimary, sizingText = "88", alignment = Alignment.CenterStart)
            AutoSizeText(ctx.timeText, clockStyle(FontWeight.Thin), Modifier.fillMaxWidth().weight(0.16f), color = Ink.TextSecondary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
            Spacer(Modifier.weight(0.04f))
            ctx.events.take(3).forEach { ev ->
                Row(Modifier.fillMaxWidth().weight(0.12f), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(3.dp).fillMaxHeight(0.7f).background(ev.colorArgb?.let { Color(it) } ?: ctx.accent))
                    Column(Modifier.padding(start = 6.dp).fillMaxHeight()) {
                        AutoSizeText(ev.title, labelStyle(FontWeight.SemiBold).copy(letterSpacing = androidx.compose.ui.text.TextStyle.Default.letterSpacing), Modifier.fillMaxWidth().weight(0.55f), color = Ink.TextPrimary, sizingText = "Weekly planning meeting", alignment = Alignment.CenterStart)
                        AutoSizeText(ev.timeRange, labelStyle(), Modifier.fillMaxWidth().weight(0.45f), color = Ink.TextTertiary, sizingText = "Tomorrow · 10:30 AM – 11:30 AM", alignment = Alignment.CenterStart)
                    }
                }
            }
            if (ctx.events.isEmpty()) AutoSizeText("No upcoming events", labelStyle(), Modifier.fillMaxWidth().weight(0.08f), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
        }
    }
}

@Composable
fun PhotoSplitFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxSize().padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
        PhotoSlideshow(ctx, Modifier.weight(1f).fillMaxHeight(), corner = 30)
        Box(Modifier.weight(1f).fillMaxHeight()) {
            StackedTime(ctx, clockStyle(FontWeight.Thin), Modifier.fillMaxSize(), timeWeight = 0.55f)
        }
    }
}
