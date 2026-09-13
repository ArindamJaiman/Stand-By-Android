package com.standbypro.ui.clock

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import java.time.LocalDateTime
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogClock(
    time: LocalDateTime,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    showSeconds: Boolean = true,
    subLabel: String = "STANDBY"
) {
    val numberPaint = remember {
        Paint().apply {
            color = android.graphics.Color.WHITE
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
    }

    val subLabelPaint = remember {
        Paint().apply {
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
        val radius = size.minDimension / 2 * 0.92f

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

        // 2. Draw 1-12 Hour Numerals in bold, rounded font inside the ticks
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

        // 3. Draw Sub-label (e.g. "STANDBY") below 12
        subLabelPaint.color = Color.White.copy(alpha = 0.5f).toArgb()
        subLabelPaint.textSize = radius * 0.11f
        drawContext.canvas.nativeCanvas.drawText(
            subLabel,
            center.x,
            center.y - radius * 0.32f,
            subLabelPaint
        )

        // Time values
        val hour = time.hour % 12
        val minute = time.minute
        val second = time.second
        val nano = time.nano

        // 4. Hour Hand (bold white pill)
        val hourAngle = (hour + minute / 60f) * 30f
        rotate(hourAngle, center) {
            drawLine(
                color = Color.White,
                start = center,
                end = center.copy(y = center.y - radius * 0.44f),
                strokeWidth = radius * 0.065f,
                cap = StrokeCap.Round
            )
        }

        // 5. Minute Hand (sleek white pill)
        val minuteAngle = (minute + second / 60f) * 6f
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

        // 6. Second Hand (accent colored needle with tail and center pivot cap)
        if (showSeconds) {
            val secondAngle = (second + nano / 1_000_000_000f) * 6f
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
