package com.standbypro.ui.faces

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import com.standbypro.astro.Astronomy
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.Dial
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.HandAngles
import com.standbypro.ui.design.HandStyle
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.TABULAR
import com.standbypro.ui.design.appliedIndices
import com.standbypro.ui.design.dialDisc
import com.standbypro.ui.design.dotIndices
import com.standbypro.ui.design.drawWeatherGlyph
import com.standbypro.ui.design.hand
import com.standbypro.ui.design.hub
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.design.numerals
import com.standbypro.ui.design.progressArc
import com.standbypro.ui.design.railTrack
import com.standbypro.ui.design.safeRadialGradient
import com.standbypro.ui.design.secondHand
import com.standbypro.ui.design.subDial
import com.standbypro.ui.design.sunburst
import com.standbypro.ui.design.textAt
import com.standbypro.ui.design.ticks
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

// ---------------------------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------------------------

internal fun DrawScope.numeralStyle(d: Dial, size: Float, weight: FontWeight = FontWeight.Bold, family: FontFamily = FaceFonts.Sans): TextStyle =
    TextStyle(fontFamily = family, fontWeight = weight, fontSize = (d.radius * size).toSp(), fontFeatureSettings = TABULAR)

internal val arabic12: List<String> = listOf("12") + (1..11).map { it.toString() }
internal val roman12: List<String> = listOf("XII") + (1..11).map { FaceMath.roman(it) }

/** Standard hand set; seconds hand is skipped when seconds are hidden. */
internal fun DrawScope.handSet(
    d: Dial,
    a: HandAngles,
    ctx: FaceContext,
    hourColor: Color = Ink.TextPrimary,
    minuteColor: Color = hourColor,
    secondColor: Color = ctx.accent,
    style: HandStyle = HandStyle.BATON,
    hourLength: Float = 0.5f,
    minuteLength: Float = 0.78f,
    hourWidth: Float = 0.075f,
    minuteWidth: Float = 0.05f,
    lume: Color? = null,
    secondDisc: Float = 0f,
    secondLength: Float = 0.88f,
    hubColor: Color? = null
) {
    hand(d, a.hour, hourLength, hourWidth, hourColor, style, tail = 0.1f, lume = lume)
    hand(d, a.minute, minuteLength, minuteWidth, minuteColor, style, tail = 0.12f, lume = lume)
    if (ctx.showSeconds) {
        secondHand(d, a.second, secondColor, length = secondLength, disc = secondDisc)
        hub(d, hubColor ?: secondColor)
    } else {
        hub(d, hubColor ?: hourColor)
    }
}

/** Framed date aperture centred at [at]. */
internal fun DrawScope.dateWindow(
    m: TextMeasurer,
    text: String,
    at: Offset,
    d: Dial,
    width: Float = 0.2f,
    height: Float = 0.14f,
    bg: Color = Color(0xFFF4F1EA),
    fg: Color = Color(0xFF111111),
    frame: Color = Ink.Steel
) {
    val w = d.px(width)
    val h = d.px(height)
    val tl = Offset(at.x - w / 2, at.y - h / 2)
    drawRoundRect(frame, Offset(tl.x - d.px(0.012f), tl.y - d.px(0.012f)), Size(w + d.px(0.024f), h + d.px(0.024f)), CornerRadius(d.px(0.02f)))
    drawRoundRect(bg, tl, Size(w, h), CornerRadius(d.px(0.012f)))
    textAt(m, text, at, numeralStyle(d, height * 0.68f, FontWeight.Bold), fg)
}

private fun DrawScope.brandText(m: TextMeasurer, d: Dial, text: String, at: Offset, color: Color, size: Float = 0.055f) {
    textAt(m, text, at, TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.Medium, fontSize = (d.radius * size).toSp(), letterSpacing = androidx.compose.ui.unit.TextUnit(0.25f, androidx.compose.ui.unit.TextUnitType.Em)), color)
}

private fun DrawScope.gear(center: Offset, radius: Float, teeth: Int, angle: Float, color: Color, spokes: Int = 5) {
    val path = Path()
    val toothDepth = radius * 0.12f
    for (i in 0 until teeth * 2) {
        val a = (angle + i * 180f / teeth) * PI.toFloat() / 180f
        val r = if (i % 2 == 0) radius else radius - toothDepth
        val a2 = (angle + (i + 1) * 180f / teeth) * PI.toFloat() / 180f
        val p1 = Offset(center.x + cos(a) * r, center.y + sin(a) * r)
        val p2 = Offset(center.x + cos(a2) * r, center.y + sin(a2) * r)
        if (i == 0) path.moveTo(p1.x, p1.y) else path.lineTo(p1.x, p1.y)
        path.lineTo(p2.x, p2.y)
    }
    path.close()
    drawPath(path, color, style = Stroke(radius * 0.06f))
    drawCircle(color, radius * 0.72f, center, style = Stroke(radius * 0.05f))
    for (s in 0 until spokes) {
        val a = (angle + s * 360f / spokes) * PI.toFloat() / 180f
        drawLine(color, center, Offset(center.x + cos(a) * radius * 0.72f, center.y + sin(a) * radius * 0.72f), radius * 0.07f)
    }
    drawCircle(color, radius * 0.14f, center)
}

// ---------------------------------------------------------------------------------------------
// Faces
// ---------------------------------------------------------------------------------------------

/** iOS StandBy-inspired analog: bold numerals, white pill hands, orange sweep. */
@Composable
fun AnalogStandByFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            ticks(d, majorEvery = 5, outer = 0.99f, majorLength = 0.07f, minorLength = 0.035f, majorWidth = 0.014f, minorWidth = 0.008f,
                majorColor = Color.White.copy(alpha = 0.85f), minorColor = Color.White.copy(alpha = 0.32f), cap = StrokeCap.Round)
            numerals(d, m, arabic12, 0.76f, numeralStyle(d, 0.2f, FontWeight.SemiBold), Ink.TextPrimary)
        },
        handsLayer = { d, a ->
            for ((angle, len, w) in listOf(Triple(a.hour, 0.5f, 0.07f), Triple(a.minute, 0.8f, 0.05f))) {
                hand(d, angle, 0.16f, 0.022f, Ink.TextPrimary, HandStyle.BATON, tail = 0f, shadow = false)
                hand(d, angle, len, w, Ink.TextPrimary, HandStyle.BATON, tail = -0.16f)
            }
            if (ctx.showSeconds) secondHand(d, a.second, ctx.accent, length = 0.9f, tail = 0.16f, width = 0.014f, counterweight = 0f)
            hub(d, if (ctx.showSeconds) ctx.accent else Ink.TextPrimary, 0.04f, Color.Black)
        }
    )
}

