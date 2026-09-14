package com.standbypro.ui.faces

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.domain.TimeZoneClock
import java.time.LocalDateTime

@Composable
fun FlipCard(
    value: String,
    cardBg: Color = Color(0xFF1E1E1E),
    textColor: Color = Color.White,
    dividerColor: Color = Color(0xFF0F0F0F),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, Color(0xFF333333), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = textColor,
                fontFamily = FontFamily.SansSerif
            )
        }
        // Horizontal middle mechanical flip seam
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(dividerColor)
        )
    }
}

@Composable
fun FlipClassicFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else "%02d".format(if (time.hour % 12 == 0) 12 else time.hour % 12)
    val mins = "%02d".format(time.minute)
    val amPm = if (time.hour >= 12) "PM" else "AM"

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FlipCard(value = hours, cardBg = Color(0xFF222222), textColor = Color.White)
            FlipCard(value = mins, cardBg = Color(0xFF222222), textColor = Color.White)
        }
        if (!use24Hour) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = amPm,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
fun FlipDarkFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else "%02d".format(if (time.hour % 12 == 0) 12 else time.hour % 12)
    val mins = "%02d".format(time.minute)

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FlipCard(value = hours, cardBg = Color(0xFF0D0D0D), textColor = Color(0xFFDDDDDD))
        Spacer(modifier = Modifier.width(12.dp))
        FlipCard(value = mins, cardBg = Color(0xFF0D0D0D), textColor = Color(0xFFDDDDDD))
    }
}

@Composable
fun FlipAmberFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else "%02d".format(if (time.hour % 12 == 0) 12 else time.hour % 12)
    val mins = "%02d".format(time.minute)

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FlipCard(value = hours, cardBg = Color(0xFF201608), textColor = Color(0xFFFF9500))
        Spacer(modifier = Modifier.width(12.dp))
        FlipCard(value = mins, cardBg = Color(0xFF201608), textColor = Color(0xFFFF9500))
    }
}

@Composable
fun FlipGraphiteFace(
    time: LocalDateTime,
    accentColor: Color,
    use24Hour: Boolean = false,
    modifier: Modifier = Modifier
) {
    val hours = if (use24Hour) "%02d".format(time.hour) else "%02d".format(if (time.hour % 12 == 0) 12 else time.hour % 12)
    val mins = "%02d".format(time.minute)

    Row(
        modifier = modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        FlipCard(value = hours, cardBg = Color(0xFF2B2C30), textColor = Color.White)
        Spacer(modifier = Modifier.width(12.dp))
        FlipCard(value = mins, cardBg = Color(0xFF2B2C30), textColor = Color.White)
    }
}

@Composable
fun WorldClockFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val clocks = TimeZoneClock.DEFAULT_WORLD_CLOCKS.take(4)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "WORLD TIME",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
                letterSpacing = 2.sp
            )
            Text(
                text = "DST-AWARE",
                fontSize = 10.sp,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            clocks.forEach { clock ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF161618))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = clock.city.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = clock.getFormattedTime(use24Hour = false),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Text(
                            text = clock.getFormattedOffset(),
                            fontSize = 10.sp,
                            color = accentColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MultiTimezoneFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    WorldClockFace(time = time, accentColor = accentColor, modifier = modifier)
}
