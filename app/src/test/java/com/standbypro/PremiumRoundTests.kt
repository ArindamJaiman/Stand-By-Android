package com.standbypro

import com.standbypro.alarm.AlarmMath
import com.standbypro.alarm.AlarmRinger
import com.standbypro.alarm.BedsideAlarm
import com.standbypro.audio.BellSynth
import com.standbypro.domain.SmartStackBrain
import com.standbypro.settings.StandByColorTheme
import com.standbypro.ui.design.SkyAccent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

class PremiumRoundTests {

    private val zone = ZoneId.of("Europe/London")

    // 2026-10-22 is a Thursday.
    private fun at(day: Int, hour: Int, minute: Int): ZonedDateTime = ZonedDateTime.of(LocalDateTime.of(2026, 10, day, hour, minute), zone)

    @Test
    fun alarm_ringsLaterTodayOrTomorrow() {
        val once = BedsideAlarm(enabled = true, hour = 7, minute = 30, days = emptySet())
        assertEquals(at(22, 7, 30), AlarmMath.nextTrigger(once, at(22, 6, 0)))
        assertEquals(at(23, 7, 30), AlarmMath.nextTrigger(once, at(22, 7, 30)))
        assertEquals(at(23, 7, 30), AlarmMath.nextTrigger(once, at(22, 22, 0)))
        assertNull(AlarmMath.nextTrigger(once.copy(enabled = false), at(22, 6, 0)))
    }

    @Test
    fun alarm_skipsDaysThatArentSelected() {
        val weekdays = BedsideAlarm(enabled = true, hour = 6, minute = 45, days = DayOfWeek.entries.filter { it.value <= 5 }.toSet())
        // Friday evening → next is Monday morning.
        assertEquals(at(26, 6, 45), AlarmMath.nextTrigger(weekdays, at(23, 21, 0)))
        val weekend = weekdays.copy(days = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY))
        assertEquals(at(24, 6, 45), AlarmMath.nextTrigger(weekend, at(22, 12, 0)))
    }

    @Test
    fun alarm_describesItsSchedule() {
        assertEquals("Once", AlarmMath.describeDays(emptySet()))
        assertEquals("Every day", AlarmMath.describeDays(DayOfWeek.entries.toSet()))
        assertEquals("Weekdays", AlarmMath.describeDays(DayOfWeek.entries.filter { it.value <= 5 }.toSet()))
        assertEquals("Weekends", AlarmMath.describeDays(setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)))
        assertEquals("Mon, Wed", AlarmMath.describeDays(setOf(DayOfWeek.WEDNESDAY, DayOfWeek.MONDAY), java.util.Locale.UK))
        assertEquals("Rings in 7 h 30 min", AlarmMath.ringsIn(at(22, 0, 0), at(22, 7, 29)))
        assertEquals("Rings in 45 min", AlarmMath.ringsIn(at(22, 0, 0), at(22, 0, 44)))
        assertEquals("Rings in 2 h", AlarmMath.ringsIn(at(22, 0, 0), at(22, 1, 59)))
    }

    @Test
    fun gentleWake_fadesInOverAMinute() {
        assertEquals(0.05f, AlarmRinger.rampVolume(0), 0.001f)
        assertTrue(AlarmRinger.rampVolume(20) < AlarmRinger.rampVolume(40))
        assertEquals(1f, AlarmRinger.rampVolume(60), 0.001f)
        assertEquals(1f, AlarmRinger.rampVolume(600), 0.001f)
    }

    @Test
    fun smartStack_surfacesWhatMattersNow() {
        val stack = listOf("widget_clock_analog", "widget_weather", "widget_music", "widget_agenda", "widget_timer")
        val quiet = SmartStackBrain.Signals(hour = 14)
        assertNull(SmartStackBrain.suggest(stack, quiet))
        assertEquals(2, SmartStackBrain.suggest(stack, quiet.copy(musicPlaying = true)))
        assertEquals(4, SmartStackBrain.suggest(stack, quiet.copy(timerRunning = true)))
        // Music outranks a meeting in ten minutes.
        assertEquals(2, SmartStackBrain.suggest(stack, quiet.copy(musicPlaying = true, minutesToNextEvent = 10)))
        assertEquals(3, SmartStackBrain.suggest(stack, quiet.copy(minutesToNextEvent = 20)))
        assertEquals(1, SmartStackBrain.suggest(stack, quiet.copy(rainSoon = true)))
        assertEquals(1, SmartStackBrain.suggest(stack, SmartStackBrain.Signals(hour = 7)))
        assertNull(SmartStackBrain.suggest(listOf("widget_clock_analog"), SmartStackBrain.Signals(hour = 7, musicPlaying = true)))
    }

    @Test
    fun bells_renderWithoutClipping() {
        val pcm = BellSynth.render(BellSynth.templeBell(), tailSeconds = 1.0)
        assertEquals(BellSynth.SAMPLE_RATE, pcm.size)
        val peak = pcm.maxOf { abs(it.toInt()) }
        assertTrue(peak in (Short.MAX_VALUE * 0.7).toInt()..(Short.MAX_VALUE * 0.81).toInt())
        // The bell decays: the last tenth is much quieter than the first.
        val head = pcm.take(pcm.size / 10).maxOf { abs(it.toInt()) }
        val tail = pcm.takeLast(pcm.size / 10).maxOf { abs(it.toInt()) }
        assertTrue(tail < head / 3)
    }

    @Test
    fun westminster_ringsTheMelodyThenTheHour() {
        val seven = BellSynth.westminsterHour(7)
        assertEquals(16 + 7, seven.size)
        assertEquals(16 + 12, BellSynth.westminsterHour(12).size)
        assertTrue(seven.zipWithNext().all { (a, b) -> b.startSeconds > a.startSeconds })
        assertEquals(1, BellSynth.westminsterHour(0).size - 16)
        BellSynth.HOUR_CHANGES.forEach { assertEquals(4, it.size) }
    }

    @Test
    fun skyAccent_followsTheDay() {
        val dawn = SkyAccent.colorAt(5 * 60 + 30)
        val noon = SkyAccent.colorAt(13 * 60)
        assertTrue("dawn is rosy", dawn.red > dawn.blue)
        assertTrue("noon is blue", noon.blue > noon.red)
        assertEquals(SkyAccent.colorAt(0), SkyAccent.colorAt(24 * 60))
        val between = SkyAccent.colorAt(10 * 60 + 45)
        val gold = SkyAccent.colorAt(10 * 60)
        assertTrue(between.blue > gold.blue && between.blue < noon.blue)
        // Every hour stays bright: no keyframe blend drops into dull grey.
        (0 until 24 * 60 step 15).forEach { m ->
            val c = SkyAccent.colorAt(m)
            assertTrue("minute $m too dull", maxOf(c.red, c.green, c.blue) > 0.7f)
        }
        assertEquals(StandByColorTheme.SKY, StandByColorTheme.fromId("sky"))
    }
}
