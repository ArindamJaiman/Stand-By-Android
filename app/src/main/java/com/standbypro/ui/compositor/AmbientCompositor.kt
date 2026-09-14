package com.standbypro.ui.compositor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.domain.ChargingState
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.IncomingCallState
import com.standbypro.domain.LiveActivityState
import com.standbypro.domain.NotificationPrivacy
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WidgetRegistry
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Composable
fun AmbientCompositor(
    state: AmbientDisplayState,
    currentTime: LocalDateTime,
    chargingState: ChargingState,
    nextAlarm: String?,
    onUserInteraction: () -> Unit,
    onCycleFace: () -> Unit = {},
    onCycleRightWidget: () -> Unit = {},
    incomingCall: IncomingCallState? = null,
    onAnswerCall: () -> Unit = {},
    onDeclineCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val appliedBurnInOffset = IntOffset(
        state.burnInOffset.x.roundToInt(),
        state.burnInOffset.y.roundToInt()
    )

    val effectiveAccent = when {
        state.isDimmed -> Color.Gray
        state.isNight && state.nightModeStyle == "RED" -> Color(0xFFFF3B30)
        state.isNight && state.nightModeStyle == "DIM_AMBER" -> Color(0xFFFF9500).copy(alpha = 0.6f)
        state.isNight && state.nightModeStyle == "MONOCHROME" -> Color.LightGray
        else -> state.activeThemeColor
    }

    // Outer Canvas Layer (OLED True Black)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onUserInteraction() },
                    onDoubleTap = {
                        onUserInteraction()
                        onCycleFace()
                    }
                )
            }
    ) {
        // Content container offset by Burn-In Protection
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { appliedBurnInOffset }
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            // Dashboard Layout Engine
            when (state.layout) {
                DashboardLayoutType.SINGLE -> {
                    // Full-screen Clock
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        WatchFaceRegistry.Render(
                            id = state.watchFaceId,
                            time = currentTime,
                            accentColor = effectiveAccent,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                DashboardLayoutType.DUO -> {
                    // Dual-Pane: Left WatchFace, Right Widget
                    val rightWidgetId = state.widgets.firstOrNull()?.widgetId ?: "widget_calendar"

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onUserInteraction()
                                    onCycleFace()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            WatchFaceRegistry.Render(
                                id = state.watchFaceId,
                                time = currentTime,
                                accentColor = effectiveAccent,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(28.dp))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    onUserInteraction()
                                    onCycleRightWidget()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            WidgetRegistry.Render(
                                id = rightWidgetId,
                                time = currentTime,
                                chargingState = chargingState,
                                nextAlarm = nextAlarm,
                                accentColor = effectiveAccent,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }

                DashboardLayoutType.TRIPLE -> {
                    // Triple Region: Left primary watch face, Right 2 stacked widgets
                    val w1 = state.widgets.getOrNull(0)?.widgetId ?: "widget_weather"
                    val w2 = state.widgets.getOrNull(1)?.widgetId ?: "widget_battery"

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.weight(1.1f).fillMaxHeight(),
                            contentAlignment = Alignment.Center
                        ) {
                            WatchFaceRegistry.Render(
                                id = state.watchFaceId,
                                time = currentTime,
                                accentColor = effectiveAccent,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(20.dp))

                        Column(
                            modifier = Modifier.weight(0.9f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                WidgetRegistry.Render(w1, currentTime, chargingState, nextAlarm, effectiveAccent, Modifier.fillMaxSize())
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                WidgetRegistry.Render(w2, currentTime, chargingState, nextAlarm, effectiveAccent, Modifier.fillMaxSize())
                            }
                        }
                    }
                }

                DashboardLayoutType.QUAD -> {
                    // Quad Matrix: 4 balanced quadrants
                    val w1 = state.widgets.getOrNull(0)?.widgetId ?: "widget_weather"
                    val w2 = state.widgets.getOrNull(1)?.widgetId ?: "widget_calendar"
                    val w3 = state.widgets.getOrNull(2)?.widgetId ?: "widget_battery"

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                WatchFaceRegistry.Render(state.watchFaceId, currentTime, effectiveAccent, modifier = Modifier.fillMaxSize())
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                WidgetRegistry.Render(w2, currentTime, chargingState, nextAlarm, effectiveAccent, Modifier.fillMaxSize())
                            }
                        }
                        Column(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                WidgetRegistry.Render(w1, currentTime, chargingState, nextAlarm, effectiveAccent, Modifier.fillMaxSize())
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                                WidgetRegistry.Render(w3, currentTime, chargingState, nextAlarm, effectiveAccent, Modifier.fillMaxSize())
                            }
                        }
                    }
                }
            }

            // Live Activity Layer (Dynamic Top Pill / Island)
            if (state.liveActivity != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF1C1C1E))
                        .border(1.dp, Color(0xFF2C2C2E), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = effectiveAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = state.liveActivity.title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "• ${state.liveActivity.subtitle}",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Notification Privacy Layer (Subtle bottom-left badge)
            if (state.notificationState.unreadCount > 0 && state.notificationState.privacyMode != NotificationPrivacy.HIDDEN) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.10f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${state.notificationState.unreadCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Incoming Call Presentation Overlay
            if (incomingCall != null && incomingCall.isIncoming) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 10.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF202024))
                        .border(1.dp, Color(0xFF38383E), RoundedCornerShape(24.dp))
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column {
                            Text(text = incomingCall.callerName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = incomingCall.callerSubtitle, fontSize = 11.sp, color = Color.Gray)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            IconButton(
                                onClick = onDeclineCall,
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFFF3B30))
                            ) {
                                Icon(imageVector = Icons.Default.CallEnd, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = onAnswerCall,
                                modifier = Modifier.size(36.dp).clip(CircleShape).background(Color(0xFF30D158))
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = "Answer", tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }

        // Auto-Dim Protection Overlay
        if (state.isDimmed) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.65f)
                    .background(Color.Black)
            )
        }
    }
}
