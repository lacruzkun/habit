package com.focusflow.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.focusflow.app.AppContainer
import com.focusflow.app.ui.screens.archive.ArchiveViewModel
import com.focusflow.app.ui.screens.categories.CategoriesViewModel
import com.focusflow.app.ui.screens.habits.HabitDetailViewModel
import com.focusflow.app.ui.screens.habits.HabitEditorViewModel
import com.focusflow.app.ui.screens.habits.HabitsViewModel
import com.focusflow.app.ui.screens.home.HomeViewModel
import com.focusflow.app.ui.screens.onboarding.OnboardingViewModel
import com.focusflow.app.ui.screens.reminders.RemindersViewModel
import com.focusflow.app.ui.screens.review.WeeklyReviewViewModel
import com.focusflow.app.ui.screens.settings.SettingsViewModel
import com.focusflow.app.ui.screens.stats.StatsViewModel
import com.focusflow.app.ui.screens.tasks.TaskDetailViewModel
import com.focusflow.app.ui.screens.tasks.TaskEditorViewModel
import com.focusflow.app.ui.screens.tasks.TasksViewModel
import com.focusflow.app.ui.screens.timer.TimerViewModel

class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val savedStateHandle = extras.createSavedStateHandle()
        val viewModel = when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(container)

            modelClass.isAssignableFrom(OnboardingViewModel::class.java) ->
                OnboardingViewModel(container)

            modelClass.isAssignableFrom(TasksViewModel::class.java) ->
                TasksViewModel(container)

            modelClass.isAssignableFrom(TaskEditorViewModel::class.java) ->
                TaskEditorViewModel(container, savedStateHandle)

            modelClass.isAssignableFrom(TaskDetailViewModel::class.java) ->
                TaskDetailViewModel(container, savedStateHandle)

            modelClass.isAssignableFrom(TimerViewModel::class.java) ->
                TimerViewModel(container, savedStateHandle)

            modelClass.isAssignableFrom(StatsViewModel::class.java) ->
                StatsViewModel(container)

            modelClass.isAssignableFrom(HabitsViewModel::class.java) ->
                HabitsViewModel(container)

            modelClass.isAssignableFrom(HabitEditorViewModel::class.java) ->
                HabitEditorViewModel(container, savedStateHandle)

            modelClass.isAssignableFrom(HabitDetailViewModel::class.java) ->
                HabitDetailViewModel(container, savedStateHandle)

            modelClass.isAssignableFrom(WeeklyReviewViewModel::class.java) ->
                WeeklyReviewViewModel(container)

            modelClass.isAssignableFrom(RemindersViewModel::class.java) ->
                RemindersViewModel(container)

            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container)

            modelClass.isAssignableFrom(ArchiveViewModel::class.java) ->
                ArchiveViewModel(container)

            modelClass.isAssignableFrom(CategoriesViewModel::class.java) ->
                CategoriesViewModel(container)

            else -> error("Unknown ViewModel class: ${modelClass.name}")
        }
        return viewModel as T
    }
}
