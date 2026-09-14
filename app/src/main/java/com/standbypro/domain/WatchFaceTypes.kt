package com.standbypro.domain

enum class WatchFaceType(val displayName: String, val description: String) {
    GMT_CALENDAR("GMT & Calendar", "Mechanical GMT dual-time analog clock paired with dynamic calendar"),
    DIGITAL_CALENDAR("Digital & Calendar", "Modern typography digital clock paired with dynamic calendar")
}

enum class BottomComplicationType(val displayName: String, val subtitle: String) {
    BATTERY("Battery & Power", "Battery %, charging speed & temperature"),
    GITHUB_GRAPH("GitHub Heatmap", "Live commit grid, yearly total & streak"),
    ALARM("Alarm & Schedule", "Next upcoming alarm and status")
}
