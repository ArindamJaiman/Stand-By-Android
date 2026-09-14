package com.standbypro.ui.faces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun DigitalMinimalFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) time.hour else (if (time.hour % 12 == 0) 12 else time.hour % 12)
    val mins = time.minute
    val amPm = if (time.hour >= 12) "PM" else "AM"

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "$hours:${"%02d".format(mins)}",
                fontSize = 62.sp,
                fontWeight = FontWeight.Light,
                color = Color.White,
                letterSpacing = (-1.5).sp
            )
            if (!use24Hour) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = amPm,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = accentColor,
                    modifier = Modifier.padding(bottom = 10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = time.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.65f)
        )
    }
}

@Composable
fun DigitalBoldFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else (if (time.hour % 12 == 0) "12" else "${time.hour % 12}")
    val mins = "%02d".format(time.minute)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = hours,
            fontSize = 58.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            lineHeight = 54.sp
        )
        Text(
            text = mins,
            fontSize = 58.sp,
            fontWeight = FontWeight.Black,
            color = accentColor,
            lineHeight = 54.sp
        )
    }
}

@Composable
fun DigitalOledFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else (if (time.hour % 12 == 0) "12" else "${time.hour % 12}")
    val mins = "%02d".format(time.minute)
    val secs = "%02d".format(time.second)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$hours:$mins",
                fontSize = 56.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = secs,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "OLED TRUE BLACK",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.4f),
            letterSpacing = 2.sp
        )
    }
}

@Composable
fun DigitalSegmentedFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else "%02d".format(if (time.hour % 12 == 0) 12 else time.hour % 12)
    val mins = "%02d".format(time.minute)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0D0D0D))
                .border(1.dp, Color(0xFF222222), RoundedCornerShape(16.dp))
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "$hours:$mins",
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = accentColor,
                letterSpacing = 4.sp
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "7-SEGMENT LED",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = accentColor.copy(alpha = 0.6f),
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
fun DigitalMatrixFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = "%02d".format(if (use24Hour) time.hour else (if (time.hour % 12 == 0) 12 else time.hour % 12))
    val mins = "%02d".format(time.minute)
    val secs = "%02d".format(time.second)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SYS.TIME // MATRIX",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF00FF66),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$hours:$mins:$secs",
            fontSize = 46.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF00FF66),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "KERNEL SECURE • TICK=${System.currentTimeMillis() % 10000}",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color(0xFF00FF66).copy(alpha = 0.5f)
        )
    }
}

@Composable
fun DigitalNeonFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else "${if (time.hour % 12 == 0) 12 else time.hour % 12}"
    val mins = "%02d".format(time.minute)

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Subtle ambient neon glow
        Text(
            text = "$hours:$mins",
            fontSize = 58.sp,
            fontWeight = FontWeight.Bold,
            color = accentColor.copy(alpha = 0.35f),
            modifier = Modifier.blur(14.dp)
        )
        Text(
            text = "$hours:$mins",
            fontSize = 58.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun DigitalWordClockFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val hourNames = listOf("TWELVE", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE", "TEN", "ELEVEN", "TWELVE")
    val hour = time.hour % 12
    val currentHourName = hourNames[hour]
    val min = time.minute

    val minutePhrase = when {
        min == 0 -> "O'CLOCK"
        min in 1..14 -> "JUST PAST"
        min in 15..29 -> "QUARTER PAST"
        min in 30..44 -> "HALF PAST"
        min in 45..59 -> "QUARTER TO"
        else -> ""
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "IT IS CURRENTLY", fontSize = 12.sp, color = Color.Gray, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = minutePhrase, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = accentColor)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = currentHourName, fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
    }
}

@Composable
fun DigitalBinaryFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val hours = time.hour
    val mins = time.minute
    val secs = time.second

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "BINARY CLOCK", fontSize = 11.sp, color = Color.Gray, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            BinaryColumn(label = "H", value = hours, bits = 5, accentColor = accentColor)
            BinaryColumn(label = "M", value = mins, bits = 6, accentColor = accentColor)
            BinaryColumn(label = "S", value = secs, bits = 6, accentColor = accentColor)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "%02d:%02d:%02d".format(hours, mins, secs),
            fontSize = 15.sp,
            color = Color.White.copy(alpha = 0.7f),
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun BinaryColumn(label: String, value: Int, bits: Int, accentColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        for (i in (bits - 1) downTo 0) {
            val isBitOn = (value and (1 shl i)) != 0
            Box(
                modifier = Modifier
                    .padding(vertical = 2.5.dp)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(if (isBitOn) accentColor else Color(0xFF222222))
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DigitalOutlineFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else "${if (time.hour % 12 == 0) 12 else time.hour % 12}"
    val mins = "%02d".format(time.minute)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "$hours:$mins",
            fontSize = 58.sp,
            fontWeight = FontWeight.ExtraBold,
            color = accentColor,
            textAlign = TextAlign.Center
        )
        Text(
            text = time.format(DateTimeFormatter.ofPattern("EEE • MMM d")),
            fontSize = 13.sp,
            color = Color.White.copy(alpha = 0.6f),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun DigitalTypewriterFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val hours = "%02d".format(time.hour)
    val mins = "%02d".format(time.minute)
    val secs = "%02d".format(time.second)

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "CHAPTER 24 — THE PASSAGE OF TIME",
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = Color.Gray,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "$hours:$mins:$secs",
            fontSize = 48.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "“Every second brings a new story.”",
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontStyle = FontStyle.Italic,
            color = accentColor
        )
    }
}

@Composable
fun DigitalPosterFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else "${if (time.hour % 12 == 0) 12 else time.hour % 12}"
    val mins = "%02d".format(time.minute)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .border(1.5.dp, accentColor, RoundedCornerShape(16.dp))
            .padding(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = hours,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                lineHeight = 48.sp
            )
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(2.5.dp)
                    .background(accentColor)
            )
            Text(
                text = mins,
                fontSize = 52.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                lineHeight = 48.sp
            )
        }
    }
}

@Composable
fun DigitalGlassFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else "${if (time.hour % 12 == 0) 12 else time.hour % 12}"
    val mins = "%02d".format(time.minute)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(
                        Color.White.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                ),
                RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$hours:$mins",
                fontSize = 54.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                letterSpacing = (-1).sp
            )
            Text(
                text = time.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")),
                fontSize = 13.sp,
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
