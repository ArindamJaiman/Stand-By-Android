package com.standbypro.ui.design

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.max
import kotlin.math.min

/**
 * Single-line text that scales itself to fill the space it is given, so every face looks
 * proportionally identical in a tiny gallery thumbnail and on a 12" tablet.
 *
 * @param sizingText template used for measuring (e.g. "88:88") so the size does not jump when
 *                   narrow digits appear. The real text is measured too and the wider of the two
 *                   wins, so long content (event titles) is never clipped.
 * @param suffix optional small trailing label (AM/PM, seconds) rendered at [suffixScale] of the
 *               main size, hugging the main text.
 * @param fill fraction of the available box the text may occupy.
 */
@Composable
fun AutoSizeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    brush: Brush? = null,
    sizingText: String = text,
    fill: Float = 1f,
    maxFontSize: TextUnit = 480.sp,
    alignment: Alignment = Alignment.Center,
    suffix: String? = null,
    suffixColor: Color = Color.Unspecified,
    suffixScale: Float = 0.28f
) {
    val measurer = rememberTextMeasurer(cacheSize = 16)
    fun annotated(main: String): AnnotatedString = buildAnnotatedString {
        append(main)
        if (suffix != null) {
            withStyle(SpanStyle(fontSize = suffixScale.em, color = suffixColor, fontWeight = FontWeight.SemiBold, letterSpacing = 0.06.em)) {
                append(" " + suffix)
            }
        }
    }
    BoxWithConstraints(modifier = modifier, contentAlignment = alignment) {
        val reference = 100.sp
        val measureStyle = remember(style) { style.copy(fontSize = reference, lineHeight = reference) }
        val sizingLayout = remember(sizingText, suffix, measureStyle) {
            measurer.measure(annotated(sizingText), measureStyle, maxLines = 1, softWrap = false)
        }
        val textLayout = remember(text, suffix, measureStyle) {
            measurer.measure(annotated(text), measureStyle, maxLines = 1, softWrap = false)
        }
        val measuredWidth = max(sizingLayout.size.width, textLayout.size.width)
        val measuredHeight = max(sizingLayout.size.height, textLayout.size.height)
        val widthScale = if (constraints.hasBoundedWidth && measuredWidth > 0) {
            constraints.maxWidth * fill / measuredWidth
        } else Float.MAX_VALUE
        val heightScale = if (constraints.hasBoundedHeight && measuredHeight > 0) {
            constraints.maxHeight * fill / measuredHeight
        } else Float.MAX_VALUE
        var scale = min(widthScale, heightScale)
        if (scale == Float.MAX_VALUE || scale.isNaN()) scale = 0.4f
        val size = (reference.value * scale).coerceIn(4f, maxFontSize.value).sp
        val finalStyle = if (brush != null) {
            style.copy(fontSize = size, lineHeight = size, brush = brush)
        } else {
            style.copy(fontSize = size, lineHeight = size, color = if (color == Color.Unspecified) style.color else color)
        }
        Text(
            text = annotated(text),
            style = finalStyle,
            maxLines = 1,
            softWrap = false,
            textAlign = TextAlign.Center
        )
    }
}

/** Convenience wrapper that centres a block inside the slot. */
@Composable
fun Centered(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) { content() }
}
