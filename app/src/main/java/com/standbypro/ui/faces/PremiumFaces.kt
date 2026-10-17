package com.standbypro.ui.faces

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.standbypro.domain.FaceContext
import com.standbypro.media.NowPlayingRepository
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.Dial
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.HandStyle
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.SecondsMotion
import com.standbypro.ui.design.appliedIndices
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.dialDisc
import com.standbypro.ui.design.hand
import com.standbypro.ui.design.hub
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.design.numerals
import com.standbypro.ui.design.rememberDayClock
import com.standbypro.ui.design.safeRadialGradient
import com.standbypro.ui.design.subDial
import com.standbypro.ui.design.textAt
import com.standbypro.ui.design.ticks
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

private const val TAU = (2 * PI).toFloat()

@Composable
private fun Bleed(modifier: Modifier, content: @Composable BoxScope.() -> Unit) =
    Box(modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(26.dp)), content = content)

/** Spaced small caps for dial branding, sized as a fraction of the dial radius. */
private fun DrawScope.brandStyle(d: Dial, size: Float, family: FontFamily = FaceFonts.Sans): TextStyle = TextStyle(
    fontFamily = family,
    fontWeight = FontWeight.Medium,
    fontSize = (d.radius * size).toSp(),
    letterSpacing = TextUnit(0.25f, TextUnitType.Em)
)

// ---------------------------------------------------------------------------------------------
// Vinyl — a turntable that spins while music plays
// ---------------------------------------------------------------------------------------------

