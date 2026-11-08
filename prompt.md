PROJECT: FocusFlow

Personal Productivity, Habit Tracking and Time Management Application

You are an autonomous senior mobile application developer, UI/UX designer, software architect and QA engineer.

Your task is to design, implement, test and document a complete personal productivity application called FocusFlow.

This is not a generic to-do list application. It is a personal productivity system designed to help users regain control of their time, build meaningful habits, reduce distractions and consistently invest time in activities that contribute to their long-term goals.

You are expected to implement the actual application, not simply produce designs, recommendations or a development plan.

---

1. PROJECT VISION

The application should solve a specific problem:

People often spend hours doing things they did not intend to do, such as doom-scrolling YouTube, X (Twitter), Instagram and other social media platforms.

Meanwhile, important activities such as studying, programming, exercising and learning new skills get neglected.

FocusFlow should help users:

1. Become intentional about how they spend their time.
2. Allocate time to activities that matter.
3. Track how much time they genuinely invest in each activity.
4. Build consistent daily habits.
5. Understand their productivity through meaningful statistics.
6. Recognize distractions and gradually reduce unproductive behavior.
7. See long-term progress rather than focusing exclusively on daily achievements.

The application must encourage consistency without making users feel guilty when they miss a day.

The philosophy is:

Small consistent improvements are more valuable than occasional bursts of productivity.

---

2. DEVELOPMENT WORKFLOW AND GIT REQUIREMENTS

These instructions are mandatory.

2.1 Use Git

Git must be used throughout the project.

Before changing anything:

1. Inspect the current working directory.
2. Check whether the directory is already a Git repository.
3. Inspect the existing project structure.
4. Run "git status".
5. Inspect existing branches and recent commits.
6. Identify existing changes that must be preserved.

Do not overwrite existing work or discard uncommitted changes.

If a repository does not exist, initialize one.

Create an appropriate feature branch if compatible with the existing repository workflow.

Use meaningful commits at logical implementation milestones.

Suggested commit sequence:

- "chore: initialize application structure"
- "feat: implement application design system"
- "feat: implement dashboard and task management"
- "feat: implement pomodoro focus timer"
- "feat: implement habit tracking"
- "feat: implement productivity statistics"
- "feat: implement notifications and settings"
- "test: add application tests"
- "docs: add project documentation"

Do not create meaningless commits merely to satisfy this list.

Never commit credentials, API keys, passwords or other secrets.

2.2 Mandatory Todo Tracking

You MUST use your available Todo tool to maintain a persistent implementation checklist.

If your environment does not provide a Todo tool, create a "TODO.md" file in the project root and use it as the implementation tracker.

Break the project into small, independently implementable features.

For every feature, maintain one of these states:

- Pending
- In Progress
- Completed
- Blocked

Todo management rules

1. Create the initial Todo list before implementation begins.
2. Include all major features, subtasks, testing and documentation.
3. Mark a task In Progress when you begin implementing it.
4. Mark a task Completed immediately after its implementation and relevant verification.
5. If implementation fails, do not falsely mark it Completed.
6. Add newly discovered requirements or necessary subtasks to the Todo list.
7. Update the Todo list as you implement features, not only at the end.
8. Do not leave completed tasks marked In Progress.
9. Do not mark the entire project Completed while any required feature remains unfinished.

At the beginning of each major development phase, review the Todo list.

At the end of each phase, update the Todo list and Git repository.

The Todo list must accurately reflect the actual implementation status at all times.

2.3 Autonomous Development

Do not stop after producing a development plan.

Do not ask for approval after every implementation phase.

Make sensible engineering decisions independently.

When you encounter missing information, use reasonable defaults and document your assumptions.

When something cannot be implemented because of a genuine technical limitation, explain the limitation and implement the best practical alternative.

Your objective is to deliver a functional application.

---

3. VISUAL REFERENCE REQUIREMENTS

IMPORTANT: reference.png

A combined visual reference image named:

"reference.png"

has been provided in the project root.

You MUST inspect and use this image as the primary visual reference for the entire application.

Do not ignore it.

Do not replace it with an unrelated design generated from your own imagination.

Before implementing the UI:

1. Locate "reference.png".
2. Open and visually inspect the complete image.
3. Analyze its color palette.
4. Analyze its typography.
5. Analyze its card shapes and corner radii.
6. Examine spacing, shadows and visual hierarchy.
7. Study its navigation patterns.
8. Identify recurring UI components.
9. Identify the characteristics of each individual screen.
10. Extract a consistent design language from the combined references.

