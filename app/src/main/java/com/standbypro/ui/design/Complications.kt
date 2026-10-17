package com.standbypro.ui.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Premium card surface used by widgets and modular faces: soft gradient + hairline. */
fun Modifier.premiumCard(
    corner: Dp = 26.dp,
    tint: Color = Color.Transparent,
    fill: Color = Ink.Card
): Modifier = this
    .clip(RoundedCornerShape(corner))
    .background(
        Brush.verticalGradient(
            listOf(
                lerpColor(fill, tint, if (tint == Color.Transparent) 0f else 0.10f).copy(alpha = 1f),
                lerpColor(fill, Color.Black, 0.35f)
            )
        )
    )
    .border(1.dp, Brush.verticalGradient(listOf(Ink.HairlineStrong, Ink.Hairline.copy(alpha = 0.04f))), RoundedCornerShape(corner))

/** Card with a header label and free content. */
@Composable
fun ComplicationCard(
    title: String,
    accent: Color,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier.premiumCard().padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        AutoSizeText(
            title.uppercase(),
            labelStyle(FontWeight.Bold),
            Modifier.fillMaxWidth().weight(0.16f),
            color = accent,
            sizingText = "UPCOMING EVENTS",
            alignment = Alignment.CenterStart
        )
        Column(Modifier.fillMaxWidth().weight(0.84f)) { content() }
    }
}

/** Circular gauge (Apple-style) with centred content. */
@Composable
fun RingGauge(
    fraction: Float,
    color: Color,
    modifier: Modifier = Modifier,
    track: Color = color.copy(alpha = 0.18f),
    strokeFraction: Float = 0.12f,
    startAngle: Float = 135f,
    sweep: Float = 270f,
    gradient: List<Color>? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = size.minDimension * strokeFraction
            val d = size.minDimension - stroke
            val tl = Offset((size.width - d) / 2f, (size.height - d) / 2f)
            drawArc(track, startAngle, sweep, false, tl, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round))
            val s = sweep * fraction.coerceIn(0f, 1f)
            if (s > 0.5f) {
                if (gradient != null) {
                    drawArc(Brush.sweepGradient(gradient, Offset(size.width / 2f, size.height / 2f)), startAngle, s, false, tl, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round))
                } else {
                    drawArc(color, startAngle, s, false, tl, Size(d, d), style = Stroke(stroke, cap = StrokeCap.Round))
                }
            }
        }
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val side = if (maxWidth < maxHeight) maxWidth else maxHeight
            Box(Modifier.size(side * 0.6f), contentAlignment = Alignment.Center, content = content)
        }
    }
}

/** Two-line metric: small caption over a big value. */
@Composable
fun Metric(caption: String, value: String, accent: Color, modifier: Modifier = Modifier, valueColor: Color = Ink.TextPrimary) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        AutoSizeText(caption.uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().weight(0.35f), color = accent, sizingText = "SUNRISE")
        AutoSizeText(value, clockStyle(FontWeight.Medium), Modifier.fillMaxWidth().weight(0.65f), color = valueColor, sizingText = "88:88 PM")
    }
}
