package com.standbypro.domain

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.standbypro.ui.faces.*

/**
 * Centralized registry of every StandBy Pro watch face: metadata for the Studio and
 * composable dispatch for the display. Faces are pure functions of [FaceContext].
 */
object WatchFaceRegistry {

    private class Entry(val definition: WatchFaceDefinition, val content: @Composable (FaceContext, Modifier) -> Unit)

    private fun face(
        id: String,
        name: String,
        category: WatchFaceCategory,
        description: String,
        isNew: Boolean = false,
        interactive: Boolean = false,
        live: Boolean = false,
        content: @Composable (FaceContext, Modifier) -> Unit
    ) = Entry(
        WatchFaceDefinition(
            id = id,
            name = name,
            category = category,
            description = description,
            isNew = isNew,
            isInteractive = interactive,
            usesLiveData = live,
            isFavoriteByDefault = id in com.standbypro.settings.StandBySettings.DEFAULT_FAVORITES
        ),
        content
    )

    private val entries: List<Entry> = listOf(
        // ---------------- ANALOG ----------------
        face("analog_standby", "StandBy Analog", WatchFaceCategory.ANALOG, "The iconic bedside analog: bold numerals, pill hands and a glowing sweep", true) { c, m -> AnalogStandByFace(c, m) },
        face("analog_classic", "Classic", WatchFaceCategory.ANALOG, "Serif numerals over a railway minute track with cathedral hands") { c, m -> AnalogClassicFace(c, m) },
        face("analog_modern", "Bauhaus", WatchFaceCategory.ANALOG, "Quartered dial with long cardinal markers and slender rail hands") { c, m -> AnalogModernFace(c, m) },
        face("analog_luxury", "Luxury Gold", WatchFaceCategory.ANALOG, "Blue sunburst, applied gold indices, dauphine hands and date") { c, m -> AnalogLuxuryFace(c, m) },
        face("analog_chronograph", "Chronograph", WatchFaceCategory.ANALOG, "Tachymeter bezel with working 24h, 30-minute and 12-hour registers") { c, m -> ChronographFace(c, m) },
        face("analog_pilot", "Pilot Flieger", WatchFaceCategory.ANALOG, "Type-A aviation dial with the 12 o'clock triangle and lumed swords") { c, m -> PilotFace(c, m) },
        face("analog_diver", "Diver 300M", WatchFaceCategory.ANALOG, "Ceramic dive bezel, chunky lume plots and a lollipop seconds hand") { c, m -> DiverFace(c, m) },
        face("analog_skeleton", "Skeleton", WatchFaceCategory.ANALOG, "Open-worked movement with turning gears and an oscillating balance") { c, m -> SkeletonFace(c, m) },
        face("analog_tourbillon", "Tourbillon", WatchFaceCategory.ANALOG, "Rose-gold dial with a one-minute rotating tourbillon cage", true) { c, m -> TourbillonFace(c, m) },
        face("analog_roman", "Roman", WatchFaceCategory.ANALOG, "Rotated Roman chapter ring with polished leaf hands") { c, m -> RomanFace(c, m) },
        face("analog_pocket", "Pocket Watch", WatchFaceCategory.ANALOG, "Enamel dial, Breguet hands, small seconds and a gold crown") { c, m -> PocketWatchFace(c, m) },
        face("analog_regulator", "Regulator", WatchFaceCategory.ANALOG, "Central minutes with separate hour and seconds sub-dials", true) { c, m -> RegulatorFace(c, m) },
        face("analog_railway", "Railway", WatchFaceCategory.ANALOG, "Swiss station clock with the famous red disc seconds hand", true) { c, m -> RailwayFace(c, m) },
        face("analog_art_deco", "Art Deco", WatchFaceCategory.ANALOG, "Octagonal gold frame over an emerald sunburst") { c, m -> ArtDecoFace(c, m) },
        face("analog_california", "California", WatchFaceCategory.ANALOG, "Half Roman, half Arabic vintage military dial", true) { c, m -> CaliforniaFace(c, m) },
        face("analog_field", "Field", WatchFaceCategory.ANALOG, "Military 12/24-hour dial with lumed pencil hands", true) { c, m -> FieldFace(c, m) },
        face("analog_nordic", "Nordic", WatchFaceCategory.ANALOG, "Matte Scandinavian minimalism with a single accent date") { c, m -> NordicFace(c, m) },
        face("analog_meridian", "Meridian", WatchFaceCategory.ANALOG, "Black dial in a bright minute ring — instantly readable", true) { c, m -> MeridianFace(c, m) },
        face("analog_typograph", "Typograph", WatchFaceCategory.ANALOG, "Huge 12-3-6-9 numerals with whisper-thin hands", true) { c, m -> TypographFace(c, m) },
        face("analog_contour", "Contour", WatchFaceCategory.ANALOG, "Numerals swell as the hour hand approaches them", true) { c, m -> ContourFace(c, m) },
        face("analog_numerals_duo", "Numerals", WatchFaceCategory.ANALOG, "A giant hour numeral with a single sweeping minute hand", true) { c, m -> NumeralsDuoFace(c, m) },
        face("analog_carbon", "Carbon", WatchFaceCategory.ANALOG, "Sport chronometer on woven carbon with a battery register", true, live = true) { c, m -> CarbonFace(c, m) },
        face("analog_marble", "Marble", WatchFaceCategory.ANALOG, "Carrara marble dial in a polished gold case", true) { c, m -> MarbleFace(c, m) },
        face("analog_minimal_dot", "Minimal Dot", WatchFaceCategory.ANALOG, "Nothing but dots, two hands and an orbiting seconds pearl", true) { c, m -> MinimalDotFace(c, m) },
        face("analog_bauhaus_color", "Memphis", WatchFaceCategory.ANALOG, "Playful primary shapes mark the quarters", true) { c, m -> BauhausColorFace(c, m) },
        face("analog_compass", "Field Compass", WatchFaceCategory.ANALOG, "Cardinal directions and a degree ring for the outdoors") { c, m -> CompassFace(c, m) },
        face("analog_sundial", "Sundial", WatchFaceCategory.ANALOG, "A stone gnomon casts the hour; a bronze pearl tells the minutes") { c, m -> SundialFace(c, m) },
        face("analog_moonphase", "Moonphase", WatchFaceCategory.ANALOG, "Real lunar phase in a starry aperture with date") { c, m -> MoonphaseFace(c, m) },
        face("analog_solar", "Solar Dial", WatchFaceCategory.ANALOG, "24-hour track where the sun rides; the sky follows the real sun", true, live = true) { c, m -> SolarFace(c, m) },
        face("analog_infograph", "Infograph", WatchFaceCategory.ANALOG, "Rich dial with live battery, weather, date and alarm complications", true, live = true) { c, m -> InfographFace(c, m) },
        face("analog_activity", "Rings", WatchFaceCategory.ANALOG, "Hour, minute and second progress as activity-style rings", true) { c, m -> ActivityRingsFace(c, m) },

        // ---------------- DIGITAL ----------------
        face("digital_minimal", "Minimal", WatchFaceCategory.DIGITAL, "Ultra-thin numerals with perfect proportions") { c, m -> DigitalMinimalFace(c, m) },
        face("digital_float", "Float", WatchFaceCategory.DIGITAL, "Chunky overlapping numerals in gradients that change through the day", true) { c, m -> DigitalFloatFace(c, m) },
        face("digital_bold", "Bold Stack", WatchFaceCategory.DIGITAL, "Stacked heavyweight hours and accent minutes") { c, m -> DigitalBoldFace(c, m) },
        face("digital_vertical", "Vertical", WatchFaceCategory.DIGITAL, "Two-tone stacked time with a calendar column", true) { c, m -> DigitalVerticalFace(c, m) },
        face("digital_oled", "OLED Pure", WatchFaceCategory.DIGITAL, "Seconds pill and a live progress hairline on true black") { c, m -> DigitalOledFace(c, m) },
        face("digital_split", "Split", WatchFaceCategory.DIGITAL, "Hours and minutes divided by a glowing accent line", true) { c, m -> DigitalSplitFace(c, m) },
        face("digital_gradient", "Gradient", WatchFaceCategory.DIGITAL, "Time painted with the colours of the hour", true) { c, m -> DigitalGradientFace(c, m) },
        face("digital_neon", "Neon Tube", WatchFaceCategory.DIGITAL, "Double-glow neon in your accent colour") { c, m -> DigitalNeonFace(c, m) },
        face("digital_outline", "Outline", WatchFaceCategory.DIGITAL, "Architectural hollow numerals") { c, m -> DigitalOutlineFace(c, m) },
        face("digital_glass", "Frosted Glass", WatchFaceCategory.DIGITAL, "Glass card floating over drifting colour") { c, m -> DigitalGlassFace(c, m) },
        face("digital_poster", "Swiss Poster", WatchFaceCategory.DIGITAL, "Condensed International-style typography with week number") { c, m -> DigitalPosterFace(c, m) },
        face("digital_typewriter", "Typewriter", WatchFaceCategory.DIGITAL, "Literary time: “It is 9:41 in the evening”") { c, m -> DigitalTypewriterFace(c, m) },
        face("digital_spoken", "Spoken", WatchFaceCategory.DIGITAL, "The time in plain words — “quarter past seven”", true) { c, m -> DigitalSpokenFace(c, m) },
        face("digital_word_clock", "Word Clock", WatchFaceCategory.DIGITAL, "QLOCKTWO-style letter grid with minute dots") { c, m -> DigitalWordClockFace(c, m) },
        face("digital_segmented", "Seven Segment", WatchFaceCategory.DIGITAL, "Vintage LED alarm clock with ghost segments and blinking colon") { c, m -> DigitalSegmentedFace(c, m) },
        face("digital_retro_lcd", "Retro LCD", WatchFaceCategory.DIGITAL, "80s calculator-watch LCD with day, date and seconds", true) { c, m -> DigitalRetroLcdFace(c, m) },
        face("digital_dot_matrix", "Dot Matrix", WatchFaceCategory.DIGITAL, "Glyph-style dot matrix inspired by Nothing", true) { c, m -> DigitalDotMatrixFace(c, m) },
        face("digital_nixie", "Nixie Tubes", WatchFaceCategory.DIGITAL, "Warm glowing cathodes in glass tubes", true) { c, m -> DigitalNixieFace(c, m) },
        face("digital_matrix", "Terminal", WatchFaceCategory.DIGITAL, "Phosphor console with live battery and weather readouts") { c, m -> DigitalMatrixFace(c, m) },
        face("digital_binary", "Binary", WatchFaceCategory.DIGITAL, "BCD binary columns for hours, minutes and seconds") { c, m -> DigitalBinaryFace(c, m) },
        face("digital_seconds_ring", "Seconds Ring", WatchFaceCategory.DIGITAL, "Digital time inside a ring of 60 lighting pearls", true) { c, m -> DigitalSecondsRingFace(c, m) },
        face("digital_code_rain", "Code Rain", WatchFaceCategory.DIGITAL, "Falling glyph columns around a glowing terminal clock", true) { c, m -> CodeRainFace(c, m) },
        face("digital_kinetic", "Kinetic", WatchFaceCategory.DIGITAL, "24 little clocks sweep their hands into the time", true) { c, m -> KineticFace(c, m) },
        face("digital_liquid_glass", "Liquid Glass", WatchFaceCategory.DIGITAL, "A frosted glass pill floating over moving colour", true) { c, m -> LiquidGlassFace(c, m) },
        face("digital_big_date", "Big Date", WatchFaceCategory.CALENDAR, "Editorial calendar: a giant date, the time and what's next", true, live = true) { c, m -> BigDateFace(c, m) },
        face("music_vinyl", "Vinyl", WatchFaceCategory.MUSIC, "A turntable that spins your album art while music plays", true, live = true) { c, m -> VinylFace(c, m) },

        // ---------------- GMT & WORLD ----------------
        face("gmt_explorer", "GMT Master", WatchFaceCategory.GMT, "Two-tone 24h bezel and an arrow hand for your second time zone") { c, m -> GmtExplorerFace(c, m) },
        face("gmt_calendar", "GMT Calendar", WatchFaceCategory.GMT, "Pointer-date ring, 24h track and second time zone") { c, m -> GmtCalendarFace(c, m) },
        face("gmt_world_time", "World Time", WatchFaceCategory.GMT, "Rotating day/night ring against 24 reference cities", true) { c, m -> WorldTimeFace(c, m) },
        face("hybrid_dual_time", "Dual Time", WatchFaceCategory.GMT, "Home dial with an away-time sub-dial and day/night indicator") { c, m -> HybridDualTimeFace(c, m) },
        face("world_clock_face", "World Clocks", WatchFaceCategory.WORLD_CLOCK, "Mini analog clocks for your cities, tinted by day and night") { c, m -> WorldClockFace(c, m) },
        face("world_multi_timezone", "Time Zones", WatchFaceCategory.WORLD_CLOCK, "Cities, offsets and live times at a glance") { c, m -> MultiTimezoneFace(c, m) },
        face("world_terminator", "Day & Night", WatchFaceCategory.WORLD_CLOCK, "World map with the live day/night terminator and your cities", true) { c, m -> DayNightMapFace(c, m) },

        // ---------------- HYBRID & MODULAR ----------------
        face("hybrid_analog_digital", "Ana-Digi", WatchFaceCategory.HYBRID, "Analog dial with twin LCD windows for time and date") { c, m -> HybridAnalogDigitalFace(c, m) },
        face("hybrid_analog_battery", "Battery Gauge", WatchFaceCategory.HYBRID, "Fuel-gauge sub-dial for your real battery level", live = true) { c, m -> HybridAnalogBatteryFace(c, m) },
        face("hybrid_analog_weather", "Weather Dial", WatchFaceCategory.HYBRID, "Live conditions and temperature in a sub-dial", live = true) { c, m -> HybridAnalogWeatherFace(c, m) },
        face("hybrid_analog_calendar", "Day-Date", WatchFaceCategory.HYBRID, "Full weekday window at 12 and date at 3") { c, m -> HybridAnalogCalendarFace(c, m) },
        face("hybrid_gmt_weather", "GMT Weather", WatchFaceCategory.HYBRID, "Stealth GMT bezel with a weather sub-dial", live = true) { c, m -> HybridGmtWeatherFace(c, m) },
        face("modular_compact", "Modular", WatchFaceCategory.MODULAR, "Big time with weather, battery and next-event complications", true, live = true) { c, m -> ModularCompactFace(c, m) },
        face("modular_infograph", "Infograph Modular", WatchFaceCategory.MODULAR, "Gauges for battery, temperature and humidity plus your agenda", true, live = true) { c, m -> ModularInfographFace(c, m) },
        face("modular_dashboard", "Dashboard", WatchFaceCategory.MODULAR, "Clock with sunrise, sunset, rain and wind metrics", true, live = true) { c, m -> ModularDashboardFace(c, m) },

        // ---------------- FLIP ----------------
        face("flip_classic", "Flip Classic", WatchFaceCategory.FLIP, "Animated split-flap cards with a real hinge flip") { c, m -> FlipClassicFace(c, m) },
        face("flip_dark", "Flip Midnight", WatchFaceCategory.FLIP, "Low-glare dark flaps for the bedside") { c, m -> FlipDarkFace(c, m) },
        face("flip_amber", "Flip Amber", WatchFaceCategory.FLIP, "Warm amber glowing digits for night-time comfort") { c, m -> FlipAmberFace(c, m) },
        face("flip_graphite", "Flip Graphite", WatchFaceCategory.FLIP, "Brushed graphite modules") { c, m -> FlipGraphiteFace(c, m) },
        face("flip_paper", "Flip Paper", WatchFaceCategory.FLIP, "Crisp white card stock, like a 70s desk clock", true) { c, m -> FlipPaperFace(c, m) },
        face("flip_seconds", "Flip Seconds", WatchFaceCategory.FLIP, "Split-flap with a ticking seconds card", true) { c, m -> FlipSecondsFace(c, m) },

        // ---------------- WEATHER ----------------
        face("weather_sky", "Living Sky", WatchFaceCategory.WEATHER, "Animated sky with real sun, moon, clouds, rain and snow", true, live = true) { c, m -> WeatherSkyFace(c, m) },
        face("weather_digital", "Weather Now", WatchFaceCategory.WEATHER, "Conditions, temperature and details beside the time", live = true) { c, m -> WeatherDigitalFace(c, m) },
        face("weather_hourly", "Hourly", WatchFaceCategory.WEATHER, "Smooth 12-hour temperature curve with rain chance", live = true) { c, m -> WeatherHourlyFace(c, m) },
        face("weather_forecast", "Forecast", WatchFaceCategory.WEATHER, "Six-day forecast with temperature range bars", live = true) { c, m -> WeatherForecastFace(c, m) },
        face("weather_analog", "Thermo Dial", WatchFaceCategory.WEATHER, "Analog dial wrapped in a live thermometer arc", live = true) { c, m -> WeatherAnalogFace(c, m) },
        face("weather_sun_arc", "Sunrise & Sunset", WatchFaceCategory.WEATHER, "Today's sun path with real sunrise, sunset and daylight", true) { c, m -> WeatherSunArcFace(c, m) },
        face("weather_rain_glass", "Rain on Glass", WatchFaceCategory.WEATHER, "City lights behind a wet window; rivulets run when it rains", true, live = true) { c, m -> RainGlassFace(c, m) },

        // ---------------- ASTRONOMY ----------------
        face("astro_moon", "Moon", WatchFaceCategory.ASTRONOMY, "Photographic moon in its real phase with upcoming full and new moons", true) { c, m -> MoonFace(c, m) },
        face("astro_orrery", "Orrery", WatchFaceCategory.ASTRONOMY, "The solar system with every planet where it is today", true) { c, m -> OrreryFace(c, m) },
        face("astro_night_sky", "Night Sky", WatchFaceCategory.ASTRONOMY, "Star field turning around the pole with shooting stars", true) { c, m -> NightSkyFace(c, m) },

        // ---------------- PRODUCTIVITY ----------------
        face("productivity_pomodoro", "Pomodoro", WatchFaceCategory.PRODUCTIVITY, "Working focus timer with auto breaks and session tracking", interactive = true) { c, m -> PomodoroClockFace(c, m) },
        face("productivity_timer", "Timer", WatchFaceCategory.PRODUCTIVITY, "Countdown with presets, +1 minute and an alarm chime", interactive = true) { c, m -> TimerFace(c, m) },
        face("productivity_stopwatch", "Stopwatch", WatchFaceCategory.PRODUCTIVITY, "Precise stopwatch with best/worst lap highlighting", interactive = true) { c, m -> StopwatchFace(c, m) },
        face("productivity_focus", "Deep Work", WatchFaceCategory.PRODUCTIVITY, "A calm clock with your current focus session", interactive = true) { c, m -> FocusClockFace(c, m) },
        face("productivity_day_progress", "Progress", WatchFaceCategory.PRODUCTIVITY, "How much of today, this week, month and year has passed", true) { c, m -> DayProgressFace(c, m) },
        face("productivity_countdown", "Countdown", WatchFaceCategory.PRODUCTIVITY, "Days, hours and minutes until your big day", true) { c, m -> CountdownFace(c, m) },
        face("productivity_year_dots", "Year in Dots", WatchFaceCategory.PRODUCTIVITY, "Every day of the year as a dot, one row per month", true) { c, m -> YearDotsFace(c, m) },
        face("productivity_sleep_cycles", "Sleep Cycles", WatchFaceCategory.PRODUCTIVITY, "Sleep left before your alarm, in 90-minute cycles", true, live = true) { c, m -> SleepCyclesFace(c, m) },
        face("productivity_agenda", "Agenda", WatchFaceCategory.CALENDAR, "Today's calendar events beside the date", true, live = true) { c, m -> AgendaFace(c, m) },

        // ---------------- AMBIENT ----------------
        face("ambient_aurora", "Aurora", WatchFaceCategory.AMBIENT, "Living northern lights over a mountain ridge") { c, m -> AuroraFace(c, m) },
        face("ambient_galaxy", "Deep Cosmos", WatchFaceCategory.AMBIENT, "Nebulae and a slowly turning spiral galaxy") { c, m -> GalaxyFace(c, m) },
        face("ambient_ocean", "Moonlit Ocean", WatchFaceCategory.AMBIENT, "Layered waves glittering under the moon") { c, m -> OceanFace(c, m) },
        face("ambient_forest", "Firefly Forest", WatchFaceCategory.AMBIENT, "Misty pine layers with drifting fireflies") { c, m -> ForestFace(c, m) },
        face("ambient_mountain", "Alpine", WatchFaceCategory.AMBIENT, "Mountain ridges under a sky that follows the hour") { c, m -> MountainFace(c, m) },
        face("ambient_zen", "Zen Garden", WatchFaceCategory.AMBIENT, "Raked sand and slow ripples") { c, m -> ZenFace(c, m) },
        face("ambient_minimal_gradient", "Void", WatchFaceCategory.AMBIENT, "A breathing glow on pure black — the most restful face") { c, m -> MinimalGradientFace(c, m) },
        face("ambient_lava", "Lava Lamp", WatchFaceCategory.AMBIENT, "Warm wax blobs rising and falling", true) { c, m -> LavaLampFace(c, m) },
        face("ambient_breathe", "Breathe", WatchFaceCategory.AMBIENT, "Guided box breathing: a bloom that grows as you inhale", true) { c, m -> BreathingFace(c, m) },
        face("ambient_silk", "Silk", WatchFaceCategory.AMBIENT, "Flowing ribbons of light in your accent colour", true) { c, m -> SilkFace(c, m) },
        face("ambient_sakura", "Sakura", WatchFaceCategory.ARTISTIC, "A cherry branch under the moon with drifting petals", true) { c, m -> SakuraFace(c, m) },
        face("ambient_fireplace", "Fireplace", WatchFaceCategory.AMBIENT, "A crackling hearth with rising embers", true) { c, m -> FireplaceFace(c, m) },
        face("ambient_lighthouse", "Lighthouse", WatchFaceCategory.AMBIENT, "A beacon sweeping across a moonlit sea", true) { c, m -> LighthouseFace(c, m) },
        face("ambient_snowfall", "Snowfall", WatchFaceCategory.AMBIENT, "A warm cabin in a snowy pine valley", true) { c, m -> SnowfallFace(c, m) },
        face("artistic_pixel_day", "Pixel Day", WatchFaceCategory.ARTISTIC, "An 8-bit landscape whose sky follows the real time of day", true) { c, m -> PixelDayFace(c, m) },
        face("ambient_tide", "Tide", WatchFaceCategory.AMBIENT, "Water rises through the hour; the hour floats inside", true) { c, m -> TideFace(c, m) },

        // ---------------- ARTISTIC ----------------
        face("ambient_geometric", "Bauhaus Shapes", WatchFaceCategory.ARTISTIC, "Wedge, bar and square encode hours, minutes and seconds") { c, m -> GeometricFace(c, m) },
        face("ambient_neon", "Synthwave", WatchFaceCategory.ARTISTIC, "Striped retro sun over an endless neon grid") { c, m -> NeonAmbientFace(c, m) },
        face("ambient_japanese", "Ensō", WatchFaceCategory.ARTISTIC, "A brush circle that completes each hour, sealed with the hour") { c, m -> JapaneseFace(c, m) },
        face("ambient_rings", "Orbits", WatchFaceCategory.ARTISTIC, "Hour, minute and second bodies orbiting with trails", true) { c, m -> OrbitsFace(c, m) },
        face("ambient_radar", "Radar", WatchFaceCategory.ARTISTIC, "Sweeping scope; contacts mark the hour and minute", true) { c, m -> RadarFace(c, m) },
        face("ambient_fibonacci", "Fibonacci", WatchFaceCategory.ARTISTIC, "The Fibonacci clock — read time from coloured squares", true) { c, m -> FibonacciFace(c, m) },
        face("ambient_pendulum", "Pendulum", WatchFaceCategory.ARTISTIC, "A brass pendulum beating seconds", true) { c, m -> PendulumFace(c, m) },
        face("ambient_hourglass", "Hourglass", WatchFaceCategory.ARTISTIC, "Sand that drains exactly once an hour", true) { c, m -> HourglassFace(c, m) },
        face("ambient_spiral", "Spiral", WatchFaceCategory.ARTISTIC, "The whole day as a two-turn spiral", true) { c, m -> SpiralFace(c, m) },

        // ---------------- PHOTO ----------------
        face("photo_frame", "Photo Frame", WatchFaceCategory.PHOTO, "Your photos with a Ken Burns drift and a corner clock", live = true) { c, m -> PhotoFrameFace(c, m) },
        face("photo_clock", "Photo Clock", WatchFaceCategory.PHOTO, "Large clock over your photo slideshow", live = true) { c, m -> PhotoClockFace(c, m) },
        face("photo_weather", "Photo + Weather", WatchFaceCategory.PHOTO, "Slideshow with a glass weather chip", live = true) { c, m -> PhotoWeatherFace(c, m) },
        face("photo_calendar", "Photo + Agenda", WatchFaceCategory.PHOTO, "Slideshow beside your date and events", live = true) { c, m -> PhotoCalendarFace(c, m) },
        face("photo_split", "Photo Split", WatchFaceCategory.PHOTO, "Half photo, half clock", true, live = true) { c, m -> PhotoSplitFace(c, m) },

        // ---------------- CHARGING ----------------
        face("charging_orb", "Charging Orb", WatchFaceCategory.CHARGING, "A liquid orb that fills with your battery level", true) { c, m -> ChargingOrbFace(c, m) },
        face("charging_ring", "Power Ring", WatchFaceCategory.CHARGING, "Battery ring with temperature, health, voltage and time to full", true) { c, m -> ChargingRingFace(c, m) }
    )

