package com.standbypro.ui.faces

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.data.DemoDataProvider
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// ================= WEATHER FACES =================

@Composable
fun WeatherDigitalFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val weather = DemoDataProvider.demoWeather
    val hours = "%02d".format(if (time.hour % 12 == 0) 12 else time.hour % 12)
    val mins = "%02d".format(time.minute)

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${weather.temperatureCelsius}°",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = weather.condition.displayName,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
        Text(
            text = "$hours:$mins",
            fontSize = 72.sp,
            fontWeight = FontWeight.Thin,
            color = Color.White
        )
        Text(
            text = "H:${weather.highCelsius}°  L:${weather.lowCelsius}° • ${weather.cityName}",
            fontSize = 13.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun WeatherAnalogFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    HybridAnalogWeatherFace(time = time, accentColor = accentColor, modifier = modifier)
}

@Composable
fun WeatherHourlyFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val weather = DemoDataProvider.demoWeather

    Column(
        modifier = modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "HOURLY FORECAST", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = accentColor, letterSpacing = 2.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weather.hourlyForecast.take(5).forEach { hour ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = hour.timeLabel, fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = accentColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "${hour.temperatureCelsius}°", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun WeatherForecastFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    WeatherDigitalFace(time = time, accentColor = accentColor, modifier = modifier)
}

// ================= PRODUCTIVITY FACES =================

@Composable
fun PomodoroClockFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFF1E1010))
                .border(2.dp, Color(0xFFFF3B30).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 28.dp, vertical = 18.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "DEEP FOCUS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF3B30), letterSpacing = 2.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "23:45", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "CYCLE 3 OF 4", fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun TimerFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(imageVector = Icons.Default.Timer, contentDescription = null, tint = accentColor, modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "14:52", fontSize = 68.sp, fontWeight = FontWeight.Bold, color = Color.White, fontFamily = FontFamily.Monospace)
        Text(text = "COUNTDOWN ACTIVE", fontSize = 11.sp, color = accentColor, letterSpacing = 2.sp)
    }
}

@Composable
fun StopwatchFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "04:18.82", fontSize = 64.sp, fontWeight = FontWeight.Black, color = Color.White, fontFamily = FontFamily.Monospace)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = "LAP 2 • SPLIT 02:11.40", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun FocusClockFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    PomodoroClockFace(time = time, accentColor = accentColor, modifier = modifier)
}

// ================= AMBIENT / ARTISTIC FACES =================

@Composable
fun AmbientGradientFace(
    time: LocalDateTime,
    accentColor: Color,
    title: String,
    gradientColors: List<Color>,
    modifier: Modifier = Modifier
) {
    val hours = "%02d".format(if (time.hour % 12 == 0) 12 else time.hour % 12)
    val mins = "%02d".format(time.minute)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.radialGradient(colors = gradientColors)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$hours:$mins", fontSize = 72.sp, fontWeight = FontWeight.ExtraLight, color = Color.White)
            Text(text = title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.7f), letterSpacing = 3.sp)
        }
    }
}

@Composable
fun AuroraFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Aurora Borealis", listOf(Color(0xFF00382B), Color(0xFF00121C)), modifier)

@Composable
fun GalaxyFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Deep Cosmos", listOf(Color(0xFF20003B), Color(0xFF070014)), modifier)

@Composable
fun OceanFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Abyssal Ocean", listOf(Color(0xFF00223D), Color(0xFF000B14)), modifier)

@Composable
fun ForestFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Redwood Canopy", listOf(Color(0xFF0F2615), Color(0xFF051008)), modifier)

@Composable
fun MountainFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Alpine Twilight", listOf(Color(0xFF28253B), Color(0xFF0D0C14)), modifier)

@Composable
fun GeometricFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Geometric Bauhaus", listOf(Color(0xFF2B1D0E), Color(0xFF0D0904)), modifier)

@Composable
fun NeonAmbientFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Cyber Neon", listOf(Color(0xFF2B0024), Color(0xFF0A0009)), modifier)

@Composable
fun ZenFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Zen Garden", listOf(Color(0xFF1F2022), Color(0xFF0A0A0A)), modifier)

@Composable
fun JapaneseFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Kyoto Dusk", listOf(Color(0xFF261010), Color(0xFF0A0505)), modifier)

@Composable
fun MinimalGradientFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    AmbientGradientFace(time, accentColor, "Minimal Void", listOf(Color(0xFF141416), Color(0xFF000000)), modifier)

// ================= PHOTO FACES =================

@Composable
fun PhotoFrameFace(
    time: LocalDateTime,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val hours = "%02d".format(if (time.hour % 12 == 0) 12 else time.hour % 12)
    val mins = "%02d".format(time.minute)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1E2836), Color(0xFF0B1017))
                )
            ),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "$hours:$mins", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = time.format(DateTimeFormatter.ofPattern("EEEE, MMMM d")), fontSize = 14.sp, color = accentColor)
            Text(text = "Photo Stream • Bedside Album", fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PhotoClockFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    PhotoFrameFace(time, accentColor, modifier)

@Composable
fun PhotoWeatherFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    PhotoFrameFace(time, accentColor, modifier)

@Composable
fun PhotoCalendarFace(time: LocalDateTime, accentColor: Color, modifier: Modifier = Modifier) =
    PhotoFrameFace(time, accentColor, modifier)
