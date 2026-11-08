package com.focusflow.app.ui.screens.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.db.ReminderEntity
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.domain.model.ReminderSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class RemindersUiState(
    val loading: Boolean = true,
    val enabled: Boolean = true,
    val customs: List<ReminderEntity> = emptyList(),
    val taskReminders: List<TaskEntity> = emptyList(),
    val habitReminders: List<HabitEntity> = emptyList(),
)

class RemindersViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<RemindersUiState> = combine(
        container.reminderRepository.reminders,
        container.taskRepository.currentTasks,
        container.habitRepository.currentHabits,
        container.preferencesRepository.preferences,
    ) { customs, tasks, habits, prefs ->
        RemindersUiState(
            loading = false,
            enabled = prefs.notificationsEnabled,
            customs = customs.sortedBy { it.timeMinutes },
            taskReminders = tasks.sortedBy { it.name },
            habitReminders = habits.sortedBy { it.name },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = RemindersUiState(),
    )

    fun createCustom(title: String, message: String, timeMinutes: Int, daysMask: Int) {
        if (title.isBlank()) return
        viewModelScope.launch {
            container.reminderRepository.create(
                title = title,
                message = message,
                type = com.focusflow.app.domain.model.ReminderType.CUSTOM,
                timeMinutes = timeMinutes,
                daysMask = daysMask,
            )
            container.reminderScheduler.sync()
        }
    }

    fun updateCustom(reminder: ReminderEntity) {
        viewModelScope.launch {
            container.reminderRepository.update(reminder)
            container.reminderScheduler.sync()
        }
    }

    fun setCustomEnabled(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            container.reminderRepository.setEnabled(id, enabled)
            container.reminderScheduler.sync()
        }
    }

    fun deleteCustom(id: Long) {
        viewModelScope.launch {
            container.reminderScheduler.cancel(ReminderSource.CUSTOM, id)
            container.reminderRepository.delete(id)
        }
    }

    fun setTaskReminder(task: TaskEntity, enabled: Boolean, minutes: Int = 9 * 60) {
        viewModelScope.launch {
            val updated = task.copy(
                reminderEnabled = enabled,
                reminderMinutes = if (enabled) minutes else task.reminderMinutes,
            )
            container.taskRepository.updateTask(updated)
            if (enabled) container.reminderScheduler.scheduleTaskReminder(updated)
            else container.reminderScheduler.cancel(ReminderSource.TASK, task.id)
        }
    }

    fun setTaskReminderTime(task: TaskEntity, minutes: Int) {
        setTaskReminder(task, enabled = true, minutes = minutes)
    }

    fun setHabitReminder(habit: HabitEntity, enabled: Boolean, minutes: Int = 8 * 60) {
        viewModelScope.launch {
            val updated = habit.copy(
                reminderEnabled = enabled,
                reminderMinutes = if (enabled) minutes else habit.reminderMinutes,
            )
            container.habitRepository.updateHabit(updated)
            if (enabled) container.reminderScheduler.scheduleHabitReminder(updated)
            else container.reminderScheduler.cancel(ReminderSource.HABIT, habit.id)
        }
    }

    fun setHabitReminderTime(habit: HabitEntity, minutes: Int) {
        setHabitReminder(habit, enabled = true, minutes = minutes)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            container.preferencesRepository.setNotificationsEnabled(enabled)
            container.reminderScheduler.sync()
        }
    }
}
