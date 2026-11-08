# FocusFlow — Design Notes

## Product intent

FocusFlow is a calm, local-first productivity app. The visual reference is a lavender and
purple mobile concept (`reference.png`) with soft cards, generous rounded corners, a floating
pill navigation bar, and an oversized circular focus timer.

The app deliberately avoids accounts, cloud sync, social features, XP/levels, and focus
soundscapes. Those are outside the requested scope and would weaken the offline-first,
low-friction experience.

## Design system

| Token | Value |
| --- | --- |
| Background | `#FBF4FD` with a subtle vertical lavender gradient |
| Primary | `#6C4CF1` |
| Indigo text/accent | `#2E2459` |
| Lavender surface | `#D8CCFA` |
| Peach | `#FFB877` |
| Mint | `#3FCB92` |
| Pink | `#FF7A9E` |
| Danger | `#E5484D` |
| Cards | white/translucent, 24–28dp corner radius |
| Buttons | 18–50dp radius, full-width primary actions |
| Navigation | floating pill with four tabs: Home, Stats, Habits, More |

- Typography uses bundled Poppins (regular, medium, semibold, bold).
- Icons come from Material Icons Extended through `IconCatalog`, which maps stable string
  keys stored in the database to vector icons.
- Charts are custom Compose Canvas components (`FocusRing`, `MiniBarChart`, `PomodoroDots`,
  `HabitHeatmap`, `WeekBars`) so no charting dependency is required.
- Dark mode uses a deep indigo background with lavender/peach accents.

## Information architecture

| Tab/route | Purpose |
| --- | --- |
| Home | Daily overview, focus launch, tasks, habits, recent sessions |
| Stats | Period-based focus analytics and distraction summary |
| Habits | Habit checklist and consistency |
| More | Plan and app settings hub |
| Tasks | Full task management with filters and search |
| Timer | Active focus/break experience |
| Weekly review | Reflection and weekly comparison |
| Reminders | Custom, task, and habit reminders |
| Archive | Restore or permanently delete archived items |
| Categories | Manage task categories |
| Settings | Profile, appearance, focus defaults, data |

## Timer state machine

- Sessions are `FOCUS`, `SHORT_BREAK`, or `LONG_BREAK`.
- Elapsed work time is derived from `SystemClock.elapsedRealtime()` deltas accumulated in
  `TimerManager`; a wall-clock anchor is stored as a reboot fallback.
- A focus session may be paused. Paused time is excluded from elapsed time and recorded as
  gaps between `FocusSegmentEntity` rows.
- Completing focus increments `focusCycleCount`; after N sessions (per-task or default) the
  next break is a long break. Completing a long break resets the cycle count.
- Ending a focus session manually saves it as `INTERRUPTED` when at least 60 seconds were
  focused; shorter runs are `ABANDONED`.
- The active session is persisted to the single-row `active_timer` table so a process restart
  can restore it. `TimerService` is a `specialUse` foreground service with an ongoing
  notification and pause/resume/end actions.

## Notifications and reminders

- `NotificationHelper` owns channels for the timer and reminders.
- `ReminderScheduler` prefers exact alarms when `AlarmManager.canScheduleExactAlarms()` is
  true and falls back to inexact alarms otherwise. Alarms survive reboot and app updates via
  `BootReceiver`.
- Task reminders use the task's `reminderMinutes` on scheduled days; habits use their own
  reminder settings. Custom reminders store a title, message, time, and day mask.
- If notification permission is denied, the timer still runs; only alerts are suppressed.

## Data and privacy

- All data stays on-device. JSON backup is explicit and schema-versioned.
- Import replaces the local data set inside a Room transaction after basic validation
  (schema version, record-count limit, bounded file size).
- “Clear all data” wipes every table, resets preferences (returning to onboarding), and
  reseeds default categories and reminders.
