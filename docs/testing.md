# FocusFlow — Testing

## Automated tests

Run all JVM unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

Coverage:

| Area | Test |
| --- | --- |
| Timer elapsed time, pause, reboot fallback, remaining time, config | `domain/timer/TimerMathTest` |
| Daily focus aggregation, midnight splitting, focus rate, task totals, month buckets, goal comparisons | `domain/stats/FocusStatsCalculatorTest` |
| Daily streaks, missing-today grace, partial values, weekly-count habits, weekday schedules, weekly completion counts | `domain/habits/HabitCalculatorTest` |
| Weekly goal, best/quietest day, insight generation | `domain/review/WeeklyReviewCalculatorTest` |
| Room CRUD, task archiving, cascade deletes, segment overlap, habit completion upsert, weekly reflection upsert | `data/db/FocusFlowDatabaseTest` |
| Shared Compose components render and handle clicks (Robolectric) | `ui/components/CommonComponentsTest` |
| App launch on Robolectric | `AppSmokeTest` |

Debug builds also exercise Room schema export (`app/schemas/`) via KSP.

## Manual device checklist

Because the timer and reminder behavior depends on the OS, verify on a physical device:

1. Fresh install → onboarding completes and Home opens.
2. Create a task with a category, schedule, target, and reminder; verify it appears today.
3. Start focus from Home and from a task; background the app and confirm the countdown
   continues from the notification.
4. Pause/resume, then end early; check the session appears as interrupted (or abandoned under
   one minute).
5. Let a focus session complete; verify a break starts automatically and a long break arrives
   after the configured number of cycles.
6. Log a distraction with and without pausing the timer.
7. Create each habit frequency; toggle, use numeric progress, and verify streaks/heatmap.
8. Check the Stats periods and weekly review against the recorded sessions.
9. Trigger a task/habit/custom reminder; reboot the device and confirm alarms are restored.
10. Export JSON, clear all data, import the JSON, and verify data returns.
11. Export CSV and confirm it opens in a spreadsheet app.
12. Switch light/dark/system themes and verify readability.

## Known limitations

- Exact alarm delivery depends on the `SCHEDULE_EXACT_ALARM` permission and battery
  optimization settings; inexact delivery is the fallback.
- Notification permission is requested during onboarding; declining it suppresses alerts but
  not timer tracking.
- The app is intentionally offline-only; there is no cloud backup.
