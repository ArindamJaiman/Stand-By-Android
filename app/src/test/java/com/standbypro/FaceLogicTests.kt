package com.standbypro

import com.standbypro.astro.Astronomy
import com.standbypro.data.WeatherRepository
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WeatherCondition
import com.standbypro.domain.WidgetRegistry
import com.standbypro.settings.StandBySettings
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.design.HandAngles
import com.standbypro.ui.faces.FaceMath
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

class FaceLogicTests {

    @Test
    fun wordClock_readsNaturally() {
        assertEquals("IT IS A QUARTER PAST SEVEN", FaceMath.wordClockSentence(7, 15))
        assertEquals("IT IS TWENTY TO ELEVEN", FaceMath.wordClockSentence(22, 40))
        assertEquals("IT IS TWELVE OCLOCK", FaceMath.wordClockSentence(0, 2))
        assertEquals("IT IS HALF PAST THREE", FaceMath.wordClockSentence(15, 33))
        assertEquals("IT IS FIVE TO TWELVE", FaceMath.wordClockSentence(11, 57))
    }

    @Test
    fun wordClock_gridIsRectangular() {
        val width = FaceMath.wordGrid.first().length
        assertTrue(FaceMath.wordGrid.all { it.length == width })
    }

    @Test
    fun fibonacci_encodesEveryFiveMinutes() {
        for (h in 0 until 24) for (m in 0 until 60 step 5) {
            val colors = FaceMath.fibonacciColors(h, m, variant = h + m)
            val (hours, minutes) = FaceMath.fibonacciDecode(colors)
            val expectedHour = (h % 12).let { if (it == 0) 12 else it }
            assertEquals("hour at $h:$m", expectedHour, hours)
            assertEquals("minute at $h:$m", m, minutes)
        }
    }

    @Test
    fun roman_usesWatchmakersFour() {
        assertEquals("IIII", FaceMath.roman(4))
        assertEquals("IV", FaceMath.roman(4, watchmakerFour = false))
        assertEquals("IX", FaceMath.roman(9))
        assertEquals("XII", FaceMath.roman(12))
    }

    @Test
    fun spokenTime_handlesQuartersAndTo() {
        assertEquals("quarter past seven", FaceMath.spokenTime(7, 15))
        assertEquals("twenty-five to nine", FaceMath.spokenTime(8, 35))
        assertEquals("twelve o'clock", FaceMath.spokenTime(12, 0))
    }

    @Test
    fun sevenSegment_coversDigits() {
        assertEquals(0b1111111, FaceMath.sevenSegment('8'))
        assertEquals(0b0000110, FaceMath.sevenSegment('1'))
        assertEquals(0, FaceMath.sevenSegment('x'))
    }

    @Test
    fun dotFont_glyphsAre5x7() {
        "0123456789: ".forEach { c ->
            val glyph = FaceMath.dotGlyph(c)
            assertEquals(7, glyph.size)
            assertTrue(glyph.all { it.length == 5 })
        }
    }

    @Test
    fun handAngles_matchClockPositions() {
        val three = HandAngles.from(3 * 3600f)
        assertEquals(90f, three.hour, 0.01f)
        assertEquals(0f, three.minute, 0.01f)
        val halfPastSix = HandAngles.from(18 * 3600f + 30 * 60f)
        assertEquals(195f, halfPastSix.hour, 0.01f)
        assertEquals(180f, halfPastSix.minute, 0.01f)
        assertEquals(277.5f, halfPastSix.hour24, 0.01f)
    }

