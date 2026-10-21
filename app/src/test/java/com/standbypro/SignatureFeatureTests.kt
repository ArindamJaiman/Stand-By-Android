package com.standbypro

import com.standbypro.data.ChargeHistoryRepository
import com.standbypro.data.ChargeSession
import com.standbypro.data.ChargeSessionTracker
import com.standbypro.data.CoinPrice
import com.standbypro.data.CryptoRepository
import com.standbypro.data.DemoDataProvider
import com.standbypro.domain.Briefing
import com.standbypro.domain.CalendarEventItem
import com.standbypro.domain.FaceContext
import com.standbypro.domain.SleepMath
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WidgetRegistry
import com.standbypro.hardware.AmbientLightMonitor
import com.standbypro.hardware.WaveDetector
import com.standbypro.ui.faces.Kinetic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class SignatureFeatureTests {

    @Test
    fun kinetic_everyDigitUsesSixClocks() {
        ('0'..'9').forEach { d ->
            val hands = Kinetic.hands(d)
            assertEquals("digit $d", 6, hands.size)
            hands.forEach { (a, b) -> assertTrue(a in 0f..360f && b in 0f..360f) }
        }
        // Each digit is distinct, so the face never shows two numbers the same way.
        assertEquals(10, ('0'..'9').map { Kinetic.hands(it) }.toSet().size)
    }

    @Test
    fun kinetic_handsSweepClockwiseWithAFullTurn() {
        assertEquals(90f, Kinetic.sweep(90f, 180f, 0f), 0.001f)
        assertEquals(180f + 360f, Kinetic.sweep(90f, 180f, 1f), 0.001f)
        // 270° → 180° goes the long way round clockwise (270°) plus the full turn.
        assertEquals(270f + 270f + 360f, Kinetic.sweep(270f, 180f, 1f), 0.001f)
    }

    @Test
    fun sleepMath_plansToTheAlarm() {
        val now = 1_000_000_000L
        val min = 60_000L
        val plan = SleepMath.planToAlarm(now, now + (15 + 450) * min)
        assertNotNull(plan)
        assertEquals(450, plan!!.sleepMinutes)
        assertEquals(5f, plan.cycles, 0.001f)
        assertEquals("7 h 30 m", SleepMath.durationLabel(plan.sleepMinutes))
        assertNull(SleepMath.planToAlarm(now, now + 10 * min))
        assertNull(SleepMath.planToAlarm(now, now + 20 * 60 * min))
        assertNull(SleepMath.planToAlarm(now, null))
    }

    @Test
    fun sleepMath_suggestsEndsOfFullCycles() {
        val now = 0L
        val s = SleepMath.wakeSuggestions(now)
        assertEquals(listOf(4, 5, 6), s.map { it.first })
        assertEquals((15 + 5 * 90) * 60_000L, s[1].second)
    }

    @Test
    fun briefing_readsTimeWeatherEventAndAlarm() {
        val now = 5_000_000L
        val ctx = FaceContext(
            time = LocalDateTime.of(2026, 10, 21, 7, 15),
            weather = DemoDataProvider.demoWeather.copy(temperatureCelsius = 22, highCelsius = 25),
            events = listOf(
                CalendarEventItem("past", "Breakfast", "06:00 AM - 06:30 AM", startMillis = now - 60_000L),
                CalendarEventItem("next", "Sprint Sync", "10:30 AM - 11:00 AM", startMillis = now + 3_600_000L)
            ),
            nextAlarmTime = "6:30 AM"
        )
        val text = Briefing.compose(ctx, now)
        assertTrue(text, text.startsWith("It's quarter past seven."))
        assertTrue(text, text.contains("22 degrees"))
        assertTrue(text, text.contains("high of 25 degrees"))
        assertTrue(text, text.contains("Next up at 10:30 AM: Sprint Sync."))
        assertTrue(text, text.endsWith("Your alarm is set for 6:30 AM."))
        val fahrenheit = Briefing.compose(ctx.copy(useFahrenheit = true, nextAlarmTime = null, events = emptyList()), now)
        assertTrue(fahrenheit, fahrenheit.contains("71 degrees"))
        assertTrue(fahrenheit, fahrenheit.endsWith("No alarm is set."))
    }

    @Test
    fun waveDetector_acceptsQuickPassesOnly() {
        val w = WaveDetector()
        assertFalse(w.onSample(near = true, timeMillis = 0))
        assertTrue(w.onSample(near = false, timeMillis = 300))
        assertFalse(w.onSample(near = false, timeMillis = 400))
        w.onSample(near = true, timeMillis = 1000)
        assertFalse("pocket, not a wave", w.onSample(near = false, timeMillis = 5000))
        w.onSample(near = true, timeMillis = 6000)
        assertFalse("sensor glitch", w.onSample(near = false, timeMillis = 6010))
    }

    @Test
    fun chargeTracker_recordsMeaningfulCharges() {
        val t = ChargeSessionTracker()
        val min = 60_000L
        assertNull(t.update(true, 20, 30f, 0))
        assertNull(t.update(true, 60, 34.5f, 30 * min))
        assertNull(t.update(true, 80, 33f, 50 * min))
        val s = t.update(false, 80, null, 51 * min)
        assertNotNull(s)
        assertEquals(20, s!!.startPercent)
        assertEquals(80, s.endPercent)
        assertEquals(60, s.gained)
        assertEquals(51, s.minutes)
        assertEquals(34.5f, s.maxTempC!!, 0.001f)
        // A quick plug-unplug isn't a charge.
        t.update(true, 50, null, 100 * min)
        assertNull(t.update(false, 50, null, 100 * min + 30_000L))
        // Unplugged while already unplugged: nothing to record.
        assertNull(t.update(false, 50, null, 200 * min))
    }

    @Test
    fun chargeHistory_roundTripsThroughJson() {
        val sessions = listOf(ChargeSession(1, 2, 10, 90, 32.5f), ChargeSession(3, 4, 40, 100, null))
        assertEquals(sessions, ChargeHistoryRepository.decode(ChargeHistoryRepository.encode(sessions)))
        assertEquals(7, ChargeHistoryRepository.sample(10_000_000_000L).size)
    }

    @Test
    fun crypto_parsesAndFormats() {
        val json = """{"bitcoin":{"usd":67412.3,"usd_24h_change":1.8412},"ethereum":{"usd":3286.5,"usd_24h_change":-0.62},"solana":{"usd":0.5123,"usd_24h_change":0}}"""
        val prices = CryptoRepository.parse(json)
        assertEquals(listOf("BTC", "ETH", "SOL"), prices.map { it.symbol })
        assertEquals("$67,412", prices[0].priceLabel)
        assertEquals("▲ 1.84%", prices[0].changeLabel)
        assertEquals("$3,286.50", prices[1].priceLabel)
        assertEquals("▼ 0.62%", prices[1].changeLabel)
        assertEquals("$0.5123", prices[2].priceLabel)
        assertTrue(CryptoRepository.parse("""{"bitcoin":{"usd":1}}""").all { it.change24h == 0.0 })
        assertEquals("$142.35", CoinPrice("x", "X", "X", 142.35, 0.0).priceLabel)
    }

    @Test
    fun adaptiveBrightness_followsRoomLight() {
        val dark = AmbientLightMonitor.adaptiveBrightness(0f, 0.5f)
        val lamp = AmbientLightMonitor.adaptiveBrightness(50f, 0.5f)
        val daylight = AmbientLightMonitor.adaptiveBrightness(5000f, 0.5f)
        assertTrue(dark < lamp && lamp < daylight)
        assertEquals(1f, daylight, 0f)
        assertTrue(dark >= 0.02f)
        assertTrue(AmbientLightMonitor.adaptiveBrightness(50f, 0.1f) < AmbientLightMonitor.adaptiveBrightness(50f, 0.9f))
        assertEquals(0f, (AmbientLightMonitor.adaptiveBrightness(123f, 0.37f) * 50f) % 1f, 0.001f)
    }

    @Test
    fun newFacesAndWidgets_areRegistered() {
        listOf("digital_kinetic", "digital_liquid_glass", "productivity_year_dots", "productivity_sleep_cycles", "ambient_fireplace", "ambient_lighthouse", "ambient_snowfall", "artistic_pixel_day")
            .forEach { assertTrue(it, WatchFaceRegistry.contains(it)) }
        assertTrue(WidgetRegistry.contains("widget_crypto"))
        assertTrue(WidgetRegistry.contains("widget_charge_history"))
        assertEquals(129, WatchFaceRegistry.getAll().size)
        assertEquals(28, WidgetRegistry.getAll().size)
    }
}
