package com.focusflow.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.CategoryEntity
import com.focusflow.app.data.db.FocusSessionEntity
import com.focusflow.app.data.db.HabitCompletionEntity
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.data.prefs.UserPreferences
import com.focusflow.app.data.repository.matchesSchedule
import com.focusflow.app.data.repository.toFact
import com.focusflow.app.domain.habits.HabitCalculator
import com.focusflow.app.domain.model.AvatarStyle
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.time.endOfDayExclusiveMillis
import com.focusflow.app.domain.time.startOfDayMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class TaskProgressUi(
    val task: TaskEntity,
    val category: CategoryEntity?,
    val todaySeconds: Long,
    val progress: Float,
)

data class HabitTodayUi(
    val habit: HabitEntity,
    val completed: Boolean,
    val scheduled: Boolean,
)

data class HomeUiState(
    val loading: Boolean = true,
    val userName: String = "Friend",
    val avatar: AvatarStyle = AvatarStyle.MOON,
    val today: LocalDate = LocalDate.now(),
    val greeting: String = "Hello",
    val dateLabel: String = "",
    val focusSeconds: Long = 0,
    val targetSeconds: Long = 0,
    val sessionsStarted: Int = 0,
    val tasks: List<TaskProgressUi> = emptyList(),
    val recommended: TaskProgressUi? = null,
    val habits: List<HabitTodayUi> = emptyList(),
    val recentSessions: List<FocusSessionEntity> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(private val container: AppContainer) : ViewModel() {
    private val today = MutableStateFlow(LocalDate.now())

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(60_000)
                today.value = LocalDate.now()
            }
        }
    }

    private val segmentsToday = today.flatMapLatest { date ->
        container.sessionRepository.segments(
            date.startOfDayMillis(container.timeSource.zoneId()),
            date.endOfDayExclusiveMillis(container.timeSource.zoneId()),
        )
    }

    val state: StateFlow<HomeUiState> = today.flatMapLatest { date ->
        combine(
            container.preferencesRepository.preferences,
            container.taskRepository.currentTasks,
            container.taskRepository.categories,
            combine(segmentsToday, container.sessionRepository.recentSessions(6)) { segments, recent ->
                segments to recent
            },
            combine(
                container.habitRepository.currentHabits,
                container.habitRepository.allCompletions,
            ) { habits, completions -> habits to completions },
        ) { prefs, tasks, categories, sessionsPair, habitsPair ->
            buildState(
                date = date,
                prefs = prefs,
                tasks = tasks,
                categories = categories,
                segments = sessionsPair.first,
                recent = sessionsPair.second,
                habits = habitsPair.first,
                completions = habitsPair.second,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )

    fun toggleHabit(habitId: Long) {
        viewModelScope.launch {
            container.habitRepository.toggleCompletion(habitId, LocalDate.now().toEpochDay())
        }
    }

    private fun buildState(
        date: LocalDate,
        prefs: UserPreferences,
        tasks: List<TaskEntity>,
        categories: List<CategoryEntity>,
        segments: List<com.focusflow.app.data.db.SessionSegmentRow>,
        recent: List<FocusSessionEntity>,
        habits: List<HabitEntity>,
        completions: List<HabitCompletionEntity>,
    ): HomeUiState {
        val categoryMap = categories.associateBy { it.id }
        val focusSegments = segments.filter { it.sessionType == SessionType.FOCUS }
        val secondsByTask = focusSegments
            .filter { it.taskId != null }
            .groupBy { it.taskId!! }
            .mapValues { (_, rows) -> rows.sumOf { (it.segmentEnd - it.segmentStart).coerceAtLeast(0) } / 1000L }
        val totalSeconds = focusSegments.sumOf { (it.segmentEnd - it.segmentStart).coerceAtLeast(0) } / 1000L
        val sessionsStarted = focusSegments.map { it.sessionId }.distinct().size
        val taskItems = tasks
            .filter { it.active && it.archivedAt == null && it.matchesSchedule(date) }
            .map { task ->
                val seconds = secondsByTask[task.id] ?: 0L
                val target = task.dailyTargetMinutes.coerceAtLeast(1) * 60L
                TaskProgressUi(
                    task = task,
                    category = task.categoryId?.let { categoryMap[it] },
                    todaySeconds = seconds,
                    progress = (seconds.toFloat() / target.toFloat()).coerceIn(0f, 1f),
                )
            }
        val habitFacts = habits.map { it.toFact() }
        val completionsByHabit = completions.map { it.toFact() }.groupBy { it.habitId }
        val habitItems = habits.map { habit ->
            val fact = habit.toFact()
            val qualified = completionsByHabit[habit.id].orEmpty()
                .filter { HabitCalculator.completionQualifies(it) }
                .map { it.epochDay }
                .toSet()
            HabitTodayUi(
                habit = habit,
                completed = date.toEpochDay() in qualified,
                scheduled = habit.frequency == com.focusflow.app.domain.model.HabitFrequency.WEEKLY_COUNT ||
                    HabitCalculator.isScheduled(fact, date),
            )
        }.filter { it.scheduled || it.completed }
        val recommended = container.taskRepository.recommendedTask(
            tasks = taskItems.map { it.task },
            focusSecondsByTask = secondsByTask,
            today = date,
        )?.let { selected -> taskItems.firstOrNull { it.task.id == selected.id } }

        return HomeUiState(
            loading = false,
            userName = prefs.userName,
            avatar = prefs.avatarStyle,
            today = date,
            greeting = greetingFor(LocalTime.now()),
            dateLabel = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.getDefault())),
            focusSeconds = totalSeconds,
            targetSeconds = prefs.dailyFocusTargetMinutes * 60L,
            sessionsStarted = sessionsStarted,
            tasks = taskItems.sortedWith(
                compareByDescending<TaskProgressUi> { it.task.priority.level }
                    .thenBy { it.task.sortOrder },
            ),
            recommended = recommended,
            habits = habitItems,
            recentSessions = recent,
        )
    }

    private fun greetingFor(time: LocalTime): String = when (time.hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..21 -> "Good evening"
        else -> "Hello"
    }
}
