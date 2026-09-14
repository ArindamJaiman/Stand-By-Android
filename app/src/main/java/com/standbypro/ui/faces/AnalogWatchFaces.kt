package com.standbypro.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.standbypro.clock.rememberSweepingSeconds
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Shared helper to draw hour and minute ticks on analog dials.
 */
fun DrawScope.drawStandardBezel(
    center: Offset,
    radius: Float,
    hourColor: Color = Color.White,
    tickColor: Color = Color.White.copy(alpha = 0.35f),
    numHourTicks: Int = 12
) {
    for (i in 0 until 60) {
        val angleRad = (i * 6f - 90f) * (PI / 180f).toFloat()
        val isHour = i % (60 / numHourTicks) == 0
        val tickLength = if (isHour) radius * 0.12f else radius * 0.05f
        val strokeWidth = if (isHour) 3f else 1.2f
        val color = if (isHour) hourColor else tickColor

        val startX = center.x + (radius - tickLength) * cos(angleRad)
        val startY = center.y + (radius - tickLength) * sin(angleRad)
        val endX = center.x + radius * cos(angleRad)
        val endY = center.y + radius * sin(angleRad)

        drawLine(color, Offset(startX, startY), Offset(endX, endY), strokeWidth, StrokeCap.Round)
    }
}

/**
 * Standard Analog Hands with sweeping second hand.
 */
fun DrawScope.drawStandardHands(
    center: Offset,
    radius: Float,
    hours: Int,
    minutes: Int,
    sweepingSeconds: Float,
    hourHandColor: Color = Color.White,
    minuteHandColor: Color = Color.White,
    secondHandColor: Color = Color.Red
) {
    // Hour Hand
    val hourAngle = ((hours % 12) + minutes / 60f + sweepingSeconds / 3600f) * 30f - 90f
    val hourRad = hourAngle * (PI / 180f).toFloat()
    val hourLen = radius * 0.52f
    drawLine(
        color = hourHandColor,
        start = center - Offset(cos(hourRad) * radius * 0.08f, sin(hourRad) * radius * 0.08f),
        end = Offset(center.x + hourLen * cos(hourRad), center.y + hourLen * sin(hourRad)),
        strokeWidth = 6.5f,
        cap = StrokeCap.Round
    )

    // Minute Hand
    val minAngle = (minutes + sweepingSeconds / 60f) * 6f - 90f
    val minRad = minAngle * (PI / 180f).toFloat()
    val minLen = radius * 0.76f
    drawLine(
        color = minuteHandColor,
        start = center - Offset(cos(minRad) * radius * 0.1f, sin(minRad) * radius * 0.1f),
        end = Offset(center.x + minLen * cos(minRad), center.y + minLen * sin(minRad)),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // Sweeping Seconds Hand
    val secAngle = sweepingSeconds * 6f - 90f
    val secRad = secAngle * (PI / 180f).toFloat()
    val secLen = radius * 0.88f
    val tailLen = radius * 0.18f
    drawLine(
        color = secondHandColor,
        start = center - Offset(cos(secRad) * tailLen, sin(secRad) * tailLen),
        end = Offset(center.x + secLen * cos(secRad), center.y + secLen * sin(secRad)),
        strokeWidth = 2f,
        cap = StrokeCap.Round
    )

    // Center Cap
    drawCircle(color = secondHandColor, radius = 5.5f, center = center)
    drawCircle(color = Color.Black, radius = 2.5f, center = center)
}

@Composable
fun AnalogClassicFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF141414), radius, center)
            drawCircle(Color(0xFF282828), radius, center, style = Stroke(2f))

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.3f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun AnalogModernFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            // Minimal outer ring
            drawCircle(Color(0xFF1C1C1E), radius, center)
            drawCircle(accentColor.copy(alpha = 0.4f), radius, center, style = Stroke(1.5f))

            // 4 primary modern indices
            for (i in 0 until 4) {
                val angleRad = (i * 90f - 90f) * (PI / 180f).toFloat()
                val start = Offset(center.x + (radius - 20f) * cos(angleRad), center.y + (radius - 20f) * sin(angleRad))
                val end = Offset(center.x + radius * cos(angleRad), center.y + radius * sin(angleRad))
                drawLine(Color.White, start, end, strokeWidth = 4f, cap = StrokeCap.Round)
            }

            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun AnalogLuxuryFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            // Sunburst dial effect
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF2C2C2E), Color(0xFF0F0F10)),
                    center = center,
                    radius = radius
                ),
                radius = radius,
                center = center
            )
            drawCircle(Color(0xFFD4AF37), radius, center, style = Stroke(2.5f))

            // Gold faceted hour markers
            for (i in 0 until 12) {
                val angleRad = (i * 30f - 90f) * (PI / 180f).toFloat()
                val start = Offset(center.x + (radius - 24f) * cos(angleRad), center.y + (radius - 24f) * sin(angleRad))
                val end = Offset(center.x + (radius - 6f) * cos(angleRad), center.y + (radius - 6f) * sin(angleRad))
                drawLine(Color(0xFFE5C158), start, end, strokeWidth = 4.5f, cap = StrokeCap.Square)
            }

            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color(0xFFE5C158), Color(0xFFE5C158), accentColor)
        }
    }
}

