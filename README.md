# 🌙 StandBy Pro for Android

<p align="center">
  <b>An iOS-style StandBy smart display for Android — 120+ watch faces, 26 live widgets, curated themes, smart stacks, a true red night mode and a full bedside toolkit.</b><br>
  Built with Jetpack Compose and Material 3. No ads, no accounts, no trackers.
</p>

---

## ✨ What's inside

### StandBy display
- **Automatic launch** when the phone is charging, on its side and locked — exactly like iPhone. The monitor sleeps completely when unplugged (zero drain).
- **Swipeable pages** (just like iOS): **Dashboard → Smart Stacks → Photos → Clock**.
  - **Clock**: swipe up/down through your favourite faces.
  - **Smart Stacks**: two independent widget stacks with **Smart Rotate**.
  - **Photos**: your own photos with Ken Burns drift and cross-fades.
  - **Dashboard**: face + widgets in Single, Dual, Triple or Quad layouts.
- **Gestures**: tap for controls, double-tap for the next face, long-press for a live favourites picker.
- **True night mode**: the entire display is re-rendered in red (or amber / dim white) through a colour matrix — triggered by the light sensor, a bedtime schedule, **sunset to sunrise** at your location, or always on. Brightness is capped automatically, and you can pick a separate **night face** that takes over while it's dark.
- **Live Activity pill** for running timers, Pomodoro, stopwatch and now-playing media.
- **Charging moment**: a glowing ring sweeps to your battery level when you plug in.
- **Screen care**: slow burn-in drift, configurable auto-dim, per-frame-cheap rendering.
- **Launch anywhere**: Quick Settings tile, plus launcher shortcuts for **StandBy** and **Bedside** (starts straight into night mode).
- **Face shuffle**: rotate through your favourite faces every 5–60 minutes while untouched.
- **Talking clock**: one tap speaks the time ("It's quarter past seven").
- **Hourly chime** (silent at night) and **Bedtime blackout** that fades the display almost to black at night.
- **Up Next pill**: a meeting starting within 15 minutes appears in the Live Activity pill with its calendar colour; a **Rain likely** heads-up appears when rain is forecast in the next two hours.
- **Screen saver**: pick StandBy Pro in Android's Display → Screen saver and your dashboard appears whenever the phone is charging or docked — night mode and burn-in drift included.

### Themes
- **10 curated looks** — StandBy, Midnight Gold, Nordic Frost, Tokyo Neon, Forest Calm, Mono Pro, Rosé, Deep Space, Vinyl Lounge and Material You. One tap sets the accent, face, typeface, backdrop and night tint together.
- **20 accent colours**, including Lavender, Ruby, Copper, Electric Lime, Lagoon and **Wallpaper** (Material You colours from your wallpaper on Android 12+).
- **Clock typeface**: Modern, Classic, Elegant, Condensed or Mono — applied to every face without a signature typeface of its own.
- **Backdrops** behind the dashboard: OLED black, Graphite, Accent glow, Aurora, Dusk and Starfield. They drift very slowly and pause at night.
- **Backup & restore**: export every setting to a JSON file and restore it on any phone.

### Bedside toolkit
- **Sleep sounds**: rain, ocean, fan, brown, pink and white noise — synthesised on the device, no audio files, with a volume slider.
- **Sleep timer** (15 min – 1 h 30): fades the sounds out over 30 seconds and pauses whatever music or podcast app is playing.
- **Sunrise wake light**: 10–30 minutes before your next alarm the display glows from deep red to warm daylight with a rising sun, and the screen brightens with it.
- **Night light**: a full-screen warm light — drag up/down for brightness, sideways from amber to soft white, tap to turn off.
- **Do Not Disturb at night**: switches to priority-only while night mode is on (alarms still ring) and restores it afterwards.
- **Charge limit alert**: an audible notification when charging reaches 80/85/90/100% to protect battery health.
- **Home-screen widget**: a resizable clock with date, next alarm and battery that never wakes the phone; tap it to start StandBy.