@Composable
fun AnalogClassicFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF26272B), Color(0xFF0E0E10), ring = Color(0xFF3A3B40))
            railTrack(d, 0.95f, 0.88f, Color.White.copy(alpha = 0.55f))
            numerals(d, m, arabic12, 0.72f, numeralStyle(d, 0.17f, FontWeight.Normal, FaceFonts.Serif), Ink.TextPrimary)
            brandText(m, d, "STANDBY", d.point(0f, 0.42f), Ink.TextSecondary)
        },
        handsLayer = { d, a -> handSet(d, a, ctx, style = HandStyle.SPADE, hourWidth = 0.06f, minuteWidth = 0.04f, secondColor = Ink.Red) }
    )
}

@Composable
fun AnalogModernFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF1C1C1E), Color(0xFF141416))
            ticks(d, majorEvery = 15, outer = 0.95f, majorLength = 0.16f, minorLength = 0.03f, majorWidth = 0.03f, minorWidth = 0.006f, minorColor = Color.White.copy(alpha = 0.3f))
            numerals(d, m, listOf("12", null, null, "3", null, null, "6", null, null, "9", null, null), 0.66f, numeralStyle(d, 0.13f, FontWeight.Light), Ink.TextSecondary)
        },
        handsLayer = { d, a -> handSet(d, a, ctx, style = HandStyle.RAIL, hourWidth = 0.05f, minuteWidth = 0.035f, hourLength = 0.52f, minuteLength = 0.84f) }
    )
}

@Composable
fun AnalogLuxuryFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    val day = ctx.time.dayOfMonth.toString()
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF233353), Color(0xFF0A1020), ring = Ink.Gold, ringWidth = 0.02f)
            sunburst(d, Color.White, rays = 160)
            ticks(d, majorEvery = 5, outer = 0.97f, majorLength = 0.0f, minorLength = 0.03f, minorWidth = 0.006f, minorColor = Ink.Champagne.copy(alpha = 0.6f))
            appliedIndices(d, Ink.GoldLight, outer = 0.92f, length = 0.15f, width = 0.05f, skip = setOf(3))
            dateWindow(m, day, d.point(90f, 0.72f), d)
            brandText(m, d, "STANDBY PRO", d.point(0f, 0.4f), Ink.Champagne)
            brandText(m, d, "AUTOMATIC", d.point(180f, 0.42f), Ink.Champagne.copy(alpha = 0.6f), 0.045f)
        },
        handsLayer = { d, a ->
            handSet(d, a, ctx, hourColor = Ink.GoldLight, style = HandStyle.DAUPHINE, hourWidth = 0.075f, minuteWidth = 0.06f, secondColor = Ink.GoldLight, lume = null)
        }
    )
}

@Composable
fun ChronographFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 48)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF2B2D31), Color(0xFF141518), ring = Color(0xFF4A4C52))
            // Tachymeter
            val tachy = listOf(500, 400, 300, 250, 200, 180, 160, 140, 120, 110, 100, 90, 80, 75, 70, 65, 60)
            tachy.forEach { v ->
                val angle = 21600f / v
                textAt(m, v.toString(), d.point(angle, 0.93f), numeralStyle(d, 0.045f, FontWeight.Medium), Ink.TextSecondary)
            }
            drawCircle(Color.White.copy(alpha = 0.25f), d.px(0.86f), d.center, style = Stroke(d.px(0.006f)))
            ticks(d, count = 240, majorEvery = 4, outer = 0.86f, majorLength = 0.04f, minorLength = 0.02f, majorWidth = 0.008f, minorWidth = 0.004f, majorColor = Color.White.copy(alpha = 0.7f), minorColor = Color.White.copy(alpha = 0.25f))
            appliedIndices(d, Ink.Steel, outer = 0.8f, length = 0.12f, width = 0.035f, skip = setOf(3, 6, 9))
            for (sd in listOf(d.sub(270f, 0.4f, 0.22f), d.sub(90f, 0.4f, 0.22f), d.sub(180f, 0.4f, 0.22f))) {
                subDial(sd, Color(0xFF101113), Color.White.copy(alpha = 0.5f), ticks = 12, ring = Color.White.copy(alpha = 0.12f))
            }
            brandText(m, d, "CHRONOGRAPH", d.point(0f, 0.45f), Ink.TextSecondary, 0.045f)
        },
        handsLayer = { d, a ->
            // 24h at 9, 30-minute register at 3, 12-hour register at 6.
            val left = d.sub(270f, 0.4f, 0.22f)
            val right = d.sub(90f, 0.4f, 0.22f)
            val bottom = d.sub(180f, 0.4f, 0.22f)
            hand(left, a.hour24, 0.82f, 0.08f, Ink.TextPrimary, HandStyle.NEEDLE, tail = 0.15f, shadow = false)
            hand(right, (a.minute / 6f % 30f) * 12f, 0.82f, 0.08f, ctx.accent, HandStyle.NEEDLE, tail = 0.15f, shadow = false)
            hand(bottom, a.hour, 0.82f, 0.08f, Ink.TextPrimary, HandStyle.NEEDLE, tail = 0.15f, shadow = false)
            for (sd in listOf(left, right, bottom)) hub(sd, Ink.Steel, 0.1f)
            handSet(d, a, ctx, style = HandStyle.BATON, hourWidth = 0.06f, minuteWidth = 0.04f, lume = Ink.Lume, secondLength = 0.92f)
        }
    )
}

@Composable
fun PilotFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF17191C), Color(0xFF0B0C0E))
            ticks(d, majorEvery = 5, outer = 0.97f, majorLength = 0.06f, minorLength = 0.04f, majorWidth = 0.012f, minorWidth = 0.007f, minorColor = Color.White.copy(alpha = 0.7f))
            numerals(d, m, listOf(null) + (1..11).map { it.toString() }, 0.74f, numeralStyle(d, 0.2f, FontWeight.Medium), Color(0xFFF2EFE6))
            val top = d.point(0f, 0.9f)
            val tri = Path().apply {
                moveTo(top.x, d.center.y - d.px(0.62f))
                lineTo(top.x - d.px(0.09f), d.center.y - d.px(0.86f))
                lineTo(top.x + d.px(0.09f), d.center.y - d.px(0.86f))
                close()
            }
            drawPath(tri, Color(0xFFF2EFE6))
            drawCircle(Color(0xFFF2EFE6), d.px(0.022f), Offset(top.x - d.px(0.13f), d.center.y - d.px(0.86f)))
            drawCircle(Color(0xFFF2EFE6), d.px(0.022f), Offset(top.x + d.px(0.13f), d.center.y - d.px(0.86f)))
        },
        handsLayer = { d, a -> handSet(d, a, ctx, hourColor = Color(0xFFE8E4DA), style = HandStyle.SWORD, hourWidth = 0.08f, minuteWidth = 0.06f, lume = Color(0xFFDDE8C8), secondColor = Color(0xFFE8E4DA)) }
    )
}

