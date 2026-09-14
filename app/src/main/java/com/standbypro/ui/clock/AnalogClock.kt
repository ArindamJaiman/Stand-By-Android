package com.standbypro.ui.clock

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import java.time.LocalDateTime
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClock(
    time: LocalDateTime,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    showSeconds: Boolean = true
) {
    // 120Hz/60Hz high-frequency frame loop for mechanical smooth gliding sweep
    val currentMillis by produceState(initialValue = System.currentTimeMillis()) {
        while (true) {
            withFrameMillis {
                value = System.currentTimeMillis()
            }
        }
    }

    val numberPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .aspectRatio(1f)
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        // Scaled to 0.74f for perfectly balanced proportions with breathing room around dial
        val radius = size.minDimension / 2 * 0.74f

        // 1. Draw outer ticks around the perimeter (60 marks)
        for (i in 0..59) {
            val angle = (i * 6 - 90) * (Math.PI / 180)
            val isHour = i % 5 == 0

            val lineLength = if (isHour) radius * 0.08f else radius * 0.04f
            val strokeWidth = if (isHour) 3.5f else 1.8f
            val tickColor = if (isHour) Color.White.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.35f)

            val start = Offset(
                x = center.x + (radius - lineLength) * cos(angle).toFloat(),
                y = center.y + (radius - lineLength) * sin(angle).toFloat()
            )
            val end = Offset(
                x = center.x + radius * cos(angle).toFloat(),
                y = center.y + radius * sin(angle).toFloat()
            )

            drawLine(
                color = tickColor,
                start = start,
                end = end,
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }

        // 2. Draw 1-12 Hour Numerals in bold font inside ticks (plain dial, no extra text)
        val numberRadius = radius * 0.72f
        numberPaint.textSize = radius * 0.17f

        for (n in 1..12) {
            val angle = (n * 30 - 90) * (Math.PI / 180)
            val numX = center.x + numberRadius * cos(angle).toFloat()
            val numY = center.y + numberRadius * sin(angle).toFloat() - (numberPaint.descent() + numberPaint.ascent()) / 2

            drawContext.canvas.nativeCanvas.drawText(
                n.toString(),
                numX,
                numY,
                numberPaint
            )
        }

        // 3. High-precision continuous time calculations (mechanical watch glide)
        val calendar = Calendar.getInstance().apply { timeInMillis = currentMillis }
        val hour = calendar.get(Calendar.HOUR)
        val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY) // 0..23 for GMT 24-hour cycle
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        val continuousSeconds = second + millis / 1000f
        val continuousMinutes = minute + continuousSeconds / 60f
        val continuousHours = hour + continuousMinutes / 60f
        val continuousGmtHours = hourOfDay + continuousMinutes / 60f

        // 4. GMT Hand (24-Hour Arrow Complication)
        // Sweeps through a 24-hour cycle (15° per hour) with a signature luxury arrowhead
        val gmtAngle = continuousGmtHours * 15f
        rotate(gmtAngle, center) {
            val stemEnd = center.y - radius * 0.64f
            val arrowTip = center.y - radius * 0.78f
            val arrowHalfWidth = radius * 0.040f

            // Slender GMT stem in accent color
            drawLine(
                color = accentColor.copy(alpha = 0.85f),
                start = center,
                end = Offset(center.x, stemEnd),
                strokeWidth = 2.8f,
                cap = StrokeCap.Round
            )

            // Distinctive arrowhead pointer
            val arrowPath = Path().apply {
                moveTo(center.x, arrowTip)
                lineTo(center.x + arrowHalfWidth, stemEnd)
                lineTo(center.x, stemEnd + radius * 0.015f)
                lineTo(center.x - arrowHalfWidth, stemEnd)
                close()
            }

            drawPath(
                path = arrowPath,
                color = accentColor
            )
        }

        // 5. Hour Hand (bold white pill)
        val hourAngle = continuousHours * 30f
        rotate(hourAngle, center) {
            drawLine(
                color = Color.White,
                start = center,
                end = center.copy(y = center.y - radius * 0.44f),
                strokeWidth = radius * 0.065f,
                cap = StrokeCap.Round
            )
        }

        // 6. Minute Hand (sleek white pill)
        val minuteAngle = continuousMinutes * 6f
        rotate(minuteAngle, center) {
            drawLine(
                color = Color.White,
                start = center,
                end = center.copy(y = center.y - radius * 0.68f),
                strokeWidth = radius * 0.045f,
                cap = StrokeCap.Round
            )
        }

        // Center hub circle
        drawCircle(
            color = Color.Black,
            radius = radius * 0.035f,
            center = center
        )

        // 7. Mechanical Sweeping Second Hand (silky smooth glide, high frequency)
        if (showSeconds) {
            val secondAngle = continuousSeconds * 6f
            rotate(secondAngle, center) {
                drawLine(
                    color = accentColor,
                    start = center.copy(y = center.y + radius * 0.14f), // Tail
                    end = center.copy(y = center.y - radius * 0.82f),
                    strokeWidth = 3.5f,
                    cap = StrokeCap.Round
                )
                // Center accent cap
                drawCircle(
                    color = accentColor,
                    radius = radius * 0.032f,
                    center = center
                )
            }
        } else {
            drawCircle(
                color = accentColor,
                radius = radius * 0.032f,
                center = center
            )
        }
    }
}