@Composable
fun VinylFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val media by NowPlayingRepository.state.collectAsState()
    val playing = media.hasSession && media.isPlaying
    val clock = rememberDayClock(SecondsMotion.SWEEP, ctx.animate && playing, 33L)
    val arm by animateFloatAsState(if (playing || ctx.isPreview) 1f else 0f, tween(1400), label = "tonearm")
    val m = rememberTextMeasurer(cacheSize = 8)
    val accent = ctx.accent
    val title = if (media.hasSession) media.title.ifBlank { "Now Playing" } else "Nothing playing"
    val artist = if (media.hasSession) listOf(media.artist, media.appName).filter { it.isNotBlank() }.joinToString(" · ") else "Start music on your phone"

    val record: @Composable (Modifier) -> Unit = { mod ->
        Canvas(mod) {
            if (size.minDimension < 2f) return@Canvas
            val r = min(size.width * 0.40f, size.height * 0.45f)
            val c = Offset(size.width * 0.45f, size.height / 2f)
            drawCircle(Color.Black.copy(alpha = 0.6f), r * 1.05f, c + Offset(0f, r * 0.04f))
            drawCircle(Color(0xFF0B0B0D), r, c)
            var gr = r * 0.97f
            var i = 0
            while (gr > r * 0.37f) {
                drawCircle(Color.White.copy(alpha = if (i % 3 == 0) 0.055f else 0.022f), gr, c, style = Stroke(r * 0.004f))
                gr -= r * 0.011f
                i++
            }
            listOf(0.83f, 0.67f, 0.52f).forEach { drawCircle(Color.Black, r * it, c, style = Stroke(r * 0.014f)) }
            drawCircle(
                Brush.sweepGradient(
                    listOf(Color.Transparent, Color.White.copy(alpha = 0.10f), Color.Transparent, Color.Transparent, Color.White.copy(alpha = 0.07f), Color.Transparent, Color.Transparent),
                    c
                ),
                r, c
            )
            val angle = (clock.value * 200f) % 360f
            val labelR = r * 0.34f
            rotate(angle, c) {
                val art = media.art
                if (art != null) {
                    clipPath(Path().apply { addOval(Rect(c, labelR)) }) {
                        drawImage(
                            art,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(art.width, art.height),
                            dstOffset = IntOffset((c.x - labelR).toInt(), (c.y - labelR).toInt()),
                            dstSize = IntSize((labelR * 2).toInt(), (labelR * 2).toInt())
                        )
                    }
                } else {
                    drawCircle(safeRadialGradient(listOf(lerpColor(accent, Color.White, 0.15f), lerpColor(accent, Color.Black, 0.45f)), c, labelR), labelR, c)
                    drawCircle(Color.Black.copy(alpha = 0.18f), labelR * 0.82f, c, style = Stroke(labelR * 0.02f))
                    textAt(m, "STANDBY", Offset(c.x, c.y - labelR * 0.48f), brandStyle(Dial(c, labelR), 0.16f), Color.Black.copy(alpha = 0.55f))
                    textAt(m, "33⅓", Offset(c.x, c.y + labelR * 0.5f), brandStyle(Dial(c, labelR), 0.14f), Color.Black.copy(alpha = 0.45f))
                }
            }
            drawCircle(Color(0xFFDADAE0), r * 0.025f, c)
            // Tonearm: swings onto the record while playing.
            val pivot = Offset(c.x + r * 1.02f, c.y - r * 0.82f)
            rotate(-6f + 28f * arm, pivot) {
                drawCircle(safeRadialGradient(listOf(Color(0xFFE6E7EA), Color(0xFF7D8086)), pivot, r * 0.1f), r * 0.1f, pivot)
                drawLine(Color.Black.copy(alpha = 0.4f), pivot + Offset(r * 0.02f, r * 0.03f), Offset(pivot.x + r * 0.02f, pivot.y + r * 1.25f), r * 0.032f, cap = StrokeCap.Round)
                drawLine(Color(0xFFC4C7CC), pivot, Offset(pivot.x, pivot.y + r * 1.22f), r * 0.03f, cap = StrokeCap.Round)
                drawRoundRect(Color(0xFF2A2A2E), Offset(pivot.x - r * 0.055f, pivot.y + r * 1.18f), Size(r * 0.11f, r * 0.19f), CornerRadius(r * 0.02f))
            }
        }
    }

    Bleed(modifier) {
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF17120F), Color(0xFF0A0807)))))
        if (ctx.isCompact) {
            Column(Modifier.fillMaxSize().padding(10.dp)) {
                record(Modifier.fillMaxWidth().weight(0.74f))
                AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.17f), color = Ink.TextPrimary, sizingText = ctx.sizingTime())
                AutoSizeText(title, labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().weight(0.07f), color = accent)
            }
        } else {
            Row(Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                record(Modifier.weight(1.1f).fillMaxHeight())
                Column(Modifier.weight(1f).fillMaxHeight().padding(start = 8.dp, end = 10.dp)) {
                    Spacer(Modifier.weight(0.2f))
                    AutoSizeText(ctx.timeText, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.3f), color = Ink.TextPrimary, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart)
                    Spacer(Modifier.weight(0.04f))
                    AutoSizeText(title, labelStyle(FontWeight.Bold).copy(letterSpacing = TextUnit.Unspecified), Modifier.fillMaxWidth().weight(0.09f), color = accent, sizingText = "Midnight City Remix", alignment = Alignment.CenterStart)
                    AutoSizeText(artist, labelStyle(FontWeight.Medium).copy(letterSpacing = TextUnit.Unspecified), Modifier.fillMaxWidth().weight(0.06f), color = Ink.TextSecondary, sizingText = "M83 · Spotify Premium", alignment = Alignment.CenterStart)
                    Spacer(Modifier.weight(0.31f))
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Big Date — editorial calendar typography
// ---------------------------------------------------------------------------------------------

@Composable
fun BigDateFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val accent = ctx.accent
    val next = ctx.events.firstOrNull()
    val dayOfYear = ctx.time.dayOfYear
    val week = ctx.time.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
    Row(modifier.fillMaxSize().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        AutoSizeText(
            ctx.time.dayOfMonth.toString(), clockStyle(FontWeight.Black, letterSpacingEm = -0.05f),
            Modifier.weight(1.05f).fillMaxHeight(0.9f),
            brush = Brush.verticalGradient(listOf(lerpColor(accent, Color.White, 0.25f), accent, lerpColor(accent, Color.Black, 0.35f))),
            sizingText = "28"
        )
        Column(Modifier.weight(1f).fillMaxHeight().padding(start = 16.dp)) {
            Spacer(Modifier.weight(0.16f))
            AutoSizeText(ctx.formatDate("EEEE").uppercase(), labelStyle(FontWeight.Black), Modifier.fillMaxWidth().weight(0.11f), color = Ink.TextPrimary, sizingText = "WEDNESDAY", alignment = Alignment.CenterStart)
            AutoSizeText(ctx.formatDate("MMMM yyyy").uppercase(), labelStyle(FontWeight.Medium), Modifier.fillMaxWidth(0.8f).weight(0.06f), color = Ink.TextSecondary, sizingText = "SEPTEMBER 2026", alignment = Alignment.CenterStart)
            Spacer(Modifier.weight(0.05f))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Ink.HairlineStrong))
            Spacer(Modifier.weight(0.05f))
            AutoSizeText(
                ctx.timeText, clockStyle(FontWeight.Light), Modifier.fillMaxWidth().weight(0.24f), color = Ink.TextPrimary,
                sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart,
                suffix = if (ctx.use24Hour) null else " ${ctx.amPm}", suffixColor = Ink.TextTertiary
            )
            AutoSizeText(
                next?.let { "${it.timeRange.substringBefore(" -")}  ·  ${it.title}" } ?: "Nothing else today",
                labelStyle(FontWeight.SemiBold).copy(letterSpacing = TextUnit.Unspecified), Modifier.fillMaxWidth().weight(0.06f),
                color = accent, sizingText = "10:30 AM  ·  Sprint Sync & Review", alignment = Alignment.CenterStart
            )
            AutoSizeText("DAY $dayOfYear  ·  WEEK $week", labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth(0.6f).weight(0.05f), color = Ink.TextTertiary, sizingText = "DAY 365  ·  WEEK 52", alignment = Alignment.CenterStart)
            Spacer(Modifier.weight(0.16f))
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Code Rain — falling glyph columns with a glowing terminal clock
// ---------------------------------------------------------------------------------------------

private const val RAIN_GLYPHS = "ｱｲｳｴｵｶｷｸｹｺｻｼｽｾｿﾀﾁﾂﾃﾄﾅﾆﾇﾈﾊﾋﾌﾍﾎﾏﾐﾑﾒﾓﾔﾕﾖﾗﾘﾙﾚﾛﾜ0123456789Z:+*<>"

@Composable
fun CodeRainFace(ctx: FaceContext, modifier: Modifier = Modifier) = Bleed(modifier) {
    val m = rememberTextMeasurer(cacheSize = 80)
    val green = Color(0xFF35FF7A)
    AnimatedCanvas(ctx, Modifier.fillMaxSize().background(Color.Black), frameIntervalMs = 66L) { t ->
        val glyph = (size.minDimension / 16f).coerceAtLeast(8f)
        val style = TextStyle(fontFamily = FaceFonts.Mono, fontWeight = FontWeight.Medium, fontSize = glyph.toSp())
        val cols = (size.width / glyph).toInt().coerceAtLeast(1)
        val rows = (size.height / glyph).toInt() + 1
        val tick = (t * 3f).toInt()
        for (col in 0 until cols) {
            val rnd = Random(col * 7919 + 13)
            val speed = 4f + rnd.nextFloat() * 8f
            val trail = 6 + rnd.nextInt(12)
            val span = rows + trail + 6
            val head = ((t + rnd.nextFloat() * 60f) * speed) % span
            val headRow = head.toInt()
            for (k in 0 until trail) {
                val row = headRow - k
                if (row < 0 || row >= rows) continue
                val flicker = if (k == 0) tick else (tick + row) / 7
                val ch = RAIN_GLYPHS[Math.floorMod(col * 31 + row * 17 + flicker * 13, RAIN_GLYPHS.length)].toString()
                val color = if (k == 0) Color(0xFFD9FFE6) else green.copy(alpha = 0.85f * (1f - k / trail.toFloat()))
                drawText(m.measure(ch, style), color = color, topLeft = Offset(col * glyph, row * glyph))
            }
        }
    }
    Box(
        Modifier.align(Alignment.Center).fillMaxWidth(if (ctx.isCompact) 0.82f else 0.6f).fillMaxHeight(0.42f)
            .clip(RoundedCornerShape(18.dp)).background(Color.Black.copy(alpha = 0.8f))
            .border(1.dp, green.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        AutoSizeText(
            ctx.timeText, clockStyle(FontWeight.Light, FaceFonts.Mono).copy(shadow = Shadow(green.copy(alpha = 0.8f), Offset.Zero, 28f)),
            Modifier.fillMaxSize(), color = Color(0xFFBDFFD3), sizingText = ctx.sizingTime()
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Carbon — sport chronograph on a woven carbon dial
// ---------------------------------------------------------------------------------------------

private fun DrawScope.carbonWeave(d: Dial) {
    val cell = d.px(0.07f)
    val dark = Color(0xFF0E0E10)
    val light = Color(0xFF2B2B30)
    var y = d.center.y - d.radius
    var row = 0
    while (y < d.center.y + d.radius) {
        var x = d.center.x - d.radius
        var col = 0
        while (x < d.center.x + d.radius) {
            val brush = if ((row + col) % 2 == 0) {
                Brush.verticalGradient(listOf(light, dark, light), startY = y, endY = y + cell)
            } else {
                Brush.horizontalGradient(listOf(light, dark, light), startX = x, endX = x + cell)
            }
            drawRect(brush, Offset(x, y), Size(cell + 0.5f, cell + 0.5f))
            x += cell
            col++
        }
        y += cell
        row++
    }
}

@Composable
fun CarbonFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 48)
    val red = Color(0xFFFF2D2D)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF1C1C1F), Color(0xFF09090A), ring = Color(0xFF3C3C41), ringWidth = 0.02f)
            numerals(d, m, (0 until 12).map { if (it == 0) "60" else (it * 5).toString() }, 0.925f, numeralStyle(d, 0.065f, FontWeight.Bold), Color.White.copy(alpha = 0.78f))
            val inner = Dial(d.center, d.px(0.85f))
            clipPath(Path().apply { addOval(inner.bounds()) }) {
                carbonWeave(inner)
                drawCircle(safeRadialGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f)), d.center, inner.radius), inner.radius, d.center)
            }
            drawCircle(red.copy(alpha = 0.7f), inner.radius, d.center, style = Stroke(d.px(0.006f)))
            ticks(d, outer = 0.84f, majorLength = 0.06f, minorLength = 0.025f, majorWidth = 0.014f, minorWidth = 0.005f, majorColor = Color.White, minorColor = Color.White.copy(alpha = 0.35f))
            appliedIndices(d, Color(0xFFE8E8EA), outer = 0.76f, length = 0.12f, width = 0.04f, skip = setOf(3, 6, 9))
            val left = d.sub(270f, 0.42f, 0.2f)
            val right = d.sub(90f, 0.42f, 0.2f)
            subDial(left, Color(0xFF0D0D0F), Color.White.copy(alpha = 0.5f), ring = red.copy(alpha = 0.85f))
            subDial(right, Color(0xFF0D0D0F), Color.White.copy(alpha = 0.5f), ring = red.copy(alpha = 0.85f))
            textAt(m, "SEC", left.point(180f, 0.42f), brandStyle(left, 0.2f), Color.White.copy(alpha = 0.6f))
            textAt(m, "PWR", right.point(180f, 0.42f), brandStyle(right, 0.2f), Color.White.copy(alpha = 0.6f))
            textAt(m, "CARBON", d.point(0f, 0.36f), brandStyle(d, 0.06f).copy(fontWeight = FontWeight.Bold), red)
            textAt(m, "CHRONOMETER", d.point(0f, 0.27f), brandStyle(d, 0.032f), Color.White.copy(alpha = 0.55f))
            dateWindow(m, ctx.time.dayOfMonth.toString(), d.point(180f, 0.52f), d, bg = Color(0xFF111113), fg = Color.White, frame = Color(0xFF3C3C41))
        },
        handsLayer = { d, a ->
            val left = d.sub(270f, 0.42f, 0.2f)
            val right = d.sub(90f, 0.42f, 0.2f)
            hand(left, a.second, 0.85f, 0.06f, red, HandStyle.NEEDLE, tail = 0.2f, shadow = false)
            hub(left, red, 0.12f)
            hand(right, ctx.battery.batteryPercent * 3.6f, 0.8f, 0.07f, Color.White, HandStyle.NEEDLE, tail = 0.15f, shadow = false)
            hub(right, Color.White, 0.12f)
            handSet(d, a, ctx.copy(showSeconds = false), style = HandStyle.SWORD, lume = Ink.Lume, hourWidth = 0.075f, minuteWidth = 0.055f, hubColor = red)
        }
    )
}