@Composable
fun ChronographFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF161618), radius, center)
            drawCircle(Color(0xFF333336), radius, center, style = Stroke(2f))

            // 3 Subdials: Left (seconds), Right (minute counter), Bottom (hour counter)
            val subRadius = radius * 0.22f
            val subOffset = radius * 0.42f
            val leftSub = Offset(center.x - subOffset, center.y)
            val rightSub = Offset(center.x + subOffset, center.y)
            val bottomSub = Offset(center.x, center.y + subOffset)

            listOf(leftSub, rightSub, bottomSub).forEach { subCenter ->
                drawCircle(Color(0xFF0F0F10), subRadius, subCenter)
                drawCircle(Color.White.copy(alpha = 0.2f), subRadius, subCenter, style = Stroke(1f))
            }

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.35f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun PilotFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF101418), radius, center)
            drawCircle(Color(0xFF2A3440), radius, center, style = Stroke(2f))

            // 12 o'clock Pilot Triangle
            val topY = center.y - radius + 10f
            val path = Path().apply {
                moveTo(center.x, topY)
                lineTo(center.x - 12f, topY + 20f)
                lineTo(center.x + 12f, topY + 20f)
                close()
            }
            drawPath(path, Color(0xFF00E5FF))

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.4f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, Color(0xFF00E5FF))
        }
    }
}

@Composable
fun DiverFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            // Unidirectional rotating bezel ring simulation
            drawCircle(Color(0xFF00241B), radius, center)
            drawCircle(Color(0xFF007A5E), radius, center, style = Stroke(12f))

            // Luminescent round indices
            for (i in 0 until 12) {
                val angleRad = (i * 30f - 90f) * (PI / 180f).toFloat()
                val p = Offset(center.x + (radius - 26f) * cos(angleRad), center.y + (radius - 26f) * sin(angleRad))
                drawCircle(Color(0xFF00FFB2), radius = 7f, center = p)
                drawCircle(Color.White, radius = 4f, center = p)
            }

            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun SkeletonFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF0A0A0A), radius, center)

            // Exposed escapement and balance wheel simulation
            drawCircle(Color(0xFF282828), radius * 0.45f, center, style = Stroke(8f))
            drawCircle(Color(0xFF444444), radius * 0.28f, Offset(center.x + 20f, center.y - 15f), style = Stroke(4f))
            drawCircle(Color(0xFFD4AF37), radius * 0.22f, Offset(center.x - 20f, center.y + 20f), style = Stroke(3f))

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.3f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun RomanFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF141414), radius, center)
            drawCircle(Color(0xFF333333), radius, center, style = Stroke(2f))

            // Roman numerals track
            drawCircle(Color.White.copy(alpha = 0.15f), radius * 0.85f, center, style = Stroke(1f))
            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.25f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun PocketWatchFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 16f

            // Pocket watch crown at 12 o'clock
            val crownY = center.y - radius - 8f
            drawCircle(Color(0xFFD4AF37), 10f, Offset(center.x, crownY), style = Stroke(3f))

            drawCircle(Color(0xFF1A1A18), radius, center)
            drawCircle(Color(0xFFD4AF37), radius, center, style = Stroke(3f))

            drawStandardBezel(center, radius, Color(0xFFE5C158), Color(0xFFE5C158).copy(alpha = 0.35f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color(0xFFE5C158), Color(0xFFE5C158), accentColor)
        }
    }
}

