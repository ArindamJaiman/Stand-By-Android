package com.standbypro.domain

data class TodoItem(
    val id: String,
    val title: String,
    val isCompleted: Boolean = false,
    val category: String = "General"
)

data class BedsideNote(
    val id: String,
    val title: String,
    val content: String,
    val isPinned: Boolean = true,
    val timestamp: String = "Today"
)

data class CalendarEventItem(
    val id: String,
    val title: String,
    val timeRange: String,
    val location: String? = null,
    val isAllDay: Boolean = false,
    val colorHex: String = "#00E5FF"
)
