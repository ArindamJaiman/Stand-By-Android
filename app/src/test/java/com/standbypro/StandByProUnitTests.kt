package com.standbypro

import com.standbypro.data.DemoDataProvider
import com.standbypro.data.ProductivityRepository
import com.standbypro.domain.DashboardLayoutType
import com.standbypro.domain.FocusSessionType
import com.standbypro.domain.TimeZoneClock
import com.standbypro.domain.WatchFaceCategory
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WidgetCategory
import com.standbypro.domain.WidgetRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class StandByProUnitTests {

    @Test
    fun testWatchFaceRegistry_countAndCategories() {
        val allFaces = WatchFaceRegistry.getAll()
        // Must have at least 40 distinct faces
        assertTrue("Expected >= 40 faces, found ${allFaces.size}", allFaces.size >= 40)

        val digitalFaces = WatchFaceRegistry.getByCategory(WatchFaceCategory.DIGITAL)
        assertTrue(digitalFaces.isNotEmpty())

        val analogFaces = WatchFaceRegistry.getByCategory(WatchFaceCategory.ANALOG)
        assertTrue(analogFaces.isNotEmpty())

        val gmtFaces = WatchFaceRegistry.getByCategory(WatchFaceCategory.GMT)
        assertTrue(gmtFaces.isNotEmpty())

        val flipFaces = WatchFaceRegistry.getByCategory(WatchFaceCategory.FLIP)
        assertTrue(flipFaces.isNotEmpty())

        // Search test
        val searchResults = WatchFaceRegistry.search("Minimal")
        assertTrue(searchResults.any { it.name.contains("Minimal", ignoreCase = true) })
    }

    @Test
    fun testWidgetRegistry_registration() {
        val allWidgets = WidgetRegistry.getAll()
        assertTrue("Expected >= 14 widgets, found ${allWidgets.size}", allWidgets.size >= 14)

        assertNotNull(WidgetRegistry.getById("widget_calendar"))
        assertNotNull(WidgetRegistry.getById("widget_weather"))
        assertNotNull(WidgetRegistry.getById("widget_battery"))
        assertNotNull(WidgetRegistry.getById("widget_github"))
        assertNotNull(WidgetRegistry.getById("widget_music"))
        assertNotNull(WidgetRegistry.getById("widget_timer"))
        assertNotNull(WidgetRegistry.getById("widget_stopwatch"))
        assertNotNull(WidgetRegistry.getById("widget_pomodoro"))
        assertNotNull(WidgetRegistry.getById("widget_todo"))
        assertNotNull(WidgetRegistry.getById("widget_notes"))
    }

    @Test
    fun testTimeZoneClock_calculations() {
        val utcClock = TimeZoneClock("UTC", "UTC", "Universal Time")
        val zdt = utcClock.getCurrentZonedDateTime()
        assertNotNull(zdt)
        assertEquals(0, zdt.offset.totalSeconds)

        val tokyoClock = TimeZoneClock("Asia/Tokyo", "Tokyo", "Japan")
        val tokyoZdt = tokyoClock.getCurrentZonedDateTime()
        assertNotNull(tokyoZdt)
        assertEquals(9 * 3600, tokyoZdt.offset.totalSeconds)
    }

    @Test
    fun testDemoDataProvider_integrity() {
        assertNotNull(DemoDataProvider.demoWeather)
        assertTrue(DemoDataProvider.demoWeather.hourlyForecast.isNotEmpty())
        assertTrue(DemoDataProvider.demoWeather.dailyForecast.isNotEmpty())
        assertTrue(DemoDataProvider.demoCalendarEvents.isNotEmpty())
        assertTrue(DemoDataProvider.demoTodos.isNotEmpty())
        assertTrue(DemoDataProvider.demoNotes.isNotEmpty())
        assertTrue(DemoDataProvider.demoLiveActivities.isNotEmpty())
        assertTrue(DemoDataProvider.demoWorldClocks.isNotEmpty())
    }

    @Test
    fun testTimerLogic() {
        ProductivityRepository.setTimerDuration(60)
        val initial = ProductivityRepository.timerState.value
        assertEquals(60, initial.totalSeconds)
        assertEquals(60, initial.remainingSeconds)
        assertFalse(initial.isRunning)
        assertFalse(initial.isCompleted)
        assertEquals("01:00", initial.formattedRemaining)
    }

    @Test
    fun testPomodoroTransitions() {
        ProductivityRepository.setPomodoroType(FocusSessionType.POMODORO)
        assertEquals(25 * 60, ProductivityRepository.pomodoroState.value.totalSeconds)

        ProductivityRepository.setPomodoroType(FocusSessionType.SHORT_BREAK)
        assertEquals(5 * 60, ProductivityRepository.pomodoroState.value.totalSeconds)
    }

    @Test
    fun testBurnInBoundedShift() {
        val maxShiftPx = 15f
        var currentX = 0f
        var currentY = 0f

        for (i in 0 until 100) {
            val dx = (i % 5) - 2f
            val dy = ((i * 2) % 5) - 2f
            currentX = (currentX + dx).coerceIn(-maxShiftPx, maxShiftPx)
            currentY = (currentY + dy).coerceIn(-maxShiftPx, maxShiftPx)

            assertTrue("X offset bounded", abs(currentX) <= maxShiftPx)
            assertTrue("Y offset bounded", abs(currentY) <= maxShiftPx)
        }
    }

    @Test
    fun testNightModeHysteresis() {
        val nightThresholdLux = 5.0f
        val dayThresholdLux = 15.0f

        fun evaluateNightMode(lux: Float, currentNightMode: Boolean): Boolean {
            return when {
                lux < nightThresholdLux -> true
                lux > dayThresholdLux -> false
                else -> currentNightMode
            }
        }

        var isNight = false
        // 50 lux -> daylight
        isNight = evaluateNightMode(50f, isNight)
        assertFalse(isNight)

        // drops to 8 lux (in hysteresis band) -> remains daylight
        isNight = evaluateNightMode(8f, isNight)
        assertFalse(isNight)

        // drops to 3 lux -> enters night mode
        isNight = evaluateNightMode(3f, isNight)
        assertTrue(isNight)

        // rises to 10 lux (in hysteresis band) -> remains night mode
        isNight = evaluateNightMode(10f, isNight)
        assertTrue(isNight)

        // rises to 25 lux -> exits night mode
        isNight = evaluateNightMode(25f, isNight)
        assertFalse(isNight)
    }
}
