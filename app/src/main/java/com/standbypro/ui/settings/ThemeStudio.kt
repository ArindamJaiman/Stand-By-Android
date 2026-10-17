package com.standbypro.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.domain.FaceContext
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.settings.StandByColorTheme
import com.standbypro.settings.StandBySettings
import com.standbypro.ui.design.Backdrop
import com.standbypro.ui.design.BackdropLayer
import com.standbypro.ui.design.ClockFont
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.TABULAR
import com.standbypro.ui.design.ThemePreset
import com.standbypro.ui.design.ThemePresets

/** True when the user's current settings match every part of [preset]. */
fun StandBySettings.matches(preset: ThemePreset): Boolean =
    colorThemeId == preset.accentId && activeWatchFaceId == preset.faceId && clockFont == preset.clockFont &&
        backdrop == preset.backdrop && nightModeStyle == preset.nightStyleId

/** Horizontally scrolling gallery of curated looks with live previews. */
@Composable
fun ThemeGallery(settings: StandBySettings, ctx: FaceContext, onApply: (ThemePreset) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemePresets.all.forEach { preset ->
            val accent = StandByColorTheme.fromId(preset.accentId).color
            val active = settings.matches(preset)
            Column(Modifier.width(176.dp)) {
                Box(
                    Modifier.fillMaxWidth().height(112.dp).clip(RoundedCornerShape(20.dp)).background(Color.Black)
                        .border(if (active) 2.dp else 1.dp, if (active) accent else Ink.Hairline, RoundedCornerShape(20.dp))
                        .clickable { onApply(preset) }
                ) {
                    BackdropLayer(preset.backdrop, accent, animate = false, modifier = Modifier.fillMaxSize())
                    WatchFaceRegistry.Render(preset.faceId, ctx.copy(accent = accent, isPreview = true, isCompact = true), Modifier.fillMaxSize().padding(6.dp))
                    if (active) {
                        Box(
                            Modifier.align(Alignment.TopEnd).padding(7.dp).size(22.dp).clip(CircleShape).background(accent),
                            contentAlignment = Alignment.Center
                        ) { Icon(Icons.Default.Check, null, tint = Color.Black, modifier = Modifier.size(14.dp)) }
                    }
                }
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(9.dp).clip(CircleShape).background(accent))
                    Spacer(Modifier.width(6.dp))
                    Text(preset.name, color = if (active) accent else Ink.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
                Text(preset.tagline, color = Ink.TextTertiary, fontSize = 11.sp, maxLines = 2, lineHeight = 14.sp)
            }
        }
    }
}

/** Typeface cards, each showing the time in that face. */
@Composable
fun ClockFontPicker(selected: ClockFont, timeText: String, accent: Color, onPick: (ClockFont) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ClockFont.entries.forEach { font ->
            val isSelected = font == selected
            Column(
                Modifier.width(104.dp).clip(RoundedCornerShape(18.dp))
                    .background(if (isSelected) accent.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.05f))
                    .border(1.dp, if (isSelected) accent.copy(alpha = 0.8f) else Color.Transparent, RoundedCornerShape(18.dp))
                    .clickable { onPick(font) }
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    timeText,
                    style = TextStyle(fontFamily = font.family(), fontWeight = FontWeight.Normal, fontSize = 30.sp, fontFeatureSettings = TABULAR),
                    color = if (isSelected) accent else Ink.TextPrimary, maxLines = 1
                )
                Text(font.label, color = Ink.TextTertiary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

/** Backdrop swatches rendered with the real (static) backdrop. */
@Composable
fun BackdropPicker(selected: Backdrop, accent: Color, onPick: (Backdrop) -> Unit) {
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Backdrop.entries.forEach { b ->
            val isSelected = b == selected
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier.size(width = 96.dp, height = 60.dp).clip(RoundedCornerShape(14.dp)).background(Color.Black)
                        .border(if (isSelected) 2.dp else 1.dp, if (isSelected) accent else Ink.Hairline, RoundedCornerShape(14.dp))
                        .clickable { onPick(b) }
                ) {
                    BackdropLayer(b, accent, animate = false, modifier = Modifier.fillMaxSize())
                }
                Spacer(Modifier.height(5.dp))
                Text(b.label, color = if (isSelected) accent else Ink.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            }
        }
    }
}