// ---------------------------------------------------------------------------------------------
// Marble — Carrara dial in a polished gold case
// ---------------------------------------------------------------------------------------------

@Composable
fun MarbleFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    val gold = Color(0xFFC9A24A)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            drawCircle(Brush.sweepGradient(listOf(Ink.GoldLight, Ink.Gold, Color(0xFF8C6A1F), Ink.GoldLight, Ink.Gold, Color(0xFF8C6A1F), Ink.GoldLight), d.center), d.radius, d.center)
            val face = Dial(d.center, d.px(0.93f))
            drawCircle(
                safeRadialGradient(listOf(Color(0xFFFCFBF8), Color(0xFFEDEAE4), Color(0xFFD6D1C8)), Offset(d.center.x - face.radius * 0.3f, d.center.y - face.radius * 0.35f), face.radius * 1.5f),
                face.radius, d.center
            )
            clipPath(Path().apply { addOval(face.bounds()) }) {
                val rnd = Random(21)
                repeat(9) { k ->
                    val start = face.point(rnd.nextFloat() * 360f, 1.05f)
                    val end = face.point(rnd.nextFloat() * 360f, 1.05f)
                    val path = Path().apply { moveTo(start.x, start.y) }
                    val jitter = face.radius * 0.16f
                    for (step in 1..6) {
                        val f = step / 6f
                        val bx = start.x + (end.x - start.x) * f
                        val by = start.y + (end.y - start.y) * f
                        path.quadraticTo(
                            bx + (rnd.nextFloat() - 0.5f) * jitter * 2, by + (rnd.nextFloat() - 0.5f) * jitter * 2,
                            bx + (rnd.nextFloat() - 0.5f) * jitter, by + (rnd.nextFloat() - 0.5f) * jitter
                        )
                    }
                    val major = k < 3
                    drawPath(path, Color(0xFF9A958E).copy(alpha = if (major) 0.16f else 0.08f), style = Stroke(face.px(if (major) 0.035f else 0.014f), cap = StrokeCap.Round))
                    drawPath(path, Color(0xFF6E6963).copy(alpha = if (major) 0.38f else 0.2f), style = Stroke(face.px(if (major) 0.006f else 0.003f), cap = StrokeCap.Round))
                }
            }
            drawCircle(gold, face.radius, d.center, style = Stroke(d.px(0.01f)))
            ticks(face, outer = 0.97f, majorLength = 0.07f, minorLength = 0.025f, majorWidth = 0.012f, minorWidth = 0.005f, majorColor = gold, minorColor = gold.copy(alpha = 0.55f))
            appliedIndices(face, gold, highlight = Color.White.copy(alpha = 0.7f), outer = 0.86f, length = 0.13f, width = 0.035f)
            textAt(m, "CARRARA", face.point(0f, 0.4f), brandStyle(face, 0.06f, FaceFonts.Serif), Color(0xFF6B5A3A))
            textAt(m, "AUTOMATIC", face.point(180f, 0.4f), brandStyle(face, 0.036f), Color(0xFF8A7A5A))
        },
        handsLayer = { d, a ->
            val face = Dial(d.center, d.px(0.93f))
            handSet(face, a, ctx, hourColor = Color(0xFFB8913A), style = HandStyle.DAUPHINE, secondColor = Color(0xFF1E2A44), hourWidth = 0.075f, minuteWidth = 0.055f, hubColor = gold)
        }
    )
}