@Composable
fun DiverFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            // Ceramic bezel
            drawCircle(Brush.sweepGradient(listOf(Color(0xFF1A1C20), Color(0xFF2E3138), Color(0xFF1A1C20), Color(0xFF2E3138), Color(0xFF1A1C20)), d.center), d.radius, d.center)
            for (i in 0 until 60) {
                val a = i * 6f
                if (i == 0) continue
                if (i % 10 == 0) {
                    textAt(m, i.toString(), d.point(a, 0.9f), numeralStyle(d, 0.1f, FontWeight.Bold), Color(0xFFE6E6E6))
                } else if (i < 15 || i % 5 == 0) {
                    drawLine(Color(0xFFE6E6E6), d.point(a, 0.84f), d.point(a, if (i % 5 == 0) 0.96f else 0.92f), d.px(if (i % 5 == 0) 0.02f else 0.01f))
                }
            }
            val tri = Path().apply {
                moveTo(d.center.x, d.center.y - d.px(0.84f))
                lineTo(d.center.x - d.px(0.075f), d.center.y - d.px(0.98f))
                lineTo(d.center.x + d.px(0.075f), d.center.y - d.px(0.98f))
                close()
            }
            drawPath(tri, Color(0xFFE6E6E6))
            drawCircle(Ink.Lume, d.px(0.02f), Offset(d.center.x, d.center.y - d.px(0.93f)))
            // Dial
            dialDisc(d, Color(0xFF0A0B0D), Color(0xFF050506), fraction = 0.8f)
            ticks(d, majorEvery = 5, outer = 0.78f, majorLength = 0.0f, minorLength = 0.025f, minorWidth = 0.006f, minorColor = Color.White.copy(alpha = 0.55f))
            for (i in 0 until 12) {
                val p = d.point(i * 30f, 0.66f)
                when (i) {
                    0 -> {
                        val t = Path().apply {
                            moveTo(p.x, p.y + d.px(0.07f))
                            lineTo(p.x - d.px(0.07f), p.y - d.px(0.07f))
                            lineTo(p.x + d.px(0.07f), p.y - d.px(0.07f))
                            close()
                        }
                        drawPath(t, Color(0xFFD9D9D9))
                        drawPath(t, Ink.Lume, style = Stroke(d.px(0.0f)))
                    }
                    3, 6, 9 -> rotate(i * 30f, p) {
                        drawRoundRect(Color(0xFFD9D9D9), Offset(p.x - d.px(0.035f), p.y - d.px(0.08f)), Size(d.px(0.07f), d.px(0.16f)), CornerRadius(d.px(0.01f)))
                        drawRoundRect(Ink.Lume, Offset(p.x - d.px(0.025f), p.y - d.px(0.07f)), Size(d.px(0.05f), d.px(0.14f)), CornerRadius(d.px(0.008f)))
                    }
                    else -> {
                        drawCircle(Color(0xFFD9D9D9), d.px(0.055f), p)
                        drawCircle(Ink.Lume, d.px(0.045f), p)
                    }
                }
            }
            brandText(m, d, "SUBMERSIBLE  300M", d.point(180f, 0.36f), Ink.Red, 0.04f)
        },
        handsLayer = { d, a ->
            handSet(d, a, ctx, hourColor = Color(0xFFDADADA), style = HandStyle.SWORD, hourWidth = 0.09f, minuteWidth = 0.065f, hourLength = 0.44f, minuteLength = 0.7f,
                lume = Ink.Lume, secondColor = Color(0xFFDADADA), secondDisc = 0.035f, secondLength = 0.76f)
        }
    )
}

@Composable
fun SkeletonFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    AnalogFace(
        ctx, modifier,
        motion = com.standbypro.ui.design.SecondsMotion.SWEEP,
        dialLayer = { d ->
            drawCircle(Color(0xFF0B0B0C), d.radius, d.center)
            drawCircle(Ink.Gold.copy(alpha = 0.8f), d.px(0.97f), d.center, style = Stroke(d.px(0.03f)))
            drawCircle(Ink.Gold.copy(alpha = 0.35f), d.px(0.86f), d.center, style = Stroke(d.px(0.005f)))
            numerals(d, m, roman12, 0.91f, numeralStyle(d, 0.07f, FontWeight.Medium, FaceFonts.Serif), Ink.Champagne, rotateWithDial = true)
            // Bridges
            val bridge = Ink.Gold.copy(alpha = 0.22f)
            drawArc(bridge, 200f, 140f, false, Offset(d.center.x - d.px(0.62f), d.center.y - d.px(0.62f)), Size(d.px(1.24f), d.px(1.24f)), style = Stroke(d.px(0.08f), cap = StrokeCap.Round))
            drawArc(bridge, 20f, 140f, false, Offset(d.center.x - d.px(0.5f), d.center.y - d.px(0.5f)), Size(d.px(1.0f), d.px(1.0f)), style = Stroke(d.px(0.07f), cap = StrokeCap.Round))
            for (jewel in listOf(d.point(300f, 0.36f), d.point(60f, 0.42f), d.point(180f, 0.44f), d.point(140f, 0.2f))) {
                drawCircle(Color(0xFF9B1B30), d.px(0.025f), jewel)
                drawCircle(Color.White.copy(alpha = 0.5f), d.px(0.008f), Offset(jewel.x - d.px(0.008f), jewel.y - d.px(0.008f)))
            }
        },
        handsLayer = { d, a ->
            val t = a.second / 6f
            gear(d.point(300f, 0.36f), d.px(0.24f), 18, t * 6f, Ink.Gold.copy(alpha = 0.55f))
            gear(d.point(60f, 0.42f), d.px(0.18f), 14, -t * 7.7f, Ink.Steel.copy(alpha = 0.45f))
            gear(d.point(140f, 0.2f), d.px(0.12f), 10, t * 11f, Ink.Gold.copy(alpha = 0.45f))
            // Balance wheel oscillating at 4 Hz amplitude.
            val bc = d.point(180f, 0.44f)
            val swing = sin(t * 2f * PI.toFloat() * 2.5f) * 130f
            rotate(swing, bc) {
                drawCircle(Ink.Gold.copy(alpha = 0.75f), d.px(0.16f), bc, style = Stroke(d.px(0.022f)))
                for (k in 0 until 4) {
                    val ang = k * 90f * PI.toFloat() / 180f
                    drawLine(Ink.Gold.copy(alpha = 0.75f), bc, Offset(bc.x + cos(ang) * d.px(0.16f), bc.y + sin(ang) * d.px(0.16f)), d.px(0.012f))
                }
            }
            drawCircle(Ink.Steel.copy(alpha = 0.4f), d.px(0.06f), bc, style = Stroke(d.px(0.006f)))
            handSet(d, a, ctx, hourColor = Ink.Champagne, style = HandStyle.SKELETON, hourWidth = 0.08f, minuteWidth = 0.06f, lume = Ink.Lume.copy(alpha = 0.8f))
        }
    )
}