    private val byId: Map<String, Entry> = entries.associateBy { it.definition.id }
    private val faces: List<WatchFaceDefinition> = entries.map { it.definition }

    const val DEFAULT_FACE_ID = "analog_standby"

    fun getAll(): List<WatchFaceDefinition> = faces

    fun contains(id: String): Boolean = byId.containsKey(id)

    fun getById(id: String): WatchFaceDefinition =
        byId[id]?.definition ?: faces.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: byId.getValue(DEFAULT_FACE_ID).definition

    fun getByCategory(category: WatchFaceCategory): List<WatchFaceDefinition> = faces.filter { it.category == category }

    fun categoriesInUse(): List<WatchFaceCategory> = WatchFaceCategory.entries.filter { cat -> faces.any { it.category == cat } }

    fun search(query: String): List<WatchFaceDefinition> {
        if (query.isBlank()) return faces
        val clean = query.trim().lowercase()
        return faces.filter {
            it.name.lowercase().contains(clean) ||
                it.description.lowercase().contains(clean) ||
                it.category.displayName.lowercase().contains(clean)
        }
    }

    fun getFavorites(favIds: Set<String>): List<WatchFaceDefinition> = faces.filter { favIds.contains(it.id) }

    @Composable
    fun Render(id: String, ctx: FaceContext, modifier: Modifier = Modifier) {
        val entry = byId[id] ?: byId.getValue(DEFAULT_FACE_ID)
        entry.content(ctx, modifier)
    }
}
