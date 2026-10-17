package com.standbypro.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.TABULAR
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.design.safeRadialGradient
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------------------------------------------------------------------------------------------
// Shared helpers
// ---------------------------------------------------------------------------------------------

internal fun FaceContext.formatDate(pattern: String): String =
    time.format(DateTimeFormatter.ofPattern(pattern, Locale.getDefault()))

internal fun FaceContext.sizingTime(): String = if (use24Hour || hour12 >= 10) "88:88" else "8:88"

internal fun ordinal(n: Int): String {
    val suffix = if (n % 100 in 11..13) "th" else when (n % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
    return "$n$suffix"
}

/** Time + optional date stacked with golden-ratio proportions. */
@Composable
internal fun StackedTime(
    ctx: FaceContext,
    timeStyle: TextStyle,
    modifier: Modifier = Modifier,
    timeColor: Color = Ink.TextPrimary,
    timeBrush: Brush? = null,
    date: String? = ctx.formatDate("EEEE, MMMM d"),
    dateColor: Color = Ink.TextSecondary,
    dateStyle: TextStyle = labelStyle(FontWeight.Medium),
    timeWeight: Float = 0.5f,
    meridiem: Boolean = !ctx.use24Hour
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(0.2f))
        AutoSizeText(
            text = ctx.timeText,
            style = timeStyle,
            color = timeColor,
            brush = timeBrush,
            sizingText = ctx.sizingTime(),
            modifier = Modifier.fillMaxWidth().weight(timeWeight),
            suffix = if (meridiem) ctx.amPm else null,
            suffixColor = ctx.accent
        )
        if (date != null) {
            AutoSizeText(
                text = date.uppercase(Locale.getDefault()),
                style = dateStyle,
                color = dateColor,
                sizingText = "WEDNESDAY, SEPTEMBER 28",
                modifier = Modifier.fillMaxWidth(0.82f).weight(0.08f)
            )
        }
        Spacer(Modifier.weight(0.2f))
    }
}

// ---------------------------------------------------------------------------------------------
// Faces
// ---------------------------------------------------------------------------------------------

@Composable
fun DigitalMinimalFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    StackedTime(ctx, clockStyle(FontWeight.Thin, letterSpacingEm = -0.03f), modifier)
}

@Composable
fun DigitalBoldFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        val style = clockStyle(FontWeight.Black, letterSpacingEm = -0.05f)
        AutoSizeText(ctx.displayHour.padStart(2, '0'), style, Modifier.fillMaxWidth().weight(1f), color = Ink.TextPrimary, sizingText = "88")
        AutoSizeText(ctx.minuteText, style, Modifier.fillMaxWidth().weight(1f), color = ctx.accent, sizingText = "88")
    }
}

@Composable
fun DigitalOledFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val clock = rememberFaceClock(ctx)
    Column(modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.weight(0.22f))
        AutoSizeText(
            ctx.timeText, clockStyle(FontWeight.ExtraLight), Modifier.fillMaxWidth().weight(0.46f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(),
            suffix = if (ctx.showSeconds) ctx.secondText else null, suffixColor = ctx.accent, suffixScale = 0.34f
        )
        Canvas(Modifier.fillMaxWidth(0.7f).weight(0.03f).graphicsLayer()) {
            val s = clock.value % 60f
            val y = size.height / 2f
            drawLine(Color.White.copy(alpha = 0.1f), Offset(0f, y), Offset(size.width, y), size.height * 0.5f, StrokeCap.Round)
            drawLine(ctx.accent, Offset(0f, y), Offset(size.width * (s / 60f), y), size.height * 0.5f, StrokeCap.Round)
        }
        AutoSizeText(ctx.formatDate("EEE d MMM").uppercase(), labelStyle(), Modifier.fillMaxWidth(0.5f).weight(0.07f), color = Ink.TextTertiary)
        Spacer(Modifier.weight(0.22f))
    }
}

