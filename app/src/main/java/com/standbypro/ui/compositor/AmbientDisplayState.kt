package com.standbypro.ui.compositor

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.LiveActivityState
import com.standbypro.domain.NotificationDisplayState
import com.standbypro.domain.WidgetSlot

/**
 * Immutable complete snapshot of the ambient display.
 * Rendered identically in the full-screen StandBy display and the Settings Studio preview.
 */
data class AmbientDisplayState(
    val isCharging: Boolean = true,
    val isLandscape: Boolean = true,
    val isNight: Boolean = false,
    val ambientLux: Float = 25f,
    val brightness: Float = 0.5f,
    val selectedProfile: String = "HOME",
    val watchFaceId: String = "gmt_explorer",
    val layout: DashboardLayoutType = DashboardLayoutType.DUO,
    val widgets: List<WidgetSlot> = emptyList(),
    val liveActivity: LiveActivityState? = null,
    val notificationState: NotificationDisplayState = NotificationDisplayState(),
    val burnInOffset: Offset = Offset.Zero,
    val activeThemeColor: Color = Color(0xFFFF9500),
    val nightModeStyle: String = "RED",
    val isDimmed: Boolean = false,
    val showControls: Boolean = false
)
