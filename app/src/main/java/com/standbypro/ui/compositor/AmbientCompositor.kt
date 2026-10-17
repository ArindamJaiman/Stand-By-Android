package com.standbypro.ui.compositor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.FaceContext
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WidgetRegistry

/**
 * Dashboard layout engine: places the active face and assigned widgets according to the
 * chosen [DashboardLayoutType]. Shared by the StandBy display and the Studio live preview.
 */
@Composable
fun AmbientCompositor(
    state: AmbientDisplayState,
    ctx: FaceContext,
    modifier: Modifier = Modifier
) {
    val widgets = state.widgets.map { it.widgetId }
    fun w(i: Int, fallback: String) = widgets.getOrNull(i)?.takeIf { WidgetRegistry.contains(it) } ?: fallback
    val gap = 14.dp
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state.layout) {
            DashboardLayoutType.SINGLE -> {
                WatchFaceRegistry.Render(state.watchFaceId, ctx, Modifier.fillMaxSize())
            }
            DashboardLayoutType.DUO -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).fillMaxHeight()) { WatchFaceRegistry.Render(state.watchFaceId, ctx, Modifier.fillMaxSize()) }
                Box(Modifier.weight(1f).fillMaxHeight()) { WidgetRegistry.Render(w(0, "widget_calendar"), ctx.copy(isCompact = true), Modifier.fillMaxSize()) }
            }
            DashboardLayoutType.TRIPLE -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                Box(Modifier.weight(1.15f).fillMaxHeight()) { WatchFaceRegistry.Render(state.watchFaceId, ctx, Modifier.fillMaxSize()) }
                Column(Modifier.weight(0.85f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(gap)) {
                    WidgetRegistry.Render(w(0, "widget_weather"), ctx.copy(isCompact = true), Modifier.fillMaxWidth().weight(1f))
                    WidgetRegistry.Render(w(1, "widget_battery"), ctx.copy(isCompact = true), Modifier.fillMaxWidth().weight(1f))
                }
            }
            DashboardLayoutType.QUAD -> Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(gap)) {
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(gap)) {
                    Box(Modifier.fillMaxWidth().weight(1f)) { WatchFaceRegistry.Render(state.watchFaceId, ctx.copy(isCompact = true), Modifier.fillMaxSize()) }
                    WidgetRegistry.Render(w(1, "widget_calendar"), ctx.copy(isCompact = true), Modifier.fillMaxWidth().weight(1f))
                }
                Column(Modifier.weight(1f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(gap)) {
                    WidgetRegistry.Render(w(0, "widget_weather"), ctx.copy(isCompact = true), Modifier.fillMaxWidth().weight(1f))
                    WidgetRegistry.Render(w(2, "widget_battery"), ctx.copy(isCompact = true), Modifier.fillMaxWidth().weight(1f))
                }
            }
        }
    }
}
