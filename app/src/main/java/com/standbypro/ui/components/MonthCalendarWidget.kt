package com.standbypro.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.domain.ChargingState
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun MonthCalendarWidget(
    time: LocalDateTime,
    chargingState: ChargingState,
    nextAlarm: String?,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val yearMonth = YearMonth.from(time)
    val firstOfMonth = yearMonth.atDay(1)
    // Sunday = 0, Monday = 1, ..., Saturday = 6
    val firstDayOffset = firstOfMonth.dayOfWeek.value % 7
    val daysInMonth = yearMonth.lengthOfMonth()
    val today = time.dayOfMonth

    val monthFormatter = DateTimeFormatter.ofPattern("MMMM")
    val monthName = time.format(monthFormatter).uppercase()

    val daysOfWeek = listOf("S", "M", "T", "W", "T", "F", "S")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Month Header (e.g. "DECEMBER") in accent color
        Text(
            text = monthName,
            color = accentColor,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            textAlign = TextAlign.Start
        )

        // 2. Day of Week Header row: S M T W T F S
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            daysOfWeek.forEach { dayLetter ->
                Text(
                    text = dayLetter,
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 3. Calendar Days Grid
        val totalSlots = firstDayOffset + daysInMonth
        val rows = (totalSlots + 6) / 7

        for (row in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (col in 0..6) {
                    val index = row * 7 + col
                    val dayNumber = index - firstDayOffset + 1

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNumber in 1..daysInMonth) {
                            val isToday = dayNumber == today

                            if (isToday) {
                                // Today highlighted in filled accent circle
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else {
                                Text(
                                    text = dayNumber.toString(),
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Sleek Battery & Alarm Info Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Battery Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (chargingState.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${chargingState.batteryPercent}%${if (chargingState.isCharging) " Charging" else ""}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                chargingState.temperatureCelsius?.let { temp ->
                    Text(
                        text = " • ${temp.roundToInt()}°C",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp
                    )
                }
            }

            // Next Alarm Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Alarm,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = nextAlarm ?: "No Alarm",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
