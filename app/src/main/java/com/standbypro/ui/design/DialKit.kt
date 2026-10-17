package com.standbypro.ui.design

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Geometry of a round dial. Every measurement in the dial kit is expressed as a fraction of
 * [radius] so faces scale perfectly from 80 dp thumbnails to full-screen tablets.
 */
data class Dial(val center: Offset, val radius: Float) {
    /** Point at [angleDeg] (0° = 12 o'clock, clockwise) and [fraction] of the radius. */
    fun point(angleDeg: Float, fraction: Float): Offset {
        val rad = (angleDeg - 90f) * (PI.toFloat() / 180f)
        return Offset(center.x + cos(rad) * radius * fraction, center.y + sin(rad) * radius * fraction)
    }

    fun px(fraction: Float): Float = radius * fraction

    fun sub(angleDeg: Float, distance: Float, size: Float): Dial = Dial(point(angleDeg, distance), radius * size)

    fun bounds(fraction: Float = 1f): Rect = Rect(center, radius * fraction)
}

fun DrawScope.dial(fill: Float = 0.94f): Dial = Dial(center, size.minDimension / 2f * fill)

enum class HandStyle { BATON, DAUPHINE, SWORD, LEAF, ARROW, PENCIL, SKELETON, NEEDLE, BREGUET, RAIL, SPADE }

/** Draws a filled dial disc, optionally with a radial sunburst gradient and a bezel ring. */
fun DrawScope.dialDisc(
    d: Dial,
    inner: Color,
    outer: Color = inner,
    ring: Color? = null,
    ringWidth: Float = 0.012f,
    fraction: Float = 1f
) {
    drawCircle(
        brush = safeRadialGradient(listOf(inner, outer), center = d.center, radius = d.px(fraction).coerceAtLeast(1f)),
        radius = d.px(fraction),
        center = d.center
    )
    if (ring != null) {
        drawCircle(ring, d.px(fraction), d.center, style = Stroke(d.px(ringWidth)))
    }
}

/** Sunburst: alternating faint radial rays, the classic brushed-metal dial finish. */
fun DrawScope.sunburst(d: Dial, color: Color, rays: Int = 120, fraction: Float = 1f) {
    for (i in 0 until rays) {
        val a = i * 360f / rays
        val alpha = if (i % 2 == 0) 0.05f else 0.018f
        drawLine(color.copy(alpha = alpha), d.center, d.point(a, fraction), strokeWidth = d.px(0.012f))
    }
}

fun DrawScope.ticks(
    d: Dial,
    count: Int = 60,
    majorEvery: Int = 5,
    outer: Float = 0.98f,
    majorLength: Float = 0.10f,
    minorLength: Float = 0.04f,
    majorWidth: Float = 0.022f,
    minorWidth: Float = 0.007f,
    majorColor: Color = Color.White,
    minorColor: Color = Color.White.copy(alpha = 0.4f),
    cap: StrokeCap = StrokeCap.Butt,
    skipMajors: Boolean = false
) {
    for (i in 0 until count) {
        val major = majorEvery > 0 && i % majorEvery == 0
        if (major && skipMajors) continue
        val a = i * 360f / count
        val len = if (major) majorLength else minorLength
        drawLine(
            color = if (major) majorColor else minorColor,
            start = d.point(a, outer - len),
            end = d.point(a, outer),
            strokeWidth = d.px(if (major) majorWidth else minorWidth),
            cap = cap
        )
    }
}

/** Railway "minute track": two concentric hairlines with ladder rungs. */
fun DrawScope.railTrack(d: Dial, outer: Float, inner: Float, color: Color) {
    drawCircle(color, d.px(outer), d.center, style = Stroke(d.px(0.006f)))
    drawCircle(color, d.px(inner), d.center, style = Stroke(d.px(0.006f)))
    for (i in 0 until 60) {
        val a = i * 6f
        drawLine(color, d.point(a, inner), d.point(a, outer), strokeWidth = d.px(if (i % 5 == 0) 0.016f else 0.006f))
    }
}