    @Test
    fun moonPhase_knownFullAndNewMoons() {
        val full = ZonedDateTime.of(2024, 1, 25, 17, 54, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli()
        val new = ZonedDateTime.of(2024, 1, 11, 11, 57, 0, 0, ZoneId.of("UTC")).toInstant().toEpochMilli()
        assertTrue(Astronomy.moonPhase(full).illumination > 0.97)
        assertEquals("Full Moon", Astronomy.moonPhase(full).name)
        assertTrue(Astronomy.moonPhase(new).illumination < 0.03)
    }

    @Test
    fun sunTimes_newDelhiSolstice() {
        val zone = ZoneId.of("Asia/Kolkata")
        val times = Astronomy.sunTimes(LocalDate.of(2024, 6, 21), 28.6139, 77.2090, zone)
        val rise = times.sunrise!!
        val set = times.sunset!!
        assertTrue("sunrise ${rise.toLocalTime()}", abs(rise.hour * 60 + rise.minute - (5 * 60 + 24)) <= 10)
        assertTrue("sunset ${set.toLocalTime()}", abs(set.hour * 60 + set.minute - (19 * 60 + 22)) <= 10)
    }

    @Test
    fun sunTimes_polarNight() {
        val times = Astronomy.sunTimes(LocalDate.of(2024, 12, 21), 78.22, 15.65, ZoneId.of("Arctic/Longyearbyen"))
        assertTrue(times.polarNight)
    }

    @Test
    fun nightScheduleWindow_wrapsMidnight() {
        assertTrue(StandByViewModel.inWindow(23 * 60, 22 * 60, 7 * 60))
        assertTrue(StandByViewModel.inWindow(3 * 60, 22 * 60, 7 * 60))
        assertFalse(StandByViewModel.inWindow(12 * 60, 22 * 60, 7 * 60))
        assertTrue(StandByViewModel.inWindow(14 * 60, 13 * 60, 15 * 60))
    }

    @Test
    fun wmoCodes_mapToConditions() {
        assertEquals(WeatherCondition.CLEAR_DAY, WeatherCondition.fromWmo(0, true))
        assertEquals(WeatherCondition.CLEAR_NIGHT, WeatherCondition.fromWmo(1, false))
        assertEquals(WeatherCondition.THUNDERSTORM, WeatherCondition.fromWmo(95, true))
        assertEquals(WeatherCondition.SNOW, WeatherCondition.fromWmo(73, true))
    }

    @Test
    fun weatherParser_readsOpenMeteoPayload() {
        val json = """
            {"current":{"temperature_2m":21.6,"apparent_temperature":22.4,"relative_humidity_2m":58,"weather_code":2,"wind_speed_10m":13.2,"is_day":1},
             "daily":{"time":["2026-10-17","2026-10-18"],"weather_code":[2,61],"temperature_2m_max":[25.1,19.0],"temperature_2m_min":[15.2,12.8],
                      "sunrise":["2026-10-17T06:31","2026-10-18T06:32"],"sunset":["2026-10-17T17:48","2026-10-18T17:47"],
                      "uv_index_max":[4.5,2.0],"precipitation_probability_max":[10,80]},
             "hourly":{"time":["2099-01-01T00:00","2099-01-01T01:00"],"temperature_2m":[20.0,19.5],"weather_code":[1,3],"precipitation_probability":[0,40],"is_day":[0,0]}}
        """.trimIndent()
        val w = WeatherRepository.parse(json, "Delhi")
        assertEquals(22, w.temperatureCelsius)
        assertEquals(WeatherCondition.PARTLY_CLOUDY_DAY, w.condition)
        assertEquals(25, w.highCelsius)
        assertEquals(15, w.lowCelsius)
        assertEquals(2, w.dailyForecast.size)
        assertEquals(WeatherCondition.RAIN, w.dailyForecast[1].condition)
        assertEquals(2, w.hourlyForecast.size)
        assertEquals("Now", w.hourlyForecast.first().timeLabel)
        assertEquals(10, w.precipitationChance)
        assertTrue(w.isLive)
    }

    @Test
    fun defaults_referenceRealFacesAndWidgets() {
        val s = StandBySettings()
        assertTrue(WatchFaceRegistry.contains(s.activeWatchFaceId))
        s.favoriteFaceIds.forEach { assertTrue("favourite $it", WatchFaceRegistry.contains(it)) }
        (s.leftStack + s.rightStack + s.assignedWidgets.map { it.widgetId }).forEach {
            assertTrue("widget $it", WidgetRegistry.contains(it))
        }
        assertNotNull(WatchFaceRegistry.getById("does-not-exist"))
    }
}
