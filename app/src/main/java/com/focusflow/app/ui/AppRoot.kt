package com.focusflow.app.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.SelfImprovement
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.focusflow.app.AppContainer
import com.focusflow.app.data.prefs.UserPreferences
import com.focusflow.app.ui.navigation.Routes
import com.focusflow.app.ui.screens.archive.ArchiveScreen
import com.focusflow.app.ui.screens.archive.ArchiveViewModel
import com.focusflow.app.ui.screens.categories.CategoriesScreen
import com.focusflow.app.ui.screens.categories.CategoriesViewModel
import com.focusflow.app.ui.screens.habits.HabitDetailScreen
import com.focusflow.app.ui.screens.habits.HabitDetailViewModel
import com.focusflow.app.ui.screens.habits.HabitEditorScreen
import com.focusflow.app.ui.screens.habits.HabitEditorViewModel
import com.focusflow.app.ui.screens.habits.HabitsScreen
import com.focusflow.app.ui.screens.habits.HabitsViewModel
import com.focusflow.app.ui.screens.home.HomeScreen
import com.focusflow.app.ui.screens.home.HomeViewModel
import com.focusflow.app.ui.screens.more.MoreScreen
import com.focusflow.app.ui.screens.onboarding.OnboardingScreen
import com.focusflow.app.ui.screens.onboarding.OnboardingViewModel
import com.focusflow.app.ui.screens.reminders.RemindersScreen
import com.focusflow.app.ui.screens.reminders.RemindersViewModel
import com.focusflow.app.ui.screens.review.WeeklyReviewScreen
import com.focusflow.app.ui.screens.review.WeeklyReviewViewModel
import com.focusflow.app.ui.screens.settings.SettingsScreen
import com.focusflow.app.ui.screens.settings.SettingsViewModel
import com.focusflow.app.ui.screens.stats.StatsScreen
import com.focusflow.app.ui.screens.stats.StatsViewModel
import com.focusflow.app.ui.screens.tasks.TaskDetailScreen
import com.focusflow.app.ui.screens.tasks.TaskDetailViewModel
import com.focusflow.app.ui.screens.tasks.TaskEditorScreen
import com.focusflow.app.ui.screens.tasks.TaskEditorViewModel
import com.focusflow.app.ui.screens.tasks.TasksScreen
import com.focusflow.app.ui.screens.tasks.TasksViewModel
import com.focusflow.app.ui.screens.timer.TimerScreen
import com.focusflow.app.ui.screens.timer.TimerViewModel
import com.focusflow.app.ui.components.AppBackground
import com.focusflow.app.ui.theme.FocusFlowTheme
import com.focusflow.app.ui.theme.LocalFocusColors
import androidx.lifecycle.viewmodel.compose.viewModel

private data class TabItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val tabs = listOf(
    TabItem(Routes.HOME, "Home", Icons.Rounded.Home),
    TabItem(Routes.STATS, "Stats", Icons.Rounded.BarChart),
    TabItem(Routes.HABITS, "Habits", Icons.Rounded.SelfImprovement),
    TabItem(Routes.SETTINGS, "Settings", Icons.Rounded.Settings),
)

private val fullScreenRoutes = setOf(Routes.TASK_EDITOR, Routes.HABIT_EDITOR, Routes.TIMER)

