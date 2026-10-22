package com.standbypro.ui.settings

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.standbypro.domain.WatchFaceCategory
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.media.NowPlayingRepository
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.compositor.AmbientCompositor
import com.standbypro.ui.design.BackdropLayer
import com.standbypro.ui.design.Ink
import com.standbypro.ui.main.nightFilter

enum class StudioTab(val label: String) { FACES("Faces"), WIDGETS("Widgets"), DISPLAY("Display"), DATA("Data"), BEHAVIOUR("Behaviour") }

@Composable
fun SettingsScreen(
    viewModel: StandByViewModel,
    onLaunchPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val ambient by viewModel.ambientDisplayState.collectAsState()
    val ctx by viewModel.faceContext.collectAsState()
    val resumeTick by viewModel.resumeTick.collectAsState()
    val calendarGranted by viewModel.calendarPermission.collectAsState()
    val notificationAccess by viewModel.notificationAccess.collectAsState()
    val context = LocalContext.current
    val accent = settings.activeColorTheme.color

    var tab by rememberSaveable { mutableStateOf(StudioTab.FACES) }
    var query by rememberSaveable { mutableStateOf("") }
    var category by remember { mutableStateOf<WatchFaceCategory?>(null) }
    var favoritesOnly by remember { mutableStateOf(false) }
    var previewNight by remember { mutableStateOf(false) }

    val calendarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { viewModel.onResume() }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { viewModel.onResume() }

    val fsiAllowed = remember(resumeTick) {
        if (Build.VERSION.SDK_INT >= 34) {
            (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).canUseFullScreenIntent()
        } else true
    }
    val postNotificationsAllowed = remember(resumeTick) {
        Build.VERSION.SDK_INT < 33 || context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
    val batteryUnrestricted = remember(resumeTick) {
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isIgnoringBatteryOptimizations(context.packageName)
    }
    val listenerAccess = remember(resumeTick, notificationAccess) { NowPlayingRepository.hasNotificationAccess(context) }

    LazyColumn(
        modifier = modifier.fillMaxSize().background(Color.Black).windowInsetsPadding(WindowInsets.safeDrawing),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // ---------------- Header ----------------
        item(key = "header") {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("StandBy", color = Ink.TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.Bold)
                    Text("${WatchFaceRegistry.getAll().size} faces · ${com.standbypro.domain.WidgetRegistry.getAll().size} widgets", color = Ink.TextTertiary, fontSize = 13.sp)
                }
                Row(
                    Modifier.clip(RoundedCornerShape(50)).background(accent).clickable(onClick = onLaunchPreview).padding(horizontal = 18.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Start", color = Color.Black, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // ---------------- What's new ----------------
        if (settings.whatsNewSeen < WhatsNew.VERSION) {
            item(key = "whats_new") {
                WhatsNewCard(accent) { viewModel.dismissWhatsNew(WhatsNew.VERSION) }
            }
        }

        // ---------------- Live preview ----------------
        item(key = "preview") {
            Column {
                Box(
                    Modifier.fillMaxWidth().aspectRatio(2.05f).clip(RoundedCornerShape(26.dp)).background(Color.Black)
                        .border(1.dp, Brush.verticalGradient(listOf(Ink.HairlineStrong, Ink.Hairline)), RoundedCornerShape(26.dp))
                        .nightFilter(settings.nightStyle, if (previewNight) 1f else 0f)
                ) {
                    BackdropLayer(settings.backdrop, accent, animate = false, modifier = Modifier.fillMaxSize())
                    AmbientCompositor(ambient.copy(isNight = previewNight), ctx.copy(isNight = previewNight), Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp))
                }
                Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PreviewChip(WatchFaceRegistry.getById(settings.activeWatchFaceId).name, accent, true) { tab = StudioTab.FACES }
                    PreviewChip(settings.layoutType.displayName, accent, false) { tab = StudioTab.WIDGETS }
                    Spacer(Modifier.weight(1f))
                    PreviewChip(if (previewNight) "Night ✓" else "Night", Ink.Red, previewNight, Icons.Default.NightsStay) { previewNight = !previewNight }
                }
            }
        }

        // ---------------- Setup checklist ----------------
        val missing = listOf(
            !postNotificationsAllowed, !fsiAllowed, !listenerAccess, !calendarGranted, !batteryUnrestricted
        ).count { it }
        if (missing > 0) {
            item(key = "setup") {
                StudioSection("Finish setup · $missing left", footer = "These unlock auto-launch over the lock screen, Now Playing, notifications and your agenda.") {
                    if (!postNotificationsAllowed && Build.VERSION.SDK_INT >= 33) {
                        NavRow("Allow notifications", "Needed for the auto-launch notification", icon = Icons.Default.Notifications, iconTint = Ink.Red) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        RowDivider()
                    }
                    if (!fsiAllowed && Build.VERSION.SDK_INT >= 34) {
                        NavRow("Full-screen launch", "Lets StandBy appear over the lock screen", icon = Icons.Default.Fullscreen, iconTint = Ink.Orange) {
                            context.startActivity(Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:${context.packageName}")))
                        }
                        RowDivider()
                    }
                    if (!listenerAccess) {
                        NavRow("Notification access", "Now Playing controls and notification badges", icon = Icons.Default.NotificationsActive, iconTint = Ink.Pink) {
                            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        }
                        RowDivider()
                    }
                    if (!calendarGranted) {
                        NavRow("Calendar", "Show your events on agenda faces and widgets", icon = Icons.Default.CalendarMonth, iconTint = Ink.Red) {
                            calendarLauncher.launch(Manifest.permission.READ_CALENDAR)
                        }
                        RowDivider()
                    }
                    if (!batteryUnrestricted) {
                        NavRow("Battery optimisation", "Keep the charging monitor alive in the background", icon = Icons.Default.BatteryAlert, iconTint = Ink.Green) {
                            context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                        }
                    }
                }
            }
        }

        // ---------------- Tabs ----------------
        item(key = "tabs") {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).clip(RoundedCornerShape(16.dp)).background(Ink.Card).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                StudioTab.entries.forEach { t ->
                    val sel = t == tab
                    Box(
                        Modifier.clip(RoundedCornerShape(12.dp)).background(if (sel) Color.White.copy(alpha = 0.14f) else Color.Transparent)
                            .clickable { tab = t }.padding(horizontal = 18.dp, vertical = 10.dp)
                    ) {
                        Text(t.label, color = if (sel) Ink.TextPrimary else Ink.TextTertiary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        when (tab) {
            StudioTab.FACES -> facesTab(viewModel, settings, ctx, query, { query = it }, category, { category = it }, favoritesOnly, { favoritesOnly = it })
            StudioTab.WIDGETS -> widgetsTab(viewModel, settings, ctx)
            StudioTab.DISPLAY -> displayTab(viewModel, settings, ctx)
            StudioTab.DATA -> dataTab(viewModel, settings, calendarGranted, listenerAccess, { calendarLauncher.launch(Manifest.permission.READ_CALENDAR) })
            StudioTab.BEHAVIOUR -> behaviourTab(viewModel, settings)
        }

        item(key = "footer") {
            Text(
                "StandBy Pro · crafted by Arindam Jaiman",
                color = Ink.TextTertiary, fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun PreviewChip(text: String, accent: Color, emphasised: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector? = null, onClick: () -> Unit) {
    Row(
        Modifier.clip(RoundedCornerShape(50))
            .background(if (emphasised) accent.copy(alpha = 0.16f) else Color.White.copy(alpha = 0.07f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(icon, null, tint = if (emphasised) accent else Ink.TextSecondary, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(text, color = if (emphasised) accent else Ink.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}
