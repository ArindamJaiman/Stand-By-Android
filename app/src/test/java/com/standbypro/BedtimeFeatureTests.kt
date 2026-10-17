package com.standbypro

import com.standbypro.audio.NoiseGenerator
import com.standbypro.audio.SleepSound
import com.standbypro.audio.SoundState
import com.standbypro.data.AirQualityRepository
import com.standbypro.domain.AqiCategory
import com.standbypro.domain.CalendarEventItem
import com.standbypro.domain.UpNext
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WidgetRegistry
import com.standbypro.power.ChargeLimitWatcher
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.faces.Breathing
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class BedtimeFeatureTests {

    private fun rms(sound: SleepSound, samples: Int = 22050): Double {
        val gen = NoiseGenerator(sound, seed = 7)
        var sum = 0.0
        repeat(samples) { val v = gen.next(); sum += v * v }
        return sqrt(sum / samples)
    }

    private fun zeroCrossingRate(sound: SleepSound, samples: Int = 22050): Double {
        val gen = NoiseGenerator(sound, seed = 7)
        var last = gen.next()
        var crossings = 0
        repeat(samples) {
            val v = gen.next()
            if ((v >= 0f) != (last >= 0f)) crossings++
            last = v
        }
        return crossings.toDouble() / samples
    }

    @Test
    fun noise_isAudibleAndInRange_forEverySound() {
        SleepSound.entries.forEach { sound ->
            val gen = NoiseGenerator(sound, seed = 3)
            repeat(44100) {
                val v = gen.next()
                assertTrue("$sound sample out of range: $v", v in -1f..1f)
            }
            assertTrue("$sound is too quiet", rms(sound) > 0.02)
        }
    }

    @Test
    fun noise_isDeterministicForASeed() {
        val a = NoiseGenerator(SleepSound.RAIN, seed = 42)
        val b = NoiseGenerator(SleepSound.RAIN, seed = 42)
        repeat(1000) { assertEquals(a.next(), b.next(), 0f) }
    }

    @Test
    fun brownNoise_isDeeperThanWhiteNoise() {
        assertTrue(zeroCrossingRate(SleepSound.BROWN) < zeroCrossingRate(SleepSound.WHITE) / 4)
    }

    @Test
    fun fill_rampsGainWithoutClipping() {
        val buffer = ShortArray(1000)
        NoiseGenerator(SleepSound.WHITE, seed = 1).fill(buffer, 0f, 1f)
        val firstQuarter = buffer.take(250).maxOf { kotlin.math.abs(it.toInt()) }
        val lastQuarter = buffer.takeLast(250).maxOf { kotlin.math.abs(it.toInt()) }
        assertTrue(firstQuarter < lastQuarter)
        NoiseGenerator(SleepSound.WHITE, seed = 1).fill(buffer, 0f)
        assertTrue(buffer.all { it.toInt() == 0 })
    }

    @Test
    fun sleepTimer_reportsRemainingMinutesRoundedUp() {
        val now = 1_000_000L
        assertNull(SoundState().sleepTimerRemainingMinutes(now))
        assertEquals(30, SoundState(sleepTimerEndsAt = now + 30 * 60_000L).sleepTimerRemainingMinutes(now))
        assertEquals(1, SoundState(sleepTimerEndsAt = now + 5_000L).sleepTimerRemainingMinutes(now))
        assertEquals(0, SoundState(sleepTimerEndsAt = now - 5_000L).sleepTimerRemainingMinutes(now))
    }

    @Test
    fun sunrise_rampsBeforeTheAlarmAndHoldsAfterIt() {
        val alarm = 10_000_000L
        val min = 60_000L
        assertEquals(0f, StandByViewModel.sunriseProgress(alarm - 31 * min, alarm, 30), 0f)
        assertEquals(0.5f, StandByViewModel.sunriseProgress(alarm - 15 * min, alarm, 30), 0.001f)
        assertEquals(1f, StandByViewModel.sunriseProgress(alarm + 5 * min, alarm, 30), 0f)
        assertEquals(0f, StandByViewModel.sunriseProgress(alarm + 20 * min, alarm, 30), 0f)
        assertEquals(0f, StandByViewModel.sunriseProgress(alarm - min, alarm, 0), 0f)
        assertEquals(0f, StandByViewModel.sunriseProgress(alarm - min, null, 30), 0f)
    }

    @Test
    fun chargeLimit_alertsOncePerCharge() {
        val w = ChargeLimitWatcher()
        assertFalse(w.update(isCharging = true, percent = 70, limitPercent = 80))
        assertTrue(w.update(isCharging = true, percent = 80, limitPercent = 80))
        assertFalse(w.update(isCharging = true, percent = 81, limitPercent = 80))
        assertFalse(w.update(isCharging = false, percent = 81, limitPercent = 80))
        assertTrue(w.update(isCharging = true, percent = 81, limitPercent = 80))
        assertFalse(ChargeLimitWatcher().update(isCharging = true, percent = 100, limitPercent = 0))
    }

    @Test
    fun upNext_picksTheEventAboutToStart() {
        val now = 50_000_000L
        val min = 60_000L
        val events = listOf(
            CalendarEventItem("later", "Later", "", startMillis = now + 40 * min),
            CalendarEventItem("soon", "Standup", "", startMillis = now + 8 * min),
            CalendarEventItem("allday", "Holiday", "", isAllDay = true, startMillis = now + min),
            CalendarEventItem("past", "Old", "", startMillis = now - 30 * min)
        )
        val pick = UpNext.eventFor(events, now)
        assertEquals("soon", pick?.id)
        assertEquals("in 8 min", UpNext.startsIn(pick!!, now))
        assertEquals("Now", UpNext.startsIn(pick, now + 9 * min))
        assertNull(UpNext.eventFor(listOf(events[0]), now))
    }

    @Test
    fun breathing_followsBoxPattern() {
        assertEquals("Breathe in", Breathing.at(0.5f).label)
        assertEquals(0f, Breathing.at(0f).scale, 0.001f)
        assertEquals(1f, Breathing.at(4f).scale, 0.001f)
        assertEquals("Hold", Breathing.at(5f).label)
        assertEquals("Breathe out", Breathing.at(9f).label)
        assertEquals("Hold", Breathing.at(13f).label)
        assertEquals(0f, Breathing.at(13f).scale, 0.001f)
        assertEquals(Breathing.at(2f), Breathing.at(2f + Breathing.CYCLE_SECONDS * 100))
        assertEquals(4, Breathing.at(16f).secondsLeft)
        assertEquals(1, Breathing.at(3.9f).secondsLeft)
    }

    @Test
    fun airQuality_parsesAndCategorises() {
        val json = """{"current":{"time":"2026-10-17T09:00","us_aqi":118,"pm2_5":42.6,"pm10":71.0,"ozone":88.0,"uv_index":6.4}}"""
        val air = AirQualityRepository.parse(json)
        assertEquals(118, air.usAqi)
        assertEquals(42.6, air.pm25, 0.001)
        assertEquals(AqiCategory.SENSITIVE, air.category)
        assertEquals("High", air.uvLabel)
        assertEquals(AqiCategory.GOOD, AqiCategory.of(0))
        assertEquals(AqiCategory.HAZARDOUS, AqiCategory.of(450))
    }

    @Test
    fun newFaceAndWidgets_areRegistered() {
        assertTrue(WatchFaceRegistry.contains("ambient_breathe"))
        assertTrue(WidgetRegistry.contains("widget_air_quality"))
        assertTrue(WidgetRegistry.contains("widget_breathe"))
    }
}