If the file cannot be found, report that immediately instead of pretending to have inspected it. Continue with a documented temporary design direction only if the image is genuinely unavailable.

3.1 How to use the reference

The image combines different UI concepts.

Use their strengths together rather than copying every screen literally.

Habit tracker inspiration

Take inspiration from:

- Lime-green and soft-green backgrounds.
- Rounded phone interfaces.
- Soft, spacious cards.
- Black and green contrast.
- Minimal, illustrated elements.
- Simple progress indicators.

Learning dashboard inspiration

Take inspiration from:

- Pastel backgrounds.
- Friendly interface components.
- Large typography.
- Colorful progress visualizations.
- Learning-oriented dashboards.
- Rounded cards and sections.
- Attractive statistics.

Bill-splitting interface inspiration

Take inspiration from:

- Deep purple backgrounds.
- Layered cards.
- Strong foreground/background contrast.
- Soft shadows.
- Rounded controls.
- Carefully selected accent colors.

Combined FocusFlow concept

Use the combined reference to guide the overall layout, proportions, component style and visual personality.

The objective is to create an original, cohesive application inspired by the image.

Do not simply recreate the image as a static screen.

3.2 Reference-driven implementation

Create a design specification from "reference.png" before developing the main screens.

The specification should document:

- Primary colors.
- Secondary colors.
- Background colors.
- Text colors.
- Accent colors.
- Typography hierarchy.
- Component dimensions and spacing.
- Card styling.
- Button styling.
- Icon style.
- Navigation appearance.
- Chart appearance.
- Light and dark theme behavior.

Use the specification to create reusable design tokens and components.

When implementing each screen, compare it visually against "reference.png".

Make adjustments where the result deviates unnecessarily from the reference.

Do not force components into the design if doing so compromises usability.

The reference is a visual guide, not a restriction on functional improvements.

Do not use "reference.png" as a full-screen background. Recreate its design language using actual native UI components.

---

4. TARGET PLATFORM AND ARCHITECTURE

Build a mobile-first application.

The primary target is Android, with an installable APK as a deliverable.

Prefer native Android development with:

- Kotlin.
- Jetpack Compose.
- Material 3 as a foundation, with a custom visual design system.
- Room for persistent local storage.
- Kotlin Coroutines and Flow for asynchronous operations and reactive state.
- Android notification APIs for reminders and timer notifications.
- DataStore for user preferences.

If the existing repository already uses an appropriate technology stack, inspect it before deciding whether to retain or replace it.

The application should be:

- Offline-first.
- Responsive across different Android screen sizes.
- Persistent across application restarts.
- Modular and maintainable.
- Designed so additional features can be introduced without restructuring the entire project.

Avoid unnecessary backend infrastructure.

A personal productivity application should not require an account or internet connection for its primary functionality.

Do not introduce large dependencies without a clear reason.

---

5. APPLICATION NAVIGATION

Use a bottom navigation bar with four primary sections:

1. Home
2. Statistics
3. Habits
4. Settings

Task management should be accessible from Home and through an appropriate task screen.

The Pomodoro timer should be accessible from task cards and a dedicated focus interface.

Use proper navigation architecture rather than manually switching between unrelated composables.

---

6. HOME DASHBOARD

The Home screen is the user's primary command center.

It should answer these questions immediately:

- What should I be doing today?
- How much focused time have I completed?
- What should I work on next?
- How am I progressing toward today's targets?

6.1 Header

Display:

- Greeting based on the time of day.
- User's name.
- Profile avatar.
- Current date.
- Optional notification indicator.

Example:

"Good evening, Michael."

"Friday, November 6"

6.2 Today's Focus card

Create a prominent card displaying:

- Current or recommended task.
- Task icon.
- Daily target.
- Time already completed.
- Progress percentage.
- Start Focus button.

Example:

GRAPHICS PROGRAMMING

Target: 2 hours

Completed: 45 minutes

Progress: 37.5%

6.3 Today's overview

Display summary cards:

- Total focus time.
- Completed sessions.
- Tasks completed.
- Habits completed.

These values must come from actual stored application data.

Never use hardcoded statistics in the finished application.

6.4 Today's tasks

