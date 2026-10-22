package com.standbypro.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.ui.design.Ink

/** Release highlights shown once in Studio after an update. */
object WhatsNew {
    /** Bump with each release that should show the card again. */
    const val VERSION = 21
    const val TITLE = "New in StandBy Pro 2.1"

    val highlights = listOf(
        "Bedside alarm" to "Gentle wake, snooze and repeat days — rings even when StandBy is closed",
        "Smart Stack suggestions" to "Stacks turn to music, timers, your next meeting or the weather when it matters",
        "Notification banners" to "New notifications slide in on the display, following your privacy setting",
        "Westminster chimes" to "Big Ben's melody or a deep temple bell on the hour",
        "Sky accent & Daybreak theme" to "An accent colour that follows the sky through the day",
        "129 faces, 28 widgets" to "Kinetic, Fireplace, Lighthouse, Pixel Day, Crypto, Charge History and more"
    )
}

@Composable
fun WhatsNewCard(accent: Color, onDismiss: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.22f), Ink.Card, Ink.Card)))
            .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(34.dp).clip(CircleShape).background(accent), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.AutoAwesome, null, tint = Color.Black, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(WhatsNew.TITLE, color = Ink.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        WhatsNew.highlights.forEach { (title, detail) ->
            Row {
                Box(Modifier.padding(top = 7.dp).size(6.dp).clip(CircleShape).background(accent))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(title, color = Ink.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(detail, color = Ink.TextTertiary, fontSize = 12.sp)
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(accent).clickable(onClick = onDismiss).padding(vertical = 11.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Got it", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}
