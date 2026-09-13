# 🌙 StandBy Pro for Android

<p align="center">
  <b>A production-grade, immersive ambient smart display for Android inspired by iOS StandBy.</b><br>
  Engineered with Jetpack Compose, Material 3, and native Android hardware sensors.
</p>

---

## 📱 Highlights & Features

- ⚡ **Auto-Activation Engine**: Background foreground service (`StandByService`) constantly detects real hardware charging state (USB, AC, Wireless) and physical orientation (via accelerometer telemetry) to launch StandBy automatically when placed on a stand or charger.
- 🕒 **Adaptive Clock Faces**:
  - **Digital Clock**: Ultra-clean typographic design with configurable 12h/24h formats, seconds display, and date typography.
  - **Analog Clock**: Precision canvas-rendered analog face with smooth second sweep and hour tick markers.
  - **Instant Style Switcher**: Tap the clock face at any time to fluidly toggle between digital and analog modes.
- 📊 **Two-Panel Smart Widget Stack**:
  - **Left Panel**: Ambient clock presentation.
  - **Right Panel**: Real-time live status cards including full calendar date, exact battery percentage & charging mode with battery temperature, and upcoming system alarms.
- 🔴 **Sensor-Driven Night Mode**:
  - Hardware ambient light sensor monitoring (`< 5 lux` threshold with hysteresis).
  - Smooth 1.5-second chromatic transition to deep crimson red (`#E53935`), preserving dark adaptation and nighttime comfort.
- 🛡️ **OLED Burn-In Protection**:
  - Periodic sub-pixel displacement algorithm applying micro-coordinate shifts to prevent OLED / AMOLED image retention.
- 💡 **Intelligent Auto-Dimming**:
  - Smoothly dims the screen after 30 seconds of inactivity to save battery and reduce glare.
  - Wakes up immediately upon screen touch.
- ⚙️ **Comprehensive In-App Settings**:
  - Full preference suite backed by Jetpack DataStore.
  - Live "Preview StandBy Mode" button to experience the ambient screen instantly.
- 🎯 **Device Optimizations**:
  - Engineered for modern Android devices, with special attention to **Nothing Phone (2a) Plus (Nothing OS)** and **Samsung Galaxy Tab (LineageOS / AOSP)**.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: Jetpack Compose with Material 3 design tokens
- **Architecture**: MVI / MVVM reactive architecture with Kotlin Coroutines & `StateFlow`
- **Sensors & Telemetry**:
  - `Sensor.TYPE_LIGHT` (Ambient illuminance)
  - `Sensor.TYPE_ACCELEROMETER` (Physical orientation detection)
  - `BatteryManager` Broadcast telemetry (Charging status, temperature, health)
  - `AlarmManager` (Next alarm scheduling integration)
- **Persistence**: Jetpack DataStore Preferences
- **Background Service**: Android Foreground Service with full-screen intent capabilities and debounce throttling
- **Min SDK**: 26 (Android 8.0 Oreo) | **Target SDK**: 36 (Android 15+)

---

## 🚀 Getting Started & Building

### Prerequisites
- Android Studio Ladybug / Meerkat or command line tools
- Android SDK (API 26 to API 36)
- Java 17+

### Build APK
Clone the repository and run Gradle:
```bash
git clone https://github.com/ArindamJaiman/Stand-By-Android.git
cd Stand-By-Android
.\gradlew.bat assembleDebug
```
The output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Install onto Device
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 👨‍💻 Developer & Author

Crafted by **Arindam Jaiman**.

- **GitHub**: [@ArindamJaiman](https://github.com/ArindamJaiman)
- **LinkedIn**: [arindamjaiman](https://www.linkedin.com/in/arindamjaiman/)
- **Instagram**: [@thearindamjaiman](https://www.instagram.com/thearindamjaiman)

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