@Composable
fun ArtDecoFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF121214), radius, center)
            // Octagonal Art Deco geometric geometry
            for (i in 0 until 8) {
                val rad1 = (i * 45f) * (PI / 180f).toFloat()
                val rad2 = ((i + 1) * 45f) * (PI / 180f).toFloat()
                val p1 = Offset(center.x + radius * 0.9f * cos(rad1), center.y + radius * 0.9f * sin(rad1))
                val p2 = Offset(center.x + radius * 0.9f * cos(rad2), center.y + radius * 0.9f * sin(rad2))
                drawLine(Color(0xFFD4AF37), p1, p2, strokeWidth = 2f)
            }

            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color(0xFFD4AF37), Color.White, accentColor)
        }
    }
}

@Composable
fun NordicFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            // Clean Scandinavian minimalism: pristine matte dial
            drawCircle(Color(0xFF18191A), radius, center)

            // Ultra-thin minimal indices
            for (i in 0 until 12) {
                val angleRad = (i * 30f - 90f) * (PI / 180f).toFloat()
                val start = Offset(center.x + (radius - 14f) * cos(angleRad), center.y + (radius - 14f) * sin(angleRad))
                val end = Offset(center.x + (radius - 4f) * cos(angleRad), center.y + (radius - 4f) * sin(angleRad))
                drawLine(Color.White.copy(alpha = 0.7f), start, end, strokeWidth = 1.5f, cap = StrokeCap.Round)
            }

            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun CompassFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF0E1318), radius, center)
            drawCircle(Color(0xFF233240), radius, center, style = Stroke(2f))

            // Cardinal direction compass points
            val points = listOf("N" to -90f, "E" to 0f, "S" to 90f, "W" to 180f)
            points.forEach { (_, angle) ->
                val rad = angle * (PI / 180f).toFloat()
                val p1 = Offset(center.x + (radius - 20f) * cos(rad), center.y + (radius - 20f) * sin(rad))
                val p2 = Offset(center.x + radius * cos(rad), center.y + radius * sin(rad))
                drawLine(if (angle == -90f) Color.Red else Color.White, p1, p2, strokeWidth = 3.5f, cap = StrokeCap.Round)
            }

            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun SundialFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            // Stone-textured sundial dial
            drawCircle(Color(0xFF1C1B18), radius, center)
            drawCircle(Color(0xFF4A4438), radius, center, style = Stroke(2f))

            // Radial shadow lines
            for (i in 0 until 12) {
                val angleRad = (i * 30f - 90f) * (PI / 180f).toFloat()
                val end = Offset(center.x + radius * cos(angleRad), center.y + radius * sin(angleRad))
                drawLine(Color(0xFF8C7D6B).copy(alpha = 0.4f), center, end, strokeWidth = 1f)
            }

            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color(0xFFD8C7B0), Color(0xFFD8C7B0), accentColor)
        }
    }
}

@Composable
fun MoonphaseFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF0F1420), radius, center)
            drawCircle(Color(0xFF28344E), radius, center, style = Stroke(2f))

            // Moonphase aperture at 6 o'clock
            val moonCenter = Offset(center.x, center.y + radius * 0.42f)
            val moonRadius = radius * 0.22f
            drawCircle(Color(0xFF080B12), moonRadius, moonCenter)
            drawCircle(Color(0xFFE8E0C5), moonRadius * 0.7f, moonCenter)

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.35f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}
