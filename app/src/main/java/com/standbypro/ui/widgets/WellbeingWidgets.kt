package com.standbypro.ui.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.RingGauge
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.lerpColor
import com.standbypro.ui.faces.BreathingFace

/** US AQI gauge with PM2.5 and today's UV index from Open-Meteo. */
@Composable
fun AirQualityWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val air = ctx.airQuality
    val color = air?.category?.color ?: Ink.TextTertiary
    WidgetTile(modifier, background = Brush.verticalGradient(listOf(lerpColor(Color(0xFF101418), color, 0.16f), Color(0xFF07090B)))) {
        if (air == null) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
                TileHeader("Air quality", Ink.Mint, Modifier.fillMaxWidth().height(18.dp))
                Spacer(Modifier.height(8.dp))
                AutoSizeText("Waiting for data", plainLabel, Modifier.fillMaxWidth().height(24.dp), color = Ink.TextPrimary, alignment = Alignment.CenterStart)
                AutoSizeText("Needs an internet connection", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(16.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
            }
            return@WidgetTile
        }
        Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            RingGauge(
                (air.usAqi / 300f).coerceIn(0.02f, 1f), color, Modifier.weight(1f).fillMaxHeight(), strokeFraction = 0.1f,
                gradient = listOf(Color(0xFF30D158), Color(0xFFFFD60A), Color(0xFFFF9500), Color(0xFFFF3B30), Color(0xFFBF5AF2))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    AutoSizeText("${air.usAqi}", clockStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().height(40.dp), color = Ink.TextPrimary, sizingText = "888")
                    AutoSizeText("US AQI", labelStyle(FontWeight.Bold), Modifier.fillMaxWidth(0.6f).height(14.dp), color = Ink.TextSecondary)
                }
            }
            Column(Modifier.weight(1.15f).fillMaxHeight().padding(start = 12.dp), verticalArrangement = Arrangement.Center) {
                TileHeader("Air quality", color, Modifier.fillMaxWidth().height(18.dp))
                Spacer(Modifier.height(4.dp))
                AutoSizeText(air.category.label, plainLabel.copy(fontWeight = FontWeight.Bold), Modifier.fillMaxWidth().height(24.dp), color = Ink.TextPrimary, sizingText = "Very unhealthy", alignment = Alignment.CenterStart)
                AutoSizeText(air.category.advice, labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(15.dp), color = Ink.TextSecondary, sizingText = "Sensitive groups should limit time outside", alignment = Alignment.CenterStart)
                Spacer(Modifier.height(6.dp))
                AutoSizeText("PM2.5 ${"%.0f".format(air.pm25)} µg/m³  ·  UV ${"%.0f".format(air.uvIndex)} ${air.uvLabel}", labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().height(15.dp), color = Ink.TextTertiary, sizingText = "PM2.5 888 µg/m³  ·  UV 88 Very high", alignment = Alignment.CenterStart)
            }
        }
    }
}

@Composable
fun BreatheWidget(ctx: FaceContext, modifier: Modifier = Modifier) =
    WidgetTile(modifier, background = Brush.verticalGradient(listOf(Color(0xFF041A1A), Color.Black)), padding = 4) {
        BreathingFace(ctx.copy(isCompact = true), Modifier.fillMaxSize())
    }