Display a list of tasks scheduled for the day.

Each task should show:

- Name.
- Category.
- Icon.
- Target duration.
- Completed duration.
- Progress indicator.
- Start timer button.

Allow users to reorder tasks.

Provide quick access to create a new task.

6.5 Daily progress

Include a visual representation of progress throughout the day.

For example, show how much of the planned focus time has been completed.

---

7. TASK MANAGEMENT

Tasks represent activities that require time and attention.

A user should be able to create tasks such as:

- Graphics Programming.
- Japanese.
- C++.
- Mathematics.
- Reading.
- Music Production.
- Exercise.

7.1 Task properties

Each task should support:

- Unique ID.
- Name.
- Description.
- Category.
- Icon.
- Accent color.
- Daily target duration.
- Weekly target duration.
- Optional yearly target.
- Priority.
- Creation date.
- Active/inactive status.
- Scheduled days.
- Optional reminder.
- Associated Pomodoro configuration.

7.2 Categories

Allow tasks to be grouped into categories.

Suggested default categories:

- Programming.
- Languages.
- Education.
- Personal Development.
- Health.
- Creativity.
- Other.

Users must be able to create custom categories.

7.3 Task operations

Implement:

- Create task.
- Edit task.
- Delete task.
- Archive task.
- Activate/deactivate task.
- Change task color and icon.
- Change daily target.
- Change weekly target.
- View task history.
- View task-specific analytics.

Deleting a task must not silently destroy historical productivity records.

Use an appropriate archival or soft-deletion strategy.

---

8. POMODORO FOCUS TIMER

This is one of the most important application features.

Each task must have its own Pomodoro configuration.

8.1 Timer configuration

Provide sensible defaults:

- Focus: 25 minutes.
- Short break: 5 minutes.
- Long break: 15 minutes.
- Long break after 4 completed focus sessions.

Allow users to customize these values.

Users should also be able to select:

- 25/5.
- 50/10.
- Custom durations.

8.2 Focus interface

Create an attractive, distraction-free timer interface.

Display:

- Large countdown.
- Selected task.
- Current session type.
- Progress ring.
- Pause button.
- Resume button.
- Finish button.
- Cancel button.

Use animations sparingly.

The interface should visually communicate focus without overwhelming the user.

8.3 Timer behavior

The timer must:

1. Start correctly.
2. Pause correctly.
3. Resume correctly.
4. Stop correctly.
5. Handle application backgrounding.
6. Handle application process termination.
7. Recover correctly after reopening.
8. Send appropriate notifications.
9. Record completed sessions.
10. Distinguish completed, interrupted and abandoned sessions.

IMPORTANT:

Do not implement timer accuracy using a simple counter that assumes the application remains open.

Use reliable time-based calculations.

Persist the session's start time, pause state, accumulated elapsed time and other required state.

Use an appropriate monotonic time source for elapsed-time calculations and handle device clock changes carefully.

Background execution should not depend on keeping the UI process alive unnecessarily.

8.4 Session history

Every focus session should record:

- Task ID.
- Start time.
- End time.
- Duration.
- Session type.
- Completion status.
- Optional user note.

Only actual focus time should count toward productivity statistics.

Break time must not be included in focused work totals.

If the user abandons a session, record the actual elapsed focus time appropriately.

Do not count an entire 25-minute session when the user worked for only 8 minutes.

8.5 Focus insights

Display:

- Completed Pomodoros.
- Total focus time.
- Average session length.
- Most productive task.
- Longest completed session.

---

9. HABIT TRACKING

Habits are different from tasks.

A task represents an activity the user wants to spend time doing.

A habit represents a behavior the user wants to perform consistently.

Examples:

- Read every day.
- Drink enough water.
- Exercise.
- Practice Japanese.
- Avoid social media in the morning.
- Sleep at a consistent time.

9.1 Habit properties

Each habit should support:

- Name.
- Description.
- Icon.
- Color.
- Frequency.
- Target.
- Start date.
- Reminder.
- Current streak.
- Longest streak.
- Completion history.

9.2 Frequency

Support:

- Daily.
- Specific days of the week.
- Weekly.
- Custom schedules.

9.3 Habit completion

Allow users to:

- Mark a habit complete.
- Undo completion.
- Record numerical progress.
- Add optional notes.

Examples:

Japanese:

Target: 30 minutes.

