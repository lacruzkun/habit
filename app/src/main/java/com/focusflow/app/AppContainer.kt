package com.focusflow.app

import android.content.Context
import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.export.ExportManager
import com.focusflow.app.data.prefs.UserPreferencesRepository
import com.focusflow.app.data.repository.DistractionRepository
import com.focusflow.app.data.repository.HabitRepository
import com.focusflow.app.data.repository.ReflectionRepository
import com.focusflow.app.data.repository.ReminderRepository
import com.focusflow.app.data.repository.SessionRepository
import com.focusflow.app.data.repository.TaskRepository
import com.focusflow.app.domain.time.SystemTimeSource
import com.focusflow.app.notifications.NotificationHelper
import com.focusflow.app.notifications.ReminderScheduler
import com.focusflow.app.timer.TimerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AppContainer(context: Context) {
    val appContext = context.applicationContext
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val timeSource = SystemTimeSource

    val database: FocusFlowDatabase = FocusFlowDatabase.get(appContext)
    val preferencesRepository = UserPreferencesRepository(appContext)
    val taskRepository = TaskRepository(database, timeSource)
    val sessionRepository = SessionRepository(database)
    val habitRepository = HabitRepository(database, timeSource)
    val distractionRepository = DistractionRepository(database, timeSource)
    val reflectionRepository = ReflectionRepository(database, timeSource)
    val reminderRepository = ReminderRepository(database, timeSource)
    val notificationHelper = NotificationHelper(appContext)
    val reminderScheduler = ReminderScheduler(
        context = appContext,
        reminderRepository = reminderRepository,
        taskRepository = taskRepository,
        habitRepository = habitRepository,
        preferencesRepository = preferencesRepository,
    )
    val timerManager = TimerManager(
        database = database,
        sessionRepository = sessionRepository,
        preferencesRepository = preferencesRepository,
        timeSource = timeSource,
        scope = applicationScope,
    )
    val exportManager = ExportManager(
        context = appContext,
        database = database,
        taskRepository = taskRepository,
        sessionRepository = sessionRepository,
        habitRepository = habitRepository,
        distractionRepository = distractionRepository,
        reflectionRepository = reflectionRepository,
        reminderRepository = reminderRepository,
        preferencesRepository = preferencesRepository,
        reminderScheduler = reminderScheduler,
        timeSource = timeSource,
    )

    init {
        notificationHelper.createChannels()
        applicationScope.launch {
            taskRepository.seedDefaultCategories()
            reminderRepository.seedDefault()
            reminderScheduler.sync()
            timerManager.restore()
            if (timerManager.state.value != null) {
                NotificationHelper.startTimerService(appContext)
            }
        }
    }
}
