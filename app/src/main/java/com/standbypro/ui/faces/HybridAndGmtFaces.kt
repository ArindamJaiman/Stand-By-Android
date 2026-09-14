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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.standbypro.clock.rememberSweepingSeconds
import java.time.LocalDateTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 24-Hour GMT Hand drawing helper.
 */
fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGmtHand(
    center: Offset,
    radius: Float,
    hours: Int,
    minutes: Int,
    sweepingSeconds: Float,
    gmtColor: Color = Color(0xFFFF3B30)
) {
    // 24-hour rotation: 360 degrees in 24 hours = 15 degrees per hour
    val gmtTotalHours = hours + (minutes / 60f) + (sweepingSeconds / 3600f)
    val gmtAngle = (gmtTotalHours * 15f) - 90f
    val gmtRad = gmtAngle * (PI / 180f).toFloat()

    val gmtLength = radius * 0.82f
    val arrowTip = Offset(center.x + gmtLength * cos(gmtRad), center.y + gmtLength * sin(gmtRad))

    // Red stalk
    drawLine(
        color = gmtColor,
        start = center,
        end = arrowTip,
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )

    // Red GMT arrow triangle with luminous center
    val arrowSize = 14f
    val normalAngle1 = gmtRad + (PI * 0.85).toFloat()
    val normalAngle2 = gmtRad - (PI * 0.85).toFloat()

    val p1 = Offset(arrowTip.x + arrowSize * cos(normalAngle1), arrowTip.y + arrowSize * sin(normalAngle1))
    val p2 = Offset(arrowTip.x + arrowSize * cos(normalAngle2), arrowTip.y + arrowSize * sin(normalAngle2))

    val arrowPath = Path().apply {
        moveTo(arrowTip.x, arrowTip.y)
        lineTo(p1.x, p1.y)
        lineTo(p2.x, p2.y)
        close()
    }

    drawPath(arrowPath, gmtColor)
}

@Composable
fun GmtExplorerFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            // Bezel with 24-hour markings
            drawCircle(Color(0xFF141414), radius, center)
            drawCircle(Color(0xFF2E2E30), radius, center, style = Stroke(12f))

            // 24-hour bezel ticks
            for (i in 0 until 24) {
                val angleRad = (i * 15f - 90f) * (PI / 180f).toFloat()
                val isMajor = i % 2 == 0
                val tickLen = if (isMajor) 10f else 5f
                val p1 = Offset(center.x + (radius - 2f) * cos(angleRad), center.y + (radius - 2f) * sin(angleRad))
                val p2 = Offset(center.x + (radius - 2f - tickLen) * cos(angleRad), center.y + (radius - 2f - tickLen) * sin(angleRad))
                drawLine(Color.White.copy(alpha = if (isMajor) 0.9f else 0.4f), p1, p2, strokeWidth = if (isMajor) 2f else 1f)
            }

            drawStandardBezel(center, radius - 16f, Color.White, Color.White.copy(alpha = 0.35f))
            drawGmtHand(center, radius - 16f, time.hour, time.minute, sweepingSeconds, Color(0xFFFF3B30))
            drawStandardHands(center, radius - 16f, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun GmtCalendarFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GmtExplorerFace(time = time, accentColor = accentColor, modifier = modifier)
}

@Composable
fun HybridAnalogDigitalFace(
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

            // Top Digital readout window
            val digCenter = Offset(center.x, center.y - radius * 0.38f)
            drawRoundRect(
                color = Color(0xFF0A0A0A),
                topLeft = Offset(digCenter.x - 38f, digCenter.y - 14f),
                size = androidx.compose.ui.geometry.Size(76f, 28f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.3f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun HybridAnalogBatteryFace(
    time: LocalDateTime,
    accentColor: Color,
    batteryPercent: Int = 85,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF141414), radius, center)

            // Battery gauge subdial at 9 o'clock
            val battCenter = Offset(center.x - radius * 0.42f, center.y)
            val battRadius = radius * 0.22f
            drawCircle(Color(0xFF1F1F1F), battRadius, battCenter)
            drawCircle(Color(0xFF30D158).copy(alpha = 0.3f), battRadius, battCenter, style = Stroke(2f))
            drawArc(
                color = Color(0xFF30D158),
                startAngle = -90f,
                sweepAngle = (batteryPercent / 100f) * 360f,
                useCenter = false,
                topLeft = Offset(battCenter.x - battRadius, battCenter.y - battRadius),
                size = androidx.compose.ui.geometry.Size(battRadius * 2, battRadius * 2),
                style = Stroke(3.5f)
            )

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.3f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun HybridAnalogWeatherFace(
    time: LocalDateTime,
    accentColor: Color,
    temperatureCelsius: Int = 22,
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF12141A), radius, center)

            // Weather subdial at 3 o'clock
            val wCenter = Offset(center.x + radius * 0.42f, center.y)
            val wRadius = radius * 0.22f
            drawCircle(Color(0xFF1A202C), wRadius, wCenter)
            drawCircle(Color(0xFF00E5FF).copy(alpha = 0.5f), wRadius, wCenter, style = Stroke(2f))

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.3f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun HybridAnalogCalendarFace(
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

            // Date window at 3 o'clock
            val dateWindow = Offset(center.x + radius * 0.52f, center.y)
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(dateWindow.x - 16f, dateWindow.y - 12f),
                size = androidx.compose.ui.geometry.Size(32f, 24f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
            )

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.3f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}

@Composable
fun HybridGmtWeatherFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    GmtExplorerFace(time = time, accentColor = accentColor, modifier = modifier)
}

@Composable
fun HybridDualTimeFace(
    time: LocalDateTime,
    accentColor: Color,
    secondaryHoursOffset: Int = -5, // e.g. New York
    modifier: Modifier = Modifier
) {
    val sweepingSeconds by rememberSweepingSeconds()

    Box(modifier = modifier.fillMaxSize().padding(12.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize().aspectRatio(1f)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f - 12f

            drawCircle(Color(0xFF131316), radius, center)

            // Secondary Timezone subdial at 6 o'clock
            val subCenter = Offset(center.x, center.y + radius * 0.42f)
            val subRadius = radius * 0.28f
            drawCircle(Color(0xFF1F1F24), subRadius, subCenter)
            drawCircle(Color.White.copy(alpha = 0.25f), subRadius, subCenter, style = Stroke(1.5f))

            // Subdial hands
            val secHour = (time.hour + secondaryHoursOffset + 24) % 24
            val subHourAngle = ((secHour % 12) + time.minute / 60f) * 30f - 90f
            val subHourRad = subHourAngle * (PI / 180f).toFloat()
            drawLine(
                Color.White,
                subCenter,
                Offset(subCenter.x + subRadius * 0.5f * cos(subHourRad), subCenter.y + subRadius * 0.5f * sin(subHourRad)),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            drawStandardBezel(center, radius, Color.White, Color.White.copy(alpha = 0.3f))
            drawStandardHands(center, radius, time.hour, time.minute, sweepingSeconds, Color.White, Color.White, accentColor)
        }
    }
}
