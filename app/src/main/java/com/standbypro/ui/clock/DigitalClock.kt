package com.standbypro.ui.clock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    val timePattern = buildString {
        append(if (use24Hour) "HH:mm" else "h:mm")
        if (showSeconds) append(":ss")
    }
    
    val timeFormatter = DateTimeFormatter.ofPattern(timePattern)
    val amPmFormatter = DateTimeFormatter.ofPattern("a")
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE\nd MMMM")

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = time.format(timeFormatter),
                fontSize = 120.sp,
                fontWeight = FontWeight.Light,
                color = accentColor,
                lineHeight = 120.sp
            )
            
            if (!use24Hour) {
                Text(
                    text = time.format(amPmFormatter),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Normal,
                    color = accentColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(start = 8.dp, bottom = 24.dp)
                )
            }
        }
        
        Text(
            text = time.format(dateFormatter),
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 16.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
