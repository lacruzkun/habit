package com.focusflow.app.ui.screens.stats

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.focusflow.app.AppContainer
import com.focusflow.app.data.db.CategoryEntity
import com.focusflow.app.data.db.DistractionEntity
import com.focusflow.app.data.db.FocusSessionEntity
import com.focusflow.app.data.db.SessionSegmentRow
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.data.prefs.UserPreferences
import com.focusflow.app.data.repository.toFact
import com.focusflow.app.domain.stats.DayFocus
import com.focusflow.app.domain.stats.FocusStatsCalculator
import com.focusflow.app.domain.stats.FocusStatsRequest
import com.focusflow.app.domain.stats.FocusSummary
import com.focusflow.app.domain.stats.PeriodSelection
import com.focusflow.app.domain.stats.PeriodType
import com.focusflow.app.domain.stats.TaskFocus
import com.focusflow.app.domain.stats.metricChange
import com.focusflow.app.domain.stats.periodRange
import com.focusflow.app.domain.stats.previousRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Immutable
data class DistractionUi(
    val entity: DistractionEntity,
    val durationMinutes: Long,
)

@Immutable
data class StatsUiState(
    val loading: Boolean = true,
    val period: PeriodType = PeriodType.WEEK,
    val rangeLabel: String = "",
    val summary: FocusSummary? = null,
    val previous: FocusSummary? = null,
    val totalChangePercent: Double? = null,
    val sessionChange: Int = 0,
    val rateChangePoints: Double? = null,
    val chartValues: List<Long> = emptyList(),
    val chartLabels: List<String> = emptyList(),
    val topTasks: List<TaskFocus> = emptyList(),
    val distractions: List<DistractionUi> = emptyList(),
    val distractionMinutes: Long = 0,
    val bestDay: DayFocus? = null,
    val tasks: List<TaskEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModel(private val container: AppContainer) : ViewModel() {
    private val period = MutableStateFlow(PeriodType.WEEK)

    private data class PeriodBundle(
        val type: PeriodType,
        val rangeLabel: String,
        val current: FocusSummary,
        val previous: FocusSummary,
        val currentSegments: List<SessionSegmentRow>,
        val prefs: UserPreferences,
    )

    private val periodData = combine(
        period,
        container.preferencesRepository.preferences,
    ) { type, prefs -> type to prefs }
        .flatMapLatest { (type, prefs) ->
            val today = LocalDate.now()
            val zone = container.timeSource.zoneId()
            val selection = PeriodSelection(type)
            val currentRange = periodRange(selection, today, prefs.startOfWeek)
            val previousRange = previousRange(selection, currentRange, today, prefs.startOfWeek)
            combine(
                container.sessionRepository.segments(
                    currentRange.startMillis(zone),
                    currentRange.endMillis(zone),
                ),
                container.sessionRepository.sessionsStarted(
                    currentRange.startMillis(zone),
                    currentRange.endMillis(zone),
                ),
                container.sessionRepository.segments(
                    previousRange.startMillis(zone),
                    previousRange.endMillis(zone),
                ),
                container.sessionRepository.sessionsStarted(
                    previousRange.startMillis(zone),
                    previousRange.endMillis(zone),
                ),
            ) { currentSegments, currentSessions, previousSegments, previousSessions ->
                PeriodBundle(
                    type = type,
                    rangeLabel = currentRange.label(),
                    current = FocusStatsCalculator.summarize(
                        FocusStatsRequest(
                            range = currentRange,
                            segments = currentSegments.map { it.toFact() },
                            sessions = currentSessions.map { it.toFact() },
                            zone = zone,
                        ),
                    ),
                    previous = FocusStatsCalculator.summarize(
                        FocusStatsRequest(
                            range = previousRange,
                            segments = previousSegments.map { it.toFact() },
                            sessions = previousSessions.map { it.toFact() },
                            zone = zone,
                        ),
                    ),
                    currentSegments = currentSegments,
                    prefs = prefs,
                )
            }
        }

    val state: StateFlow<StatsUiState> = combine(
        periodData,
        container.taskRepository.currentTasks,
        container.taskRepository.categories,
        container.distractionRepository.allDistractions,
        container.preferencesRepository.preferences,
    ) { bundle, tasks, categories, distractions, prefs ->
        val summary = bundle.current
        val previous = bundle.previous
        val zone = container.timeSource.zoneId()
        val selection = PeriodSelection(bundle.type)
        val today = LocalDate.now()
        val range = periodRange(selection, today, prefs.startOfWeek)
        val rangeStart = range.startMillis(zone)
        val rangeEnd = range.endMillis(zone)
        val filteredDistractions = distractions.filter {
            it.startedAt >= rangeStart && it.startedAt < rangeEnd
        }
        val distractionItems = filteredDistractions.map {
            DistractionUi(it, ((it.endedAt - it.startedAt).coerceAtLeast(0)) / 60_000L)
        }
        val chart = when (bundle.type) {
            PeriodType.YEAR -> FocusStatsCalculator.bucketByMonth(range, summary.daily)
            PeriodType.MONTH -> summary.daily
            else -> summary.daily
        }
        val labels = when (bundle.type) {
            PeriodType.YEAR -> chart.map { it.date.format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault())) }
            PeriodType.MONTH -> chart.chunked(5).mapNotNull { chunk ->
                chunk.firstOrNull()?.date?.dayOfMonth?.toString()
            }
            else -> chart.map { it.date.format(DateTimeFormatter.ofPattern("EEE", Locale.getDefault())) }
        }
        val totalChange = metricChange(summary.totalSeconds, previous.totalSeconds)
        val rateChange = if (summary.focusRate != null && previous.focusRate != null) {
            (summary.focusRate!! - previous.focusRate!!) * 100.0
        } else {
            null
        }
        StatsUiState(
            loading = false,
            period = bundle.type,
            rangeLabel = bundle.rangeLabel,
            summary = summary,
            previous = previous,
            totalChangePercent = totalChange.percent,
            sessionChange = summary.completedSessions - previous.completedSessions,
            rateChangePoints = rateChange,
            chartValues = chart.map { it.seconds },
            chartLabels = labels,
            topTasks = summary.taskTotals.take(5),
            distractions = distractionItems,
            distractionMinutes = distractionItems.sumOf { it.durationMinutes },
            bestDay = summary.bestDay,
            tasks = tasks,
            categories = categories,
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = StatsUiState(),
    )

    fun setPeriod(value: PeriodType) {
        if (value == PeriodType.CUSTOM) return
        period.value = value
    }
}
