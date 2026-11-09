package com.focusflow.app.ui.screens.review

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.ReflectionEntity
import com.focusflow.app.data.repository.toFact
import com.focusflow.app.domain.habits.HabitPeriodStats
import com.focusflow.app.domain.review.WeeklyReviewCalculator
import com.focusflow.app.domain.review.WeeklyReviewData
import com.focusflow.app.domain.stats.DateRange
import com.focusflow.app.domain.stats.FocusStatsCalculator
import com.focusflow.app.domain.stats.FocusStatsRequest
import com.focusflow.app.domain.stats.weekRange
import com.focusflow.app.domain.time.startOfWeek
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@Immutable
data class HabitReviewRow(
    val name: String,
    val stats: HabitPeriodStats,
)

@Immutable
data class WeeklyReviewUiState(
    val loading: Boolean = true,
    val weekStart: LocalDate = LocalDate.now(),
    val data: WeeklyReviewData? = null,
    val habitRows: List<HabitReviewRow> = emptyList(),
    val reflection: ReflectionEntity? = null,
    val saving: Boolean = false,
    val saved: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class WeeklyReviewViewModel(private val container: AppContainer) : ViewModel() {
    private data class Base(
        val today: LocalDate,
        val weekStartDate: LocalDate,
        val range: DateRange,
        val previous: DateRange,
        val tasks: List<Pair<Long, Int>>,
        val habits: List<com.focusflow.app.domain.habits.HabitFact>,
        val completionsByHabit: Map<Long, List<com.focusflow.app.domain.habits.HabitCompletionFact>>,
        val weekStart: com.focusflow.app.domain.model.WeekStart,
    )

    private val base = combine(
        container.preferencesRepository.preferences,
        container.taskRepository.currentTasks,
        container.habitRepository.currentHabits,
        container.habitRepository.allCompletions,
    ) { prefs, tasks, habits, completions ->
        val today = LocalDate.now()
        val week = weekRange(today, prefs.startOfWeek)
        Base(
            today = today,
            weekStartDate = week.start,
            range = week,
            previous = DateRange(week.start.minusWeeks(1), week.start),
            tasks = tasks.filter { it.active }.map { it.id to it.weeklyTargetMinutes },
            habits = habits.map { it.toFact() },
            completionsByHabit = completions.map { it.toFact() }.groupBy { it.habitId },
            weekStart = prefs.startOfWeek,
        )
    }

    private val reviewData = base.flatMapLatest { base ->
        val zone = container.timeSource.zoneId()
        combine(
            container.sessionRepository.segments(
                base.range.startMillis(zone),
                base.range.endMillis(zone),
            ),
            container.sessionRepository.sessionsStarted(
                base.range.startMillis(zone),
                base.range.endMillis(zone),
            ),
            container.sessionRepository.segments(
                base.previous.startMillis(zone),
                base.previous.endMillis(zone),
            ),
            container.sessionRepository.sessionsStarted(
                base.previous.startMillis(zone),
                base.previous.endMillis(zone),
            ),
        ) { currentSegments, currentSessions, previousSegments, previousSessions ->
            val current = FocusStatsCalculator.summarize(
                FocusStatsRequest(
                    base.range,
                    currentSegments.map { it.toFact() },
                    currentSessions.map { it.toFact() },
                    zone,
                ),
            )
            val previous = FocusStatsCalculator.summarize(
                FocusStatsRequest(
                    base.previous,
                    previousSegments.map { it.toFact() },
                    previousSessions.map { it.toFact() },
                    zone,
                ),
            )
            val habitStats = WeeklyReviewCalculator.habitStats(
                base.habits,
                base.completionsByHabit,
                base.range,
                base.today,
                base.weekStart,
            )
            val bestAndWorst = WeeklyReviewCalculator.bestAndWorst(current)
            val data = WeeklyReviewData(
                range = base.range,
                current = current,
                previous = previous,
                habitStats = habitStats,
                taskTotals = current.taskTotals,
                weeklyGoalSeconds = WeeklyReviewCalculator.weeklyGoalSeconds(base.tasks),
                insight = WeeklyReviewCalculator.buildInsight(current, previous, habitStats),
                bestDay = bestAndWorst.first,
                leastProductiveDay = bestAndWorst.second,
            )
            data to habitStats
        }
    }

    private val reflectionFlow = base.flatMapLatest { base ->
        container.reflectionRepository.observeWeek(base.weekStartDate.toEpochDay())
    }

    val state: StateFlow<WeeklyReviewUiState> = combine(
        base,
        reviewData,
        reflectionFlow,
        container.reflectionRepository.allReflections,
    ) { base, (data, habitStats), weekReflection, allReflections ->
        val reflection = weekReflection ?: allReflections.firstOrNull {
            it.weekStartEpochDay == base.weekStartDate.toEpochDay()
        }
        WeeklyReviewUiState(
            loading = false,
            weekStart = base.weekStartDate,
            data = data,
            habitRows = habitStats.map { HabitReviewRow(it.first.name, it.second) },
            reflection = reflection,
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = WeeklyReviewUiState(),
    )

    fun saveReflection(wentWell: String, needsImprovement: String, nextFocus: String) {
        viewModelScope.launch {
            val current = state.value
            container.reflectionRepository.save(
                weekStartEpochDay = current.weekStart.toEpochDay(),
                wentWell = wentWell,
                needsImprovement = needsImprovement,
                nextFocus = nextFocus,
            )
        }
    }
}