Completed: 30 minutes.

Reading:

Target: 20 pages.

Completed: 12 pages.

Drinking water:

Target: 8 glasses.

Completed: 5 glasses.

9.4 Streaks

Show:

- Current streak.
- Longest streak.
- Completion calendar.
- Weekly consistency.
- Monthly consistency.

IMPORTANT:

Do not make streaks the only measure of success.

Missing one day should not make the application feel punishing.

Show consistency percentages and historical achievements alongside streaks.

---

10. PRODUCTIVITY STATISTICS

Statistics are a major part of FocusFlow.

The user should be able to understand exactly where their time is going.

Create a dedicated Statistics screen.

10.1 Time filters

Support:

- Today.
- This week.
- This month.
- This year.
- Custom date range.

10.2 Main statistics

Display:

- Total focused hours.
- Number of completed sessions.
- Average daily focus time.
- Most productive day.
- Most productive task.
- Total tasks completed.
- Habit completion percentage.

10.3 Weekly statistics

Create a bar chart displaying focused hours per day.

Example:

Monday: 2h 30m

Tuesday: 1h 45m

Wednesday: 3h 10m

Thursday: 2h 00m

Friday: 4h 00m

Saturday: 1h 30m

Sunday: 2h 15m

These are example values only.

The chart must use actual session records.

10.4 Monthly statistics

Display:

- Total hours.
- Daily averages.
- Total focus sessions.
- Task distribution.
- Habit consistency.
- Comparison with the previous month.

10.5 Yearly statistics

Provide a yearly overview.

Display:

- Total hours invested.
- Total sessions.
- Monthly focus-hour distribution.
- Most productive month.
- Most practiced task.
- Habit consistency.
- Monthly comparison.

Include a calendar heatmap representing daily productivity.

The heatmap should visually communicate how consistently the user invested time throughout the year.

10.6 Task-specific statistics

Selecting a task should reveal its own analytics.

For example:

GRAPHICS PROGRAMMING

This week: 8h 30m

This month: 32h 15m

This year: 125h 40m

Completed sessions: 64

Daily average: 1h 10m

Weekly target: 10 hours

Progress: 85%

Include a historical chart.

Allow switching between weekly, monthly and yearly views.

10.7 Comparisons

Provide meaningful comparisons:

- This week versus last week.
- This month versus last month.
- This year versus last year.

Show percentage changes where the comparison is mathematically meaningful.

Never treat a percentage increase from zero as an ordinary finite percentage.

---

11. DISTRACTION MANAGEMENT

FocusFlow should help users become conscious of time wasted on distractions.

This functionality should be useful without making the application invasive.

11.1 Quick distraction button

Add an easily accessible action:

"I'm about to scroll."

When pressed:

1. Show a short intervention screen.
2. Ask the user what they intended to accomplish.
3. Offer alternative actions.
4. Let the user start a short focus session.
5. Allow them to dismiss the intervention.

Example alternatives:

- Study Japanese for 5 minutes.
- Work on graphics programming.
- Read something.
- Take a proper break.
- Continue with my planned task.

The interface should not shame the user.

11.2 Distraction logging

Allow users to manually record distractions.

Properties:

- Application or activity.
- Start time.
- End time.
- Duration.
- Optional note.

11.3 Optional Android usage monitoring

Consider an optional future feature using Android Usage Access permissions.

This could help users understand how much time they spend in selected applications.

However:

- It must be optional.
- Explain permissions clearly.
- Do not silently monitor application usage.
- Do not collect unnecessary personal information.
- Do not transmit usage data to external servers.
- Keep monitoring functionality separate from the core application.

Do not make this a blocker for the initial release.

---

12. NOTIFICATIONS AND REMINDERS

Implement configurable reminders.

Examples:

- Time to practice Japanese.
- Your graphics programming target is incomplete.
- Time for a short break.
- Review today's progress.
- Prepare your tasks for tomorrow.

Allow users to:

- Enable/disable notifications.
- Set reminder times.
- Select reminder days.
- Customize reminder messages.

Avoid excessive notifications.

Respect Android notification permissions.

---

13. WEEKLY REVIEW

Implement a dedicated weekly review.

The review should summarize:

- Total focus time.
- Focus time per task.
- Completed Pomodoros.
- Habit completion.
- Most productive day.
- Least productive day.
- Progress toward weekly goals.
- Changes compared with the previous week.

