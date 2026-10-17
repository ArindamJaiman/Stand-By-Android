package com.standbypro.data

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import android.util.Log
import com.standbypro.domain.CalendarEventItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Upcoming events from the device calendars (CalendarContract, read-only). */
object CalendarRepository {

    private val _events = MutableStateFlow<List<CalendarEventItem>>(emptyList())
    val events: StateFlow<List<CalendarEventItem>> = _events.asStateFlow()

    private val _hasPermission = MutableStateFlow(false)
    val hasPermission: StateFlow<Boolean> = _hasPermission.asStateFlow()

    fun permissionGranted(context: Context): Boolean =
        context.checkSelfPermission(Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED

    suspend fun refresh(context: Context, use24Hour: Boolean = false) {
        val granted = permissionGranted(context)
        _hasPermission.value = granted
        if (!granted) {
            _events.value = emptyList()
            return
        }
        _events.value = withContext(Dispatchers.IO) { query(context, use24Hour) }
    }

    private fun query(context: Context, use24Hour: Boolean): List<CalendarEventItem> {
        val now = System.currentTimeMillis()
        val end = now + 7L * 24 * 60 * 60 * 1000
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, now - 6L * 60 * 60 * 1000)
            ContentUris.appendId(it, end)
        }.build()
        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.DISPLAY_COLOR
        )
        val zone = ZoneId.systemDefault()
        val timeFmt = DateTimeFormatter.ofPattern(if (use24Hour) "HH:mm" else "h:mm a", Locale.getDefault())
        val dayFmt = DateTimeFormatter.ofPattern("EEE d", Locale.getDefault())
        val today = LocalDate.now(zone)
        val result = mutableListOf<CalendarEventItem>()
        try {
            context.contentResolver.query(uri, projection, null, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { c ->
                while (c.moveToNext() && result.size < 40) {
                    val begin = c.getLong(2)
                    val finish = c.getLong(3)
                    if (finish < now) continue
                    val allDay = c.getInt(4) == 1
                    val startDate = Instant.ofEpochMilli(begin).atZone(if (allDay) ZoneId.of("UTC") else zone)
                    val endDate = Instant.ofEpochMilli(finish).atZone(if (allDay) ZoneId.of("UTC") else zone)
                    val dayPrefix = when (startDate.toLocalDate()) {
                        today -> ""
                        today.plusDays(1) -> "Tomorrow · "
                        else -> startDate.format(dayFmt) + " · "
                    }
                    val range = if (allDay) "${dayPrefix}All day" else "$dayPrefix${startDate.format(timeFmt)} – ${endDate.format(timeFmt)}"
                    result += CalendarEventItem(
                        id = "${c.getLong(0)}_$begin",
                        title = c.getString(1)?.takeIf { it.isNotBlank() } ?: "(No title)",
                        timeRange = range,
                        location = c.getString(5)?.takeIf { it.isNotBlank() },
                        isAllDay = allDay,
                        startMillis = begin,
                        endMillis = finish,
                        colorArgb = if (c.isNull(6)) null else c.getInt(6)
                    )
                }
            }
        } catch (e: SecurityException) {
            Log.w("CalendarRepository", "Calendar permission revoked", e)
        } catch (e: Exception) {
            Log.w("CalendarRepository", "Calendar query failed", e)
        }
        return result
    }
}