Find the sound machine, timer and night light behind the moon button on the StandBy display; the rest lives in Studio → Behaviour → Bedtime & battery.

### 120+ watch faces in 15 families
| Family | Highlights |
| --- | --- |
| **Analog (31)** | StandBy Analog, **Carbon** (woven carbon chronometer with a battery register), **Marble** (Carrara dial in gold), Luxury Gold, Chronograph (working 24h/30-min/12h registers + tachymeter), Pilot, Diver 300M, Skeleton (moving gears), Tourbillon, Railway, Regulator, Pocket Watch, California, Field, Meridian, Typograph, Contour, Numerals, Solar Dial (real sun elevation), Infograph (live complications), Moonphase (real phase), Sundial, Rings… |
| **Digital (22)** | **Code Rain**, Minimal, Float, Bold Stack, Vertical, OLED, Split, Gradient, Neon, Outline, Frosted Glass, Swiss Poster, Typewriter, Spoken, Word Clock, Seven Segment, Retro LCD, Dot Matrix, Nixie Tubes, Terminal, Binary, Seconds Ring |
| **GMT & World (7)** | GMT Master (second time zone), GMT Calendar, World Time (rotating 24-city ring), Dual Time, World Clocks, Time Zones, Day & Night map with live terminator |
| **Hybrid & Modular (8)** | Ana-Digi, Battery Gauge, Weather Dial, Day-Date, GMT Weather, Modular, Infograph Modular, Dashboard |
| **Flip (6)** | Real animated split-flap cards: Classic, Midnight, Amber, Graphite, Paper, Seconds |
| **Weather (7)** | **Rain on Glass** (rivulets run when it's really raining), Living Sky (animated real conditions), Weather Now, Hourly curve, Forecast bars, Thermo Dial, Sunrise & Sunset |
| **Astronomy (3)** | Moon (real phase, next full/new), Orrery (planets where they are today), Night Sky |
| **Productivity (7)** | Pomodoro, Timer, Stopwatch (working, with alarm chime), Deep Work, Progress, Countdown, Agenda |
| **Ambient & Artistic (21)** | **Silk**, **Sakura**, Breathe (guided box breathing), Aurora, Deep Cosmos, Moonlit Ocean, Firefly Forest, Alpine, Zen, Void, Lava Lamp, Tide, Bauhaus Shapes, Synthwave, Ensō, Orbits, Radar, Fibonacci clock, Pendulum, Hourglass, Spiral |
| **Photo (5)** & **Charging (2)** | Photo Frame / Clock / Weather / Agenda / Split, Charging Orb, Power Ring |
| **Music & Calendar (2)** | **Vinyl** (a turntable that spins your album art while music plays), **Big Date** (editorial calendar with what's next) |

Every face is a pure function of live data (time, battery, weather, calendar, alarms, locations) and scales from gallery thumbnails to tablets.

### 26 widgets — all real data
Analog & digital clocks · World clock · Month calendar · Up Next (device calendar) · Weather · 5-day forecast · **Air quality** (US AQI, PM2.5, UV) · Breathe · **Now Playing** (album art + controls for Spotify, YouTube Music, podcasts…) · Battery (time to full, temperature) · Next alarm · Timer · Stopwatch · Pomodoro · Progress · Countdown · Reminders · Notes · Notifications (privacy-aware) · Moon · Sun · Photos · Quote of the day · System (RAM, storage, uptime) · GitHub heatmap

### Live data sources
- **Weather**: [Open-Meteo](https://open-meteo.com) (free, no key). City search, or automatic from your time zone — no location permission needed. Cached for offline starts.
- **Air quality & UV**: Open-Meteo air-quality API (free, no key).
- **Calendar**: device calendars via `CalendarContract` (read-only).
- **Media & notifications**: `MediaSessionManager` + notification listener.
- **Alarms**: `AlarmManager.nextAlarmClock` from any clock app.
- **Sun, moon & planets**: computed offline on-device.

### StandBy Studio
A redesigned settings app with a live preview, a setup checklist for permissions, and five tabs:
**Faces** (searchable gallery of live previews, favourites) · **Widgets** (layouts, slots, smart stacks) · **Display** (themes, typeface, backdrop, 20 accents, seconds motion, brightness, night mode and night face, screen care) · **Data** (weather city, world clocks, photos, countdown, calendar, privacy, reminders, notes, GitHub) · **Behaviour** (auto-start rules, screen saver, bedtime & battery, backup, demo mode, gestures).

---

## ⚡ Performance & battery
- Second hands read the clock **only in the draw phase** — the composition never recomposes at 60 fps.
- Static dials are cached in their own graphics layer; only the hands layer redraws.
- Sweep is capped at 60 fps (30 fps for ambient art); night mode switches to a once-a-second tick; "Hidden" seconds stops frame loops entirely.
- Gallery thumbnails render statically (no animation loops).
- Timers run on monotonic deadlines, so they never drift.
- Release builds are shrunk and optimised with R8 (code + resources).

---

## 🛠 Architecture
- **UI**: Jetpack Compose + Material 3, custom design system (`ui/design`: tokens, `AutoSizeText`, `DialKit`, weather glyphs, complications).
- **State**: MVVM with Kotlin Flows — the view model assembles one immutable `FaceContext` for every face and widget.
- **Registry**: `WatchFaceRegistry` / `WidgetRegistry` map ids to metadata and composables.
- **Persistence**: Jetpack DataStore (settings), SharedPreferences (weather cache, reminders, notes).
- **Background**: a foreground service gates sensors on charging state, then watches lock state and orientation.
- **Min SDK** 26 · **Target SDK** 36.

```
app/src/main/java/com/standbypro
├── appwidget/        Home-screen clock widget
├── astro/            Sun, moon and planet maths
├── audio/            Sleep-sound synthesis + sleep timer
├── data/             Weather, air quality, calendar, alarms, tasks, timers, locations, GitHub
├── domain/           Models, FaceContext, face & widget registries
├── dream/            System screen saver (DreamService)
├── media/            Now Playing (MediaSession)
├── notifications/    Notification listener + repository
├── services/         Charging/orientation/lock monitor service
├── settings/         Settings model, DataStore repository, JSON backup
└── ui/
    ├── design/       Design system
    ├── faces/        All watch faces
    ├── widgets/      All widgets
    ├── main/         StandBy display (pages, stacks, night filter, chrome)
    ├── compositor/   Dashboard layouts
    └── settings/     StandBy Studio
```

---

## 🚀 Build
Prerequisites: Android Studio (latest stable), Android SDK 36, JDK 17+.

```bash
git clone https://github.com/ArindamJaiman/Stand-By-Android.git
cd Stand-By-Android
./gradlew assembleDebug          # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest      # unit tests (faces logic, astronomy, weather parsing…)
adb install app/build/outputs/apk/debug/app-debug.apk
```

### First run checklist (shown in Studio)
1. Allow **notifications** and **full-screen launch** (Android 14+) so StandBy can appear over the lock screen.
2. Grant **notification access** for Now Playing and notification badges.
3. Allow **calendar** access for agenda faces.
4. Exclude StandBy from **battery optimisation** so the charging monitor stays alive.
5. Optional: allow **Do Not Disturb access** if you turn on *Do Not Disturb at night*.

---

## 👨‍💻 Author
Crafted by **Arindam Jaiman** — [GitHub](https://github.com/ArindamJaiman) · [LinkedIn](https://www.linkedin.com/in/arindamjaiman/) · [Instagram](https://www.instagram.com/thearindamjaiman)

## 📄 License
MIT — see [LICENSE](LICENSE).
