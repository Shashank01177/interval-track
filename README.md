# ⏱️ IntervalTrack (TimeSlice)

An Android application that monitors **from which time to which time** you open and use specific apps throughout the day, rather than just showing a cumulative aggregate total (e.g. "YouTube: 2 hours").

---

## 🌟 Why IntervalTrack?

Standard screen-time tools (like Google Digital Wellbeing or Apple Screen Time) only give you static summaries:
> *"You spent 2 hours on Instagram today."*

They don't tell you the vital behavioral story:
- *Did you open it once for 2 hours at night, or did you open it 25 times in short 5-minute distraction bursts?*
- *What was the exact start time and end time of each session?*
- *How long was the focus gap before you opened the app again?*

**IntervalTrack solves this:**
- **Exact Interval Log**: Records `10:15:02 AM → 10:42:30 AM (27m 28s)`.
- **24-Hour Visual Timeline (Gantt Chart)**: Visualizes all session blocks chronologically across morning, afternoon, and night.
- **Per-App Deep Dive**: Filter down to a specific app (e.g., YouTube, WhatsApp, Instagram) to view its distribution and session metrics.
- **Gap Tracking**: Tells you the exact time elapsed between sessions (e.g. *"Opened after a 2h 15m gap"*).
- **Zero Battery Drain**: Leverages Android's native `UsageStatsManager` and `UsageEvents` system log — Android OS already records foreground resume and pause events at the kernel level.
- **100% On-Device & Private**: Operates fully offline without internet permission or data tracking.

---

## 📱 Project Structure

```
friendly-euclid/
├── android/                         # Complete Native Android App (Kotlin + Jetpack Compose)
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── java/com/intervaltrack/app/
│   │   │   │   ├── data/
│   │   │   │   │   ├── model/       # AppSession, AppSummary
│   │   │   │   │   └── usage/       # UsageStatsHelper (UsageEvents parser)
│   │   │   │   ├── ui/
│   │   │   │   │   ├── components/  # TimelineBar, SessionCard, AppFilterRow, DateSelector
│   │   │   │   │   ├── screens/     # HomeScreen, AppDetailScreen, PermissionScreen
│   │   │   │   │   ├── theme/       # Dark Material 3 Palette & Typography
│   │   │   │   │   └── viewmodel/   # UsageViewModel (StateFlow & real-time updates)
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/                 # Vector launcher icons, themes, strings
│   │   │   └── AndroidManifest.xml  # PACKAGE_USAGE_STATS permission
│   │   └── build.gradle.kts
│   ├── gradle/wrapper/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   └── gradlew.bat
├── web/                             # Interactive Web Preview / Simulator (React + Vite + Tailwind)
│   ├── src/
│   │   ├── components/
│   │   ├── App.tsx
│   │   └── mockData.ts
│   └── package.json
└── .github/workflows/
    └── build-apk.yml                # Automatic APK build on push to GitHub
```

---

## 🚀 How to Build & Install the APK on Your Phone

You have **3 simple ways** to get the APK onto your Android phone:

### Option 1: Automatic 1-Click Cloud Build (Recommended & Easiest)
1. Push this repository to your GitHub account (or fork it).
2. GitHub Actions will automatically run the `.github/workflows/build-apk.yml` workflow.
3. Open GitHub on your phone's browser, go to **Actions** → click the latest workflow run → download `IntervalTrack-debug-apk`.
4. Install the `.apk` on your phone!

### Option 2: Using Android Studio
1. Open Android Studio and select **Open**, then select the `android/` folder.
2. Allow Gradle to sync.
3. Go to **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**.
4. Android Studio will generate the APK at:
   `android/app/build/outputs/apk/debug/app-debug.apk`
5. Transfer it to your phone via USB or Google Drive and install.

### Option 3: Command Line (Gradle)
If you have JDK 17+ and the Android SDK installed:
```bash
cd android
./gradlew assembleDebug
```
The output file will be in `app/build/outputs/apk/debug/app-debug.apk`.

---

## 💻 Interactive Web Simulator

You can preview and test the complete UI right in your browser right now:
```bash
cd web
npm run dev
```
Open [http://localhost:3000](http://localhost:3000) to test:
- Interactive 24-hour visual bar with hover tooltips and hour markers
- Live session pulse indicator
- Filter by specific apps (YouTube, Instagram, WhatsApp, Spotify, Chrome)
- App deep dive modal showing average session duration and longest session
- Phone frame toggle vs desktop view

---

## 🔒 Permission Setup on Android Phone

When you install the APK on your Android device:
1. Open **IntervalTrack**.
2. Tap **Grant Usage Access**.
3. Android will open **Settings → Usage Access**. Toggle **IntervalTrack** to **Allow**.
4. Return to the app — your actual app session intervals from morning to night will immediately load!
