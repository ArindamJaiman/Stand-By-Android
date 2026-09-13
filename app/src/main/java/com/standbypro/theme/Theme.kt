package com.standbypro.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StandByColorScheme = darkColorScheme(
    primary = StandByAccent,
    secondary = StandByAccentDim,
    tertiary = StandByNightRed,
    background = StandByBackground,
    surface = StandBySurface,
    surfaceVariant = StandBySurfaceVariant,
    onPrimary = StandByOnAccent,
    onBackground = StandByOnSurface,
    onSurface = StandByOnSurface,
    error = StandByError
)

@Composable
fun StandByProTheme(
    content: @Composable () -> Unit,
) {
    // Always dark — this is an ambient display app
    MaterialTheme(
        colorScheme = StandByColorScheme,
        typography = Typography,
        content = content
    )
}
