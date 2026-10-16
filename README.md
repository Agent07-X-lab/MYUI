# Minimal Widgets — Android Source Reconstruction

Reconstructed modern Android Studio project for **Minimal Widgets v2.2.06** by JustNewDesigns.

## 📱 Project Architecture

- **UI Framework:** 100% Jetpack Compose (Material 3) with dynamic animated transitions.
- **Widgets:** Android Native AppWidget Framework (`RemoteViews`) supporting dynamic theme tinting, opacity blending, and real-time sensor/battery binding.
- **Language:** Kotlin 2.0 with Jetpack Compose Compiler plugin.
- **Build System:** Gradle with Version Catalog (`gradle/libs.versions.toml`) and Kotlin DSL (`build.gradle.kts`).
- **Target SDK:** 35 / 36 | **Min SDK:** 32

---

## 📂 Project Structure

```
MinimalWidgets_Reconstructed/
├── gradle/
│   ├── libs.versions.toml             # Standard Gradle Version Catalog
│   └── wrapper/
│       └── gradle-wrapper.properties  # Gradle 8.10.2 configuration
├── app/
│   ├── build.gradle.kts               # Module build configuration with Compose dependencies
│   ├── proguard-rules.pro             # Proguard & R8 keep rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml    # Component declarations, permissions & widget receivers
│           ├── assets/
│           │   ├── font/              # 43 custom typefaces (Bebas, gobold, iwidget_01-31, mainb/m/r)
│           │   ├── icons/             # Phosphor icons JSON definitions
│           │   ├── more_apps.json     # App directory catalogue
│           │   └── *.json             # Lottie animations (panda, confetti, loading, etc.)
│           ├── res/                   # 2,140+ XML layouts, drawables, styles, and dimensions
│           └── java/com/jndapp/minimal/widgets/
│               ├── ApplicationClass.kt       # Application entry point & notification channels
│               ├── MainActivity.kt           # Compose root activity with BottomNavigation
│               ├── ConfigActivities.kt      # Widget configuration dialogs
│               ├── BluetoothPermissionDialog.kt
│               ├── NotificationAccessDialog.kt
│               ├── NotificationListener.kt
│               ├── AutorotatePermissionActivity.kt
│               ├── QuickNoteEditActivity.kt
│               ├── BatteryMonitorService.kt
│               ├── model/
│               │   ├── Wallpaper.kt          # Wallpaper model with collection parsing
│               │   ├── CustomQuote.kt        # User custom quote model
│               │   └── ThemeModel.kt         # 50+ Studio color themes (Dark, Light, Pastel, Muted)
│               ├── models/
│               │   └── MoreApp.kt            # Cross-promotion app model
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
│               │   │   └── Theme.kt          # MinimalWidgetsTheme with Material 3 Dark/Light
│               │   └── screens/
│               │       ├── WidgetsScreen.kt   # Widget catalog, search, category chips, favorites
│               │       ├── WallpapersScreen.kt# High-res wallpaper grid and preview modal
│               │       ├── StudioScreen.kt    # Widget customization studio with live preview
│               │       └── SettingsScreen.kt  # Version info, updater, What's New, Lottie mascot
│               └── widgets/
│                   ├── analog/
│                   │   ├── BaseAnalogWidget.kt# Abstract base analog clock provider
│                   │   ├── analog1.kt         # Concrete Analog 1 provider
│                   │   └── AnalogWidgets.kt   # Concrete providers analog2 through analog31
│                   ├── battery/
│                   │   ├── BaseBatteryWidget.kt# Abstract base battery ring provider
│                   │   ├── Battery1.kt        # Concrete Battery 1 provider
│                   │   └── BatteryWidgets.kt  # Concrete providers Battery2 through Battery20
│                   └── weather/
│                       ├── WeatherBase.kt     # Abstract base weather provider
│                       ├── Weather1.kt        # Concrete Weather 1 provider
│                       └── WeatherWidgetConfigActivity.kt
```

---

## 🚀 How to Open and Build in Android Studio

### Prerequisites
- **Android Studio:** Ladybug (2024.2.1) or newer recommended.
- **JDK:** Java 17 or Java 21 (bundled with Android Studio).
- **Android SDK:** Platforms 34, 35, or 36 installed via SDK Manager.

### Step-by-Step Instructions
1. Open **Android Studio**.
2. Select **File > Open…** and browse to:
   ```
   MinimalWidgets_Reconstructed
   ```
3. Allow Gradle to perform the initial sync. Android Studio will automatically resolve all dependencies declared in `gradle/libs.versions.toml`.
4. To build debug APK via terminal:
   ```bash
   ./gradlew assembleDebug
   ```
5. To install directly to a connected Android device or emulator:
   ```bash
   ./gradlew installDebug
   ```
6. Alternatively, click the green **Run (Shift+F10)** button in the Android Studio toolbar.

---

## 🔍 Features Recovered from Evidence

1. **4-Tab Navigation & Screens:**
   - **Widgets:** Dynamic browsing, category filtering, search, favorite toggling persisted in `widget_favorites_prefs`, and `AppWidgetManager.requestPinAppWidget` shortcuts.
   - **Wallpapers:** Categorized wallpaper grid matching original model fields (`name`, `author`, `url`, `thumbnail`, `collections`, `dimensions`).
   - **Studio:** Color theming system with over 50 verified color palettes across Dark, Light, Pastel, and Muted themes recovered from `hg0.java`. Live widget preview card with interactive opacity slider persisted in `widget_settings`.
   - **Settings:** App version `V2.2.06 (16)`, Google Play In-App Updates, What's New dialog, restored purchase actions, verified social links, and Lottie `panda_animation.json` mascot.
2. **Assets & Resources:**
   - 43 authentic typography files (`assets/font/`).
   - 6 Lottie animation JSONs.
   - 2,142 XML layouts, drawables, and widget receiver configurations.
   - Cross-promotion catalogue (`more_apps.json`).
3. **Background Services & Receivers:**
   - `WidgetKeepAliveService` with notification channel `"widget_keep_alive_channel"`.
   - `PedometerService` reading hardware step counter sensors.
   - `CompassSensorService` reading rotation vector & orientation.
   - `ConnectivityMonitorService` monitoring network state.
   - `BootCompletedReceiver` ensuring widgets persist across device reboots.

---

## ⚠️ Notes & Limitations

- **Ad Units & Analytics:** Decompiled Google Ad units and Crashlytics tracking IDs have been decoupled to maintain clean, buildable open-source code without proprietary credentials.
- **Billing Bypass:** In-app purchase gates (`PremiumGateConfigActivity`) default to verified unlocked state in preferences without bypassing Google Play licensing security.
- **Smali / Dex Obfuscation:** Decompiled classes in `defpackage` containing inlined synthetic Compose lambdas were completely re-engineered into clean, idiomatic Kotlin composables.