// ---------------------------------------------------------------------------------------------
// Rain on Glass — city bokeh behind a wet window; rivulets run when it's actually raining
// ---------------------------------------------------------------------------------------------

private fun DrawScope.drop(p: Offset, r: Float) {
    drawCircle(Color.Black.copy(alpha = 0.22f), r, p + Offset(0f, r * 0.25f))
    drawCircle(safeRadialGradient(listOf(Color.White.copy(alpha = 0.04f), Color.White.copy(alpha = 0.22f)), p, r), r, p)
    drawCircle(Color.White.copy(alpha = 0.55f), r * 0.28f, p + Offset(-r * 0.3f, -r * 0.35f))
}

@Composable
fun RainGlassFace(ctx: FaceContext, modifier: Modifier = Modifier) = Bleed(modifier) {
    val weather = ctx.weather
    val rainy = weather?.condition?.isRainy == true
    Canvas(Modifier.fillMaxSize().graphicsLayer()) {
        if (size.minDimension < 2f) return@Canvas
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0A1120), Color(0xFF121A2A), Color(0xFF221A24))))
        val bokeh = Random(5)
        val palette = listOf(Color(0xFFFFB86B), Color(0xFFFF6B6B), Color(0xFF6BC8FF), Color(0xFFFFE08A))
        repeat(28) {
            val p = Offset(bokeh.nextFloat() * size.width, size.height * (0.3f + bokeh.nextFloat() * 0.65f))
            val r = size.minDimension * (0.03f + bokeh.nextFloat() * 0.07f)
            val col = palette[bokeh.nextInt(palette.size)]
            drawCircle(safeRadialGradient(listOf(col.copy(alpha = 0.35f), col.copy(alpha = 0.1f), Color.Transparent), p, r), r, p)
        }
        drawRect(Color.White.copy(alpha = 0.035f))
        val drops = Random(9)
        repeat(if (rainy) 190 else 70) {
            val p = Offset(drops.nextFloat() * size.width, drops.nextFloat() * size.height)
            drop(p, size.minDimension * (0.003f + drops.nextFloat() * 0.009f))
        }
    }
    if (rainy) {
        AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 33L) { t ->
            val rr = Random(17)
            repeat(14) { i ->
                val x = rr.nextFloat() * size.width
                val speed = 0.05f + rr.nextFloat() * 0.1f
                val progress = (t * speed + rr.nextFloat()) % 1.3f
                val y = progress * size.height - size.height * 0.15f
                val r = size.minDimension * (0.008f + rr.nextFloat() * 0.008f)
                drawLine(Color.White.copy(alpha = 0.09f), Offset(x, y - size.height * 0.25f), Offset(x, y), r * 0.7f, cap = StrokeCap.Round)
                drop(Offset(x + sin(t * 2f + i) * r * 0.3f, y), r)
            }
        }
    }
    Column(Modifier.fillMaxSize().padding(22.dp)) {
        AutoSizeText(
            ctx.timeText, clockStyle(FontWeight.Thin).copy(shadow = Shadow(Color.Black.copy(alpha = 0.5f), Offset(0f, 3f), 16f)),
            Modifier.fillMaxWidth(if (ctx.isCompact) 0.8f else 0.5f).weight(0.32f), color = Color.White, sizingText = ctx.sizingTime(), alignment = Alignment.CenterStart
        )
        AutoSizeText(
            weather?.let { "${ctx.temperature(it.temperatureCelsius)}  ·  ${it.condition.displayName}" } ?: ctx.formatDate("EEEE d MMMM"),
            labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth(if (ctx.isCompact) 0.7f else 0.4f).weight(0.07f),
            color = Color.White.copy(alpha = 0.8f), sizingText = "28°  ·  Thunderstorm", alignment = Alignment.CenterStart
        )
        Spacer(Modifier.weight(0.61f))
    }
}

