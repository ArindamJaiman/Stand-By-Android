package com.standbypro.ui.design

import android.graphics.Typeface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

/**
 * StandBy Pro premium palette. Everything is tuned for OLED: true black canvases,
 * soft hairlines and restrained, warm metallics.
 */
object Ink {
    val Black = Color(0xFF000000)
    val Graphite = Color(0xFF0B0B0D)
    val Card = Color(0xFF121215)
    val CardHigh = Color(0xFF1A1A1F)
    val Hairline = Color(0x1FFFFFFF)
    val HairlineStrong = Color(0x33FFFFFF)

    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xA6EBEBF5)
    val TextTertiary = Color(0x59EBEBF5)

    val Gold = Color(0xFFD4AF37)
    val GoldLight = Color(0xFFE9CC7A)
    val Champagne = Color(0xFFF3E3BD)
    val Rose = Color(0xFFE8B4A0)
    val Steel = Color(0xFFC7CCD1)
    val Lume = Color(0xFFC9FFE0)
    val LumeGlow = Color(0xFF6CFFB0)

    val Red = Color(0xFFFF3B30)
    val Orange = Color(0xFFFF9500)
    val Yellow = Color(0xFFFFD60A)
    val Green = Color(0xFF30D158)
    val Mint = Color(0xFF63E6BE)
    val Teal = Color(0xFF40C8E0)
    val Cyan = Color(0xFF64D2FF)
    val Blue = Color(0xFF0A84FF)
    val Indigo = Color(0xFF5E5CE6)
    val Purple = Color(0xFFBF5AF2)
    val Pink = Color(0xFFFF375F)
}

/**
 * Font families resolved from the system so the APK stays small. Weights are applied
 * through [FontWeight]; Compose maps them onto the variable system font on modern devices.
 */
object FaceFonts {
    val Sans: FontFamily = FontFamily.SansSerif
    val Serif: FontFamily = FontFamily.Serif
    val Mono: FontFamily = FontFamily.Monospace
    val Condensed: FontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.NORMAL))
    val CondensedBold: FontFamily = FontFamily(Typeface.create("sans-serif-condensed", Typeface.BOLD))
    val SerifItalic: FontFamily = FontFamily(Typeface.create(Typeface.SERIF, Typeface.ITALIC))

    /**
     * The user's clock typeface (Studio → Display). Snapshot state, so every face that uses the
     * default [clockStyle] family re-renders when it changes; faces with a signature typeface
     * pass their own family and keep it.
     */
    var clock: FontFamily by mutableStateOf(Sans)
}

/** Tabular figures keep clock digits from jittering as they change. */
const val TABULAR = "tnum"

fun clockStyle(
    weight: FontWeight = FontWeight.Light,
    family: FontFamily = FaceFonts.clock,
    letterSpacingEm: Float = -0.02f
): TextStyle = TextStyle(
    fontFamily = family,
    fontWeight = weight,
    fontFeatureSettings = TABULAR,
    letterSpacing = androidx.compose.ui.unit.TextUnit(letterSpacingEm, androidx.compose.ui.unit.TextUnitType.Em)
)

fun labelStyle(weight: FontWeight = FontWeight.SemiBold, family: FontFamily = FaceFonts.Sans): TextStyle =
    TextStyle(
        fontFamily = family,
        fontWeight = weight,
        letterSpacing = androidx.compose.ui.unit.TextUnit(0.14f, androidx.compose.ui.unit.TextUnitType.Em)
    )

/** Linear blend between two colours. */
fun lerpColor(a: Color, b: Color, t: Float): Color {
    val f = t.coerceIn(0f, 1f)
    return Color(
        red = a.red + (b.red - a.red) * f,
        green = a.green + (b.green - a.green) * f,
        blue = a.blue + (b.blue - a.blue) * f,
        alpha = a.alpha + (b.alpha - a.alpha) * f
    )
}

/**
 * Radial gradient that never asks Android for a zero radius (which throws) — this happens for
 * one frame when a face is laid out at 0×0 during transitions.
 */
fun safeRadialGradient(
    colors: List<androidx.compose.ui.graphics.Color>,
    center: androidx.compose.ui.geometry.Offset = androidx.compose.ui.geometry.Offset.Unspecified,
    radius: Float = Float.POSITIVE_INFINITY
): androidx.compose.ui.graphics.Brush =
    androidx.compose.ui.graphics.Brush.radialGradient(colors, center, if (radius.isFinite()) radius.coerceAtLeast(1f) else radius)
