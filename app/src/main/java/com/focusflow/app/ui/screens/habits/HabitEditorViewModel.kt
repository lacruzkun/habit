package com.focusflow.app.ui.screens.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.domain.model.ALL_DAYS_MASK
import com.focusflow.app.domain.model.HabitFrequency
import com.focusflow.app.domain.model.maskHasDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate

data class HabitEditorUiState(
    val loading: Boolean = true,
    val isNew: Boolean = true,
    val name: String = "",
    val description: String = "",
    val iconKey: String = "self_improvement",
    val colorArgb: Long = 0xFF45C98A,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val daysMask: Int = ALL_DAYS_MASK,
    val weeklyTargetCount: Int = 3,
    val targetValue: Double = 1.0,
    val unit: String = "times",
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int? = null,
    val error: String? = null,
    val saved: Boolean = false,
    val archived: Boolean = false,
)

class HabitEditorViewModel(
    private val container: AppContainer,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val habitId: Long = savedStateHandle["habitId"] ?: -1L
    private val original = MutableStateFlow<HabitEntity?>(null)
    private val _state = MutableStateFlow(HabitEditorUiState(isNew = habitId <= 0))
    val state: StateFlow<HabitEditorUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val habit = if (habitId > 0) container.habitRepository.getHabit(habitId) else null
            original.value = habit
            _state.update {
                if (habit != null) {
                    it.copy(
                        loading = false,
                        isNew = false,
                        name = habit.name,
                        description = habit.description,
                        iconKey = habit.iconKey,
                        colorArgb = habit.colorArgb,
                        frequency = habit.frequency,
                        daysMask = habit.daysMask,
                        weeklyTargetCount = habit.weeklyTargetCount,
                        targetValue = habit.targetValue,
                        unit = habit.unit,
                        reminderEnabled = habit.reminderEnabled,
                        reminderMinutes = habit.reminderMinutes,
                        archived = habit.archivedAt != null,
                    )
                } else {
                    it.copy(loading = false)
                }
            }
        }
    }

    fun setName(value: String) = _state.update { it.copy(name = value.take(80), error = null) }

    fun setDescription(value: String) = _state.update { it.copy(description = value.take(500)) }

    fun setIcon(key: String) = _state.update { it.copy(iconKey = key) }

    fun setColor(color: Long) = _state.update { it.copy(colorArgb = color) }

    fun setFrequency(value: HabitFrequency) = _state.update { it.copy(frequency = value) }

    fun toggleDay(day: DayOfWeek) = _state.update { current ->
        val bit = 1 shl (day.value - 1)
        val next = current.daysMask xor bit
        current.copy(daysMask = if (next == 0) current.daysMask else next)
    }

    fun isDaySelected(day: DayOfWeek): Boolean = maskHasDay(_state.value.daysMask, day)

    fun setAllDays() = _state.update { it.copy(daysMask = ALL_DAYS_MASK) }

    fun setWeeklyTarget(count: Int) =
        _state.update { it.copy(weeklyTargetCount = count.coerceIn(1, 7)) }

    fun setTargetValue(value: Double) =
        _state.update { it.copy(targetValue = value.coerceIn(0.1, 100_000.0)) }

    fun setUnit(value: String) = _state.update { it.copy(unit = value.take(20)) }

    fun setReminderEnabled(enabled: Boolean) = _state.update {
        it.copy(
            reminderEnabled = enabled,
            reminderMinutes = it.reminderMinutes ?: (8 * 60),
        )
    }

    fun setReminderMinutes(minutes: Int) =
        _state.update { it.copy(reminderMinutes = minutes.coerceIn(0, 1439)) }

    fun save() {
        val current = _state.value
        if (current.name.isBlank()) {
            _state.update { it.copy(error = "Give your habit a name.") }
            return
        }
        viewModelScope.launch {
            val existing = original.value
            val entity = HabitEntity(
                id = existing?.id ?: 0,
                name = current.name.trim(),
                description = current.description.trim(),
                iconKey = current.iconKey,
                colorArgb = current.colorArgb,
                frequency = current.frequency,
                daysMask = current.daysMask,
                weeklyTargetCount = current.weeklyTargetCount,
                targetValue = current.targetValue,
                unit = current.unit.ifBlank { "times" },
                startEpochDay = existing?.startEpochDay ?: LocalDate.now().toEpochDay(),
                reminderEnabled = current.reminderEnabled,
                reminderMinutes = current.reminderMinutes,
                createdAt = existing?.createdAt ?: 0,
                archivedAt = existing?.archivedAt,
            )
            val savedId = if (existing == null) {
                container.habitRepository.createHabit(entity)
            } else {
                container.habitRepository.updateHabit(entity)
                entity.id
            }
            val saved = entity.copy(id = savedId)
            if (saved.reminderEnabled) {
                container.reminderScheduler.scheduleHabitReminder(saved)
            } else {
                container.reminderScheduler.cancel(
                    com.focusflow.app.domain.model.ReminderSource.HABIT,
                    savedId,
                )
            }
            _state.update { it.copy(saved = true, error = null) }
        }
    }

    fun archive() {
        val id = original.value?.id ?: return
        viewModelScope.launch {
            container.habitRepository.archiveHabit(id)
            container.reminderScheduler.cancel(
                com.focusflow.app.domain.model.ReminderSource.HABIT,
                id,
            )
            _state.update { it.copy(saved = true) }
        }
    }

    fun restore() {
        val id = original.value?.id ?: return
        viewModelScope.launch {
            container.habitRepository.restoreHabit(id)
            original.value?.let { container.reminderScheduler.scheduleHabitReminder(it) }
            _state.update { it.copy(saved = true) }
        }
    }

    fun deletePermanently() {
        val id = original.value?.id ?: return
        viewModelScope.launch {
            container.habitRepository.deleteHabitPermanently(id)
            container.reminderScheduler.cancel(
                com.focusflow.app.domain.model.ReminderSource.HABIT,
                id,
            )
            _state.update { it.copy(saved = true) }
        }
    }
}
