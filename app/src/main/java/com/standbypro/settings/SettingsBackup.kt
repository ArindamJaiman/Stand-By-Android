package com.standbypro.settings

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Portable settings file: every stored preference with its type, so a backup restores exactly
 * onto a new phone or after a reinstall.
 */
object SettingsBackup {

    const val FORMAT = "standby-pro-settings"
    const val VERSION = 1

    /** Photo grants are tied to this install, so they never travel in a backup. */
    val LOCAL_ONLY_KEYS = setOf("photo_uris")

    fun encode(values: Map<String, Any>): String {
        val settings = JSONObject()
        values.toSortedMap().forEach { (key, value) ->
            if (key in LOCAL_ONLY_KEYS) return@forEach
            val entry = when (value) {
                is Boolean -> typed("boolean", value)
                is Int -> typed("int", value)
                is Long -> typed("long", value)
                is Float -> typed("float", value.toDouble())
                is Double -> typed("double", value)
                is String -> typed("string", value)
                is Set<*> -> typed("stringSet", JSONArray(value.map { it.toString() }.sorted()))
                else -> return@forEach
            }
            settings.put(key, entry)
        }
        return JSONObject()
            .put("format", FORMAT)
            .put("version", VERSION)
            .put("settings", settings)
            .toString(2)
    }

    /** @throws IllegalArgumentException when [json] isn't a StandBy Pro backup. */
    fun decode(json: String): Map<String, Any> {
        val root = try {
            JSONObject(json)
        } catch (e: JSONException) {
            throw IllegalArgumentException("This file isn't a StandBy Pro backup")
        }
        require(root.optString("format") == FORMAT) { "This file isn't a StandBy Pro backup" }
        val settings = root.optJSONObject("settings") ?: return emptyMap()
        val out = mutableMapOf<String, Any>()
        for (key in settings.keys()) {
            if (key in LOCAL_ONLY_KEYS) continue
            val entry = settings.optJSONObject(key) ?: continue
            out[key] = when (entry.optString("type")) {
                "boolean" -> entry.getBoolean("value")
                "int" -> entry.getInt("value")
                "long" -> entry.getLong("value")
                "float" -> entry.getDouble("value").toFloat()
                "double" -> entry.getDouble("value")
                "string" -> entry.getString("value")
                "stringSet" -> entry.getJSONArray("value").let { a -> (0 until a.length()).map { a.getString(it) }.toSet() }
                else -> continue
            }
        }
        return out
    }

    private fun typed(type: String, value: Any): JSONObject = JSONObject().put("type", type).put("value", value)
}