// ---------------------------------------------------------------------------------------------
// Silk — flowing ribbons of light in the accent colour
// ---------------------------------------------------------------------------------------------

@Composable
private fun CenterTime(
    ctx: FaceContext,
    color: Color,
    dateColor: Color,
    widthFraction: Float = 0.6f,
    alignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    topWeight: Float = 0.3f
) {
    val textAlign = if (alignment == Alignment.End) Alignment.CenterEnd else Alignment.Center
    Column(Modifier.fillMaxSize().padding(18.dp), horizontalAlignment = alignment) {
        Spacer(Modifier.weight(topWeight))
        AutoSizeText(
            ctx.timeText, clockStyle(FontWeight.ExtraLight).copy(shadow = Shadow(Color.Black.copy(alpha = 0.45f), Offset(0f, 3f), 18f)),
            Modifier.fillMaxWidth(widthFraction).weight(0.32f), color = color, sizingText = ctx.sizingTime(), alignment = textAlign
        )
        AutoSizeText(ctx.formatDate("EEEE · MMMM d").uppercase(), labelStyle(), Modifier.fillMaxWidth(widthFraction * 0.75f).weight(0.07f), color = dateColor, alignment = textAlign)
        Spacer(Modifier.weight(0.61f - topWeight))
    }
}

@Composable
fun SilkFace(ctx: FaceContext, modifier: Modifier = Modifier) = Bleed(modifier) {
    val accent = ctx.accent
    val second = lerpColor(accent, Color(0xFF6A5CFF), 0.6f)
    AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 33L) { t ->
        drawRect(Color.Black)
        val lines = 28
        val step = size.width / 48f
        for (i in 0 until lines) {
            val f = i / (lines - 1f)
            val path = Path()
            var x = -step
            var first = true
            while (x <= size.width + step) {
                val nx = x / size.width
                val y = size.height * (0.5f + 0.17f * sin(nx * 5.2f + t * 0.35f + f * 1.6f) + 0.1f * sin(nx * 2.1f - t * 0.22f + f * 3.1f) + (f - 0.5f) * 0.24f)
                if (first) path.moveTo(x, y) else path.lineTo(x, y)
                first = false
                x += step
            }
            val col = lerpColor(accent, second, f)
            drawPath(path, col.copy(alpha = 0.1f + 0.24f * (1f - abs(f - 0.5f) * 2f)), style = Stroke(size.minDimension * 0.004f), blendMode = BlendMode.Plus)
        }
    }
    CenterTime(ctx, Ink.TextPrimary, Ink.TextSecondary)
}

