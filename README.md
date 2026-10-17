# 🌙 StandBy Pro for Android

<p align="center">
  <b>An iOS-style StandBy smart display for Android — 110+ watch faces, 24 live widgets, smart stacks and a true red night mode.</b><br>
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
- **True night mode**: the entire display is re-rendered in red (or amber / dim white) through a colour matrix — triggered by the light sensor, a bedtime schedule or always on. Brightness is capped automatically.
- **Live Activity pill** for running timers, Pomodoro, stopwatch and now-playing media.
- **Charging moment**: a glowing ring sweeps to your battery level when you plug in.
- **Screen care**: slow burn-in drift, configurable auto-dim, per-frame-cheap rendering.

### 110+ watch faces in 14 families
| Family | Highlights |
| --- | --- |
| **Analog (29)** | StandBy Analog, Luxury Gold, Chronograph (working 24h/30-min/12h registers + tachymeter), Pilot, Diver 300M, Skeleton (moving gears), Tourbillon, Railway, Regulator, Pocket Watch, California, Field, Meridian, Typograph, Contour, Numerals, Solar Dial (real sun elevation), Infograph (live complications), Moonphase (real phase), Sundial, Rings… |
| **Digital (21)** | Minimal, Float, Bold Stack, Vertical, OLED, Split, Gradient, Neon, Outline, Frosted Glass, Swiss Poster, Typewriter, Spoken, Word Clock, Seven Segment, Retro LCD, Dot Matrix, Nixie Tubes, Terminal, Binary, Seconds Ring |
| **GMT & World (7)** | GMT Master (second time zone), GMT Calendar, World Time (rotating 24-city ring), Dual Time, World Clocks, Time Zones, Day & Night map with live terminator |
| **Hybrid & Modular (8)** | Ana-Digi, Battery Gauge, Weather Dial, Day-Date, GMT Weather, Modular, Infograph Modular, Dashboard |
| **Flip (6)** | Real animated split-flap cards: Classic, Midnight, Amber, Graphite, Paper, Seconds |
| **Weather (6)** | Living Sky (animated real conditions), Weather Now, Hourly curve, Forecast bars, Thermo Dial, Sunrise & Sunset |
| **Astronomy (3)** | Moon (real phase, next full/new), Orrery (planets where they are today), Night Sky |
| **Productivity (7)** | Pomodoro, Timer, Stopwatch (working, with alarm chime), Deep Work, Progress, Countdown, Agenda |
| **Ambient & Artistic (18)** | Aurora, Deep Cosmos, Moonlit Ocean, Firefly Forest, Alpine, Zen, Void, Lava Lamp, Tide, Bauhaus Shapes, Synthwave, Ensō, Orbits, Radar, Fibonacci clock, Pendulum, Hourglass, Spiral |
| **Photo (5)** & **Charging (2)** | Photo Frame / Clock / Weather / Agenda / Split, Charging Orb, Power Ring |

Every face is a pure function of live data (time, battery, weather, calendar, alarms, locations) and scales from gallery thumbnails to tablets.

### 24 widgets — all real data
Analog & digital clocks · World clock · Month calendar · Up Next (device calendar) · Weather · 5-day forecast · **Now Playing** (album art + controls for Spotify, YouTube Music, podcasts…) · Battery (time to full, temperature) · Next alarm · Timer · Stopwatch · Pomodoro · Progress · Countdown · Reminders · Notes · Notifications (privacy-aware) · Moon · Sun · Photos · Quote of the day · System (RAM, storage, uptime) · GitHub heatmap

### Live data sources
- **Weather**: [Open-Meteo](https://open-meteo.com) (free, no key). City search, or automatic from your time zone — no location permission needed. Cached for offline starts.
- **Calendar**: device calendars via `CalendarContract` (read-only).
- **Media & notifications**: `MediaSessionManager` + notification listener.
- **Alarms**: `AlarmManager.nextAlarmClock` from any clock app.
- **Sun, moon & planets**: computed offline on-device.

### StandBy Studio
A redesigned settings app with a live preview, a setup checklist for permissions, and five tabs:
**Faces** (searchable gallery of live previews, favourites) · **Widgets** (layouts, slots, smart stacks) · **Display** (14 accents, seconds motion, brightness, night mode, screen care) · **Data** (weather city, world clocks, photos, countdown, calendar, privacy, reminders, notes, GitHub) · **Behaviour** (auto-start rules, demo mode, gestures).

---

## ⚡ Performance & battery
- Second hands read the clock **only in the draw phase** — the composition never recomposes at 60 fps.
- Static dials are cached in their own graphics layer; only the hands layer redraws.
- Sweep is capped at 60 fps (30 fps for ambient art); night mode switches to a once-a-second tick; "Hidden" seconds stops frame loops entirely.
- Gallery thumbnails render statically (no animation loops).
- Timers run on monotonic deadlines, so they never drift.

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
├── astro/            Sun, moon and planet maths
├── data/             Weather, calendar, alarms, tasks, timers, locations, GitHub
├── domain/           Models, FaceContext, face & widget registries
├── media/            Now Playing (MediaSession)
├── notifications/    Notification listener + repository
├── services/         Charging/orientation/lock monitor service
├── settings/         Settings model + DataStore repository
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

---

## 👨‍💻 Author
Crafted by **Arindam Jaiman** — [GitHub](https://github.com/ArindamJaiman) · [LinkedIn](https://www.linkedin.com/in/arindamjaiman/) · [Instagram](https://www.instagram.com/thearindamjaiman)

## 📄 License
MIT — see [LICENSE](LICENSE).
