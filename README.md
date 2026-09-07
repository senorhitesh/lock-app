# FocusBlock — Standalone Android App Blocker

FocusBlock is a native Android application that helps users manage screen time and maintain productivity by temporarily blocking access to selected applications.

## Key Features
- **App Selection**: Select any installed launchable app on device to block.
- **Flexible Sessions**: Quick durations (15m, 30m, 1h, 2h, 4h) or custom hours & minutes.
- **Dual Layer Detection**: 
  - Accessibility Service (`TYPE_WINDOW_STATE_CHANGED`) for instant blocking screen presentation.
  - Background Foreground Service (`UsageStatsManager`) polling fallback every 1.5 seconds.
- **Anti-Loop Protection & Cooldown**: Smart debounce prevents re-launch cycles and UI flickering.
- **Strict Mode**: Optional mode that disables the early Stop button to build discipline.
- **Scheduled Blocking**: Set recurring daily or weekly schedules (e.g. 9:00 - 17:00). Supports overnight / midnight-crossing intervals.
- **History Tracking**: Local history log of blocked sessions with duration stats.
- **Reboot Resilience**: Automatically recovers active session and schedules upon device reboot.
- **100% Offline & Private**: Zero network permissions, no trackers, no accounts.

## Architecture
- **Language**: Kotlin 2.0.21
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM with Repository Pattern
- **Storage**:
  - Jetpack DataStore (Preferences) for active session state & configs
  - Room SQLite Database for recurring schedules & history
- **Android Services**:
  - `BlockerAccessibilityService` (Primary detector)
  - `BlockerForegroundService` (Fallback detector + Ongoing notification)
  - `AlarmManager` with `AlarmReceiver` & `ScheduleAlarmReceiver` for exact session triggers

## Building the APK

### Requirements
- JDK 17
- Android SDK (API 35 compileSdk, minSdk 26)

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease
```
Outputs:
- Debug APK: `app/build/outputs/apk/debug/app-debug.apk`
- Release APK: `app/build/outputs/apk/release/app-release-unsigned.apk`