@Composable
fun RomanFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF16161A), Color(0xFF0A0A0C), ring = Color(0xFF2E2E34))
            railTrack(d, 0.96f, 0.9f, Color.White.copy(alpha = 0.45f))
            numerals(d, m, roman12, 0.76f, numeralStyle(d, 0.13f, FontWeight.Normal, FaceFonts.Serif), Ink.TextPrimary, rotateWithDial = true)
            drawCircle(Color.White.copy(alpha = 0.08f), d.px(0.6f), d.center, style = Stroke(d.px(0.004f)))
        },
        handsLayer = { d, a -> handSet(d, a, ctx, hourColor = Ink.Steel, style = HandStyle.LEAF, hourWidth = 0.06f, minuteWidth = 0.045f, hourLength = 0.5f, minuteLength = 0.8f) }
    )
}

@Composable
fun PocketWatchFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val blued = Color(0xFF1F3A93)
    AnalogFace(
        ctx, modifier, fill = 0.82f,
        dialLayer = { d ->
            // Crown and bow
            val crown = Offset(d.center.x, d.center.y - d.px(1.1f))
            drawCircle(Ink.Gold, d.px(0.16f), Offset(crown.x, crown.y - d.px(0.08f)), style = Stroke(d.px(0.035f)))
            drawRoundRect(Brush.verticalGradient(listOf(Ink.GoldLight, Ink.Gold)), Offset(crown.x - d.px(0.07f), crown.y), Size(d.px(0.14f), d.px(0.12f)), CornerRadius(d.px(0.02f)))
            drawCircle(safeRadialGradient(listOf(Ink.GoldLight, Color(0xFF8C6A1F)), d.center, d.px(1.06f)), d.px(1.06f), d.center)
            dialDisc(d, Color(0xFFFBF8F0), Color(0xFFE9E2D1))
            railTrack(d, 0.95f, 0.89f, Color(0xFF2A2A2A))
            numerals(d, m, roman12, 0.74f, numeralStyle(d, 0.14f, FontWeight.Normal, FaceFonts.Serif), Color(0xFF151515), rotateWithDial = true)
            val sd = d.sub(180f, 0.45f, 0.2f)
            drawCircle(Color(0xFF2A2A2A), sd.radius, sd.center, style = Stroke(d.px(0.006f)))
            for (i in 0 until 60) {
                drawLine(Color(0xFF2A2A2A), sd.point(i * 6f, if (i % 5 == 0) 0.75f else 0.86f), sd.point(i * 6f, 0.97f), d.px(0.004f))
            }
        },
        handsLayer = { d, a ->
            val sd = d.sub(180f, 0.45f, 0.2f)
            if (ctx.showSeconds) {
                hand(sd, a.second, 0.9f, 0.05f, blued, HandStyle.NEEDLE, tail = 0.2f, shadow = false)
            }
            hub(sd, blued, 0.1f, blued)
            hand(d, a.hour, 0.5f, 0.02f, blued, HandStyle.BREGUET, tail = 0.1f)
            hand(d, a.minute, 0.8f, 0.016f, blued, HandStyle.BREGUET, tail = 0.12f)
            hub(d, blued, 0.035f, blued)
        }
    )
}

@Composable
fun ArtDecoFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            val oct = Path().apply {
                for (i in 0 until 8) {
                    val p = d.point(i * 45f + 22.5f, 1.0f)
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
                close()
            }
            drawPath(oct, Brush.linearGradient(listOf(Ink.GoldLight, Color(0xFF8C6A1F), Ink.GoldLight)))
            dialDisc(d, Color(0xFF0F3B33), Color(0xFF051512), fraction = 0.88f)
            for (i in 0 until 48) {
                drawLine(Ink.Gold.copy(alpha = if (i % 2 == 0) 0.18f else 0.08f), d.center, d.point(i * 7.5f, 0.88f), d.px(0.01f))
            }
            drawCircle(Color(0xFF051512), d.px(0.3f), d.center)
            numerals(d, m, listOf("12", null, null, "3", null, null, "6", null, null, "9", null, null), 0.68f, numeralStyle(d, 0.2f, FontWeight.Bold, FaceFonts.CondensedBold), Ink.GoldLight)
            for (i in 0 until 12) {
                if (i % 3 == 0) continue
                val a = i * 30f
                for (s in 0 until 3) {
                    drawLine(Ink.GoldLight, d.point(a - 1.6f + s * 1.6f, 0.78f - s * 0.0f), d.point(a - 1.6f + s * 1.6f, 0.84f - (if (s == 1) 0f else 0.03f)), d.px(0.012f))
                }
            }
        },
        handsLayer = { d, a -> handSet(d, a, ctx, hourColor = Ink.GoldLight, style = HandStyle.SWORD, hourWidth = 0.06f, minuteWidth = 0.045f, secondColor = Ink.GoldLight, hourLength = 0.46f, minuteLength = 0.72f) }
    )
}

@Composable
fun NordicFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 8)
    val day = ctx.time.dayOfMonth.toString()
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF232427), Color(0xFF1B1C1E))
            ticks(d, count = 12, majorEvery = 1, outer = 0.9f, majorLength = 0.06f, majorWidth = 0.012f, majorColor = Color.White.copy(alpha = 0.65f), cap = StrokeCap.Round)
            textAt(m, day, d.point(180f, 0.5f), numeralStyle(d, 0.09f, FontWeight.Medium), ctx.accent)
        },
        handsLayer = { d, a -> handSet(d, a, ctx, hourColor = Color(0xFFEDEDED), style = HandStyle.RAIL, hourWidth = 0.03f, minuteWidth = 0.022f, hourLength = 0.48f, minuteLength = 0.82f) }
    )
}

@Composable
fun CompassFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF14202A), Color(0xFF0A1016), ring = Color(0xFF2B4152))
            for (i in 0 until 72) {
                val a = i * 5f
                drawLine(Color.White.copy(alpha = if (i % 6 == 0) 0.8f else 0.3f), d.point(a, 0.84f), d.point(a, if (i % 6 == 0) 0.94f else 0.9f), d.px(0.008f))
            }
            listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f).forEach { (l, a) ->
                textAt(m, l, d.point(a, 0.73f), numeralStyle(d, 0.13f, FontWeight.Bold), if (l == "N") Ink.Red else Ink.TextPrimary)
            }
            listOf(30, 60, 120, 150, 210, 240, 300, 330).forEach { deg ->
                textAt(m, deg.toString(), d.point(deg.toFloat(), 0.73f), numeralStyle(d, 0.055f, FontWeight.Medium), Ink.TextSecondary)
            }
            drawCircle(Color.White.copy(alpha = 0.1f), d.px(0.6f), d.center, style = Stroke(d.px(0.004f)))
        },
        handsLayer = { d, a -> handSet(d, a, ctx, style = HandStyle.ARROW, hourWidth = 0.06f, minuteWidth = 0.045f, secondColor = Ink.Red) }
    )
}

