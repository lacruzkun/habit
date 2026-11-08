# FocusFlow

FocusFlow is an offline-first personal productivity app for Android. It combines a Pomodoro
focus timer, daily task targets, habit tracking, statistics, distraction logging, weekly
reviews, reminders, and local data export.

Everything is stored on the device: Room for structured data, DataStore for preferences.
There are no accounts, network calls, analytics, or ads.

## Features

- **Onboarding** — name, avatar, and a daily focus goal.
- **Home** — today's plan, quick focus launch, habit checklist, recent sessions.
- **Focus timer** — foreground service countdown, pause/resume, automatic short and long
  breaks, cycle dots, distraction logging.
- **Tasks** — categories, icons/colors, priority, per-task daily/weekly/yearly targets,
  schedules, per-task Pomodoro settings, reminders, archive/restore/delete.
- **Statistics** — day/week/month/year totals, focus rate, daily chart, best day, task
  breakdown, and distraction time.
- **Habits** — daily, selected-weekday, or times-per-week habits with numeric targets,
  streaks, consistency, heatmap, and reminders.
- **Weekly review** — week totals vs. the previous week, task and habit summaries, insights,
  and a saved written reflection.
- **Settings** — profile, theme, week start, daily target, Pomodoro defaults, notifications,
  JSON backup/restore, CSV export, and full local data reset.
- **Reminders** — custom daily/weekly reminders plus task and habit nudges. Exact alarms are
  used when the OS permits, with an inexact fallback.

## Screens

Onboarding · Home · Focus Timer · Tasks · Task editor · Task detail · Statistics · Habits ·
Habit editor · Habit detail · Weekly review · Reminders · Archive · Categories · Settings ·
More.

The bottom navigation exposes Home, Stats, Habits, and More; plan-related screens are
reachable from More.

## Build

Requirements: JDK 17+, Android SDK platform 35, and the Android build tools.

```bash
./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. It uses the
`com.focusflow.app.debug` application id so it can be installed beside a release build.

Release build (local signing keystore is gitignored):

```bash
./gradlew :app:assembleRelease
```

Signing credentials can be overridden with `FOCUSFLOW_KEYSTORE_PASSWORD`,
`FOCUSFLOW_KEY_ALIAS`, and `FOCUSFLOW_KEY_PASSWORD`. If `keystore/focusflow-release.jks` is
missing, the release build falls back to debug signing so it remains installable.

Install on a connected device:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Tests

```bash
./gradlew :app:testDebugUnitTest
```

The suite covers timer math, focus statistics, habit streaks and consistency, weekly review
calculations, and a Robolectric launch smoke test for `MainActivity`. See
[docs/testing.md](docs/testing.md) for the manual device checklist.

## Architecture

- Single Gradle module, MVVM with `StateFlow` UI state and a manual `AppContainer`.
- Room database with DAOs; DataStore for `UserPreferences`.
- `TimerManager` owns the timer state machine and persists an active-timer snapshot so the
  countdown survives process death. `TimerService` keeps the session alive in the foreground.
- `ReminderScheduler` uses `AlarmManager`; `BootReceiver` restores alarms after reboot or app
  update.
- Compose Material 3 UI with a custom lavender theme, Canvas charts, and bundled Poppins.
- Serialization-based JSON backup with schema versioning, transactional import, and CSV
  session export.

More detail: [docs/design.md](docs/design.md), [docs/schema.md](docs/schema.md).

## Privacy

No data leaves the device unless the user explicitly exports a backup or CSV file. The app
requests notifications and exact-alarm permissions only for timer alerts and reminders.

## License

Bundled Poppins font files are licensed under the SIL Open Font License; see
`licenses/Poppins-OFL.txt`.
