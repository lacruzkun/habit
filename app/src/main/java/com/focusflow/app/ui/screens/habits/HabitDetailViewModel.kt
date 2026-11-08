package com.focusflow.app.ui.screens.habits

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.HabitCompletionEntity
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.repository.toFact
import com.focusflow.app.domain.habits.HabitCalculator
import com.focusflow.app.domain.habits.HabitPeriodStats
import com.focusflow.app.domain.model.WeekStart
import com.focusflow.app.domain.stats.DateRange
import com.focusflow.app.domain.stats.weekRange
import com.focusflow.app.domain.time.epochDayToDate
import com.focusflow.app.domain.time.startOfWeek
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitDetailUiState(
    val loading: Boolean = true,
    val habit: HabitEntity? = null,
    val stats30: HabitPeriodStats? = null,
    val statsAll: HabitPeriodStats? = null,
    val completedDays: Set<Long> = emptySet(),
    val todayValue: Double = 0.0,
    val todayCompleted: Boolean = false,
    val weeklyCount: Int = 0,
    val weeklyTarget: Int = 1,
    val recentCompletions: List<HabitCompletionEntity> = emptyList(),
    val weekStart: WeekStart = WeekStart.MONDAY,
    val error: String? = null,
)

class HabitDetailViewModel(
    private val container: AppContainer,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val habitId: Long = savedStateHandle["habitId"] ?: -1L

    val state: StateFlow<HabitDetailUiState> = combine(
        container.habitRepository.observeHabit(habitId),
        container.habitRepository.observeCompletionsForHabit(habitId),
        container.preferencesRepository.preferences,
    ) { habit, completions, prefs ->
        if (habit == null) {
            HabitDetailUiState(loading = false, error = "Habit not found.")
        } else {
            val today = LocalDate.now()
            val facts = completions.map { it.toFact() }
            val completedDays = HabitCalculator.completedDates(facts)
            val start = epochDayToDate(habit.startEpochDay)
            val allRange = DateRange(start, today.plusDays(1))
            val statsAll = HabitCalculator.stats(
                habit.toFact(), facts, allRange, today, prefs.startOfWeek,
            )
            val stats30 = HabitCalculator.stats(
                habit.toFact(),
                facts,
                DateRange(today.minusDays(29), today.plusDays(1)),
                today,
                prefs.startOfWeek,
            )
            val week = weekRange(today, prefs.startOfWeek)
            val weeklyCount = HabitCalculator.weeklyCompletionCount(facts, week.start)
            val todayEntry = completions.firstOrNull { it.epochDay == today.toEpochDay() }
            HabitDetailUiState(
                loading = false,
                habit = habit,
                stats30 = stats30,
                statsAll = statsAll,
                completedDays = completedDays,
                todayValue = todayEntry?.value ?: 0.0,
                todayCompleted = today.toEpochDay() in completedDays,
                weeklyCount = weeklyCount,
                weeklyTarget = HabitCalculator.weeklyTarget(habit.toFact()),
                recentCompletions = completions.sortedByDescending { it.epochDay }.take(20),
                weekStart = prefs.startOfWeek,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HabitDetailUiState(),
    )

    fun toggleToday() {
        val today = LocalDate.now().toEpochDay()
        viewModelScope.launch { container.habitRepository.toggleCompletion(habitId, today) }
    }

    fun setTodayValue(value: Double) {
        val today = LocalDate.now().toEpochDay()
        viewModelScope.launch { container.habitRepository.setCompletion(habitId, today, value) }
    }

    fun archive() {
        viewModelScope.launch { container.habitRepository.archiveHabit(habitId) }
    }

    fun restore() {
        viewModelScope.launch { container.habitRepository.restoreHabit(habitId) }
    }

    fun deletePermanently() {
        viewModelScope.launch { container.habitRepository.deleteHabitPermanently(habitId) }
    }
}