@Composable
fun SundialFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val stone = Color(0xFFB7AE9C)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            drawCircle(safeRadialGradient(listOf(Color(0xFFCFC6B3), Color(0xFF8E8676)), d.center, d.radius), d.radius, d.center)
            drawCircle(Color(0xFF6F6858), d.radius, d.center, style = Stroke(d.px(0.03f)))
            for (h in 6..18) {
                val angle = (h - 12) * 15f
                drawLine(Color(0xFF4E483C).copy(alpha = 0.7f), d.center, d.point(angle, 0.86f), d.px(if (h % 3 == 0) 0.012f else 0.006f))
                textAt(m, FaceMath.roman(if (h > 12) h - 12 else h, true), d.point(angle, 0.92f), numeralStyle(d, 0.07f, FontWeight.Medium, FaceFonts.Serif), Color(0xFF3C372E))
            }
            textAt(m, "TEMPUS FUGIT", d.point(180f, 0.45f), numeralStyle(d, 0.07f, FontWeight.Normal, FaceFonts.Serif), Color(0xFF4E483C))
        },
        handsLayer = { d, a ->
            val hours = a.hour24 / 15f
            val daylight = hours in 6f..18f
            val angle = ((hours - 12f) * 15f).coerceIn(-95f, 95f)
            val shadow = Path().apply {
                moveTo(d.center.x - d.px(0.03f), d.center.y)
                lineTo(d.point(angle, 0.84f).x, d.point(angle, 0.84f).y)
                lineTo(d.center.x + d.px(0.03f), d.center.y)
                close()
            }
            rotate(0f, d.center) {
                drawPath(shadow, Color(0xFF2A261F).copy(alpha = if (daylight) 0.65f else 0.18f))
            }
            // Gnomon
            drawLine(Color(0xFF5B5446), d.center, d.point(0f, 0.55f), d.px(0.04f), StrokeCap.Round)
            drawCircle(stone, d.px(0.05f), d.center)
            // Minutes are told by a bronze ring pointer for practical reading.
            val p = d.point(a.minute, 0.985f)
            drawCircle(Color(0xFF8C5A2B), d.px(0.03f), p)
        }
    )
}

@Composable
fun MoonphaseFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    val phase = remember(ctx.time.hour) { Astronomy.moonPhase(System.currentTimeMillis()) }
    val day = ctx.time.dayOfMonth.toString()
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF1A2440), Color(0xFF080C18), ring = Ink.Steel.copy(alpha = 0.6f))
            sunburst(d, Color.White, rays = 120)
            appliedIndices(d, Ink.Steel, outer = 0.92f, length = 0.12f, width = 0.035f, skip = setOf(6))
            // Moon aperture
            val apC = d.point(180f, 0.48f)
            val apR = d.px(0.24f)
            drawCircle(Color(0xFF0B1636), apR, apC)
            for (i in 0 until 14) {
                val sx = apC.x + cos(i * 2.1f) * apR * (0.3f + (i % 5) * 0.14f)
                val sy = apC.y + sin(i * 1.7f) * apR * (0.3f + (i % 4) * 0.15f)
                drawCircle(Color.White.copy(alpha = 0.6f), d.px(0.006f), Offset(sx, sy))
            }
            drawMoon(apC, apR * 0.72f, phase.fraction)
            drawCircle(Ink.Steel, apR, apC, style = Stroke(d.px(0.014f)))
            textAt(m, "${(phase.illumination * 100).toInt()}%", d.point(180f, 0.8f), numeralStyle(d, 0.06f, FontWeight.Medium), Ink.TextSecondary)
            dateWindow(m, day, d.point(90f, 0.66f), d, bg = Color(0xFF0B1636), fg = Ink.TextPrimary)
        },
        handsLayer = { d, a -> handSet(d, a, ctx, hourColor = Ink.Steel, style = HandStyle.DAUPHINE, hourWidth = 0.07f, minuteWidth = 0.055f, secondColor = Ink.Steel) }
    )
}

@Composable
fun RailwayFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            drawCircle(Color(0xFFB9BBC0), d.px(1.0f), d.center)
            dialDisc(d, Color(0xFFF7F7F5), Color(0xFFE9E9E6), fraction = 0.96f)
            ticks(d, majorEvery = 5, outer = 0.92f, majorLength = 0.24f, minorLength = 0.07f, majorWidth = 0.06f, minorWidth = 0.02f, majorColor = Color(0xFF111111), minorColor = Color(0xFF111111))
        },
        handsLayer = { d, a ->
            hand(d, a.hour, 0.55f, 0.08f, Color(0xFF111111), HandStyle.RAIL, tail = 0.16f)
            hand(d, a.minute, 0.86f, 0.065f, Color(0xFF111111), HandStyle.RAIL, tail = 0.18f)
            if (ctx.showSeconds) secondHand(d, a.second, Color(0xFFE2231A), length = 0.66f, tail = 0.26f, width = 0.02f, disc = 0.085f, counterweight = 0f)
            hub(d, Color(0xFFE2231A), 0.03f, Color(0xFFE2231A))
        }
    )
}

/** Apple-style Solar dial: a 24-hour track with the sun riding it, sky colour follows real solar elevation. */
@Composable
fun SolarFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val minuteKey = ctx.time.hour * 60 + ctx.time.minute
    val elevation = remember(minuteKey, ctx.latitude, ctx.longitude) { Astronomy.sunElevation(System.currentTimeMillis(), ctx.latitude, ctx.longitude) }
    val sun = remember(ctx.time.toLocalDate(), ctx.latitude, ctx.longitude) { Astronomy.sunTimes(ctx.time.toLocalDate(), ctx.latitude, ctx.longitude) }
    val sky = when {
        elevation < -12 -> listOf(Color(0xFF0B1026), Color(0xFF02030A))
        elevation < -4 -> listOf(Color(0xFF2B2463), Color(0xFF0B0C25))
        elevation < 4 -> listOf(Color(0xFFFF8A5B), Color(0xFF3B2A6B))
        elevation < 18 -> listOf(Color(0xFFFFC879), Color(0xFF4F7FD9))
        else -> listOf(Color(0xFF8FD3FF), Color(0xFF2867D8))
    }
    val timeText = ctx.timeText
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            drawCircle(Brush.verticalGradient(sky, startY = d.center.y - d.radius, endY = d.center.y + d.radius), d.radius, d.center)
            // 24h track: noon at the top.
            fun angleFor(t: java.time.LocalDateTime?): Float? = t?.let { ((it.hour + it.minute / 60f) * 15f + 180f) % 360f }
            val rise = angleFor(sun.sunrise)
            val set = angleFor(sun.sunset)
            if (rise != null && set != null) {
                val sweep = ((rise - set) + 360f) % 360f
                progressArc(d, 1f, 0.86f, 0.06f, Color.Black.copy(alpha = 0.35f), startAngle = set, sweepTotal = sweep, cap = StrokeCap.Butt)
            }
            for (h in 0 until 24) {
                val a = (h * 15f + 180f) % 360f
                drawLine(Color.White.copy(alpha = if (h % 6 == 0) 0.9f else 0.35f), d.point(a, 0.94f), d.point(a, if (h % 6 == 0) 0.99f else 0.97f), d.px(if (h % 6 == 0) 0.014f else 0.007f))
            }
            listOf(0 to "0", 6 to "6", 12 to "12", 18 to "18").forEach { (h, l) ->
                textAt(m, l, d.point((h * 15f + 180f) % 360f, 0.76f), numeralStyle(d, 0.08f, FontWeight.Medium), Color.White.copy(alpha = 0.8f))
            }
            textAt(m, timeText, d.center, numeralStyle(d, 0.28f, FontWeight.Light), Color.White)
            textAt(m, "${"%.0f".format(elevation)}° sun", Offset(d.center.x, d.center.y + d.px(0.3f)), numeralStyle(d, 0.07f, FontWeight.Medium), Color.White.copy(alpha = 0.7f))
        },
        handsLayer = { d, a ->
            val angle = (a.hour24 + 180f) % 360f
            val p = d.point(angle, 0.86f)
            drawCircle(Color(0xFFFFE08A).copy(alpha = 0.3f), d.px(0.12f), p)
            drawCircle(Color(0xFFFFE08A), d.px(0.06f), p)
            if (ctx.showSeconds) drawCircle(Color.White.copy(alpha = 0.85f), d.px(0.012f), d.point(a.second, 0.62f))
        }
    )
}

