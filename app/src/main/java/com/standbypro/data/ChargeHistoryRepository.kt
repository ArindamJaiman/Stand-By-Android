package com.standbypro.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** One completed charge: plugged in at [startPercent], unplugged at [endPercent]. */
data class ChargeSession(
    val startMillis: Long,
    val endMillis: Long,
    val startPercent: Int,
    val endPercent: Int,
    val maxTempC: Float? = null
) {
    val minutes: Int get() = ((endMillis - startMillis) / 60_000L).toInt()
    val gained: Int get() = endPercent - startPercent
}

/**
 * Follows the charging state and produces a [ChargeSession] each time the phone is unplugged
 * after a meaningful charge (at least two minutes and one percent).
 */
class ChargeSessionTracker {

    private var startMillis: Long? = null
    private var startPercent = 0
    private var lastPercent = 0
    private var maxTemp: Float? = null

    fun update(isCharging: Boolean, percent: Int, tempC: Float?, nowMillis: Long): ChargeSession? {
        if (isCharging) {
            if (startMillis == null) {
                startMillis = nowMillis
                startPercent = percent
                maxTemp = null
            }
            lastPercent = percent
            if (tempC != null) maxTemp = maxOf(maxTemp ?: tempC, tempC)
            return null
        }
        val start = startMillis ?: return null
        startMillis = null
        val session = ChargeSession(start, nowMillis, startPercent, lastPercent, maxTemp)
        return session.takeIf { nowMillis - start >= 2 * 60_000L && it.gained >= 1 }
    }
}

/** The last [MAX_SESSIONS] charges, kept on the device for the Charge History widget. */
object ChargeHistoryRepository {

    private const val PREFS = "standby_charge_history"
    private const val KEY = "sessions"
    private const val MAX_SESSIONS = 40

    private val _sessions = MutableStateFlow<List<ChargeSession>>(emptyList())
    val sessions: StateFlow<List<ChargeSession>> = _sessions.asStateFlow()

    private var loaded = false

    /** Illustrative week shown in gallery previews before any real charge is recorded. */
    fun sample(nowMillis: Long): List<ChargeSession> {
        val day = 24 * 60 * 60_000L
        return listOf(
            Triple(18, 100, 31.5f), Triple(42, 88, 33.0f), Triple(25, 80, 32.1f), Triple(9, 100, 34.6f),
            Triple(55, 92, 30.8f), Triple(33, 100, 32.9f), Triple(61, 85, 31.2f)
        ).mapIndexed { i, (from, to, temp) ->
            val end = nowMillis - (6 - i) * day
            ChargeSession(end - (to - from) * 90_000L, end, from, to, temp)
        }
    }

    fun load(context: Context) {
        if (loaded) return
        loaded = true
        val json = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null) ?: return
        _sessions.value = runCatching { decode(json) }.getOrDefault(emptyList())
    }

    fun add(context: Context, session: ChargeSession) {
        load(context)
        val updated = (_sessions.value + session).takeLast(MAX_SESSIONS)
        _sessions.value = updated
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, encode(updated)).apply()
    }

    internal fun encode(sessions: List<ChargeSession>): String = JSONArray().apply {
        sessions.forEach { s ->
            put(JSONObject().apply {
                put("start", s.startMillis)
                put("end", s.endMillis)
                put("from", s.startPercent)
                put("to", s.endPercent)
                s.maxTempC?.let { put("temp", it.toDouble()) }
            })
        }
    }.toString()

    internal fun decode(json: String): List<ChargeSession> {
        val array = JSONArray(json)
        return (0 until array.length()).map { i ->
            val o = array.getJSONObject(i)
            ChargeSession(
                startMillis = o.getLong("start"),
                endMillis = o.getLong("end"),
                startPercent = o.getInt("from"),
                endPercent = o.getInt("to"),
                maxTempC = if (o.has("temp")) o.getDouble("temp").toFloat() else null
            )
        }
    }
}
