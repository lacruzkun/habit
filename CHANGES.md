# FocusFlow visual redesign

## Updated files

- `README.md` — documents the Home / Stats / Habits / Settings navigation and where secondary planning tools live.
- `app/src/main/java/com/focusflow/app/ui/AppRoot.kt` — changes the fourth bottom-navigation destination from More to Settings, adds the Settings icon, and refines the floating navigation surface.
- `app/src/main/java/com/focusflow/app/ui/components/Common.kt` — increases card rounding, refines shadows and borders, and keeps card styling compatible with light and dark themes.
- `app/src/main/java/com/focusflow/app/ui/screens/home/HomeScreen.kt` — adds per-task daily-goal progress indicators using the progress already calculated by the home view model.
- `app/src/main/java/com/focusflow/app/ui/screens/more/MoreScreen.kt` — renames the secondary utility destination to “More tools” so it is distinct from the Settings tab.
- `app/src/main/java/com/focusflow/app/ui/screens/settings/SettingsScreen.kt` — reshapes the Settings destination into the reference-style “More” hub, adds a profile summary card, improves avatar selection, and links to secondary planning tools while preserving all existing settings and data actions.
- `app/src/main/java/com/focusflow/app/ui/theme/Color.kt` — tunes the lavender/purple palette and light background gradient to better match the reference.
- `app/src/main/java/com/focusflow/app/ui/theme/Theme.kt` — adjusts the light surface and outline colors to harmonize with the refreshed palette.

## Existing functionality preserved

The changes are visual/navigation-focused. Room entities and DAOs, DataStore preferences, MVVM state flows, Pomodoro timer/service logic, reminders, task and habit CRUD, charts, export/import, and destructive-action confirmations were left intact.

## Verification

- Kotlin source parsing: no syntax diagnostics were reported by the local Kotlin compiler.
- Android debug build: not completed. The Gradle wrapper needs Gradle 8.11.1, but this environment could not reach `services.gradle.org` (`UnknownHostException`). Run `./gradlew :app:assembleDebug` in an Android development environment with the Gradle distribution and Android SDK available.
