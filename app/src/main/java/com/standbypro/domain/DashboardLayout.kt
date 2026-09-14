package com.standbypro.domain

import androidx.compose.ui.graphics.Color

/**
 * Dashboard region layout types.
 */
enum class DashboardLayoutType(val displayName: String, val slotCount: Int, val description: String) {
    SINGLE("Single Focus", 1, "Full-screen hero clock or visualizer"),
    DUO("Dual Pane", 2, "Balanced side-by-side clock and widget pane"),
    TRIPLE("Triple Region", 3, "Primary clock with two stacked accessory widgets"),
    QUAD("Quad Matrix", 4, "Four balanced high-density complication quadrants")
}

/**
 * Custom clock styling configuration allowing customization without rewriting renderers.
 */
data class CustomClockConfiguration(
    val baseFace: String = "digital_minimal",
    val primaryAccent: Color = Color(0xFFFF9500),
    val showDate: Boolean = true,
    val showSeconds: Boolean = false,
    val showBattery: Boolean = true,
    val showWeather: Boolean = true,
    val showSecondaryTimezone: Boolean = false,
    val secondaryZoneId: String = "UTC"
)
