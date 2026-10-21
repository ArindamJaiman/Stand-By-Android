package com.standbypro.ui.widgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.data.ChargeHistoryRepository
import com.standbypro.data.CryptoRepository
import com.standbypro.domain.FaceContext
import com.standbypro.ui.design.AutoSizeText
import com.standbypro.ui.design.FaceFonts
import com.standbypro.ui.design.Ink
import com.standbypro.ui.design.clockStyle
import com.standbypro.ui.design.labelStyle
import com.standbypro.ui.design.textAt
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun coinColor(symbol: String): Color = when (symbol) {
    "BTC" -> Color(0xFFF7931A)
    "ETH" -> Color(0xFF8C9EFF)
    "SOL" -> Color(0xFF14F195)
    else -> Ink.Yellow
}

/** Live BTC, ETH and SOL prices with 24-hour change. Fetches only while the widget is on screen. */
@Composable
fun CryptoWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val live by CryptoRepository.prices.collectAsState()
    LaunchedEffect(ctx.isPreview) {
        if (ctx.isPreview) return@LaunchedEffect
        while (true) {
            CryptoRepository.refresh()
            delay(10 * 60_000L)
        }
    }
    val prices = live.ifEmpty { if (ctx.isPreview) CryptoRepository.SAMPLE else emptyList() }
    WidgetTile(modifier, background = Brush.verticalGradient(listOf(Color(0xFF17150E), Color(0xFF080807)))) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center) {
            TileHeader("Crypto · USD", Ink.Yellow, Modifier.fillMaxWidth().height(18.dp))
            Spacer(Modifier.height(6.dp))
            if (prices.isEmpty()) {
                AutoSizeText("Fetching prices…", plainLabel, Modifier.fillMaxWidth().height(24.dp), color = Ink.TextPrimary, alignment = Alignment.CenterStart)
                AutoSizeText("Needs an internet connection", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(16.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
                return@Column
            }
            prices.forEach { coin ->
                Row(Modifier.fillMaxWidth().weight(1f), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(28.dp).clip(CircleShape).background(coinColor(coin.symbol)), contentAlignment = Alignment.Center) {
                        Text(coin.symbol.take(1), color = Color.Black, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        AutoSizeText(coin.symbol, plainLabel.copy(fontWeight = FontWeight.Bold), Modifier.fillMaxWidth().height(18.dp), color = Ink.TextPrimary, sizingText = "BTC", alignment = Alignment.CenterStart)
                        AutoSizeText(coin.name, labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(12.dp), color = Ink.TextTertiary, sizingText = "ETHEREUM", alignment = Alignment.CenterStart)
                    }
                    Column(Modifier.weight(1.4f), horizontalAlignment = Alignment.End) {
                        AutoSizeText(coin.priceLabel, clockStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(22.dp), color = Ink.TextPrimary, sizingText = "$188,888", alignment = Alignment.CenterEnd)
                        AutoSizeText(coin.changeLabel, labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().height(12.dp), color = if (coin.change24h >= 0) Ink.Green else Ink.Red, sizingText = "▲ 88.88%", alignment = Alignment.CenterEnd)
                    }
                }
            }
        }
    }
}

/** The last seven charges as bars from plug-in to unplug level, with weekly stats. */
@Composable
fun ChargeHistoryWidget(ctx: FaceContext, modifier: Modifier = Modifier) {
    val stored by ChargeHistoryRepository.sessions.collectAsState()
    val now = System.currentTimeMillis()
    val sessions = stored.ifEmpty { if (ctx.isPreview) ChargeHistoryRepository.sample(now) else emptyList() }.takeLast(7)
    val m = rememberTextMeasurer(cacheSize = 16)
    val dayFormat = DateTimeFormatter.ofPattern("EEEEE", Locale.getDefault())
    WidgetTile(modifier, background = Brush.verticalGradient(listOf(Color(0xFF0D1A12), Color(0xFF050906)))) {
        Column(Modifier.fillMaxSize()) {
            TileHeader("Charge history", Ink.Green, Modifier.fillMaxWidth().height(18.dp))
            if (sessions.isEmpty()) {
                Spacer(Modifier.weight(1f))
                AutoSizeText("No charges recorded yet", plainLabel, Modifier.fillMaxWidth().height(22.dp), color = Ink.TextPrimary, alignment = Alignment.CenterStart)
                AutoSizeText("Your next charge will appear here", labelStyle(FontWeight.Medium), Modifier.fillMaxWidth().height(15.dp), color = Ink.TextTertiary, alignment = Alignment.CenterStart)
                Spacer(Modifier.weight(1f))
                return@Column
            }
            Canvas(Modifier.fillMaxWidth().weight(1f).padding(top = 8.dp, bottom = 6.dp)) {
                val slot = size.width / 7f
                val barW = slot * 0.42f
                val labelH = size.height * 0.16f
                val chartH = size.height - labelH
                val style = TextStyle(fontFamily = FaceFonts.Sans, fontWeight = FontWeight.SemiBold, fontSize = (labelH * 0.42f).toSp())
                sessions.forEachIndexed { i, s ->
                    val x = slot * (i + (7 - sessions.size)) + (slot - barW) / 2f
                    drawRoundRect(Color.White.copy(alpha = 0.07f), Offset(x, 0f), Size(barW, chartH), CornerRadius(barW / 2f))
                    val top = chartH * (1f - s.endPercent / 100f)
                    val bottom = chartH * (1f - s.startPercent / 100f)
                    val hot = (s.maxTempC ?: 0f) >= 38f
                    drawRoundRect(
                        Brush.verticalGradient(listOf(if (hot) Ink.Orange else Ink.Green, (if (hot) Ink.Orange else Ink.Green).copy(alpha = 0.45f)), startY = top, endY = bottom),
                        Offset(x, top), Size(barW, (bottom - top).coerceAtLeast(barW)), CornerRadius(barW / 2f)
                    )
                    val day = Instant.ofEpochMilli(s.endMillis).atZone(ZoneId.systemDefault()).format(dayFormat)
                    textAt(m, day, Offset(x + barW / 2f, chartH + labelH * 0.62f), style, Ink.TextTertiary)
                }
            }
            val week = sessions.filter { now - it.endMillis <= 7 * 24 * 60 * 60_000L }
            val avgGain = sessions.map { it.gained }.average().toInt()
            val avgMinutes = sessions.map { it.minutes }.average().toInt()
            val peak = sessions.mapNotNull { it.maxTempC }.maxOrNull()
            AutoSizeText(
                "${week.size} this week  ·  +$avgGain% in ${avgMinutes / 60}h ${"%02d".format(avgMinutes % 60)}m" + (peak?.let { "  ·  peak %.0f°C".format(it) } ?: ""),
                labelStyle(FontWeight.SemiBold), Modifier.fillMaxWidth().height(15.dp), color = Ink.TextSecondary,
                sizingText = "7 this week  ·  +88% in 8h 88m  ·  peak 88°C", alignment = Alignment.CenterStart
            )
        }
    }
}
