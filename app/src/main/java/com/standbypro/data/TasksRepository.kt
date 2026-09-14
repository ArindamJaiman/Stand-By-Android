package com.standbypro.data

import com.standbypro.domain.BedsideNote
import com.standbypro.domain.CalendarEventItem
import com.standbypro.domain.TodoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TasksRepository {

    private val _todos = MutableStateFlow(DemoDataProvider.demoTodos)
    val todos: StateFlow<List<TodoItem>> = _todos.asStateFlow()

    private val _notes = MutableStateFlow(DemoDataProvider.demoNotes)
    val notes: StateFlow<List<BedsideNote>> = _notes.asStateFlow()

    private val _events = MutableStateFlow(DemoDataProvider.demoCalendarEvents)
    val events: StateFlow<List<CalendarEventItem>> = _events.asStateFlow()

    fun toggleTodo(id: String) {
        _todos.value = _todos.value.map {
            if (it.id == id) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    fun addTodo(title: String, category: String = "General") {
        if (title.isBlank()) return
        val newItem = TodoItem("td_${System.currentTimeMillis()}", title.trim(), false, category)
        _todos.value = listOf(newItem) + _todos.value
    }

    fun removeTodo(id: String) {
        _todos.value = _todos.value.filter { it.id != id }
    }

    fun togglePinNote(id: String) {
        _notes.value = _notes.value.map {
            if (it.id == id) it.copy(isPinned = !it.isPinned) else it
        }
    }

    fun addNote(title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        val newNote = BedsideNote("note_${System.currentTimeMillis()}", title.trim(), content.trim(), true, "Just now")
        _notes.value = listOf(newNote) + _notes.value
    }
}
