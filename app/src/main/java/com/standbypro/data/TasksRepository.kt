package com.standbypro.data

import android.content.Context
import com.standbypro.domain.BedsideNote
import com.standbypro.domain.CalendarEventItem
import com.standbypro.domain.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Bedside to-dos and notes, persisted locally so they survive restarts. */
object TasksRepository {

    private const val PREFS = "standby_tasks"
    private var appContext: Context? = null

    private val _todos = MutableStateFlow(DemoDataProvider.starterTodos)
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    private val _notes = MutableStateFlow(DemoDataProvider.starterNotes)
    val notes: StateFlow<List<BedsideNote>> = _notes.asStateFlow()

    /** Demo agenda used only when demo mode is on; real events come from [CalendarRepository]. */
    val demoEvents: List<CalendarEventItem> get() = DemoDataProvider.demoCalendarEvents

    fun init(context: Context) {
        if (appContext != null) return
        appContext = context.applicationContext
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString("todos", null)?.let { json ->
            runCatching {
                val arr = JSONArray(json)
                _todos.value = (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    TodoItem(o.getString("id"), o.getString("title"), o.optBoolean("done"), o.optString("category", "General"))
                }
            }
        }
        prefs.getString("notes", null)?.let { json ->
            runCatching {
                val arr = JSONArray(json)
                _notes.value = (0 until arr.length()).map { i ->
                    val o = arr.getJSONObject(i)
                    BedsideNote(o.getString("id"), o.optString("title"), o.optString("content"), o.optBoolean("pinned", true), o.optString("timestamp", ""))
                }
            }
        }
    }

    fun toggleTodo(id: String) {
        _todos.value = _todos.value.map { if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it }
        save()
    }

    fun addTodo(title: String, category: String = "General") {
        if (title.isBlank()) return
        _todos.value = listOf(TodoItem("td_${System.currentTimeMillis()}", title.trim(), false, category)) + _todos.value
        save()
    }

    fun removeTodo(id: String) {
        _todos.value = _todos.value.filter { it.id != id }
        save()
    }

    fun clearCompleted() {
        _todos.value = _todos.value.filter { !it.isCompleted }
        save()
    }

    fun togglePinNote(id: String) {
        _notes.value = _notes.value.map { if (it.id == id) it.copy(isPinned = !it.isPinned) else it }
        save()
    }

    fun addNote(title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        _notes.value = listOf(BedsideNote("note_${System.currentTimeMillis()}", title.trim(), content.trim(), true, "Just now")) + _notes.value
        save()
    }

    fun removeNote(id: String) {
        _notes.value = _notes.value.filter { it.id != id }
        save()
    }

    private fun save() {
        val ctx = appContext ?: return
        val todos = JSONArray().apply {
            _todos.value.forEach { put(JSONObject().put("id", it.id).put("title", it.title).put("done", it.isCompleted).put("category", it.category)) }
        }
        val notes = JSONArray().apply {
            _notes.value.forEach { put(JSONObject().put("id", it.id).put("title", it.title).put("content", it.content).put("pinned", it.isPinned).put("timestamp", it.timestamp)) }
        }
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putString("todos", todos.toString())
            .putString("notes", notes.toString())
            .apply()
    }
}