/** iOS StandBy "Float": chunky overlapping numerals in soft gradients that shift with the hour. */
@Composable
fun DigitalFloatFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer(cacheSize = 24)
    val digits = (ctx.displayHour.padStart(2, '0') + ctx.minuteText)
    val palette = floatPalette(ctx.time.hour, ctx.accent)
    Canvas(modifier.fillMaxSize().padding(10.dp)) {
        val style = TextStyle(
            fontFamily = FaceFonts.Sans,
            fontWeight = FontWeight.Black,
            fontFeatureSettings = TABULAR,
            fontSize = (size.height * 0.72f).toSp().coerceAtMostSp((size.width * 0.42f).toSp().value)
        )
        val layouts = digits.map { measurer.measure(it.toString(), style) }
        val overlap = 0.16f
        val total = layouts.sumOf { it.size.width.toDouble() }.toFloat() * (1f - overlap) + layouts.last().size.width * overlap
        var x = (size.width - total) / 2f
        layouts.forEachIndexed { i, l ->
            val top = (size.height - l.size.height) / 2f + if (i % 2 == 0) -size.height * 0.03f else size.height * 0.03f
            val (c1, c2) = palette[i]
            drawText(l, brush = Brush.verticalGradient(listOf(c1, c2), startY = top, endY = top + l.size.height), topLeft = Offset(x, top))
            x += l.size.width * (1f - overlap)
        }
    }
}

private fun androidx.compose.ui.unit.TextUnit.coerceAtMostSp(max: Float): androidx.compose.ui.unit.TextUnit =
    if (value > max) androidx.compose.ui.unit.TextUnit(max, androidx.compose.ui.unit.TextUnitType.Sp) else this

private fun floatPalette(hour: Int, accent: Color): List<Pair<Color, Color>> {
    val base = when (hour) {
        in 5..10 -> listOf(Color(0xFFFFB86B), Color(0xFFFF6B8B), Color(0xFFFFD36B), Color(0xFFFF8F5A))
        in 11..16 -> listOf(Color(0xFF6BE3FF), Color(0xFF6B8BFF), Color(0xFF7CFFB2), Color(0xFF5AC8FA))
        in 17..20 -> listOf(Color(0xFFFF7A59), Color(0xFFB66BFF), Color(0xFFFF5AA8), Color(0xFFFFB347))
        else -> listOf(Color(0xFF8E7CFF), Color(0xFF5E5CE6), Color(0xFFBF5AF2), Color(0xFF64D2FF))
    }
    return base.mapIndexed { i, c -> lerpColor(c, accent, if (i == 0) 0.25f else 0.1f) to lerpColor(c, Color.Black, 0.35f) }
}

@Composable
fun DigitalVerticalFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxSize().padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
            val style = clockStyle(FontWeight.Medium, letterSpacingEm = -0.04f)
            AutoSizeText(ctx.displayHour.padStart(2, '0'), style, Modifier.fillMaxWidth().weight(1f), color = Ink.TextPrimary, sizingText = "88")
            AutoSizeText(ctx.minuteText, style, Modifier.fillMaxWidth().weight(1f), color = ctx.accent, sizingText = "88")
        }
        Column(Modifier.weight(0.42f).fillMaxHeight().padding(start = 8.dp), verticalArrangement = Arrangement.Center) {
            AutoSizeText(ctx.formatDate("EEE").uppercase(), labelStyle(FontWeight.Bold), Modifier.fillMaxWidth().fillMaxHeight(0.12f), color = ctx.accent, alignment = Alignment.CenterStart)
            AutoSizeText(ctx.time.dayOfMonth.toString(), clockStyle(FontWeight.Light), Modifier.fillMaxWidth().fillMaxHeight(0.3f), color = Ink.TextPrimary, sizingText = "88", alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("MMMM").uppercase(), labelStyle(), Modifier.fillMaxWidth().fillMaxHeight(0.12f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
        }
    }
}

@Composable
fun DigitalNeonFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val glow = ctx.accent
        val base = clockStyle(FontWeight.Light, letterSpacingEm = 0.02f)
        AutoSizeText(
            ctx.timeText,
            base.copy(shadow = Shadow(glow, Offset.Zero, 60f)),
            Modifier.fillMaxSize().padding(18.dp), color = glow.copy(alpha = 0.55f), sizingText = ctx.sizingTime()
        )
        AutoSizeText(
            ctx.timeText,
            base.copy(shadow = Shadow(glow, Offset.Zero, 18f)),
            Modifier.fillMaxSize().padding(18.dp), color = lerpColor(glow, Color.White, 0.72f), sizingText = ctx.sizingTime()
        )
        AutoSizeText(
            ctx.formatDate("EEEE d").uppercase(),
            labelStyle().copy(shadow = Shadow(glow, Offset.Zero, 12f)),
            Modifier.align(Alignment.BottomCenter).fillMaxWidth(0.5f).fillMaxHeight(0.1f).padding(bottom = 4.dp),
            color = glow
        )
    }
}