fun DrawScope.dotIndices(d: Dial, at: Float, size: Float, color: Color, count: Int = 12, bigEvery: Int = 3, bigScale: Float = 1.6f) {
    for (i in 0 until count) {
        val r = if (bigEvery > 0 && i % bigEvery == 0) size * bigScale else size
        drawCircle(color, d.px(r), d.point(i * 360f / count, at))
    }
}

/** Applied (raised) baton indices with a highlight edge for a polished-metal look. */
fun DrawScope.appliedIndices(
    d: Dial,
    color: Color,
    highlight: Color = Color.White.copy(alpha = 0.55f),
    outer: Float = 0.9f,
    length: Float = 0.16f,
    width: Float = 0.045f,
    doubleAt12: Boolean = true,
    skip: Set<Int> = emptySet()
) {
    for (i in 0 until 12) {
        if (i in skip) continue
        val a = i * 30f
        rotate(a, d.center) {
            val w = d.px(width)
            val top = d.center.y - d.px(outer)
            val h = d.px(length)
            if (i == 0 && doubleAt12) {
                for (s in listOf(-1f, 1f)) {
                    val left = d.center.x + s * w * 0.75f - w * 0.45f
                    drawRect(Color.Black.copy(alpha = 0.45f), Offset(left + w * 0.12f, top + w * 0.12f), Size(w * 0.9f, h))
                    drawRect(color, Offset(left, top), Size(w * 0.9f, h))
                    drawRect(highlight, Offset(left, top), Size(w * 0.25f, h))
                }
            } else {
                val left = d.center.x - w / 2f
                drawRect(Color.Black.copy(alpha = 0.45f), Offset(left + w * 0.15f, top + w * 0.15f), Size(w, h))
                drawRect(color, Offset(left, top), Size(w, h))
                drawRect(highlight, Offset(left, top), Size(w * 0.28f, h))
            }
        }
    }
}

/** Draws [text] centred on [at]. Font size must already be set in [style]. */
fun DrawScope.textAt(measurer: TextMeasurer, text: String, at: Offset, style: TextStyle, color: Color) {
    val layout = measurer.measure(text, style)
    drawText(layout, color = color, topLeft = Offset(at.x - layout.size.width / 2f, at.y - layout.size.height / 2f))
}

/** Places labels around the dial at even spacing, starting at 12 o'clock. */
fun DrawScope.numerals(
    d: Dial,
    measurer: TextMeasurer,
    labels: List<String?>,
    at: Float,
    style: TextStyle,
    color: Color,
    rotateWithDial: Boolean = false
) {
    val step = 360f / labels.size
    labels.forEachIndexed { i, label ->
        if (label == null) return@forEachIndexed
        val a = i * step
        val p = d.point(a, at)
        if (rotateWithDial) {
            rotate(a, p) { textAt(measurer, label, p, style, color) }
        } else {
            textAt(measurer, label, p, style, color)
        }
    }
}

/**
 * A watch hand drawn pointing at [angle]. [length] and [width] are radius fractions; [tail] is
 * the counter-balance length behind the pivot. Hands cast a soft offset shadow for depth.
 */