Provide a small insight based on actual data.

Example:

"You spent 20% more time on graphics programming this week."

Or:

"Japanese was your most consistent activity this week."

The insight engine should use actual stored records.

Do not generate fabricated observations.

Allow users to record a reflection:

"What went well?"

"What needs improvement?"

"What should I focus on next week?"

---

14. SETTINGS AND PERSONALIZATION

Implement settings for:

- Username.
- Profile image/avatar.
- Theme.
- Light mode.
- Dark mode.
- System theme.
- Default Pomodoro durations.
- Notification preferences.
- Start-of-week preference.
- Daily focus target.
- Data export.
- Data backup.
- Application information.

14.1 Data export

Allow users to export their productivity records.

At minimum support JSON export.

CSV export is also desirable for session history and statistics.

The user should own their data.

---

15. DATA STORAGE AND DATA MODEL

Design the database before implementing dependent features.

Use a normalized and maintainable schema.

Potential entities include:

Task

Represents an activity.

Category

Groups related tasks.

FocusSession

Records actual Pomodoro sessions.

Habit

Represents a recurring behavior.

HabitCompletion

Records individual habit completions.

Distraction

Records manually logged distractions.

DailyReflection

Stores weekly or daily reflections.

UserPreferences

Stores application settings.

Reminder

Stores reminder configuration.

Define appropriate:

- Primary keys.
- Foreign keys.
- Indexes.
- Relationships.
- Validation rules.
- Database migrations.

Persist data locally.

All statistics must be derived from stored records rather than separately maintained hardcoded counters.

Be particularly careful about:

- Time zones.
- Day boundaries.
- Week boundaries.
- Month boundaries.
- Year boundaries.
- Daylight-saving changes where applicable.
- Sessions crossing midnight.
- Incomplete sessions.

Use consistent date/time handling throughout the application.

---

16. USER EXPERIENCE REQUIREMENTS

The application must be pleasant to use.

Implement:

- Smooth navigation.
- Responsive layouts.
- Loading states.
- Empty states.
- Confirmation dialogs for destructive operations.
- Helpful validation messages.
- Undo where appropriate.
- Accessibility support.
- Proper keyboard handling.
- Touch-friendly controls.

Do not overload the user with too much information.

Prioritize the most important action on each screen.

For example, the Home screen should make starting a focus session immediately obvious.

Avoid unnecessary gamification.

Optional achievements are acceptable, but productivity statistics and meaningful progress should remain the primary motivation.

---

17. IMPLEMENTATION PRIORITIES

Implement the application in the following order.

Phase 1: Foundation

- Inspect repository.
- Locate and inspect "reference.png".
- Extract design specifications from the reference.
- Create Todo list.
- Initialize or verify Git.
- Establish project architecture.
- Configure dependencies.
- Create theme and design system.
- Implement application navigation.
- Implement persistent database.

Phase 2: Core Productivity

- Home dashboard.
- Task creation and management.
- Task categories.
- Pomodoro configuration.
- Focus timer.
- Session persistence.
- Session history.

Phase 3: Habits

- Habit creation.
- Habit scheduling.
- Habit completion.
- Streak calculations.
- Habit history.
- Habit dashboard.

Phase 4: Statistics

- Daily statistics.
- Weekly statistics.
- Monthly statistics.
- Yearly statistics.
- Task-specific statistics.
- Calendar heatmap.
- Historical comparisons.

Phase 5: Additional Features

- Distraction intervention.
- Manual distraction logging.
- Notifications.
- Weekly review.
- Reflections.
- Data export.
- Personalization settings.

Phase 6: Testing and Polish

- Unit tests.
- Database tests.
- Timer tests.
- Statistics calculation tests.
- UI tests.
- Navigation tests.
- Edge-case testing.
- Performance testing.
- Visual refinement against "reference.png".
- Bug fixes.

Phase 7: Release

- Verify release configuration.
- Build installable APK.
- Verify APK installation where possible.
- Document build instructions.
- Document implemented functionality.
- Update Todo list.
- Commit final changes.

Do not skip a phase simply because its UI has been created.

A feature is not completed until its underlying functionality works.

---

18. TESTING REQUIREMENTS

Write tests for important application behavior.

Pay particular attention to:

Timer tests