@Composable
fun MeridianFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            drawCircle(Color(0xFFF2F2F2), d.radius, d.center)
            drawCircle(Color.Black, d.px(0.8f), d.center)
            for (i in 0 until 60) {
                val a = i * 6f
                if (i % 5 == 0) {
                    textAt(m, if (i == 0) "60" else i.toString(), d.point(a, 0.9f), numeralStyle(d, 0.08f, FontWeight.Bold), Color.Black)
                } else {
                    drawLine(Color.Black, d.point(a, 0.86f), d.point(a, 0.95f), d.px(0.008f))
                }
            }
            numerals(d, m, arabic12, 0.66f, numeralStyle(d, 0.12f, FontWeight.Light), Ink.TextPrimary)
        },
        handsLayer = { d, a ->
            hand(d, a.hour, 0.5f, 0.07f, Color.Black, HandStyle.BATON, tail = 0.1f)
            hand(d, a.hour, 0.49f, 0.05f, Color.White, HandStyle.BATON, tail = 0.09f, shadow = false)
            hand(d, a.minute, 0.82f, 0.055f, Color.Black, HandStyle.BATON, tail = 0.12f)
            hand(d, a.minute, 0.81f, 0.035f, Color.White, HandStyle.BATON, tail = 0.11f, shadow = false)
            if (ctx.showSeconds) secondHand(d, a.second, ctx.accent, length = 0.92f)
            hub(d, ctx.accent)
        }
    )
}

@Composable
fun CaliforniaFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val lume = Color(0xFFE8D7AE)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF2B261D), Color(0xFF14110C), ring = Color(0xFF4D4434))
            ticks(d, majorEvery = 5, outer = 0.96f, majorLength = 0.0f, minorLength = 0.035f, minorWidth = 0.007f, minorColor = lume.copy(alpha = 0.5f))
            val labels = listOf("XII", "I", "II", null, "4", "5", "6", "7", "8", null, "X", "XI")
            numerals(d, m, labels, 0.76f, numeralStyle(d, 0.16f, FontWeight.Bold, FaceFonts.CondensedBold), lume)
            for (i in listOf(3, 9)) {
                rotate(i * 30f, d.center) {
                    drawRoundRect(lume, Offset(d.center.x - d.px(0.035f), d.center.y - d.px(0.9f)), Size(d.px(0.07f), d.px(0.2f)), CornerRadius(d.px(0.01f)))
                }
            }
        },
        handsLayer = { d, a -> handSet(d, a, ctx, hourColor = Color(0xFFE0E0E0), style = HandStyle.SWORD, hourWidth = 0.08f, minuteWidth = 0.06f, lume = lume, secondColor = lume) }
    )
}

@Composable
fun TypographFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 16)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            listOf("12" to 0f, "3" to 90f, "6" to 180f, "9" to 270f).forEach { (l, a) ->
                textAt(m, l, d.point(a, 0.72f), numeralStyle(d, 0.42f, FontWeight.ExtraLight), Ink.TextPrimary)
            }
            for (i in 0 until 12) if (i % 3 != 0) drawCircle(Color.White.copy(alpha = 0.6f), d.px(0.022f), d.point(i * 30f, 0.86f))
        },
        handsLayer = { d, a -> handSet(d, a, ctx, style = HandStyle.RAIL, hourWidth = 0.035f, minuteWidth = 0.025f, hourLength = 0.5f, minuteLength = 0.85f) }
    )
}

@Composable
fun ContourFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 48)
    val hourPos = (ctx.time.hour % 12) + ctx.time.minute / 60f
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            for (n in 0 until 12) {
                val distance = min(abs(n - hourPos), 12 - abs(n - hourPos))
                val closeness = (1f - distance / 3f).coerceIn(0f, 1f)
                val size = 0.1f + 0.16f * closeness
                val color = lerpColor(Color.White.copy(alpha = 0.45f), ctx.accent, closeness)
                textAt(m, if (n == 0) "12" else n.toString(), d.point(n * 30f, 0.8f - 0.04f * closeness), numeralStyle(d, size, if (closeness > 0.5f) FontWeight.Bold else FontWeight.Light), color)
            }
        },
        handsLayer = { d, a -> handSet(d, a, ctx, style = HandStyle.BATON, hourWidth = 0.05f, minuteWidth = 0.035f, hourLength = 0.42f, minuteLength = 0.66f) }
    )
}

@Composable
fun NumeralsDuoFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 8)
    val hourText = ctx.hour12.toString()
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            textAt(m, hourText, d.center, numeralStyle(d, if (hourText.length > 1) 1.05f else 1.4f, FontWeight.Black), ctx.accent.copy(alpha = 0.85f))
        },
        handsLayer = { d, a ->
            hand(d, a.minute, 0.92f, 0.03f, Ink.TextPrimary, HandStyle.RAIL, tail = 0.08f)
            hub(d, Ink.TextPrimary, 0.035f)
            if (ctx.showSeconds) drawCircle(Ink.TextPrimary, d.px(0.025f), d.point(a.second, 0.97f))
        }
    )
}

