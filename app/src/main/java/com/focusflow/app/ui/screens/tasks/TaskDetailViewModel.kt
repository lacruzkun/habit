package com.focusflow.app.ui.screens.tasks

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.CategoryEntity
import com.focusflow.app.data.db.FocusSessionEntity
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.data.repository.toFact
import com.focusflow.app.domain.stats.DateRange
import com.focusflow.app.domain.stats.FocusStatsCalculator
import com.focusflow.app.domain.stats.FocusSummary
import com.focusflow.app.domain.stats.monthRange
import com.focusflow.app.domain.stats.weekRange
import com.focusflow.app.domain.time.endOfDayExclusiveMillis
import com.focusflow.app.domain.time.startOfDayMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TaskDetailUiState(
    val loading: Boolean = true,
    val task: TaskEntity? = null,
    val category: CategoryEntity? = null,
    val today: FocusSummary? = null,
    val week: FocusSummary? = null,
    val month: FocusSummary? = null,
    val recentSessions: List<FocusSessionEntity> = emptyList(),
    val error: String? = null,
    val removed: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class TaskDetailViewModel(
    private val container: AppContainer,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val taskId: Long = savedStateHandle["taskId"] ?: -1L
    private val today = LocalDate.now()
    private val zone = container.timeSource.zoneId()
    private val lookbackStart = today.minusDays(90).startOfDayMillis(zone)
    private val lookbackEnd = today.endOfDayExclusiveMillis(zone)

    val state: StateFlow<TaskDetailUiState> = combine(
        container.taskRepository.observeTask(taskId),
        container.taskRepository.categories,
        container.preferencesRepository.preferences,
        container.sessionRepository.segments(lookbackStart, lookbackEnd),
        container.sessionRepository.sessionsForTask(taskId, 30),
    ) { task, categories, prefs, segments, sessions ->
        if (task == null) {
            TaskDetailUiState(loading = false, error = "Task not found.")
        } else {
            val segmentsFacts = segments.map { it.toFact() }
            val sessionFacts = sessions.map { it.toFact() }
            val todayRange = DateRange(today, today.plusDays(1))
            val weekRange = weekRange(today, prefs.startOfWeek)
            val monthRange = monthRange(today)
            TaskDetailUiState(
                loading = false,
                task = task,
                category = categories.firstOrNull { it.id == task.categoryId },
                today = FocusStatsCalculator.taskSummary(
                    taskId, segmentsFacts, sessionFacts, todayRange, zone,
                ),
                week = FocusStatsCalculator.taskSummary(
                    taskId, segmentsFacts, sessionFacts, weekRange, zone,
                ),
                month = FocusStatsCalculator.taskSummary(
                    taskId, segmentsFacts, sessionFacts, monthRange, zone,
                ),
                recentSessions = sessions,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TaskDetailUiState(),
    )

    fun archive() {
        viewModelScope.launch {
            container.taskRepository.archiveTask(taskId)
            container.reminderScheduler.cancel(
                com.focusflow.app.domain.model.ReminderSource.TASK,
                taskId,
            )
        }
    }

    fun restore() {
        viewModelScope.launch { container.taskRepository.restoreTask(taskId) }
    }

    fun setActive(active: Boolean) {
        viewModelScope.launch { container.taskRepository.setActive(taskId, active) }
    }

    fun deletePermanently() {
        viewModelScope.launch {
            container.taskRepository.deleteTaskPermanently(taskId)
            container.reminderScheduler.cancel(
                com.focusflow.app.domain.model.ReminderSource.TASK,
                taskId,
            )
        }
    }
}