- Starting a session.
- Pausing.
- Resuming.
- Completing.
- Abandoning.
- Recovering after application restart.
- Handling sessions crossing midnight.

Statistics tests

- Summing focus durations.
- Calculating daily totals.
- Calculating weekly totals.
- Calculating monthly totals.
- Calculating yearly totals.
- Grouping sessions by task.
- Comparing periods correctly.
- Handling empty datasets.

Habit tests

- Daily completion.
- Weekly schedules.
- Streak calculation.
- Missed days.
- Time-zone boundaries.
- Editing existing habits.

Database tests

- Saving records.
- Reading records.
- Updating records.
- Deleting or archiving records.
- Preserving historical records.

Do not assume that a successful compilation means the application works.

Run the available tests after each relevant implementation phase.

Fix discovered bugs before proceeding when they affect dependent features.

---

19. CODE QUALITY

Follow proper software engineering practices.

Requirements:

- Separate UI from business logic.
- Use a maintainable architecture.
- Avoid giant composable functions.
- Avoid duplicated business logic.
- Use dependency injection where appropriate.
- Handle errors explicitly.
- Validate user input.
- Keep database operations out of UI components.
- Use appropriate state management.
- Make important calculations independently testable.
- Avoid unnecessary abstractions.

Use meaningful naming conventions.

Document complicated logic.

Do not leave placeholder implementations pretending to be working features.

Do not fill the project with TODO comments as a substitute for implementation.

---

20. IMPORTANT PRODUCT DECISIONS

Follow these principles throughout implementation.

1. Local-first: The application must remain fully useful without internet access.
2. Privacy: Personal productivity data stays on the device.
3. Accuracy: Time-tracking records must be trustworthy.
4. Simplicity: The primary actions must be easy to reach.
5. Consistency: The design must remain cohesive.
6. Flexibility: Users must be able to create their own tasks and habits.
7. Historical integrity: Past productivity records should not disappear when tasks are edited or archived.
8. Sustainability: The application should encourage realistic long-term habits.
9. Transparency: Users should understand how their statistics are calculated.
10. Reliability: Timer and database behavior must be dependable.

---

21. DEFINITION OF DONE

The project is not considered complete until all of the following are satisfied:

- [ ] Application builds successfully.
- [ ] Installable Android APK is produced.
- [ ] "reference.png" has been inspected and used as the visual design reference.
- [ ] Design system is consistent with the reference.
- [ ] Home dashboard works.
- [ ] Users can create, edit and archive tasks.
- [ ] Users can create categories.
- [ ] Pomodoro timer works correctly.
- [ ] Timer survives application backgrounding and restart.
- [ ] Focus sessions are persisted.
- [ ] Habit tracking works.
- [ ] Habit history is persisted.
- [ ] Streak calculations work.
- [ ] Daily statistics work.
- [ ] Weekly statistics work.
- [ ] Monthly statistics work.
- [ ] Yearly statistics work.
- [ ] Task-specific statistics work.
- [ ] Statistics use actual database records.
- [ ] Distraction intervention works.
- [ ] Notifications work where permissions allow.
- [ ] Data export works.
- [ ] Application settings work.
- [ ] Important business logic has tests.
- [ ] UI has been visually reviewed against "reference.png".
- [ ] No major known crashes remain.
- [ ] Todo list accurately reflects the final state.
- [ ] Git history contains meaningful implementation commits.
- [ ] Documentation is available.

If a feature cannot be completed, explicitly document its status and reason. Never falsely claim it works.

---

22. FINAL DELIVERABLES

At completion, provide:

1. The complete application source code.
2. The installable APK.
3. The updated Todo checklist.
4. A README containing project information.
5. Build and installation instructions.
6. A description of the application architecture.
7. Database schema documentation.
8. A list of implemented features.
9. A list of known limitations.
10. Test results.
11. Git branch and commit information.

Also provide a concise implementation report explaining what was actually built.

---

FINAL INSTRUCTION

Start by inspecting the repository, available development tools and supplied visual references.

Locate and inspect "reference.png" before implementing the UI.

Create the Todo checklist before implementation.

Then begin building FocusFlow.

Use Git throughout development.

Update the Todo checklist continuously as features are implemented and verified.

Regularly compare the implemented screens against "reference.png".

Do not stop after creating the architecture, writing documentation or producing static UI mockups.

Build the working application. Test it. Refine it. Produce the APK.
