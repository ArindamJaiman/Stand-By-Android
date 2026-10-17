package com.standbypro.ui.faces

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.TABULAR
import com.standbypro.ui.design.labelStyle

data class FlipTheme(
    val cardTop: Color,
    val cardBottom: Color,
    val text: Color,
    val hinge: Color = Color.Black,
    val label: Color = Ink.TextSecondary,
    val glow: Color? = null
)

private val classicFlip = FlipTheme(Color(0xFF232326), Color(0xFF18181A), Color(0xFFEDEDED))
private val darkFlip = FlipTheme(Color(0xFF0E0E0F), Color(0xFF080809), Color(0xFF8E8E93), label = Ink.TextTertiary)
private val amberFlip = FlipTheme(Color(0xFF1E1508), Color(0xFF140E05), Color(0xFFFFB340), label = Color(0xFFB37B2A), glow = Color(0xFFFF9500))
private val graphiteFlip = FlipTheme(Color(0xFF3D4046), Color(0xFF26282C), Color.White)
private val paperFlip = FlipTheme(Color(0xFFF4F2ED), Color(0xFFE4E1DA), Color(0xFF1C1C1C), hinge = Color(0xFF9E9A90), label = Color(0xFF6B675E))

/** One split-flap card that animates whenever [value] changes. */
@Composable
fun FlipCard(value: String, theme: FlipTheme, animate: Boolean, modifier: Modifier = Modifier, corner: Dp = 14.dp) {
    var shown by remember { mutableStateOf(value) }
    var previous by remember { mutableStateOf(value) }
    val progress = remember { Animatable(1f) }
    LaunchedEffect(value) {
        if (value != shown) {
            previous = shown
            shown = value
            if (animate) {
                progress.snapTo(0f)
                progress.animateTo(1f, tween(durationMillis = 680, easing = FastOutSlowInEasing))
            } else {
                progress.snapTo(1f)
            }
        }
    }
    BoxWithConstraints(modifier) {
        val full = maxHeight
        val p = progress.value
        val textStyle = TextStyle(
            fontFamily = FaceFonts.CondensedBold,
            fontWeight = FontWeight.Bold,
            fontFeatureSettings = TABULAR,
            shadow = theme.glow?.let { Shadow(it.copy(alpha = 0.7f), androidx.compose.ui.geometry.Offset.Zero, 28f) }
        )
        val sizing = if (value.length > 1) "88" else "8"
        Box(Modifier.fillMaxSize()) {
            FlipHalf(shown, true, full, theme, textStyle, sizing, corner, Modifier.align(Alignment.TopCenter))
            FlipHalf(if (p < 1f) previous else shown, false, full, theme, textStyle, sizing, corner, Modifier.align(Alignment.BottomCenter))
            if (p < 0.5f) {
                val t = p / 0.5f
                FlipHalf(previous, true, full, theme, textStyle, sizing, corner,
                    Modifier.align(Alignment.TopCenter).graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 1f)
                        rotationX = -90f * t
                        cameraDistance = 14f * density
                    }, shade = t * 0.5f)
            } else if (p < 1f) {
                val t = (p - 0.5f) / 0.5f
                FlipHalf(shown, false, full, theme, textStyle, sizing, corner,
                    Modifier.align(Alignment.BottomCenter).graphicsLayer {
                        transformOrigin = TransformOrigin(0.5f, 0f)
                        rotationX = 90f * (1f - t)
                        cameraDistance = 14f * density
                    }, shade = (1f - t) * 0.5f)
            }
            // Hinge
            Box(Modifier.align(Alignment.Center).fillMaxWidth().height(2.dp).background(theme.hinge.copy(alpha = 0.85f)))
        }
    }
}

@Composable
private fun FlipHalf(
    value: String,
    top: Boolean,
    full: Dp,
    theme: FlipTheme,
    style: TextStyle,
    sizing: String,
    corner: Dp,
    modifier: Modifier,
    shade: Float = 0f
) {
    val shape = if (top) RoundedCornerShape(topStart = corner, topEnd = corner) else RoundedCornerShape(bottomStart = corner, bottomEnd = corner)
    Box(
        modifier.fillMaxWidth().height(full / 2).clip(shape)
            .background(Brush.verticalGradient(if (top) listOf(theme.cardTop, theme.cardBottom) else listOf(theme.cardBottom, theme.cardTop.copy(alpha = 1f))))
    ) {
        Box(
            Modifier.fillMaxWidth()
                .wrapContentHeight(align = if (top) Alignment.Top else Alignment.Bottom, unbounded = true)
                .height(full),
            contentAlignment = Alignment.Center
        ) {
            AutoSizeText(value, style, Modifier.fillMaxSize().padding(horizontal = 6.dp), color = theme.text, sizingText = sizing, fill = 0.92f)
        }
        if (shade > 0f) Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = shade)))
    }
}

@Composable
private fun FlipClock(ctx: FaceContext, theme: FlipTheme, modifier: Modifier, withSeconds: Boolean = false) {
    Column(modifier.fillMaxSize().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            Modifier.fillMaxWidth().weight(0.82f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val cardMod = Modifier.weight(1f).fillMaxHeight(0.86f)
            Box(cardMod) {
                FlipCard(ctx.displayHour.padStart(2, '0'), theme, ctx.animate, Modifier.fillMaxSize())
                if (!ctx.use24Hour) {
                    AutoSizeText(ctx.amPm, labelStyle(FontWeight.Bold), Modifier.align(Alignment.TopStart).padding(8.dp).fillMaxWidth(0.22f).fillMaxHeight(0.1f), color = theme.label)
                }
            }
            FlipCard(ctx.minuteText, theme, ctx.animate, cardMod)
            if (withSeconds) FlipCard(ctx.secondText, theme, ctx.animate, Modifier.weight(0.7f).fillMaxHeight(0.6f))
        }
        AutoSizeText(
            ctx.formatDate("EEEE · d MMMM").uppercase(),
            labelStyle(FontWeight.Medium),
            Modifier.fillMaxWidth(0.7f).weight(0.1f),
            color = theme.label
        )
    }
}

@Composable
fun FlipClassicFace(ctx: FaceContext, modifier: Modifier = Modifier) = FlipClock(ctx, classicFlip, modifier)

@Composable
fun FlipDarkFace(ctx: FaceContext, modifier: Modifier = Modifier) = FlipClock(ctx, darkFlip, modifier)

@Composable
fun FlipAmberFace(ctx: FaceContext, modifier: Modifier = Modifier) = FlipClock(ctx, amberFlip, modifier)

@Composable
fun FlipGraphiteFace(ctx: FaceContext, modifier: Modifier = Modifier) = FlipClock(ctx, graphiteFlip, modifier)

@Composable
fun FlipPaperFace(ctx: FaceContext, modifier: Modifier = Modifier) = FlipClock(ctx, paperFlip, modifier)

@Composable
fun FlipSecondsFace(ctx: FaceContext, modifier: Modifier = Modifier) =
    FlipClock(ctx, classicFlip.copy(text = Color.White, label = ctx.accent), modifier, withSeconds = true)