@Composable
fun DigitalOutlineFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    StackedTime(
        ctx,
        clockStyle(FontWeight.Black, letterSpacingEm = 0.0f).copy(drawStyle = Stroke(width = 2.5f)),
        modifier,
        timeColor = Ink.TextPrimary
    )
}

@Composable
fun DigitalGradientFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val colors = when (ctx.time.hour) {
        in 5..9 -> listOf(Color(0xFFFFC371), Color(0xFFFF5F6D))
        in 10..16 -> listOf(Color(0xFF7DE2FC), Color(0xFF3A7BD5))
        in 17..20 -> listOf(Color(0xFFF7797D), Color(0xFFC471ED), Color(0xFF12C2E9))
        else -> listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))
    }
    StackedTime(ctx, clockStyle(FontWeight.Bold, letterSpacingEm = -0.04f), modifier, timeBrush = Brush.linearGradient(colors))
}

@Composable
fun DigitalSplitFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        val style = clockStyle(FontWeight.Light, letterSpacingEm = -0.05f)
        AutoSizeText(ctx.displayHour.padStart(2, '0'), style, Modifier.weight(1f).fillMaxHeight(0.7f), color = Ink.TextPrimary, sizingText = "88")
        Column(Modifier.fillMaxHeight(0.7f).padding(horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.width(1.5.dp).weight(1f).background(Brush.verticalGradient(listOf(Color.Transparent, ctx.accent, Color.Transparent))))
        }
        Column(Modifier.weight(1f).fillMaxHeight(0.7f)) {
            AutoSizeText(ctx.minuteText, style, Modifier.fillMaxWidth().weight(0.78f), color = ctx.accent, sizingText = "88")
            AutoSizeText(ctx.formatDate("EEE d MMM").uppercase(), labelStyle(), Modifier.fillMaxWidth().weight(0.12f), color = Ink.TextSecondary)
        }
    }
}

@Composable
fun DigitalPosterFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxSize().padding(10.dp)) {
        Box(Modifier.width(10.dp).fillMaxHeight().background(Ink.Red))
        Column(Modifier.weight(1f).fillMaxHeight().padding(start = 12.dp)) {
            AutoSizeText(ctx.formatDate("EEEE").uppercase(), labelStyle(FontWeight.Black, FaceFonts.CondensedBold), Modifier.fillMaxWidth().weight(0.1f), color = Ink.Red, alignment = Alignment.CenterStart)
            AutoSizeText(ctx.displayHour.padStart(2, '0'), clockStyle(FontWeight.Black, FaceFonts.CondensedBold, -0.04f), Modifier.fillMaxWidth().weight(0.38f), color = Ink.TextPrimary, sizingText = "88", alignment = Alignment.CenterStart)
            AutoSizeText(ctx.minuteText, clockStyle(FontWeight.Black, FaceFonts.CondensedBold, -0.04f), Modifier.fillMaxWidth().weight(0.38f), color = Ink.TextPrimary, sizingText = "88", alignment = Alignment.CenterStart)
            Box(Modifier.fillMaxWidth().weight(0.01f).background(Ink.TextPrimary))
            AutoSizeText(
                "${ctx.formatDate("dd.MM.yyyy")}  ·  WEEK ${ctx.time.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)}",
                labelStyle(FontWeight.Bold, FaceFonts.Condensed),
                Modifier.fillMaxWidth().weight(0.1f), color = Ink.TextSecondary, alignment = Alignment.CenterStart
            )
        }
    }
}

