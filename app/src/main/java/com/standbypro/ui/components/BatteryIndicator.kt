package com.standbypro.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.domain.ChargingState

@Composable
fun BatteryIndicator(
    chargingState: ChargingState,
    modifier: Modifier = Modifier,
    accentColor: Color = MaterialTheme.colorScheme.primary
) {
    Row(
        modifier = modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (chargingState.isCharging) Icons.Default.BatteryChargingFull else Icons.Default.BatteryFull,
            contentDescription = "Battery Status",
            tint = if (chargingState.isCharging) accentColor else Color.White.copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "${chargingState.batteryPercent}%",
            color = if (chargingState.isCharging) accentColor else Color.White.copy(alpha = 0.7f),
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium
        )
        
        if (chargingState.isCharging) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Charging",
                color = accentColor.copy(alpha = 0.8f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