@Composable
fun TourbillonFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 24)
    AnalogFace(
        ctx, modifier,
        motion = com.standbypro.ui.design.SecondsMotion.SWEEP,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF1E1F24), Color(0xFF0C0C0F), ring = Ink.Rose.copy(alpha = 0.7f), ringWidth = 0.02f)
            ticks(d, majorEvery = 5, outer = 0.95f, majorLength = 0.0f, minorLength = 0.03f, minorWidth = 0.005f, minorColor = Ink.Rose.copy(alpha = 0.5f))
            numerals(d, m, roman12.mapIndexed { i, s -> if (i in 5..7) null else s }, 0.8f, numeralStyle(d, 0.1f, FontWeight.Normal, FaceFonts.Serif), Ink.Rose)
            val ap = d.sub(180f, 0.45f, 0.3f)
            drawCircle(Color(0xFF050506), ap.radius, ap.center)
            drawCircle(Ink.Rose, ap.radius, ap.center, style = Stroke(d.px(0.014f)))
        },
        handsLayer = { d, a ->
            val ap = d.sub(180f, 0.45f, 0.3f)
            val cage = a.second // one revolution per minute
            rotate(cage, ap.center) {
                for (k in 0 until 3) {
                    val ang = (k * 120f) * PI.toFloat() / 180f
                    drawLine(Ink.Steel, ap.center, Offset(ap.center.x + cos(ang) * ap.px(0.9f), ap.center.y + sin(ang) * ap.px(0.9f)), d.px(0.012f))
                }
                drawCircle(Ink.Steel, ap.px(0.9f), ap.center, style = Stroke(d.px(0.008f)))
                val swing = sin(a.second / 6f * 2f * PI.toFloat() * 3f) * 150f
                rotate(swing, ap.center) {
                    drawCircle(Ink.Gold, ap.px(0.55f), ap.center, style = Stroke(d.px(0.014f)))
                    drawLine(Ink.Gold, Offset(ap.center.x - ap.px(0.55f), ap.center.y), Offset(ap.center.x + ap.px(0.55f), ap.center.y), d.px(0.008f))
                }
                drawCircle(Color(0xFF9B1B30), ap.px(0.1f), ap.center)
            }
            hand(d, a.hour, 0.48f, 0.065f, Ink.Rose, HandStyle.DAUPHINE, tail = 0.1f)
            hand(d, a.minute, 0.78f, 0.05f, Ink.Rose, HandStyle.DAUPHINE, tail = 0.12f)
            hub(d, Ink.Rose)
        }
    )
}

@Composable
fun FieldFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 32)
    val cream = Color(0xFFEDE3C8)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF1D2117), Color(0xFF0D0F0A), ring = Color(0xFF3B4230))
            ticks(d, majorEvery = 5, outer = 0.97f, majorLength = 0.08f, minorLength = 0.04f, majorWidth = 0.016f, minorWidth = 0.008f, majorColor = cream, minorColor = cream.copy(alpha = 0.6f))
            numerals(d, m, arabic12, 0.74f, numeralStyle(d, 0.17f, FontWeight.Bold, FaceFonts.CondensedBold), cream)
            numerals(d, m, listOf("24") + (13..23).map { it.toString() }, 0.5f, numeralStyle(d, 0.065f, FontWeight.Bold), Ink.Red.copy(alpha = 0.9f))
        },
        handsLayer = { d, a -> handSet(d, a, ctx, hourColor = cream, style = HandStyle.PENCIL, hourWidth = 0.07f, minuteWidth = 0.05f, lume = Color(0xFFD7E8B5), secondColor = Ink.Red) }
    )
}

@Composable
fun RegulatorFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 48)
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFFF1EEE6), Color(0xFFDCD6C7), ring = Ink.Steel, ringWidth = 0.02f)
            railTrack(d, 0.96f, 0.9f, Color(0xFF222222))
            for (i in 1..12) textAt(m, (i * 5).toString(), d.point(i * 30f, 0.82f), numeralStyle(d, 0.065f, FontWeight.Medium), Color(0xFF222222))
            val hourSd = d.sub(0f, 0.42f, 0.26f)
            drawCircle(Color(0xFF222222), hourSd.radius, hourSd.center, style = Stroke(d.px(0.006f)))
            numerals(hourSd, m, roman12, 0.72f, numeralStyle(d, 0.05f, FontWeight.Normal, FaceFonts.Serif), Color(0xFF222222))
            val secSd = d.sub(180f, 0.42f, 0.22f)
            drawCircle(Color(0xFF222222), secSd.radius, secSd.center, style = Stroke(d.px(0.006f)))
            for (i in 0 until 60) drawLine(Color(0xFF222222), secSd.point(i * 6f, if (i % 5 == 0) 0.78f else 0.88f), secSd.point(i * 6f, 0.97f), d.px(0.004f))
        },
        handsLayer = { d, a ->
            val blued = Color(0xFF1F3A93)
            val hourSd = d.sub(0f, 0.42f, 0.26f)
            val secSd = d.sub(180f, 0.42f, 0.22f)
            hand(hourSd, a.hour, 0.7f, 0.1f, blued, HandStyle.LEAF, tail = 0.15f)
            hub(hourSd, blued, 0.08f, blued)
            if (ctx.showSeconds) hand(secSd, a.second, 0.88f, 0.05f, blued, HandStyle.NEEDLE, tail = 0.2f, shadow = false)
            hub(secSd, blued, 0.08f, blued)
            hand(d, a.minute, 0.92f, 0.02f, blued, HandStyle.BREGUET, tail = 0.16f)
            hub(d, blued, 0.03f, blued)
        }
    )
}

@Composable
fun MinimalDotFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dotIndices(d, 0.9f, 0.012f, Color.White.copy(alpha = 0.35f), count = 12, bigEvery = 3, bigScale = 1.8f)
            drawCircle(ctx.accent, d.px(0.03f), d.point(0f, 0.9f))
        },
        handsLayer = { d, a ->
            hand(d, a.hour, 0.5f, 0.045f, Ink.TextPrimary, HandStyle.BATON, tail = 0f)
            hand(d, a.minute, 0.78f, 0.03f, Ink.TextPrimary, HandStyle.BATON, tail = 0f)
            drawCircle(ctx.accent, d.px(0.03f), d.point(a.hour, 0.5f))
            hub(d, Ink.TextPrimary, 0.03f, Color.Black)
            if (ctx.showSeconds) drawCircle(ctx.accent, d.px(0.018f), d.point(a.second, 0.97f))
        }
    )
}

@Composable
fun BauhausColorFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            dialDisc(d, Color(0xFF16213E), Color(0xFF0E1630))
            val r = d.px(0.1f)
            val top = d.point(0f, 0.78f)
            val tri = Path().apply {
                moveTo(top.x, top.y - r)
                lineTo(top.x - r, top.y + r * 0.8f)
                lineTo(top.x + r, top.y + r * 0.8f)
                close()
            }
            drawPath(tri, Color(0xFFE63946))
            drawCircle(Color(0xFFFFC300), r, d.point(90f, 0.78f))
            val b = d.point(180f, 0.78f)
            drawRect(Color(0xFF3A86FF), Offset(b.x - r * 0.9f, b.y - r * 0.9f), Size(r * 1.8f, r * 1.8f))
            val l = d.point(270f, 0.78f)
            drawRoundRect(Color(0xFF2EC4B6), Offset(l.x - r * 1.2f, l.y - r * 0.35f), Size(r * 2.4f, r * 0.7f), CornerRadius(r * 0.35f))
            dotIndices(d, 0.8f, 0.018f, Color.White.copy(alpha = 0.6f), count = 12, bigEvery = 0)
        },
        handsLayer = { d, a -> handSet(d, a, ctx, style = HandStyle.BATON, hourWidth = 0.07f, minuteWidth = 0.045f, secondColor = Color(0xFFFFC300)) }
    )
}