@Composable
fun DigitalTypewriterFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val cream = Color(0xFFF1E6D0)
    val part = when (ctx.time.hour) {
        in 5..11 -> "in the morning"
        in 12..16 -> "in the afternoon"
        in 17..20 -> "in the evening"
        else -> "at night"
    }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        AutoSizeText("It is", TextStyle(fontFamily = FaceFonts.Serif, fontWeight = FontWeight.Normal), Modifier.fillMaxWidth(0.3f).weight(0.1f), color = cream.copy(alpha = 0.6f), alignment = Alignment.CenterStart)
        AutoSizeText(ctx.timeText, TextStyle(fontFamily = FaceFonts.Mono, fontWeight = FontWeight.Bold), Modifier.fillMaxWidth().weight(0.42f), color = cream, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
        AutoSizeText(part, TextStyle(fontFamily = FaceFonts.SerifItalic), Modifier.fillMaxWidth(0.7f).weight(0.12f), color = ctx.accent, alignment = Alignment.CenterStart)
        Spacer(Modifier.weight(0.04f))
        AutoSizeText(
            "${ctx.formatDate("EEEE")}, the ${ordinal(ctx.time.dayOfMonth)} of ${ctx.formatDate("MMMM")}",
            TextStyle(fontFamily = FaceFonts.Serif), Modifier.fillMaxWidth().weight(0.09f), color = cream.copy(alpha = 0.55f), alignment = Alignment.CenterStart
        )
    }
}

@Composable
fun DigitalGlassFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().padding(8.dp)) {
        AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 66L) { t ->
            val p = (t % 120f) / 120f * 2f * Math.PI.toFloat()
            fun blob(cx: Float, cy: Float, r: Float, c: Color) {
                drawCircle(safeRadialGradient(listOf(c, Color.Transparent), Offset(cx, cy), r), r, Offset(cx, cy))
            }
            blob(size.width * (0.3f + 0.08f * kotlin.math.sin(p)), size.height * 0.35f, size.minDimension * 0.6f, ctx.accent.copy(alpha = 0.55f))
            blob(size.width * 0.72f, size.height * (0.62f + 0.08f * kotlin.math.cos(p)), size.minDimension * 0.55f, Color(0xFF5E5CE6).copy(alpha = 0.5f))
            blob(size.width * 0.55f, size.height * 0.2f, size.minDimension * 0.4f, Color(0xFFFF375F).copy(alpha = 0.35f))
        }
        Box(
            Modifier.align(Alignment.Center).fillMaxWidth(0.86f).fillMaxHeight(0.62f)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White.copy(alpha = 0.07f))
                .border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.45f), Color.White.copy(alpha = 0.05f))), RoundedCornerShape(28.dp))
        ) {
            StackedTime(ctx, clockStyle(FontWeight.Light), Modifier, date = ctx.formatDate("EEE, MMM d"), timeWeight = 0.6f)
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Seven segment & LCD
// ---------------------------------------------------------------------------------------------

private fun DrawScope.sevenSegmentDigit(ch: Char, left: Float, top: Float, w: Float, h: Float, on: Color, off: Color, slant: Float = 0.1f) {
    val t = w * 0.17f
    val g = t * 0.16f
    val mask = FaceMath.sevenSegment(ch)
    fun pt(x: Float, y: Float) = Offset(left + x + (h - y) * slant, top + y)
    fun hSeg(y: Float): Path = Path().apply {
        val x0 = t / 2 + g
        val x1 = w - t / 2 - g
        val p = listOf(pt(x0, y), pt(x0 + t / 2, y - t / 2), pt(x1 - t / 2, y - t / 2), pt(x1, y), pt(x1 - t / 2, y + t / 2), pt(x0 + t / 2, y + t / 2))
        moveTo(p[0].x, p[0].y)
        p.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    fun vSeg(x: Float, y0: Float, y1: Float): Path = Path().apply {
        val a = y0 + g
        val b = y1 - g
        val p = listOf(pt(x, a), pt(x + t / 2, a + t / 2), pt(x + t / 2, b - t / 2), pt(x, b), pt(x - t / 2, b - t / 2), pt(x - t / 2, a + t / 2))
        moveTo(p[0].x, p[0].y)
        p.drop(1).forEach { lineTo(it.x, it.y) }
        close()
    }
    val segs = listOf(
        hSeg(t / 2),
        vSeg(w - t / 2, t / 2, h / 2),
        vSeg(w - t / 2, h / 2, h - t / 2),
        hSeg(h - t / 2),
        vSeg(t / 2, h / 2, h - t / 2),
        vSeg(t / 2, t / 2, h / 2),
        hSeg(h / 2)
    )
    segs.forEachIndexed { i, path -> drawPath(path, if (mask and (1 shl i) != 0) on else off) }
}

private fun DrawScope.sevenSegmentTime(text: String, on: Color, off: Color, colonOn: Boolean, area: androidx.compose.ui.geometry.Rect) {
    val digits = text.count { it != ':' }
    val colons = text.count { it == ':' }
    val colonW = 0.35f
    // Digit advance is 1.18w, the last digit has no gap, and the italic slant adds 0.1h = 0.175w.
    val units = digits * 1.18f - 0.18f + colons * colonW + 0.175f
    var w = area.width / units
    var h = w * 1.75f
    if (h > area.height) {
        h = area.height
        w = h / 1.75f
    }
    val totalW = w * units
    var x = area.left + (area.width - totalW) / 2f
    val top = area.top + (area.height - h) / 2f
    text.forEach { ch ->
        if (ch == ':') {
            val c = if (colonOn) on else off
            drawCircle(c, w * 0.08f, Offset(x + w * colonW / 2 + h * 0.06f, top + h * 0.32f))
            drawCircle(c, w * 0.08f, Offset(x + w * colonW / 2 + h * 0.02f, top + h * 0.68f))
            x += w * colonW
        } else {
            sevenSegmentDigit(ch, x, top, w, h, on, off)
            x += w * 1.18f
        }
    }
}

@Composable
fun DigitalSegmentedFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val clock = rememberFaceClock(ctx)
    val led = if (ctx.accent.red > 0.9f && ctx.accent.green < 0.5f) ctx.accent else Ink.Red
    val text = ctx.displayHour.padStart(2, ' ').replace(' ', '!') + ":" + ctx.minuteText
    Column(modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.fillMaxWidth().weight(0.75f).graphicsLayer()) {
            val colonOn = ((clock.value % 1f) < 0.5f) || !ctx.animate
            sevenSegmentTime(text, led, led.copy(alpha = 0.07f), colonOn, androidx.compose.ui.geometry.Rect(Offset.Zero, size))
        }
        Row(Modifier.fillMaxWidth().weight(0.12f), horizontalArrangement = Arrangement.SpaceBetween) {
            AutoSizeText(if (ctx.use24Hour) "24H" else ctx.amPm, labelStyle(FontWeight.Bold), Modifier.weight(1f).fillMaxHeight(), color = led, alignment = Alignment.CenterStart)
            AutoSizeText(ctx.nextAlarm?.let { "ALARM $it" } ?: ctx.formatDate("EEE d").uppercase(), labelStyle(FontWeight.Bold), Modifier.weight(2f).fillMaxHeight(), color = led.copy(alpha = 0.6f), alignment = Alignment.CenterEnd)
        }
    }
}

