package com.standbypro.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.standbypro.ui.faces.*
import java.time.LocalDateTime

/**
 * Centralized registry of all watch faces in StandBy Pro.
 * Provides metadata lookup, category filtering, search, and dynamic composable dispatch.
 */
object WatchFaceRegistry {

    private val faces: List<WatchFaceDefinition> = listOf(
        // DIGITAL (12)
        WatchFaceDefinition("digital_minimal", "Digital Minimal", WatchFaceCategory.DIGITAL, "Clean, high-contrast typography focusing on pure readability", isFavoriteByDefault = true),
        WatchFaceDefinition("digital_bold", "Digital Bold", WatchFaceCategory.DIGITAL, "Stacked two-tone typographic hours and minutes"),
        WatchFaceDefinition("digital_oled", "Digital OLED", WatchFaceCategory.DIGITAL, "OLED black canvas with seconds pill and minimal bezel", isFavoriteByDefault = true),
        WatchFaceDefinition("digital_segmented", "Segmented LED", WatchFaceCategory.DIGITAL, "Retro 7-segment digital display inspired by vintage bedside clocks"),
        WatchFaceDefinition("digital_matrix", "Matrix Terminal", WatchFaceCategory.DIGITAL, "Phosphor green monospace terminal style with kernel ticks"),
        WatchFaceDefinition("digital_neon", "Neon Glow", WatchFaceCategory.DIGITAL, "Vibrant glowing neon hours and minutes with subtle bloom"),
        WatchFaceDefinition("digital_word_clock", "Word Clock", WatchFaceCategory.DIGITAL, "Natural language typographic representation of time"),
        WatchFaceDefinition("digital_binary", "Binary Matrix", WatchFaceCategory.DIGITAL, "True binary column representation of hours, minutes, and seconds"),
        WatchFaceDefinition("digital_outline", "Outline Hollow", WatchFaceCategory.DIGITAL, "Architectural outlined numbers with delicate strokes"),
        WatchFaceDefinition("digital_typewriter", "Typewriter", WatchFaceCategory.DIGITAL, "Mechanical serif typewriter aesthetic with story excerpt"),
        WatchFaceDefinition("digital_poster", "Swiss Poster", WatchFaceCategory.DIGITAL, "Bold Swiss style framing with striking horizontal divider"),
        WatchFaceDefinition("digital_glass", "Frosted Glass", WatchFaceCategory.DIGITAL, "Modern translucent glassmorphic panel with gradient border"),

        // ANALOG (14)
        WatchFaceDefinition("analog_classic", "Analog Classic", WatchFaceCategory.ANALOG, "Traditional railroad track bezel with high-contrast hands"),
        WatchFaceDefinition("analog_modern", "Analog Modern", WatchFaceCategory.ANALOG, "Minimalist 4-quadrant Bauhaus dial with floating hands"),
        WatchFaceDefinition("analog_luxury", "Analog Luxury", WatchFaceCategory.ANALOG, "Sunburst brushed dial with faceted gold indices", isFavoriteByDefault = true),
        WatchFaceDefinition("analog_chronograph", "Chronograph", WatchFaceCategory.ANALOG, "Tri-compax chronograph layout with 3 timing subdials"),
        WatchFaceDefinition("analog_pilot", "Pilot Flieger", WatchFaceCategory.ANALOG, "Aviation-inspired high-legibility dial with 12 o'clock orientation triangle"),
        WatchFaceDefinition("analog_diver", "Diver Submersible", WatchFaceCategory.ANALOG, "Heavy rotating dive bezel with oversized luminescent hour markers"),
        WatchFaceDefinition("analog_skeleton", "Mechanical Skeleton", WatchFaceCategory.ANALOG, "Exposed balance wheel and escapement complication dial"),
        WatchFaceDefinition("analog_roman", "Classic Roman", WatchFaceCategory.ANALOG, "Traditional Roman numeral dial with delicate proportions"),
        WatchFaceDefinition("analog_pocket", "Vintage Pocket Watch", WatchFaceCategory.ANALOG, "Turn-of-the-century pocket timepiece with 12 o'clock crown loop"),
        WatchFaceDefinition("analog_art_deco", "Art Deco", WatchFaceCategory.ANALOG, "Geometric 1920s octagonal geometry and gold filigree accents"),
        WatchFaceDefinition("analog_nordic", "Nordic Matte", WatchFaceCategory.ANALOG, "Understated Scandinavian clean matte aesthetic"),
        WatchFaceDefinition("analog_compass", "Field Compass", WatchFaceCategory.ANALOG, "Outdoor adventure dial with cardinal direction navigation indices"),
        WatchFaceDefinition("analog_sundial", "Sundial Cast", WatchFaceCategory.ANALOG, "Stone dial with radial shadow projection lines"),
        WatchFaceDefinition("analog_moonphase", "Astronomical Moonphase", WatchFaceCategory.ANALOG, "Celestial lunar aperture tracking moon phases at 6 o'clock"),

        // GMT / HYBRID (8)
        WatchFaceDefinition("gmt_explorer", "Rolex GMT Explorer", WatchFaceCategory.GMT, "24-hour red GMT hand with dual-time mechanical 24h bezel", isFavoriteByDefault = true),
        WatchFaceDefinition("gmt_calendar", "GMT & Calendar", WatchFaceCategory.GMT, "Dual-time mechanical sweeping watch paired with calendar widget", isFavoriteByDefault = true),
        WatchFaceDefinition("hybrid_analog_digital", "Analog + Digital", WatchFaceCategory.HYBRID, "Analog dial with top digital LED readout window"),
        WatchFaceDefinition("hybrid_analog_battery", "Analog + Battery Gauge", WatchFaceCategory.HYBRID, "Analog watch face with integrated 9 o'clock battery gauge"),
        WatchFaceDefinition("hybrid_analog_weather", "Analog + Weather Dial", WatchFaceCategory.HYBRID, "Analog dial with dedicated 3 o'clock temperature subdial"),
        WatchFaceDefinition("hybrid_analog_calendar", "Analog + Date Aperture", WatchFaceCategory.HYBRID, "Classic analog face with cyclops date window at 3 o'clock"),
        WatchFaceDefinition("hybrid_gmt_weather", "GMT + Weather", WatchFaceCategory.GMT, "24h dual-time display combined with weather condition indices"),
        WatchFaceDefinition("hybrid_dual_time", "Dual Time Zone", WatchFaceCategory.HYBRID, "Primary home dial with independent foreign timezone subdial at 6 o'clock"),

        // FLIP (4)
        WatchFaceDefinition("flip_classic", "Flip Classic", WatchFaceCategory.FLIP, "Authentic split-flap mechanical bedside clock"),
        WatchFaceDefinition("flip_dark", "Flip Dark Mode", WatchFaceCategory.FLIP, "Monochrome dark split-flap mechanical tiles"),
        WatchFaceDefinition("flip_amber", "Flip Amber Night", WatchFaceCategory.FLIP, "Warm amber glowing split-flap numbers for bedside comfort", isFavoriteByDefault = true),
        WatchFaceDefinition("flip_graphite", "Flip Graphite", WatchFaceCategory.FLIP, "Industrial brushed graphite split-flap modules"),

        // WORLD TIME (2)
        WatchFaceDefinition("world_clock_face", "World Clock Matrix", WatchFaceCategory.WORLD_CLOCK, "Simultaneous live display of multiple major global financial cities"),
        WatchFaceDefinition("world_multi_timezone", "Multi-Timezone Dial", WatchFaceCategory.WORLD_CLOCK, "Configurable international city time tiles with DST calculations"),

        // WEATHER (4)
        WatchFaceDefinition("weather_digital", "Weather Digital", WatchFaceCategory.WEATHER, "Large temperature badge with live condition and ambient time"),
        WatchFaceDefinition("weather_analog", "Weather Analog", WatchFaceCategory.WEATHER, "Analog mechanical dial with weather forecast gauges"),
        WatchFaceDefinition("weather_hourly", "Hourly Forecast", WatchFaceCategory.WEATHER, "5-hour forecast progression matrix with weather conditions"),
        WatchFaceDefinition("weather_forecast", "Forecast Dashboard", WatchFaceCategory.WEATHER, "Comprehensive multi-day weather forecast with ambient clock"),

        // PRODUCTIVITY (4)
        WatchFaceDefinition("productivity_pomodoro", "Pomodoro Focus Clock", WatchFaceCategory.PRODUCTIVITY, "25-minute deep focus timer with session cycle tracking", isFavoriteByDefault = true),
        WatchFaceDefinition("productivity_timer", "Countdown Timer", WatchFaceCategory.PRODUCTIVITY, "Large hero timer countdown with progress indication"),
        WatchFaceDefinition("productivity_stopwatch", "Stopwatch Split Face", WatchFaceCategory.PRODUCTIVITY, "Precision stopwatch with millisecond readout and split times"),
        WatchFaceDefinition("productivity_focus", "Focus Session Clock", WatchFaceCategory.PRODUCTIVITY, "Bedside focus and productivity ambient companion"),

        // AMBIENT / ARTISTIC (10)
        WatchFaceDefinition("ambient_aurora", "Aurora Borealis", WatchFaceCategory.AMBIENT, "Organic shifting green and teal atmospheric aura"),
        WatchFaceDefinition("ambient_galaxy", "Deep Cosmos", WatchFaceCategory.AMBIENT, "Deep purple and indigo interstellar gradient"),
        WatchFaceDefinition("ambient_ocean", "Abyssal Ocean", WatchFaceCategory.AMBIENT, "Calming deep oceanic blue gradient with floating time"),
        WatchFaceDefinition("ambient_forest", "Redwood Canopy", WatchFaceCategory.AMBIENT, "Deep botanical pine green gradient"),
        WatchFaceDefinition("ambient_mountain", "Alpine Twilight", WatchFaceCategory.AMBIENT, "Muted twilight mountain gradient"),
        WatchFaceDefinition("ambient_geometric", "Geometric Bauhaus", WatchFaceCategory.ARTISTIC, "Warm architectural geometric palette"),
        WatchFaceDefinition("ambient_neon", "Cyber Neon", WatchFaceCategory.ARTISTIC, "Electric magenta and violet night gradient"),
        WatchFaceDefinition("ambient_zen", "Zen Garden", WatchFaceCategory.AMBIENT, "Monochrome textured stone grey ambient display"),
        WatchFaceDefinition("ambient_japanese", "Kyoto Dusk", WatchFaceCategory.ARTISTIC, "Deep crimson and burgundy Japanese minimalist palette"),
        WatchFaceDefinition("ambient_minimal_gradient", "Minimal Void", WatchFaceCategory.AMBIENT, "Ultra-subtle pure black to charcoal gradient"),

        // PHOTO (4)
        WatchFaceDefinition("photo_frame", "Photo Frame Classic", WatchFaceCategory.PHOTO, "Bedside digital photo frame with corner clock"),
        WatchFaceDefinition("photo_clock", "Photo + Clock", WatchFaceCategory.PHOTO, "Photo background with centered ambient clock"),
        WatchFaceDefinition("photo_weather", "Photo + Weather", WatchFaceCategory.PHOTO, "Photo frame with weather condition badge"),
        WatchFaceDefinition("photo_calendar", "Photo + Calendar", WatchFaceCategory.PHOTO, "Photo frame with upcoming agenda calendar overlay")
    )