// ---------------------------------------------------------------------------------------------
// Sakura — a cherry branch under the moon, petals drifting on the breeze
// ---------------------------------------------------------------------------------------------

private val Petal = Color(0xFFFFB7C9)

private fun DrawScope.branch(rnd: Random, start: Offset, angle: Float, length: Float, width: Float, depth: Int) {
    val end = Offset(start.x + cos(angle) * length, start.y + sin(angle) * length)
    val bend = Offset((start.x + end.x) / 2f + sin(angle) * length * 0.12f, (start.y + end.y) / 2f - cos(angle) * length * 0.12f)
    val path = Path().apply {
        moveTo(start.x, start.y)
        quadraticTo(bend.x, bend.y, end.x, end.y)
    }
    drawPath(path, Color(0xFF1A0E14), style = Stroke(width, cap = StrokeCap.Round))
    if (depth == 0) {
        repeat(6) {
            val p = Offset(end.x + (rnd.nextFloat() - 0.5f) * length * 0.9f, end.y + (rnd.nextFloat() - 0.5f) * length * 0.9f)
            val r = length * (0.12f + rnd.nextFloat() * 0.14f)
            val col = listOf(Petal, Color(0xFFFF9EBB), Color(0xFFFFE3EC))[rnd.nextInt(3)]
            drawCircle(col.copy(alpha = 0.85f), r, p)
            drawCircle(Color(0xFFD9466F).copy(alpha = 0.6f), r * 0.22f, p)
        }
        return
    }
    val spread = 0.3f + rnd.nextFloat() * 0.35f
    branch(rnd, end, angle - spread, length * (0.68f + rnd.nextFloat() * 0.1f), width * 0.66f, depth - 1)
    branch(rnd, end, angle + spread * 0.8f, length * (0.62f + rnd.nextFloat() * 0.1f), width * 0.62f, depth - 1)
}