@Composable
fun DigitalRetroLcdFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val lcd = Color(0xFF9DB08A)
    val ink = Color(0xFF1C2418)
    val text = ctx.displayHour.padStart(2, '!').replace(' ', '!') + ":" + ctx.minuteText
    Box(modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.fillMaxWidth().fillMaxHeight(0.72f).clip(RoundedCornerShape(18.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF2A2C30), Color(0xFF16171A))))
                .padding(10.dp)
        ) {
            Column(
                Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp))
                    .background(Brush.verticalGradient(listOf(lcd, lerpColor(lcd, Color.Black, 0.18f))))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(Modifier.fillMaxWidth().weight(0.2f)) {
                    AutoSizeText(ctx.formatDate("EEE").uppercase(), TextStyle(fontFamily = FaceFonts.Mono, fontWeight = FontWeight.Bold), Modifier.weight(1f).fillMaxHeight(), color = ink, alignment = Alignment.CenterStart)
                    AutoSizeText(ctx.formatDate("MM-dd"), TextStyle(fontFamily = FaceFonts.Mono, fontWeight = FontWeight.Bold), Modifier.weight(1f).fillMaxHeight(), color = ink, alignment = Alignment.CenterEnd)
                }
                Row(Modifier.fillMaxWidth().weight(0.8f), verticalAlignment = Alignment.CenterVertically) {
                    Canvas(Modifier.weight(1f).fillMaxHeight()) {
                        sevenSegmentTime(text, ink, ink.copy(alpha = 0.08f), true, androidx.compose.ui.geometry.Rect(Offset.Zero, size))
                    }
                    Canvas(Modifier.width(36.dp).fillMaxHeight(0.5f)) {
                        sevenSegmentTime(ctx.secondText, ink, ink.copy(alpha = 0.08f), false, androidx.compose.ui.geometry.Rect(Offset.Zero, size))
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Dot matrix, Nixie, Terminal, Binary, Word clock, Spoken, Seconds ring
// ---------------------------------------------------------------------------------------------

@Composable
fun DigitalDotMatrixFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val text = ctx.displayHour.padStart(2, '0') + ":" + ctx.minuteText
    Column(modifier.fillMaxSize().padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.fillMaxWidth().weight(0.72f)) {
            // Colons use only their middle three columns so the digits sit closer together.
            fun span(ch: Char) = if (ch == ':') 1..3 else 0..4
            val cols = text.sumOf { span(it).count() } + (text.length - 1)
            val step = minOf(size.width / cols, size.height / 7f)
            val left = (size.width - step * cols) / 2f
            val top = (size.height - step * 7f) / 2f
            var cx = 0
            text.forEach { ch ->
                val glyph = FaceMath.dotGlyph(ch)
                val range = span(ch)
                for (r in 0 until 7) for ((i, c) in range.withIndex()) {
                    val lit = glyph[r][c] == '#'
                    val center = Offset(left + (cx + i + 0.5f) * step, top + (r + 0.5f) * step)
                    drawCircle(if (lit) Ink.TextPrimary else Color.White.copy(alpha = 0.06f), step * 0.38f, center)
                }
                cx += range.count() + 1
            }
        }
        Row(Modifier.fillMaxWidth(0.8f).weight(0.1f), horizontalArrangement = Arrangement.SpaceBetween) {
            AutoSizeText(ctx.formatDate("EEE dd").uppercase(), TextStyle(fontFamily = FaceFonts.Mono), Modifier.weight(1f).fillMaxHeight(), color = Ink.Red, alignment = Alignment.CenterStart)
            AutoSizeText("${ctx.battery.batteryPercent}%", TextStyle(fontFamily = FaceFonts.Mono), Modifier.weight(1f).fillMaxHeight(), color = Ink.TextSecondary, alignment = Alignment.CenterEnd)
        }
    }
}