@Composable
fun AppRoot(
    container: AppContainer,
    openTimerSignal: Int = 0,
) {
    val prefsState = container.preferencesRepository.preferences
        .collectAsStateWithLifecycle(initialValue = null)
    val prefs = prefsState.value
    val factory = remember(container) { AppViewModelFactory(container) }

    if (prefs == null) {
        FocusFlowTheme {
            AppBackground(Modifier.fillMaxSize()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("FocusFlow", style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
        return
    }

    FocusFlowTheme(themeMode = prefs.themeMode) {
        if (!prefs.onboardingComplete) {
            val viewModel: OnboardingViewModel = viewModel(factory = factory)
            val state by viewModel.state.collectAsStateWithLifecycle()
            OnboardingScreen(
                state = state,
                onNameChange = viewModel::setName,
                onAvatarChange = viewModel::setAvatar,
                onTargetChange = viewModel::setDailyTarget,
                onFinish = viewModel::finish,
            )
        } else {
            FocusFlowNavHost(
                container = container,
                prefs = prefs,
                factory = factory,
                openTimerSignal = openTimerSignal,
            )
        }
    }
}

@Composable
private fun FocusFlowNavHost(
    container: AppContainer,
    prefs: UserPreferences,
    factory: AppViewModelFactory,
    openTimerSignal: Int,
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != null && currentRoute !in fullScreenRoutes
    val colors = LocalFocusColors.current
    val homeViewModel: HomeViewModel = viewModel(factory = factory)
    val statsViewModel: StatsViewModel = viewModel(factory = factory)
    val habitsViewModel: HabitsViewModel = viewModel(factory = factory)
    val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
    val homeListState = rememberLazyListState()
    val statsListState = rememberLazyListState()
    val habitsListState = rememberLazyListState()
    val settingsListState = rememberLazyListState()

    val navigateToTab: (String) -> Unit = { route ->
        if (route != currentRoute) {
            navController.navigate(route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = false
                }
                launchSingleTop = true
                restoreState = false
            }
        }
    }

    LaunchedEffect(openTimerSignal) {
        if (openTimerSignal > 0) {
            navController.navigate(Routes.timer()) { launchSingleTop = true }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                FocusBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = navigateToTab,
                )
            }
        },
    ) { padding ->
        AppBackground(Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = Routes.HOME,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showBottomBar) padding.calculateBottomPadding() else 0.dp),
                enterTransition = { fadeIn(tween(180)) },
                exitTransition = { fadeOut(tween(120)) },
                popEnterTransition = { fadeIn(tween(180)) },
                popExitTransition = { fadeOut(tween(120)) },
            ) {
                composable(Routes.HOME) {
                    val state by homeViewModel.state.collectAsStateWithLifecycle()
                    HomeScreen(
                        state = state,
                        listState = homeListState,
                        onToggleHabit = homeViewModel::toggleHabit,
                        onOpenTask = { navController.navigate(Routes.taskDetail(it)) },
                        onStartTaskFocus = { navController.navigate(Routes.timer(it)) },
                        onStartFocus = { navController.navigate(Routes.timer()) },
                        onSeeTasks = { navController.navigate(Routes.TASKS) },
                        onSeeStats = { navigateToTab(Routes.STATS) },
                        onSeeHabits = { navigateToTab(Routes.HABITS) },
                    )
                }

                composable(Routes.TASKS) {
                    val viewModel: TasksViewModel = viewModel(factory = factory)
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    TasksScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onAdd = { navController.navigate(Routes.taskEditor()) },
                        onOpenTask = { navController.navigate(Routes.taskDetail(it)) },
                        onStartFocus = { navController.navigate(Routes.timer(it)) },
                        onFilterChange = viewModel::setFilter,
                        onQueryChange = viewModel::setQuery,
                        onToggleActive = viewModel::toggleActive,
                        onArchive = viewModel::archive,
                        onRestore = viewModel::restore,
                        onDelete = viewModel::deletePermanently,
                    )
                }

                composable(
                    route = Routes.TASK_EDITOR,
                    arguments = listOf(navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }),
                ) {
                    val viewModel: TaskEditorViewModel = viewModel(factory = factory)
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    TaskEditorScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onNameChange = viewModel::setName,
                        onDescriptionChange = viewModel::setDescription,
                        onCategoryChange = viewModel::setCategory,
                        onIconChange = viewModel::setIcon,
                        onColorChange = viewModel::setColor,
                        onDailyTargetChange = viewModel::setDailyTarget,
                        onWeeklyTargetChange = viewModel::setWeeklyTarget,
                        onYearlyTargetChange = viewModel::setYearlyTarget,
                        onPriorityChange = viewModel::setPriority,
                        onActiveChange = viewModel::setActive,
                        onToggleDay = viewModel::toggleDay,
                        onAllDays = viewModel::setAllDays,
                        onWeekdays = viewModel::setWeekdays,
                        onReminderEnabledChange = viewModel::setReminderEnabled,
                        onReminderTimeChange = viewModel::setReminderMinutes,
                        onFocusMinutesChange = viewModel::setFocusMinutes,
                        onShortBreakChange = viewModel::setShortBreakMinutes,
                        onLongBreakChange = viewModel::setLongBreakMinutes,
                        onSessionsChange = viewModel::setSessionsBeforeLongBreak,
                        onSave = viewModel::save,
                        onArchive = viewModel::archive,
                        onRestore = viewModel::restore,
                        onDelete = viewModel::deletePermanently,
                    )
                    LaunchedEffect(state.saved) {
                        if (state.saved) navController.popBackStack()
                    }
                }

                composable(
                    route = Routes.TASK_DETAIL,
                    arguments = listOf(navArgument("taskId") { type = NavType.LongType }),
                ) {
                    val viewModel: TaskDetailViewModel = viewModel(factory = factory)
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    TaskDetailScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(Routes.taskEditor(state.task?.id)) },
                        onStartFocus = { navController.navigate(Routes.timer(state.task?.id)) },
                        onArchive = viewModel::archive,
                        onRestore = viewModel::restore,
                        onToggleActive = viewModel::setActive,
                        onDelete = viewModel::deletePermanently,
                        onDeleted = { navController.popBackStack() },
                    )
                }

                composable(
                    route = Routes.TIMER,
                    arguments = listOf(navArgument("taskId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }),
                ) {
                    val viewModel: TimerViewModel = viewModel(factory = factory)
                    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
                    val tick by viewModel.tick.collectAsStateWithLifecycle()
                    val taskInfo by viewModel.taskInfo.collectAsStateWithLifecycle()
                    TimerScreen(
                        timerState = timerState,
                        tick = tick,
                        taskInfo = taskInfo,
                        events = viewModel.events,
                        onBack = { navController.popBackStack() },
                        onStart = viewModel::startFocus,
                        onToggle = viewModel::toggle,
                        onEnd = viewModel::endSession,
                        onSkipBreak = viewModel::skipBreak,
                        onLogDistraction = viewModel::logDistraction,
                        onOpenTasks = { navController.navigate(Routes.TASKS) },
                    )
                }

                composable(Routes.STATS) {
                    val state by statsViewModel.state.collectAsStateWithLifecycle()
                    StatsScreen(
                        state = state,
                        listState = statsListState,
                        onPeriodChange = statsViewModel::setPeriod,
                        onSeeTasks = { navController.navigate(Routes.TASKS) },
                    )
                }

                composable(Routes.HABITS) {
                    val state by habitsViewModel.state.collectAsStateWithLifecycle()
                    HabitsScreen(
                        state = state,
                        listState = habitsListState,
                        onAdd = { navController.navigate(Routes.habitEditor()) },
                        onOpenHabit = { navController.navigate(Routes.habitDetail(it)) },
                        onToggle = habitsViewModel::toggleHabit,
                        onSetProgress = habitsViewModel::setProgress,
                    )
                }

                composable(
                    route = Routes.HABIT_EDITOR,
                    arguments = listOf(navArgument("habitId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }),
                ) {
                    val viewModel: HabitEditorViewModel = viewModel(factory = factory)
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    HabitEditorScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onNameChange = viewModel::setName,
                        onDescriptionChange = viewModel::setDescription,
                        onIconChange = viewModel::setIcon,
                        onColorChange = viewModel::setColor,
                        onFrequencyChange = viewModel::setFrequency,
                        onToggleDay = viewModel::toggleDay,
                        onAllDays = viewModel::setAllDays,
                        onWeeklyTargetChange = viewModel::setWeeklyTarget,
                        onTargetValueChange = viewModel::setTargetValue,
                        onUnitChange = viewModel::setUnit,
                        onReminderEnabledChange = viewModel::setReminderEnabled,
                        onReminderTimeChange = viewModel::setReminderMinutes,
                        onSave = viewModel::save,
                        onArchive = viewModel::archive,
                        onRestore = viewModel::restore,
                        onDelete = viewModel::deletePermanently,
                    )
                    LaunchedEffect(state.saved) {
                        if (state.saved) navController.popBackStack()
                    }
                }

                composable(
                    route = Routes.HABIT_DETAIL,
                    arguments = listOf(navArgument("habitId") { type = NavType.LongType }),
                ) {
                    val viewModel: HabitDetailViewModel = viewModel(factory = factory)
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    HabitDetailScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onEdit = { navController.navigate(Routes.habitEditor(state.habit?.id)) },
                        onToggleToday = viewModel::toggleToday,
                        onSetValue = viewModel::setTodayValue,
                        onArchive = viewModel::archive,
                        onRestore = viewModel::restore,
                        onDelete = viewModel::deletePermanently,
                        onDeleted = { navController.popBackStack() },
                    )
                }

                composable(Routes.MORE) {
                    MoreScreen(
                        prefs = prefs,
                        onSettings = { navController.navigate(Routes.SETTINGS) },
                        onReminders = { navController.navigate(Routes.REMINDERS) },
                        onWeeklyReview = { navController.navigate(Routes.WEEKLY_REVIEW) },
                        onArchive = { navController.navigate(Routes.ARCHIVE) },
                        onCategories = { navController.navigate(Routes.CATEGORIES) },
                        onTasks = { navController.navigate(Routes.TASKS) },
                    )
                }

                composable(Routes.SETTINGS) {
                    val state by settingsViewModel.state.collectAsStateWithLifecycle()
                    val busy by settingsViewModel.busy.collectAsStateWithLifecycle()
                    SettingsScreen(
                        state = state,
                        listState = settingsListState,
                        busy = busy,
                        messages = settingsViewModel.messages,
                        onBack = null,
                        onMoreTools = { navController.navigate(Routes.MORE) },
                        onNameChange = settingsViewModel::setUserName,
                        onAvatarChange = settingsViewModel::setAvatar,
                        onThemeChange = settingsViewModel::setTheme,
                        onWeekStartChange = settingsViewModel::setWeekStart,
                        onDailyTargetChange = settingsViewModel::setDailyTarget,
                        onPomodoroChange = settingsViewModel::setPomodoroDefaults,
                        onNotificationsChange = settingsViewModel::setNotificationsEnabled,
                        onExportJson = settingsViewModel::exportJson,
                        onExportCsv = settingsViewModel::exportCsv,
                        onImportJson = settingsViewModel::importJson,
                        onClearData = settingsViewModel::clearAllData,
                    )
                }

                composable(Routes.REMINDERS) {
                    val viewModel: RemindersViewModel = viewModel(factory = factory)
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    RemindersScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onCreate = viewModel::createCustom,
                        onUpdate = viewModel::updateCustom,
                        onDelete = viewModel::deleteCustom,
                        onToggleCustom = viewModel::setCustomEnabled,
                        onToggleTask = viewModel::setTaskReminder,
                        onTaskTime = viewModel::setTaskReminderTime,
                        onToggleHabit = viewModel::setHabitReminder,
                        onHabitTime = viewModel::setHabitReminderTime,
                        onEnableNotifications = viewModel::setNotificationsEnabled,
                    )
                }

                composable(Routes.WEEKLY_REVIEW) {
                    val viewModel: WeeklyReviewViewModel = viewModel(factory = factory)
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    WeeklyReviewScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onSaveReflection = viewModel::saveReflection,
                    )
                }

                composable(Routes.ARCHIVE) {
                    val viewModel: ArchiveViewModel = viewModel(factory = factory)
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    ArchiveScreen(
                        state = state,
                        onBack = { navController.popBackStack() },
                        onRestoreTask = viewModel::restoreTask,
                        onDeleteTask = viewModel::deleteTask,
                        onRestoreHabit = viewModel::restoreHabit,
                        onDeleteHabit = viewModel::deleteHabit,
                    )
                }

                composable(Routes.CATEGORIES) {
                    val viewModel: CategoriesViewModel = viewModel(factory = factory)
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    CategoriesScreen(
                        state = state,
                        messages = viewModel.messages,
                        onBack = { navController.popBackStack() },
                        onCreate = viewModel::create,
                        onUpdate = viewModel::update,
                        onDelete = viewModel::delete,
                    )
                }
            }
        }
    }
}

@Composable
private fun FocusBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
) {
    val colors = LocalFocusColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = colors.card,
            tonalElevation = 0.dp,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp)),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEach { tab ->
                    val selected = currentRoute == tab.route
                    val tint = if (selected) MaterialTheme.colorScheme.primary else colors.muted
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (selected) colors.lavender else Color.Transparent)
                            .clickable { onNavigate(tab.route) }
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = tint,
                            modifier = Modifier.size(22.dp),
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = tint,
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}