fun DrawScope.hand(
    d: Dial,
    angle: Float,
    length: Float,
    width: Float,
    color: Color,
    style: HandStyle = HandStyle.BATON,
    tail: Float = 0.12f,
    lume: Color? = null,
    shadow: Boolean = true,
    accentFacet: Boolean = true
) {
    val path = handPath(d, length, width, tail, style)
    if (shadow) {
        translate(d.px(0.012f), d.px(0.02f)) {
            rotate(angle, d.center) { drawPath(path, Color.Black.copy(alpha = 0.42f)) }
        }
    }
    rotate(angle, d.center) {
        when (style) {
            HandStyle.SKELETON -> {
                drawPath(path, color, style = Stroke(d.px(width * 0.35f)))
                if (lume != null) {
                    val inner = handPath(d, length * 0.86f, width * 0.45f, -length * 0.25f, HandStyle.BATON)
                    drawPath(inner, lume)
                }
            }
            HandStyle.NEEDLE, HandStyle.BREGUET -> {
                drawLine(color, Offset(d.center.x, d.center.y + d.px(tail)), Offset(d.center.x, d.center.y - d.px(length)), d.px(width), StrokeCap.Round)
                if (style == HandStyle.BREGUET) {
                    val c = Offset(d.center.x, d.center.y - d.px(length * 0.72f))
                    drawCircle(color, d.px(width * 3.4f), c, style = Stroke(d.px(width * 1.1f)))
                }
            }
            else -> {
                drawPath(path, color)
                if (accentFacet && (style == HandStyle.DAUPHINE || style == HandStyle.SWORD || style == HandStyle.LEAF)) {
                    // Faceted right half catches less light.
                    val facet = Path().apply {
                        moveTo(d.center.x, d.center.y - d.px(length))
                        lineTo(d.center.x + d.px(width), d.center.y - d.px(length * 0.22f))
                        lineTo(d.center.x, d.center.y + d.px(tail))
                        close()
                    }
                    drawPath(facet, Color.Black.copy(alpha = 0.22f))
                }
                if (lume != null) {
                    val inset = handPath(d, length * 0.9f, width * 0.42f, -length * 0.22f, HandStyle.BATON)
                    drawPath(inset, lume)
                }
            }
        }
    }
}

private fun handPath(d: Dial, length: Float, width: Float, tail: Float, style: HandStyle): Path {
    val cx = d.center.x
    val cy = d.center.y
    val l = d.px(length)
    val w = d.px(width) / 2f
    val t = d.px(tail)
    return Path().apply {
        when (style) {
            HandStyle.BATON, HandStyle.SKELETON -> addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    Rect(cx - w, cy - l, cx + w, cy + t),
                    CornerRadius(w, w)
                )
            )
            HandStyle.RAIL -> addRect(Rect(cx - w, cy - l, cx + w, cy + t))
            HandStyle.DAUPHINE -> {
                moveTo(cx, cy - l)
                lineTo(cx + w * 2f, cy - l * 0.22f)
                lineTo(cx + w * 0.6f, cy + t)
                lineTo(cx - w * 0.6f, cy + t)
                lineTo(cx - w * 2f, cy - l * 0.22f)
                close()
            }
            HandStyle.SWORD -> {
                moveTo(cx, cy - l)
                lineTo(cx + w, cy - l * 0.84f)
                lineTo(cx + w * 0.75f, cy + t)
                lineTo(cx - w * 0.75f, cy + t)
                lineTo(cx - w, cy - l * 0.84f)
                close()
            }
            HandStyle.LEAF -> {
                moveTo(cx, cy - l)
                quadraticTo(cx + w * 2.6f, cy - l * 0.42f, cx, cy + t)
                quadraticTo(cx - w * 2.6f, cy - l * 0.42f, cx, cy - l)
                close()
            }
            HandStyle.ARROW -> {
                addRect(Rect(cx - w * 0.45f, cy - l * 0.74f, cx + w * 0.45f, cy + t))
                moveTo(cx, cy - l)
                lineTo(cx + w * 1.9f, cy - l * 0.7f)
                lineTo(cx - w * 1.9f, cy - l * 0.7f)
                close()
            }
            HandStyle.PENCIL -> {
                moveTo(cx, cy - l)
                lineTo(cx + w, cy - l * 0.9f)
                lineTo(cx + w, cy + t)
                lineTo(cx - w, cy + t)
                lineTo(cx - w, cy - l * 0.9f)
                close()
            }
            HandStyle.SPADE -> {
                addRect(Rect(cx - w * 0.5f, cy - l * 0.62f, cx + w * 0.5f, cy + t))
                moveTo(cx, cy - l)
                cubicTo(cx + w * 2.6f, cy - l * 0.82f, cx + w * 1.6f, cy - l * 0.6f, cx, cy - l * 0.6f)
                cubicTo(cx - w * 1.6f, cy - l * 0.6f, cx - w * 2.6f, cy - l * 0.82f, cx, cy - l)
                close()
            }
            HandStyle.NEEDLE, HandStyle.BREGUET -> addRect(Rect(cx - w, cy - l, cx + w, cy + t))
        }
    }
}