    fun getAll(): List<WatchFaceDefinition> = faces

    fun getById(id: String): WatchFaceDefinition {
        return faces.firstOrNull { it.id.equals(id, ignoreCase = true) }
            ?: faces.first { it.id == "gmt_explorer" }
    }

    fun getByCategory(category: WatchFaceCategory): List<WatchFaceDefinition> {
        return faces.filter { it.category == category }
    }

    fun search(query: String): List<WatchFaceDefinition> {
        if (query.isBlank()) return faces
        val clean = query.trim().lowercase()
        return faces.filter {
            it.name.lowercase().contains(clean) ||
            it.description.lowercase().contains(clean) ||
            it.category.displayName.lowercase().contains(clean)
        }
    }

    fun getFavorites(favIds: Set<String>): List<WatchFaceDefinition> {
        return faces.filter { favIds.contains(it.id) || it.isFavoriteByDefault }
    }

    @Composable
    fun Render(
        id: String,
        time: LocalDateTime,
        accentColor: Color,
        use24Hour: Boolean = false,
        modifier: Modifier = Modifier
    ) {
        when (id) {
            // DIGITAL
            "digital_minimal" -> DigitalMinimalFace(time, accentColor, use24Hour, modifier)
            "digital_bold" -> DigitalBoldFace(time, accentColor, use24Hour, modifier)
            "digital_oled" -> DigitalOledFace(time, accentColor, use24Hour, modifier)
            "digital_segmented" -> DigitalSegmentedFace(time, accentColor, use24Hour, modifier)
            "digital_matrix" -> DigitalMatrixFace(time, accentColor, use24Hour, modifier)
            "digital_neon" -> DigitalNeonFace(time, accentColor, use24Hour, modifier)
            "digital_word_clock" -> DigitalWordClockFace(time, accentColor, modifier)
            "digital_binary" -> DigitalBinaryFace(time, accentColor, modifier)
            "digital_outline" -> DigitalOutlineFace(time, accentColor, use24Hour, modifier)
            "digital_typewriter" -> DigitalTypewriterFace(time, accentColor, modifier)
            "digital_poster" -> DigitalPosterFace(time, accentColor, use24Hour, modifier)
            "digital_glass" -> DigitalGlassFace(time, accentColor, use24Hour, modifier)

            // ANALOG
            "analog_classic" -> AnalogClassicFace(time, accentColor, modifier)
            "analog_modern" -> AnalogModernFace(time, accentColor, modifier)
            "analog_luxury" -> AnalogLuxuryFace(time, accentColor, modifier)
            "analog_chronograph" -> ChronographFace(time, accentColor, modifier)
            "analog_pilot" -> PilotFace(time, accentColor, modifier)
            "analog_diver" -> DiverFace(time, accentColor, modifier)
            "analog_skeleton" -> SkeletonFace(time, accentColor, modifier)
            "analog_roman" -> RomanFace(time, accentColor, modifier)
            "analog_pocket" -> PocketWatchFace(time, accentColor, modifier)
            "analog_art_deco" -> ArtDecoFace(time, accentColor, modifier)
            "analog_nordic" -> NordicFace(time, accentColor, modifier)
            "analog_compass" -> CompassFace(time, accentColor, modifier)
            "analog_sundial" -> SundialFace(time, accentColor, modifier)
            "analog_moonphase" -> MoonphaseFace(time, accentColor, modifier)

            // GMT / HYBRID
            "gmt_explorer" -> GmtExplorerFace(time, accentColor, modifier)
            "gmt_calendar" -> GmtCalendarFace(time, accentColor, modifier)
            "hybrid_analog_digital" -> HybridAnalogDigitalFace(time, accentColor, modifier)
            "hybrid_analog_battery" -> HybridAnalogBatteryFace(time, accentColor, modifier = modifier)
            "hybrid_analog_weather" -> HybridAnalogWeatherFace(time, accentColor, modifier = modifier)
            "hybrid_analog_calendar" -> HybridAnalogCalendarFace(time, accentColor, modifier)
            "hybrid_gmt_weather" -> HybridGmtWeatherFace(time, accentColor, modifier)
            "hybrid_dual_time" -> HybridDualTimeFace(time, accentColor, modifier = modifier)

            // FLIP
            "flip_classic" -> FlipClassicFace(time, accentColor, use24Hour, modifier)
            "flip_dark" -> FlipDarkFace(time, accentColor, use24Hour, modifier)
            "flip_amber" -> FlipAmberFace(time, accentColor, use24Hour, modifier)
            "flip_graphite" -> FlipGraphiteFace(time, accentColor, use24Hour, modifier)

            // WORLD CLOCK
            "world_clock_face" -> WorldClockFace(time, accentColor, modifier)
            "world_multi_timezone" -> MultiTimezoneFace(time, accentColor, modifier)

            // WEATHER
            "weather_digital" -> WeatherDigitalFace(time, accentColor, modifier)
            "weather_analog" -> WeatherAnalogFace(time, accentColor, modifier)
            "weather_hourly" -> WeatherHourlyFace(time, accentColor, modifier)
            "weather_forecast" -> WeatherForecastFace(time, accentColor, modifier)

            // PRODUCTIVITY
            "productivity_pomodoro" -> PomodoroClockFace(time, accentColor, modifier)
            "productivity_timer" -> TimerFace(time, accentColor, modifier)
            "productivity_stopwatch" -> StopwatchFace(time, accentColor, modifier)
            "productivity_focus" -> FocusClockFace(time, accentColor, modifier)

            // AMBIENT / ARTISTIC
            "ambient_aurora" -> AuroraFace(time, accentColor, modifier)
            "ambient_galaxy" -> GalaxyFace(time, accentColor, modifier)
            "ambient_ocean" -> OceanFace(time, accentColor, modifier)
            "ambient_forest" -> ForestFace(time, accentColor, modifier)
            "ambient_mountain" -> MountainFace(time, accentColor, modifier)
            "ambient_geometric" -> GeometricFace(time, accentColor, modifier)
            "ambient_neon" -> NeonAmbientFace(time, accentColor, modifier)
            "ambient_zen" -> ZenFace(time, accentColor, modifier)
            "ambient_japanese" -> JapaneseFace(time, accentColor, modifier)
            "ambient_minimal_gradient" -> MinimalGradientFace(time, accentColor, modifier)

            // PHOTO
            "photo_frame" -> PhotoFrameFace(time, accentColor, modifier)
            "photo_clock" -> PhotoClockFace(time, accentColor, modifier)
            "photo_weather" -> PhotoWeatherFace(time, accentColor, modifier)
            "photo_calendar" -> PhotoCalendarFace(time, accentColor, modifier)

            // Default Fallback
            else -> GmtExplorerFace(time, accentColor, modifier)
        }
    }
}