/** Apple Infograph-style dial with four live circular complications. */
@Composable
fun InfographFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 64)
    val battery = ctx.battery.batteryPercent
    val charging = ctx.battery.isCharging
    val weather = ctx.weather
    val dayName = ctx.formatDate("EEE").uppercase()
    val day = ctx.time.dayOfMonth.toString()
    val alarm = ctx.nextAlarmTime
    val tempText = weather?.let { ctx.temperature(it.temperatureCelsius) } ?: "--"
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            drawCircle(Color(0xFF0E0E10), d.radius, d.center)
            for (i in 0 until 60) {
                val a = i * 6f
                if (i % 5 == 0) {
                    textAt(m, if (i == 0) "60" else i.toString(), d.point(a, 0.92f), numeralStyle(d, 0.055f, FontWeight.SemiBold), Ink.TextSecondary)
                } else {
                    drawLine(Color.White.copy(alpha = 0.3f), d.point(a, 0.9f), d.point(a, 0.95f), d.px(0.006f))
                }
            }
            // Battery (top-left)
            val b = d.sub(315f, 0.46f, 0.2f)
            progressArc(b, battery / 100f, 0.9f, 0.14f, if (battery < 20) Ink.Red else Ink.Green, Color.White.copy(alpha = 0.12f))
            textAt(m, "$battery", b.center, numeralStyle(d, 0.08f, FontWeight.Bold), Ink.TextPrimary)
            textAt(m, if (charging) "⚡" else "%", Offset(b.center.x, b.center.y + b.px(0.55f)), numeralStyle(d, 0.04f), Ink.TextSecondary)
            // Weather (top-right)
            val w = d.sub(45f, 0.46f, 0.2f)
            drawCircle(Color.White.copy(alpha = 0.08f), w.radius, w.center)
            if (weather != null) {
                val span = (weather.highCelsius - weather.lowCelsius).coerceAtLeast(1)
                progressArc(w, ((weather.temperatureCelsius - weather.lowCelsius).toFloat() / span), 0.9f, 0.14f, Ink.Cyan, Color.White.copy(alpha = 0.12f), startAngle = 225f, sweepTotal = 270f)
            }
            textAt(m, tempText, w.center, numeralStyle(d, 0.08f, FontWeight.Bold), Ink.TextPrimary)
            // Date (bottom-left)
            val c = d.sub(225f, 0.46f, 0.2f)
            drawCircle(Color.White.copy(alpha = 0.08f), c.radius, c.center)
            textAt(m, dayName, Offset(c.center.x, c.center.y - c.px(0.35f)), numeralStyle(d, 0.045f, FontWeight.Bold), Ink.Red)
            textAt(m, day, Offset(c.center.x, c.center.y + c.px(0.15f)), numeralStyle(d, 0.1f, FontWeight.Medium), Ink.TextPrimary)
            // Alarm (bottom-right)
            val al = d.sub(135f, 0.46f, 0.2f)
            drawCircle(Color.White.copy(alpha = 0.08f), al.radius, al.center)
            textAt(m, "ALARM", Offset(al.center.x, al.center.y - al.px(0.35f)), numeralStyle(d, 0.035f, FontWeight.Bold), Ink.Orange)
            textAt(m, alarm ?: "OFF", Offset(al.center.x, al.center.y + al.px(0.12f)), numeralStyle(d, if (alarm != null) 0.05f else 0.06f, FontWeight.SemiBold), Ink.TextPrimary)
        },
        handsLayer = { d, a ->
            hand(d, a.hour, 0.5f, 0.075f, Color.White, HandStyle.BATON, tail = -0.12f)
            hand(d, a.minute, 0.84f, 0.055f, Color.White, HandStyle.BATON, tail = -0.12f)
            hand(d, a.hour, 0.12f, 0.022f, Color.White, HandStyle.BATON, tail = 0f, shadow = false)
            hand(d, a.minute, 0.12f, 0.022f, Color.White, HandStyle.BATON, tail = 0f, shadow = false)
            if (ctx.showSeconds) secondHand(d, a.second, ctx.accent, length = 0.94f, counterweight = 0f)
            hub(d, if (ctx.showSeconds) ctx.accent else Color.White, 0.035f)
        }
    )
}

/** Activity-style rings: hours (outer), minutes (middle), seconds (inner). */
@Composable
fun ActivityRingsFace(ctx: FaceContext, modifier: Modifier = Modifier) {
    val m = rememberTextMeasurer(cacheSize = 8)
    val timeText = ctx.timeText
    AnalogFace(
        ctx, modifier,
        dialLayer = { d ->
            textAt(m, timeText, d.center, numeralStyle(d, 0.22f, FontWeight.SemiBold), Ink.TextPrimary)
        },
        handsLayer = { d, a ->
            progressArc(d, (a.hour / 360f), 0.88f, 0.13f, Color(0xFFFA114F), Color(0xFFFA114F).copy(alpha = 0.18f))
            progressArc(d, (a.minute / 360f), 0.71f, 0.13f, Color(0xFF92E82A), Color(0xFF92E82A).copy(alpha = 0.18f))
            if (ctx.showSeconds) {
                progressArc(d, (a.second / 360f), 0.54f, 0.13f, Color(0xFF1EEAEF), Color(0xFF1EEAEF).copy(alpha = 0.18f))
            }
        }
    )
}

/** Shared by hybrid faces: compact weather sub-dial. */
internal fun DrawScope.weatherSubDial(sd: Dial, m: TextMeasurer, d: Dial, ctx: FaceContext) {
    drawCircle(Color.White.copy(alpha = 0.06f), sd.radius, sd.center)
    drawCircle(Color.White.copy(alpha = 0.2f), sd.radius, sd.center, style = Stroke(d.px(0.006f)))
    val w = ctx.weather
    if (w != null) {
        drawWeatherGlyph(w.condition, Offset(sd.center.x, sd.center.y - sd.px(0.32f)), sd.px(0.32f))
        textAt(m, ctx.temperature(w.temperatureCelsius), Offset(sd.center.x, sd.center.y + sd.px(0.35f)), numeralStyle(d, 0.075f, FontWeight.SemiBold), Ink.TextPrimary)
    } else {
        textAt(m, "--°", sd.center, numeralStyle(d, 0.075f, FontWeight.SemiBold), Ink.TextSecondary)
    }
}
