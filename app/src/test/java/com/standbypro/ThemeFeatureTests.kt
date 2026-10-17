package com.standbypro

import com.standbypro.data.DemoDataProvider
import com.standbypro.domain.HourlyForecast
import com.standbypro.domain.RainSoon
import com.standbypro.domain.WatchFaceRegistry
import com.standbypro.domain.WeatherCondition
import com.standbypro.settings.NightStyle
import com.standbypro.settings.SettingsBackup
import com.standbypro.settings.StandByColorTheme
import com.standbypro.ui.StandByViewModel
import com.standbypro.ui.design.DynamicAccent
import com.standbypro.ui.design.ThemePresets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ThemeFeatureTests {

    @Test
    fun backup_roundTripsEveryType() {
        val values = mapOf<String, Any>(
            "is_enabled" to true,
            "auto_dim_timeout_seconds" to 120,
            "countdown_epoch_day" to 20_500L,
            "brightness_level" to 0.35f,
            "precise" to 1.25,
            "color_theme_id" to "gold",
            "world_cities" to "London\u001FTokyo",
            "favorite_faces_v2" to setOf("analog_marble", "music_vinyl")
        )
        val decoded = SettingsBackup.decode(SettingsBackup.encode(values))
        assertEquals(values, decoded)
    }

    @Test
    fun backup_neverCarriesPhotoGrants() {
        val json = SettingsBackup.encode(mapOf("photo_uris" to "content://media/1", "use_24_hour" to true))
        assertFalse(json.contains("content://"))
        assertEquals(mapOf<String, Any>("use_24_hour" to true), SettingsBackup.decode(json))
    }

    @Test(expected = IllegalArgumentException::class)
    fun backup_rejectsForeignJson() {
        SettingsBackup.decode("""{"hello":"world"}""")
    }

    @Test(expected = IllegalArgumentException::class)
    fun backup_rejectsGarbage() {
        SettingsBackup.decode("not json at all")
    }

    @Test
    fun rainSoon_onlyWarnsBeforeRainStarts() {
        val base = DemoDataProvider.demoWeather.copy(condition = WeatherCondition.PARTLY_CLOUDY_DAY)
        val dry = base.copy(hourlyForecast = listOf(
            HourlyForecast("Now", 20, WeatherCondition.CLOUDY, 10),
            HourlyForecast("3pm", 20, WeatherCondition.CLOUDY, 20),
            HourlyForecast("4pm", 19, WeatherCondition.CLOUDY, 30)
        ))
        assertNull(RainSoon.outlook(dry))
        val wet = dry.copy(hourlyForecast = dry.hourlyForecast.toMutableList().also { it[2] = HourlyForecast("4pm", 18, WeatherCondition.RAIN, 70) })
        assertEquals("70% at 4pm", RainSoon.outlook(wet))
        assertNull(RainSoon.outlook(wet.copy(condition = WeatherCondition.RAIN)))
        assertNull(RainSoon.outlook(null))
    }

    @Test
    fun themePresets_referenceRealFacesAndColours() {
        val presets = ThemePresets.all
        assertEquals(presets.size, presets.map { it.id }.toSet().size)
        presets.forEach { p ->
            assertTrue("${p.id} face ${p.faceId}", WatchFaceRegistry.contains(p.faceId))
            assertEquals("${p.id} accent", p.accentId, StandByColorTheme.fromId(p.accentId).id)
            assertEquals("${p.id} night", p.nightStyleId, NightStyle.fromId(p.nightStyleId).id)
        }
    }

    @Test
    fun wallpaperAccent_followsDynamicColour() {
        assertEquals(DynamicAccent.color, StandByColorTheme.WALLPAPER.color)
        assertEquals(StandByColorTheme.WALLPAPER, StandByColorTheme.fromId("wallpaper"))
    }

    @Test
    fun sunsetTrigger_tracksTheRealSun() {
        // New Delhi on the June solstice: noon is daylight, midnight is dark.
        val noon = ZonedDateTime.of(2026, 6, 21, 6, 30, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()
        val midnight = ZonedDateTime.of(2026, 6, 21, 18, 30, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()
        assertFalse(StandByViewModel.isSunDown(noon, 28.61, 77.21))
        assertTrue(StandByViewModel.isSunDown(midnight, 28.61, 77.21))
    }

    @Test
    fun newPremiumFaces_areRegistered() {
        listOf("music_vinyl", "digital_big_date", "digital_code_rain", "analog_carbon", "analog_marble", "weather_rain_glass", "ambient_silk", "ambient_sakura")
            .forEach { assertTrue(it, WatchFaceRegistry.contains(it)) }
        assertTrue(WatchFaceRegistry.getAll().size >= 120)
    }
}
