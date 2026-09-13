package com.standbypro.ui.clock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DigitalClock(
    time: LocalDateTime,
    modifier: Modifier = Modifier,
    use24Hour: Boolean = false,
    showSeconds: Boolean = false,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Dynamically compute responsive font sizes based on panel constraints.
        // Clamped between 52sp and 68sp so it fills the frame nicely without being
        // too huge (no clipping/wrapping) or too small/short.
        val baseScale = minOf(maxWidth.value / 380f, maxHeight.value / 320f).coerceIn(0.85f, 1.15f)
        val timeFontSize = (60f * baseScale).coerceIn(52f, 68f).sp
        val amPmFontSize = (timeFontSize.value * 0.28f).coerceIn(15f, 20f).sp
        val secondsFontSize = (timeFontSize.value * 0.50f).coerceIn(26f, 34f).sp
        val dateFontSize = (timeFontSize.value * 0.24f).coerceIn(13f, 17f).sp

        val timePattern = if (use24Hour) "HH:mm" else "h:mm"
        val timeFormatter = DateTimeFormatter.ofPattern(timePattern)
        val amPmFormatter = DateTimeFormatter.ofPattern("a")
        val secondsFormatter = DateTimeFormatter.ofPattern("ss")
        val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            // Main Time Row: Time + [Seconds] + [AM/PM]
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = time.format(timeFormatter),
                    fontSize = timeFontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    letterSpacing = (-1).sp,
                    lineHeight = timeFontSize,
                    maxLines = 1,
                    softWrap = false
                )

                if (showSeconds) {
                    Text(
                        text = ":" + time.format(secondsFormatter),
                        fontSize = secondsFontSize,
                        fontWeight = FontWeight.Medium,
                        color = accentColor.copy(alpha = 0.75f),
                        modifier = Modifier.padding(start = 2.dp, bottom = 4.dp),
                        lineHeight = secondsFontSize,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                if (!use24Hour) {
                    Text(
                        text = time.format(amPmFormatter).uppercase(),
                        fontSize = amPmFontSize,
                        fontWeight = FontWeight.Bold,
                        color = accentColor.copy(alpha = 0.85f),
                        modifier = Modifier.padding(start = 6.dp, bottom = 5.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Single Clean Date Row
            Text(
                text = time.format(dateFormatter),
                fontSize = dateFontSize,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.85f),
                letterSpacing = 0.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

