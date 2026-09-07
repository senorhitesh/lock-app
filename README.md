![Uploading image.png…]()
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
- Android SDK (API 35 compileSdk, minSdk 26) or Android Studio Iguana / Jellyfish / Ladybug+

### Build Commands
On Windows (PowerShell):
```powershell
.\gradlew.bat assembleDebug
```
On macOS / Linux:
```bash
./gradlew assembleDebug
```

Output APK location:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 How to Install and Set Up on Your Mobile Phone

Since FocusBlock is a standalone application distributed directly via APK (not via the Google Play Store), follow these steps to install and configure it on any Android device running Android 8.0 (Oreo / API 26) or higher.

### Step 1: Transfer the APK to Your Phone

Choose any convenient method:
- **USB Cable**: Connect your phone to your PC via USB, choose *File Transfer* / *MTP* mode, and copy `app-debug.apk` into your phone's `Downloads` folder.
- **Messaging / Cloud / Email**: Send the APK file to yourself via Telegram, WhatsApp, Google Drive, OneDrive, or local sharing (e.g. Quick Share / Nearby Share).
- **Direct ADB Install (Developer option)**: If USB Debugging is enabled on your phone:
  ```powershell
  adb install -r app/build/outputs/apk/debug/app-debug.apk
  ```

---

### Step 2: Install the APK on Your Android Device

1. Open your phone's **Files** or **Downloads** app.
2. Tap on `app-debug.apk`.
3. If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings**.
   - Toggle **Allow from this source** to ON.
   - Tap the Back button.
4. Tap **Install** and then tap **Open**.

> **Note on Play Protect / "Unsafe App Blocked"**: Because this APK is self-built and not signed by Google Play, Google Play Protect may show a warning saying *"Blocked by Play Protect"* or *"Unrecognized app"*. Tap **More details** and then select **Install anyway**.

---

### Step 3: Grant Required Permissions

For FocusBlock to detect when a blocked app opens and immediately display the focus screen, Android requires specific system permissions. When you first open the app, tap through the onboarding and go to the **Permissions** screen:

1. **Accessibility Access (Primary & Most Important)**:
   - Tap **Fix** next to *Accessibility Access*.
   - In your phone's Accessibility settings, scroll to *Downloaded apps* / *Installed services*.
   - Tap **FocusBlock App Blocker** and toggle the switch to **ON**.
   - Tap **Allow** when the system prompt appears.
   - *(On Android 13+: If Android shows "Restricted setting", go to phone Settings > Apps > FocusBlock > tap the 3 dots in the top right > Allow restricted settings, then enable Accessibility).*

2. **Usage Access (Fallback Detection)**:
   - Tap **Fix** next to *Usage Access*.
   - Locate **FocusBlock** in the list and toggle **Permit usage access** to ON.

3. **Notifications**:
   - Tap **Fix** to allow FocusBlock to post an ongoing notification showing your remaining focus time.

4. **Battery Optimization Exemption**:
   - Tap **Fix** and choose **Allow / Don't optimize** so Android's battery manager won't kill FocusBlock while running in the background.

---

### Step 4: Using FocusBlock

1. **Choose Apps to Block**:
   - Tap **Manage Apps** (or the **Apps** tab at the bottom).
   - Toggle ON any apps you want to restrict (social media, games, streaming, etc.).
   - Tap **Save**.
2. **Start a Focus Session**:
   - On the Home screen, tap **Start Blocking**.
   - Select a duration (e.g., 15 min, 30 min, 1 hour, 2 hours, 4 hours) or pick a custom duration using the sliders.
   - Tap **Start Blocking**.
3. **What happens during a session**:
   - If you attempt to launch any blocked app, the **Focus Time** screen will immediately appear, preventing access.
   - Tapping "Go Back" takes you back to your home launcher.
   - An ongoing notification keeps you updated on how much time is left.
   - When the countdown finishes, all apps are automatically unlocked and accessible again.
4. **Setting Up Schedules**:
   - Open the **Schedule** tab.
   - Tap the **+** button to add recurring blocks (e.g. Work hours 09:00 - 17:00 on weekdays, or bedtime schedules that cross midnight).

---

## 🔒 Important Disclaimers & Limitations
- FocusBlock is built strictly using official Android APIs and is designed as a **self-discipline and productivity companion**.
- Because it does not run as a device administrator or MDM (Mobile Device Management) kiosk, a user can bypass blocking by force-stopping the app, revoking accessibility permissions in Android Settings, or uninstalling the app.
- **Privacy First**: FocusBlock has no internet permissions, collects zero analytics, and never transmits your data anywhere. All settings and history remain 100% on your device.
