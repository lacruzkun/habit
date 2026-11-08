package com.focusflow.app.ui.screens.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.HabitCompletionEntity
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.prefs.UserPreferences
import com.focusflow.app.data.repository.toFact
import com.focusflow.app.domain.habits.HabitCalculator
import com.focusflow.app.domain.habits.HabitPeriodStats
import com.focusflow.app.domain.model.HabitFrequency
import com.focusflow.app.domain.model.WeekStart
import com.focusflow.app.domain.stats.DateRange
import com.focusflow.app.domain.time.startOfWeek
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HabitCardUi(
    val habit: HabitEntity,
    val stats: HabitPeriodStats,
    val todayCompleted: Boolean,
    val weeklyProgress: Int,
    val weeklyTarget: Int,
)

data class HabitsUiState(
    val loading: Boolean = true,
    val today: LocalDate = LocalDate.now(),
    val habits: List<HabitCardUi> = emptyList(),
    val archived: List<HabitEntity> = emptyList(),
    val completedToday: Int = 0,
    val scheduledToday: Int = 0,
    val weekStart: WeekStart = WeekStart.MONDAY,
)

class HabitsViewModel(private val container: AppContainer) : ViewModel() {
    val state: StateFlow<HabitsUiState> = combine(
        container.preferencesRepository.preferences,
        container.habitRepository.currentHabits,
        container.habitRepository.archivedHabits,
        container.habitRepository.allCompletions,
    ) { prefs, habits, archived, completions ->
        buildState(LocalDate.now(), prefs, habits, archived, completions)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HabitsUiState(),
    )

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            container.habitRepository.toggleCompletion(habitId, LocalDate.now().toEpochDay())
        }
    }

    fun setProgress(habitId: Long, value: Double) {
        viewModelScope.launch {
            container.habitRepository.setCompletion(habitId, LocalDate.now().toEpochDay(), value)
        }
    }

    private fun buildState(
        today: LocalDate,
        prefs: UserPreferences,
        habits: List<HabitEntity>,
        archived: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
    ): HabitsUiState {
        val factCompletions = completions.map { it.toFact() }
        val byHabit = factCompletions.groupBy { it.habitId }
        val statsRange = DateRange(today.minusDays(29), today.plusDays(1))
        val week = com.focusflow.app.domain.stats.weekRange(today, prefs.startOfWeek)
        val cards = habits.map { habit ->
            val fact = habit.toFact()
            val habitCompletions = byHabit[habit.id].orEmpty()
            val stats = HabitCalculator.stats(
                habit = fact,
                completions = habitCompletions,
                range = statsRange,
                today = today,
                weekStart = prefs.startOfWeek,
            )
            val completedDates = HabitCalculator.completedDates(habitCompletions)
            val weekCount = habitCompletions.count {
                it.value >= it.targetValue &&
                    it.epochDay >= week.start.toEpochDay() &&
                    it.epochDay < week.endExclusive.toEpochDay()
            }
            HabitCardUi(
                habit = habit,
                stats = stats,
                todayCompleted = today.toEpochDay() in completedDates,
                weeklyProgress = weekCount,
                weeklyTarget = HabitCalculator.weeklyTarget(fact),
            )
        }.sortedWith(
            compareByDescending<HabitCardUi> { it.stats.todayScheduled && !it.todayCompleted }
                .thenBy { it.habit.createdAt },
        )
        return HabitsUiState(
            loading = false,
            today = today,
            habits = cards,
            archived = archived,
            completedToday = cards.count { it.todayCompleted },
            scheduledToday = cards.count { it.stats.todayScheduled },
            weekStart = prefs.startOfWeek,
        )
    }
}
