package com.standbypro.ui.clock

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClock(
    time: LocalDateTime,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    showSeconds: Boolean = true
) {
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2 * 0.9f

        // Draw ticks — offset by -90° so 0 (12 o'clock) is at the top
        for (i in 0..59) {
            val angle = (i * 6 - 90) * (Math.PI / 180)
            val isHour = i % 5 == 0
            
            val lineLength = if (isHour) radius * 0.1f else radius * 0.05f
            val strokeWidth = if (isHour) 4f else 2f
            
            val start = Offset(
                x = center.x + (radius - lineLength) * cos(angle).toFloat(),
                y = center.y + (radius - lineLength) * sin(angle).toFloat()
            )
            val end = Offset(
                x = center.x + radius * cos(angle).toFloat(),
                y = center.y + radius * sin(angle).toFloat()
            )
            
            drawLine(
                color = if (isHour) accentColor else Color.Gray.copy(alpha = 0.5f),
                start = start,
                end = end,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        val hour = time.hour % 12
        val minute = time.minute
        val second = time.second
        val nano = time.nano

        // Hour hand
        val hourAngle = (hour + minute / 60f) * 30f
        rotate(hourAngle, center) {
            drawLine(
                color = Color.White,
                start = center,
                end = center.copy(y = center.y - radius * 0.5f),
                strokeWidth = 12f,
                cap = StrokeCap.Round
            )
        }

        // Minute hand
        val minuteAngle = (minute + second / 60f) * 6f
        rotate(minuteAngle, center) {
            drawLine(
                color = Color.White,
                start = center,
                end = center.copy(y = center.y - radius * 0.7f),
                strokeWidth = 8f,
                cap = StrokeCap.Round
            )
        }

        // Second hand
        if (showSeconds) {
            val secondAngle = (second + nano / 1_000_000_000f) * 6f
            rotate(secondAngle, center) {
                drawLine(
                    color = accentColor,
                    start = center.copy(y = center.y + radius * 0.1f),
                    end = center.copy(y = center.y - radius * 0.8f),
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = accentColor,
                    radius = 8f,
                    center = center
                )
            }
        } else {
            drawCircle(
                color = Color.White,
                radius = 8f,
                center = center
            )
        }
    }
}