@Composable
fun DigitalNixieFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val digits = ctx.displayHour.padStart(2, '0') + ctx.minuteText
    val glow = Color(0xFFFF7A1A)
    val measurer = rememberTextMeasurer(cacheSize = 16)
    Canvas(modifier.fillMaxSize().padding(10.dp)) {
        val gap = size.width * 0.025f
        val tubeW = (size.width - gap * 5f) / 4.4f
        val tubeH = minOf(size.height * 0.86f, tubeW * 1.9f)
        val top = (size.height - tubeH) / 2f
        var x = (size.width - (tubeW * 4 + gap * 3 + tubeW * 0.4f)) / 2f
        digits.forEachIndexed { i, ch ->
            if (i == 2) {
                drawCircle(glow, tubeW * 0.05f, Offset(x + tubeW * 0.2f, top + tubeH * 0.38f))
                drawCircle(glow, tubeW * 0.05f, Offset(x + tubeW * 0.2f, top + tubeH * 0.62f))
                x += tubeW * 0.4f
            }
            // Glass tube
            drawRoundRect(
                Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.10f), Color.White.copy(alpha = 0.02f), Color.White.copy(alpha = 0.08f)), startX = x, endX = x + tubeW),
                topLeft = Offset(x, top), size = Size(tubeW, tubeH), cornerRadius = CornerRadius(tubeW / 2f, tubeW / 2f)
            )
            drawRoundRect(Color.White.copy(alpha = 0.12f), Offset(x, top), Size(tubeW, tubeH), CornerRadius(tubeW / 2f, tubeW / 2f), style = Stroke(tubeW * 0.02f))
            // Anode mesh
            val meshStep = tubeW / 9f
            var mx = x + meshStep
            while (mx < x + tubeW - meshStep / 2) {
                drawLine(Color.White.copy(alpha = 0.035f), Offset(mx, top + tubeH * 0.08f), Offset(mx, top + tubeH * 0.92f), tubeW * 0.01f)
                mx += meshStep
            }
            val style = TextStyle(fontFamily = FaceFonts.Serif, fontWeight = FontWeight.Light, fontSize = (tubeH * 0.62f).toSp())
            val ghost = measurer.measure("8", style)
            val gx = x + (tubeW - ghost.size.width) / 2f
            val gy = top + (tubeH - ghost.size.height) / 2f
            drawText(ghost, color = Color(0xFF221509), topLeft = Offset(gx, gy))
            val layout = measurer.measure(ch.toString(), style.copy(shadow = Shadow(glow, Offset.Zero, tubeW * 0.35f)))
            drawText(layout, color = Color(0xFFFFC37A), topLeft = Offset(x + (tubeW - layout.size.width) / 2f, gy))
            x += tubeW + gap
        }
    }
}