@Composable
fun SakuraFace(ctx: FaceContext, modifier: Modifier = Modifier) = Bleed(modifier) {
    Canvas(Modifier.fillMaxSize().graphicsLayer()) {
        if (size.minDimension < 2f) return@Canvas
        drawRect(Brush.verticalGradient(listOf(Color(0xFF0D0B1E), Color(0xFF2A1638), Color(0xFF5A2A4A))))
        val moon = Offset(size.width * 0.8f, size.height * 0.24f)
        val mr = size.minDimension * 0.08f
        drawCircle(safeRadialGradient(listOf(Color(0xFFFFE9F0).copy(alpha = 0.35f), Color.Transparent), moon, mr * 4f), mr * 4f, moon)
        drawCircle(Color(0xFFFFF4EC), mr, moon)
        branch(Random(4), Offset(-size.width * 0.02f, size.height * 0.04f), 0.32f, size.minDimension * 0.32f, size.minDimension * 0.045f, 5)
    }
    AnimatedCanvas(ctx, Modifier.fillMaxSize(), frameIntervalMs = 33L) { t ->
        val rnd = Random(8)
        repeat(26) { i ->
            val speed = 0.03f + rnd.nextFloat() * 0.05f
            val progress = (t * speed + rnd.nextFloat()) % 1.2f
            val x = size.width * (rnd.nextFloat() * 1.1f - progress * 0.25f) + sin(t * 0.8f + i) * size.width * 0.03f
            val y = progress * size.height - size.height * 0.1f
            val rw = size.minDimension * (0.008f + rnd.nextFloat() * 0.008f)
            rotate((t * (40f + i * 7f)) % 360f, Offset(x, y)) {
                drawOval(Petal.copy(alpha = 0.85f), Offset(x - rw, y - rw * 0.55f), Size(rw * 2f, rw * 1.1f))
            }
        }
    }
    // On small tiles the branch fills the top, so the time sits lower.
    CenterTime(
        ctx, Color(0xFFFFF1F5), Color(0xFFFFB7C9),
        widthFraction = if (ctx.isCompact) 0.7f else 0.45f,
        alignment = if (ctx.isCompact) Alignment.CenterHorizontally else Alignment.End,
        topWeight = if (ctx.isCompact) 0.5f else 0.3f
    )
}
