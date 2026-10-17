package com.standbypro.ui.compositor

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.LiveActivityState
import com.standbypro.domain.NotificationDisplayState
import com.standbypro.domain.WidgetSlot
import com.standbypro.settings.NightStyle

/**
 * Snapshot of the ambient display chrome (layout, night rendition, protection offsets).
 * Rendered identically on the StandBy display and in the Studio preview.
 */
@Immutable
data class AmbientDisplayState(
    val isCharging: Boolean = true,
    val isNight: Boolean = false,
    val nightStyle: NightStyle = NightStyle.RED,
    val brightness: Float = 0.5f,
    val watchFaceId: String = "analog_standby",
    val layout: DashboardLayoutType = DashboardLayoutType.DUO,
    val widgets: List<WidgetSlot> = emptyList(),
    val liveActivity: LiveActivityState? = null,
    val notificationState: NotificationDisplayState = NotificationDisplayState(),
    val burnInOffset: Offset = Offset.Zero,
    val activeThemeColor: Color = Color(0xFFFF9500),
    val isDimmed: Boolean = false
)