@Composable
fun DigitalMatrixFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val green = Color(0xFF39FF88)
    val mono = TextStyle(fontFamily = FaceFonts.Mono, fontWeight = FontWeight.Medium)
    val clock = rememberFaceClock(ctx)
    val lines = listOf(
        "$ date",
        ctx.formatDate("EEE MMM dd yyyy"),
        "$ battery --status",
        "${ctx.battery.batteryPercent}% ${if (ctx.battery.isCharging) "charging (${ctx.battery.chargeType.name.lowercase()})" else "discharging"}" +
            (ctx.battery.temperatureCelsius?.let { " · ${"%.1f".format(it)}°C" } ?: ""),
        "$ weather",
        ctx.weather?.let { "${ctx.temperature(it.temperatureCelsius)} ${it.condition.displayName.lowercase()} · ${it.cityName}" } ?: "offline"
    )
    Column(modifier.fillMaxSize().padding(14.dp)) {
        AutoSizeText(
            "${ctx.timeText}${if (ctx.showSeconds) ":" + ctx.secondText else ""}",
            mono.copy(fontWeight = FontWeight.Bold, shadow = Shadow(green, Offset.Zero, 22f)),
            Modifier.fillMaxWidth().weight(0.36f), color = green, alignment = Alignment.CenterStart
        )
        lines.forEachIndexed { i, line ->
            AutoSizeText(line, mono, Modifier.fillMaxWidth().weight(0.075f), color = if (i % 2 == 0) green.copy(alpha = 0.55f) else green.copy(alpha = 0.9f), sizingText = "Wed Sep 28 2026 · 100% charging", alignment = Alignment.CenterStart)
        }
        Row(Modifier.fillMaxWidth().weight(0.075f), verticalAlignment = Alignment.CenterVertically) {
            AutoSizeText("$ ", mono, Modifier.fillMaxHeight(), color = green.copy(alpha = 0.55f), alignment = Alignment.CenterStart)
            Canvas(Modifier.width(10.dp).fillMaxHeight(0.8f).graphicsLayer()) {
                if ((clock.value % 1f) < 0.55f || !ctx.animate) drawRect(green)
            }
        }
    }
}

@Composable
fun DigitalBinaryFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val h = ctx.time.hour
    val m = ctx.time.minute
    val s = ctx.time.second
    val columns = listOf(h / 10 to 2, h % 10 to 4, m / 10 to 3, m % 10 to 4, s / 10 to 3, s % 10 to 4)
    Column(modifier.fillMaxSize().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Canvas(Modifier.fillMaxWidth().weight(0.78f)) {
            val cols = 6
            val cell = minOf(size.width / (cols + 1.2f), size.height / 4.4f)
            val totalW = cell * cols + cell * 1.2f
            val left = (size.width - totalW) / 2f
            columns.forEachIndexed { ci, (value, bits) ->
                val groupGap = (ci / 2) * cell * 0.6f
                val cx = left + groupGap + cell * (ci + 0.5f)
                for (b in 0 until 4) {
                    val cy = size.height - cell * (b + 0.7f)
                    val exists = b < bits
                    if (!exists) continue
                    val lit = (value shr b) and 1 == 1
                    if (lit) {
                        drawCircle(ctx.accent.copy(alpha = 0.25f), cell * 0.42f, Offset(cx, cy))
                        drawCircle(ctx.accent, cell * 0.3f, Offset(cx, cy))
                    } else {
                        drawCircle(Color.White.copy(alpha = 0.08f), cell * 0.3f, Offset(cx, cy))
                    }
                }
            }
        }
        AutoSizeText("%02d  %02d  %02d".format(h, m, s), TextStyle(fontFamily = FaceFonts.Mono), Modifier.fillMaxWidth(0.6f).weight(0.1f), color = Ink.TextTertiary)
    }
}