/** Thin seconds hand with counterweight and a tip that can be a disc (railway style). */
fun DrawScope.secondHand(
    d: Dial,
    angle: Float,
    color: Color,
    length: Float = 0.9f,
    tail: Float = 0.22f,
    width: Float = 0.012f,
    disc: Float = 0f,
    counterweight: Float = 0.032f
) {
    translate(d.px(0.008f), d.px(0.016f)) {
        rotate(angle, d.center) {
            drawLine(Color.Black.copy(alpha = 0.35f), Offset(d.center.x, d.center.y + d.px(tail)), Offset(d.center.x, d.center.y - d.px(length)), d.px(width))
        }
    }
    rotate(angle, d.center) {
        drawLine(color, Offset(d.center.x, d.center.y + d.px(tail)), Offset(d.center.x, d.center.y - d.px(length)), d.px(width), StrokeCap.Round)
        if (counterweight > 0f) {
            drawCircle(color, d.px(counterweight), Offset(d.center.x, d.center.y + d.px(tail * 0.82f)))
        }
        if (disc > 0f) {
            drawCircle(color, d.px(disc), Offset(d.center.x, d.center.y - d.px(length - disc)))
        }
    }
}

fun DrawScope.hub(d: Dial, color: Color, size: Float = 0.04f, inner: Color = Color.Black) {
    drawCircle(color, d.px(size), d.center)
    drawCircle(inner, d.px(size * 0.4f), d.center)
}

/** Recessed sub-dial with a fine track. */
fun DrawScope.subDial(sd: Dial, base: Color, track: Color, ticks: Int = 12, ring: Color? = null) {
    drawCircle(Color.Black.copy(alpha = 0.35f), sd.radius * 1.04f, sd.center)
    drawCircle(base, sd.radius, sd.center)
    if (ring != null) drawCircle(ring, sd.radius, sd.center, style = Stroke(sd.px(0.04f)))
    for (i in 0 until ticks) {
        val a = i * 360f / ticks
        drawLine(track, sd.point(a, 0.78f), sd.point(a, 0.95f), strokeWidth = sd.px(0.045f))
    }
}

/** A progress arc measured clockwise from 12 o'clock. */
fun DrawScope.progressArc(
    d: Dial,
    fraction: Float,
    radius: Float,
    width: Float,
    color: Color,
    track: Color? = null,
    startAngle: Float = 0f,
    sweepTotal: Float = 360f,
    cap: StrokeCap = StrokeCap.Round
) {
    val r = d.px(radius)
    val topLeft = Offset(d.center.x - r, d.center.y - r)
    val arcSize = Size(r * 2f, r * 2f)
    if (track != null) {
        drawArc(track, startAngle - 90f, sweepTotal, false, topLeft, arcSize, style = Stroke(d.px(width), cap = cap))
    }
    val sweep = sweepTotal * fraction.coerceIn(0f, 1f)
    if (sweep > 0.01f) {
        drawArc(color, startAngle - 90f, sweep, false, topLeft, arcSize, style = Stroke(d.px(width), cap = cap))
    }
}

fun DrawScope.filledWedge(d: Dial, startAngle: Float, sweep: Float, radius: Float, color: Color) {
    val r = d.px(radius)
    drawArc(color, startAngle - 90f, sweep, true, Offset(d.center.x - r, d.center.y - r), Size(r * 2f, r * 2f), style = Fill)
}
