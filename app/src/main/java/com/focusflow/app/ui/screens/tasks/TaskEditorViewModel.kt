package com.focusflow.app.ui.screens.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.CategoryEntity
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.domain.model.ALL_DAYS_MASK
import com.focusflow.app.domain.model.TaskPriority
import com.focusflow.app.domain.model.maskHasDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek

data class TaskEditorUiState(
    val loading: Boolean = true,
    val isNew: Boolean = true,
    val name: String = "",
    val description: String = "",
    val categoryId: Long? = null,
    val iconKey: String = "code",
    val colorArgb: Long = 0xFF6C4CF1,
    val dailyTargetMinutes: Int = 60,
    val weeklyTargetMinutes: Int = 300,
    val yearlyTargetMinutes: Int? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val active: Boolean = true,
    val scheduledDaysMask: Int = ALL_DAYS_MASK,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int? = null,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val categories: List<CategoryEntity> = emptyList(),
    val error: String? = null,
    val saved: Boolean = false,
    val archived: Boolean = false,
)

class TaskEditorViewModel(
    private val container: AppContainer,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val taskId: Long = savedStateHandle["taskId"] ?: -1L
    private val original = MutableStateFlow<TaskEntity?>(null)
    private val _state = MutableStateFlow(TaskEditorUiState(isNew = taskId <= 0))
    val state: StateFlow<TaskEditorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val categories = container.taskRepository.getAllCategories()
            val prefs = container.preferencesRepository.snapshot()
            val task = if (taskId > 0) container.taskRepository.getTask(taskId) else null
            original.value = task
            _state.update { current ->
                if (task != null) {
                    current.copy(
                        loading = false,
                        isNew = false,
                        name = task.name,
                        description = task.description,
                        categoryId = task.categoryId,
                        iconKey = task.iconKey,
                        colorArgb = task.colorArgb,
                        dailyTargetMinutes = task.dailyTargetMinutes,
                        weeklyTargetMinutes = task.weeklyTargetMinutes,
                        yearlyTargetMinutes = task.yearlyTargetMinutes,
                        priority = task.priority,
                        active = task.active,
                        scheduledDaysMask = task.scheduledDaysMask,
                        reminderEnabled = task.reminderEnabled,
                        reminderMinutes = task.reminderMinutes,
                        focusMinutes = task.focusMinutes,
                        shortBreakMinutes = task.shortBreakMinutes,
                        longBreakMinutes = task.longBreakMinutes,
                        sessionsBeforeLongBreak = task.sessionsBeforeLongBreak,
                        categories = categories,
                        archived = task.archivedAt != null,
                    )
                } else {
                    current.copy(
                        loading = false,
                        categories = categories,
                        focusMinutes = prefs.defaultFocusMinutes,
                        shortBreakMinutes = prefs.defaultShortBreakMinutes,
                        longBreakMinutes = prefs.defaultLongBreakMinutes,
                        sessionsBeforeLongBreak = prefs.defaultSessionsBeforeLongBreak,
                    )
                }
            }
        }
    }

    fun setName(value: String) = _state.update { it.copy(name = value.take(80), error = null) }

    fun setDescription(value: String) = _state.update { it.copy(description = value.take(500)) }

    fun setCategory(id: Long?) {
        _state.update { current ->
            val category = current.categories.firstOrNull { it.id == id }
            current.copy(
                categoryId = id,
                iconKey = category?.iconKey ?: current.iconKey,
                colorArgb = category?.colorArgb ?: current.colorArgb,
            )
        }
    }

    fun setIcon(key: String) = _state.update { it.copy(iconKey = key) }

    fun setColor(color: Long) = _state.update { it.copy(colorArgb = color) }

    fun setDailyTarget(minutes: Int) =
        _state.update { it.copy(dailyTargetMinutes = minutes.coerceIn(5, 960)) }

    fun setWeeklyTarget(minutes: Int) =
        _state.update { it.copy(weeklyTargetMinutes = minutes.coerceIn(5, 6720)) }

    fun setYearlyTarget(minutes: Int?) =
        _state.update { it.copy(yearlyTargetMinutes = minutes?.coerceIn(0, 200_000)) }

    fun setPriority(priority: TaskPriority) = _state.update { it.copy(priority = priority) }

    fun setActive(active: Boolean) = _state.update { it.copy(active = active) }

    fun toggleDay(day: DayOfWeek) = _state.update { current ->
        val bit = 1 shl (day.value - 1)
        val next = current.scheduledDaysMask xor bit
        current.copy(scheduledDaysMask = if (next == 0) current.scheduledDaysMask else next)
    }

    fun setAllDays() = _state.update { it.copy(scheduledDaysMask = ALL_DAYS_MASK) }

    fun setWeekdays() = _state.update {
        it.copy(
            scheduledDaysMask = DayOfWeek.entries
                .filter { day -> day.value <= 5 }
                .fold(0) { mask, day -> mask or (1 shl (day.value - 1)) },
        )
    }

    fun isDaySelected(day: DayOfWeek): Boolean =
        maskHasDay(_state.value.scheduledDaysMask, day)

    fun setReminderEnabled(enabled: Boolean) = _state.update {
        it.copy(
            reminderEnabled = enabled,
            reminderMinutes = it.reminderMinutes ?: (9 * 60),
        )
    }

    fun setReminderMinutes(minutes: Int) =
        _state.update { it.copy(reminderMinutes = minutes.coerceIn(0, 1439)) }

    fun setFocusMinutes(minutes: Int) =
        _state.update { it.copy(focusMinutes = minutes.coerceIn(1, 180)) }

    fun setShortBreakMinutes(minutes: Int) =
        _state.update { it.copy(shortBreakMinutes = minutes.coerceIn(1, 60)) }

    fun setLongBreakMinutes(minutes: Int) =
        _state.update { it.copy(longBreakMinutes = minutes.coerceIn(1, 120)) }

    fun setSessionsBeforeLongBreak(count: Int) =
        _state.update { it.copy(sessionsBeforeLongBreak = count.coerceIn(2, 12)) }

    fun save() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(error = "Give your task a name.") }
            return
        }
        if (current.weeklyTargetMinutes < current.dailyTargetMinutes) {
            _state.update { it.copy(error = "Weekly target should be at least the daily target.") }
            return
        }
        viewModelScope.launch {
            val existing = original.value
            val entity = TaskEntity(
                id = existing?.id ?: 0,
                name = current.name.trim(),
                description = current.description.trim(),
                categoryId = current.categoryId,
                iconKey = current.iconKey,
                colorArgb = current.colorArgb,
                dailyTargetMinutes = current.dailyTargetMinutes,
                weeklyTargetMinutes = current.weeklyTargetMinutes,
                yearlyTargetMinutes = current.yearlyTargetMinutes,
                priority = current.priority,
                createdAt = existing?.createdAt ?: 0,
                archivedAt = existing?.archivedAt,
                active = current.active,
                scheduledDaysMask = current.scheduledDaysMask,
                reminderEnabled = current.reminderEnabled,
                reminderMinutes = current.reminderMinutes,
                focusMinutes = current.focusMinutes,
                shortBreakMinutes = current.shortBreakMinutes,
                longBreakMinutes = current.longBreakMinutes,
                sessionsBeforeLongBreak = current.sessionsBeforeLongBreak,
                sortOrder = existing?.sortOrder ?: 0,
            )
            val savedId = if (existing == null) {
                container.taskRepository.createTask(entity)
            } else {
                container.taskRepository.updateTask(entity)
                entity.id
            }
            val saved = entity.copy(id = savedId)
            if (saved.reminderEnabled) {
                container.reminderScheduler.scheduleTaskReminder(saved)
            } else {
                container.reminderScheduler.cancel(
                    com.focusflow.app.domain.model.ReminderSource.TASK,
                    savedId,
                )
            }
            _state.update { it.copy(saved = true, error = null) }
        }
    }

    fun archive() {
        val id = original.value?.id ?: return
        viewModelScope.launch {
            container.taskRepository.archiveTask(id)
            container.reminderScheduler.cancel(
                com.focusflow.app.domain.model.ReminderSource.TASK,
                id,
            )
            _state.update { it.copy(saved = true) }
        }
    }

    fun restore() {
        val id = original.value?.id ?: return
        viewModelScope.launch {
            container.taskRepository.restoreTask(id)
            original.value?.let { container.reminderScheduler.scheduleTaskReminder(it) }
            _state.update { it.copy(saved = true) }
        }
    }

    fun deletePermanently() {
        val id = original.value?.id ?: return
        viewModelScope.launch {
            container.taskRepository.deleteTaskPermanently(id)
            container.reminderScheduler.cancel(
                com.focusflow.app.domain.model.ReminderSource.TASK,
                id,
            )
            _state.update { it.copy(saved = true) }
        }
    }
}
