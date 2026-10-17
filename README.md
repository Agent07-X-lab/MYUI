# MyUI — Minimal Widgets (Free & Open Source Edition)

100% Free & Open Source Software (FOSS) modern Android widget suite engineered in **Jetpack Compose** and the native **Android AppWidget framework**.

Licensed under the **Apache License 2.0**.

---

## 🌟 Open Source & Free Community Edition Features

- **No Pro Subscriptions / No Paywalls:** All commercial in-app purchase gates, subscription requirements, and billing checks have been completely stripped out.
- **422 Widgets Fully Unlocked:** All widgets (Analog Clocks, Digital Clocks, Weather, Calendar, Health/Pedometer, Battery, App Folders, Quotes, Music, System Monitors) are available to place immediately with zero restrictions.
- **41 Studio Custom Themes Unlocked:** Complete theme engine with all Dark, Light, Pastel, and Muted themes unlocked with no "PRO" locks or badges.
- **Zero Proprietary Google Play SDKs:** Removed proprietary Google Play Billing (`com.android.vending.BILLING`) and Play In-App Updates. Releases and updates link directly to GitHub Releases.
- **Zero Ads & Zero Trackers:** All Google AdMob (`AD_ID`, `AdServices`) and install referrer trackers have been completely excised.
- **Native Android Standards:** Built with Jetpack Compose (Material 3), Kotlin 2.0, and modern RemoteViews architecture compatible with Android 12L (SDK 32) through Android 15+ (SDK 35/36).

---

## 📱 Project Architecture

- **UI Framework:** 100% Jetpack Compose (Material 3) with animated tab transitions.
- **Widgets:** Android Native AppWidget Framework (`RemoteViews`) supporting dynamic theme tinting, background opacity blending, and real-time sensor/battery binding.
- **Language:** Kotlin 2.0 with Jetpack Compose Compiler plugin.
- **Build System:** Gradle 8.10.2 with Version Catalog (`gradle/libs.versions.toml`) and Kotlin DSL (`build.gradle.kts`).
- **Target SDK:** 35 / 36 | **Min SDK:** 32

---

## 📂 Project Structure

```
MinimalWidgets_Reconstructed/
├── LICENSE                            # Apache License 2.0
├── gradle/
│   ├── libs.versions.toml             # Standard Gradle Version Catalog (FOSS dependencies)
│   └── wrapper/
│       └── gradle-wrapper.properties  # Gradle 8.10.2 configuration
├── app/
│   ├── build.gradle.kts               # Module build configuration (FOSS dependencies)
│   ├── proguard-rules.pro             # Proguard & R8 keep rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml    # Component declarations, permissions & 422 widget receivers
│           ├── assets/
│           │   ├── font/              # 43 custom typefaces (Bebas, gobold, iwidget_01-31, mainb/m/r)
│           │   └── icons/             # Phosphor icons JSON definitions
│           ├── res/                   # 2,140+ XML layouts, drawables, styles, and dimensions
│           └── java/com/jndapp/minimal/widgets/
│               ├── ApplicationClass.kt       # Application entry point & notification channels
│               ├── MainActivity.kt           # Compose root activity with BottomNavigation
│               ├── ConfigActivities.kt      # Widget configuration dialogs (Compose)
│               ├── QuickNoteEditActivity.kt  # Note widget editor
│               ├── model/
│               │   ├── Wallpaper.kt          # Wallpaper model with collection parsing
│               │   └── ThemeModel.kt         # 41 Studio color themes (Dark, Light, Pastel, Muted)
│               ├── services/
│               │   ├── WidgetKeepAliveService.kt  # Foreground keep-alive service
│               │   ├── PedometerService.kt        # Hardware step counter service
│               │   ├── CompassSensorService.kt    # Azimuth rotation vector tracker
│               │   └── ConnectivityMonitorService.kt # Network & toggle monitor
│               ├── receivers/
│               │   └── BootCompletedReceiver.kt   # System boot restart receiver
│               ├── ui/
│               │   ├── theme/
│               │   │   ├── Color.kt          # Curated color tokens
│               │   │   └── Theme.kt          # MyUITheme with Material 3 Dark/Light
│               │   └── screens/
│               │       ├── WidgetsScreen.kt   # Full widget gallery, search, category chips, favorites
│               │       ├── WallpapersScreen.kt# High-res wallpaper grid and preview modal
│               │       ├── StudioScreen.kt    # Widget customization studio with live preview
│               │       └── SettingsScreen.kt  # Version info, GitHub releases, FOSS highlights
│               └── widgets/                  # Native AppWidgetProvider implementations
```

---

## 🚀 How to Build & Install

### Prerequisites
- **JDK:** Java 17 or Java 21 (Eclipse Adoptium OpenJDK 21 or Android Studio JBR).
- **Android SDK:** Platforms 34 or 35 installed.

### Build via Gradle
```bash
# Build the open-source debug APK
./gradlew assembleDebug
```
The generated APK will be at:
`app/build/outputs/apk/debug/app-debug.apk`

### Install directly to Device / Tablet via ADB
Double click `install_v22_to_tablet.bat` or run:
```bash
adb install -r MyUI-v22-Fixed.apk
```

---

## 📄 License

```
Copyright 2026 Agent07-X-lab & Community Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
