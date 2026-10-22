package com.standbypro.ui.design

import android.content.Context
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Clock typefaces offered in Studio; applied to every face that uses the default clock style. */
enum class ClockFont(val label: String, val family: () -> FontFamily) {
    MODERN("Modern", { FaceFonts.Sans }),
    CLASSIC("Classic", { FaceFonts.Serif }),
    ELEGANT("Elegant", { FaceFonts.SerifItalic }),
    CONDENSED("Condensed", { FaceFonts.Condensed }),
    MONO("Mono", { FaceFonts.Mono })
}

/**
 * Material You accent taken from the wallpaper (Android 12+). Snapshot state so the
 * "Wallpaper" colour theme follows wallpaper changes.
 */
object DynamicAccent {
    private val Fallback = Color(0xFF8AB4F8)

    var color: Color by mutableStateOf(Fallback)
        private set

    fun refresh(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching { color = Color(context.getColor(android.R.color.system_accent1_200)) }
        }
    }
}

/**
 * The "Sky" accent: a colour that follows the day — rose at dawn, gold in the morning, blue at
 * noon, coral at sunset and violet into the night.
 */
object SkyAccent {
    /**
     * (minute of day, colour) keyframes, interpolated in between. Pale-sky and golden-hour stops
     * sit between blue and the warm colours so no transition passes through muddy grey.
     */
    private val keyframes = listOf(
        0 to Color(0xFF7B6CFF),
        5 * 60 + 30 to Color(0xFFFF7A9A),
        7 * 60 to Color(0xFFFF9F45),
        10 * 60 to Color(0xFFFFC94A),
        11 * 60 + 30 to Color(0xFF9FD3FF),
        13 * 60 to Color(0xFF4FB3FF),
        16 * 60 + 30 to Color(0xFF6EC4FF),
        17 * 60 + 30 to Color(0xFFB4DCFF),
        18 * 60 + 15 to Color(0xFFFFC46B),
        19 * 60 to Color(0xFFFF6B4A),
        21 * 60 to Color(0xFFB07BFF),
        24 * 60 to Color(0xFF7B6CFF)
    )

    fun colorAt(minuteOfDay: Int): Color {
        val m = minuteOfDay.mod(24 * 60)
        val upper = keyframes.indexOfFirst { it.first > m }.coerceAtLeast(1)
        val (m0, c0) = keyframes[upper - 1]
        val (m1, c1) = keyframes[upper]
        return lerpColor(c0, c1, (m - m0).toFloat() / (m1 - m0))
    }

    fun now(): Color = java.time.LocalTime.now().let { colorAt(it.hour * 60 + it.minute) }
}

/** Ambient backdrops drawn behind the dashboard and smart stacks. */
enum class Backdrop(val label: String) {
    BLACK("OLED black"),
    GRAPHITE("Graphite"),
    GLOW("Accent glow"),
    AURORA("Aurora"),
    DUSK("Dusk"),
    STARS("Starfield")
}

private const val TAU = (2 * PI).toFloat()

/**
 * Slow, low-contrast backdrop. Redraws at ~10 fps (or not at all for static styles and
 * previews) so it costs almost nothing.
 */