@Composable
fun DigitalWordClockFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val measurer = rememberTextMeasurer(cacheSize = 64)
    val lit = remember(ctx.time.hour, ctx.time.minute) { FaceMath.wordClockLit(ctx.time.hour, ctx.time.minute) }
    val extra = ctx.time.minute % 5
    Canvas(modifier.fillMaxSize().padding(12.dp)) {
        val rows = FaceMath.wordGrid.size
        val cols = FaceMath.wordGrid[0].length
        val cell = minOf(size.width / (cols + 1f), size.height / (rows + 1f))
        val left = (size.width - cell * cols) / 2f
        val top = (size.height - cell * rows) / 2f
        val style = TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.Medium, fontSize = (cell * 0.56f).toSp())
        for (r in 0 until rows) for (c in 0 until cols) {
            val ch = FaceMath.wordGrid[r][c].toString()
            val on = lit[r][c]
            val layout = measurer.measure(ch, if (on) style.copy(fontWeight = FontWeight.Bold, shadow = Shadow(ctx.accent.copy(alpha = 0.6f), Offset.Zero, cell * 0.4f)) else style)
            drawText(
                layout,
                color = if (on) Ink.TextPrimary else Color.White.copy(alpha = 0.11f),
                topLeft = Offset(left + c * cell + (cell - layout.size.width) / 2f, top + r * cell + (cell - layout.size.height) / 2f)
            )
        }
        val corners = listOf(Offset(left - cell * 0.3f, top - cell * 0.3f), Offset(left + cell * cols + cell * 0.3f, top - cell * 0.3f),
            Offset(left + cell * cols + cell * 0.3f, top + cell * rows + cell * 0.3f), Offset(left - cell * 0.3f, top + cell * rows + cell * 0.3f))
        corners.forEachIndexed { i, p -> drawCircle(if (i < extra) ctx.accent else Color.White.copy(alpha = 0.1f), cell * 0.09f, p) }
    }
}

@Composable
fun DigitalSpokenFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val phrase = FaceMath.spokenTime(ctx.time.hour, ctx.time.minute)
    val (first, second) = if (phrase.endsWith("o'clock")) phrase.removeSuffix(" o'clock") to "o'clock" else {
        val idx = phrase.lastIndexOf(' ')
        phrase.substring(0, idx) to phrase.substring(idx + 1)
    }
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
        AutoSizeText("it's", TextStyle(fontFamily = FaceFonts.Serif, fontWeight = FontWeight.Light), Modifier.fillMaxWidth(0.25f).weight(0.12f), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
        AutoSizeText(first, clockStyle(FontWeight.Light, letterSpacingEm = -0.02f), Modifier.fillMaxWidth().weight(0.28f), color = Ink.TextPrimary, alignment = Alignment.CenterStart)
        AutoSizeText(second, clockStyle(FontWeight.Bold, letterSpacingEm = -0.03f), Modifier.fillMaxWidth().weight(0.32f), color = ctx.accent, alignment = Alignment.CenterStart)
        AutoSizeText(ctx.formatDate("EEEE, MMMM d").lowercase(), TextStyle(fontFamily = FaceFonts.Serif), Modifier.fillMaxWidth(0.7f).weight(0.08f), color = Ink.TextSecondary, alignment = Alignment.CenterStart)
    }
}

@Composable
fun DigitalSecondsRingFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val clock = rememberFaceClock(ctx)
    Box(modifier.fillMaxSize().padding(8.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().graphicsLayer()) {
            val r = size.minDimension / 2f * 0.92f
            val c = Offset(size.width / 2f, size.height / 2f)
            val sec = clock.value % 60f
            for (i in 0 until 60) {
                val a = Math.toRadians((i * 6.0) - 90.0)
                val p = Offset(c.x + (kotlin.math.cos(a) * r).toFloat(), c.y + (kotlin.math.sin(a) * r).toFloat())
                val litAmount = when {
                    i < sec.toInt() -> 1f
                    i == sec.toInt() -> sec % 1f
                    else -> 0f
                }
                val big = i % 5 == 0
                drawCircle(Color.White.copy(alpha = 0.08f), r * (if (big) 0.03f else 0.018f), p)
                if (litAmount > 0f) drawCircle(ctx.accent.copy(alpha = litAmount), r * (if (big) 0.03f else 0.018f), p)
            }
        }
        Box(Modifier.fillMaxSize(0.62f)) {
            StackedTime(ctx, clockStyle(FontWeight.Light), Modifier, date = ctx.formatDate("EEE d"), meridiem = false, timeWeight = 0.55f)
        }
    }
}