@Composable
fun BackdropLayer(style: Backdrop, accent: Color, animate: Boolean, modifier: Modifier = Modifier) {
    if (style == Backdrop.BLACK) return
    val moving = animate && style != Backdrop.GRAPHITE
    val clock = rememberDayClock(SecondsMotion.SWEEP, moving, 100L)
    Canvas(modifier) {
        if (size.minDimension < 2f) return@Canvas
        val t = clock.value
        when (style) {
            Backdrop.BLACK -> Unit
            Backdrop.GRAPHITE -> drawRect(Brush.verticalGradient(listOf(Color(0xFF17171B), Color(0xFF0A0A0C), Color(0xFF040405))))
            Backdrop.GLOW -> {
                val breathe = 0.5f + 0.5f * sin(t * TAU / 14f)
                val c1 = Offset(size.width * (0.28f + 0.04f * sin(t * TAU / 90f)), size.height * 0.35f)
                val c2 = Offset(size.width * (0.78f - 0.04f * sin(t * TAU / 70f)), size.height * 0.75f)
                drawCircle(safeRadialGradient(listOf(accent.copy(alpha = 0.16f + 0.05f * breathe), Color.Transparent), c1, size.maxDimension * 0.5f), size.maxDimension * 0.5f, c1)
                drawCircle(safeRadialGradient(listOf(accent.copy(alpha = 0.09f), Color.Transparent), c2, size.maxDimension * 0.42f), size.maxDimension * 0.42f, c2)
            }
            Backdrop.AURORA -> {
                val colors = listOf(Color(0xFF2EE6A6), Color(0xFF3FA9F5), Color(0xFF8E5CFF))
                for (i in colors.indices) {
                    val x = size.width * (0.2f + 0.3f * i + 0.08f * sin(t * TAU / (60f + i * 17f) + i))
                    val y = size.height * (0.25f + 0.1f * sin(t * TAU / (45f + i * 11f) + i * 2f))
                    val r = size.maxDimension * (0.35f + 0.05f * i)
                    drawCircle(safeRadialGradient(listOf(colors[i].copy(alpha = 0.2f), Color.Transparent), Offset(x, y), r), r, Offset(x, y), blendMode = BlendMode.Plus)
                }
            }
            Backdrop.DUSK -> {
                drawRect(Brush.verticalGradient(listOf(Color(0xFF0B0820), Color(0xFF1E0E2E), Color(0xFF3A1428), Color(0xFF2A0F10))))
                val glow = Offset(size.width * 0.5f, size.height * 1.05f)
                drawCircle(safeRadialGradient(listOf(Color(0xFFFF7A45).copy(alpha = 0.22f + 0.04f * sin(t * TAU / 20f)), Color.Transparent), glow, size.maxDimension * 0.6f), size.maxDimension * 0.6f, glow)
            }
            Backdrop.STARS -> {
                val rnd = Random(11)
                repeat(140) { i ->
                    val p = Offset(rnd.nextFloat() * size.width, rnd.nextFloat() * size.height)
                    val twinkle = 0.5f + 0.5f * sin(t * TAU / (3f + rnd.nextFloat() * 6f) + i)
                    val r = 0.6f + rnd.nextFloat() * 1.6f
                    drawCircle(Color.White.copy(alpha = (0.12f + 0.45f * twinkle) * (r / 2.2f)), r * density / 2f, p)
                }
            }
        }
    }
}

/** A curated look: one tap sets accent, face, typeface, backdrop and night tint together. */
data class ThemePreset(
    val id: String,
    val name: String,
    val tagline: String,
    val accentId: String,
    val faceId: String,
    val clockFont: ClockFont,
    val backdrop: Backdrop,
    val nightStyleId: String
)

object ThemePresets {
    val all = listOf(
        ThemePreset("standby", "StandBy", "The iconic orange bedside clock", "orange", "analog_standby", ClockFont.MODERN, Backdrop.BLACK, "RED"),
        ThemePreset("midnight_gold", "Midnight Gold", "Sunburst blue, gold and serif numerals", "gold", "analog_luxury", ClockFont.CLASSIC, Backdrop.GRAPHITE, "AMBER"),
        ThemePreset("nordic_frost", "Nordic Frost", "Glacier blue over a quiet aurora", "cyan", "analog_nordic", ClockFont.MODERN, Backdrop.AURORA, "DIM"),
        ThemePreset("tokyo_neon", "Tokyo Neon", "Pink neon tubes on a dusk skyline", "pink", "digital_neon", ClockFont.CONDENSED, Backdrop.DUSK, "RED"),
        ThemePreset("forest_calm", "Forest Calm", "Fireflies, mint and a soft glow", "mint", "ambient_forest", ClockFont.MODERN, Backdrop.GLOW, "AMBER"),
        ThemePreset("mono_pro", "Mono Pro", "Monospaced minimalism for OLED", "monochrome", "digital_minimal", ClockFont.MONO, Backdrop.BLACK, "DIM"),
        ThemePreset("rose", "Rosé", "Coral warmth and italic serif", "coral", "digital_typewriter", ClockFont.ELEGANT, Backdrop.GLOW, "AMBER"),
        ThemePreset("deep_space", "Deep Space", "Indigo nebulae under twinkling stars", "indigo", "ambient_galaxy", ClockFont.MODERN, Backdrop.STARS, "RED"),
        ThemePreset("vinyl", "Vinyl Lounge", "A spinning record for your music", "ruby", "music_vinyl", ClockFont.MODERN, Backdrop.GLOW, "RED"),
        ThemePreset("wallpaper", "Material You", "Colours taken from your wallpaper", "wallpaper", "analog_meridian", ClockFont.MODERN, Backdrop.GLOW, "RED"),
        ThemePreset("daybreak", "Daybreak", "An accent that follows the sky through the day", "sky", "ambient_mountain", ClockFont.MODERN, Backdrop.GLOW, "RED")
    )

    fun byId(id: String): ThemePreset? = all.firstOrNull { it.id == id }
}
