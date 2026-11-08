package com.focusflow.app.domain.review

import com.focusflow.app.domain.habits.HabitCalculator
import com.focusflow.app.domain.habits.HabitFact
import com.focusflow.app.domain.habits.HabitPeriodStats
import com.focusflow.app.domain.model.WeekStart
import com.focusflow.app.domain.stats.DateRange
import com.focusflow.app.domain.stats.FocusSummary
import com.focusflow.app.domain.stats.TaskFocus
import com.focusflow.app.domain.stats.metricChange
import com.focusflow.app.domain.time.formatDuration
import java.time.LocalDate
import kotlin.math.abs
import kotlin.math.roundToInt

data class WeeklyReviewData(
    val range: DateRange,
    val current: FocusSummary,
    val previous: FocusSummary,
    val habitStats: List<Pair<HabitFact, HabitPeriodStats>>,
    val taskTotals: List<TaskFocus>,
    val weeklyGoalSeconds: Long,
    val insight: String,
    val bestDay: LocalDate?,
    val leastProductiveDay: LocalDate?,
)

object WeeklyReviewCalculator {

    fun weeklyGoalSeconds(
        activeTasks: List<Pair<Long, Int>>,
    ): Long = activeTasks.sumOf { it.second.coerceAtLeast(0).toLong() * 60L }

    fun bestAndWorst(summary: FocusSummary): Pair<LocalDate?, LocalDate?> {
        val nonEmpty = summary.daily.filter { it.seconds > 0 }
        if (nonEmpty.isEmpty()) return null to null
        val best = nonEmpty.maxByOrNull { it.seconds }?.date
        val worst = nonEmpty.minByOrNull { it.seconds }?.date
        return best to worst
    }

    fun buildInsight(
        current: FocusSummary,
        previous: FocusSummary,
        habitStats: List<Pair<HabitFact, HabitPeriodStats>>,
    ): String {
        if (current.totalSeconds == 0L && current.startedSessions == 0) {
            return "No focus time recorded yet this week. A five-minute session is enough to begin."
        }
        val biggestTask = current.taskTotals.firstOrNull()
        val previousTask = biggestTask?.let { task ->
            previous.taskTotals.firstOrNull { it.taskId == task.taskId }
        }
        if (biggestTask != null && previousTask != null && previousTask.seconds > 0) {
            val change = metricChange(biggestTask.seconds, previousTask.seconds)
            val percent = change.percent
            if (percent != null && abs(percent) >= 10.0) {
                val direction = if (percent > 0) "more" else "less"
                return "You spent ${abs(percent).roundToInt()}% $direction time on ${biggestTask.name} this week."
            }
        }
        val bestHabit = habitStats
            .filter { it.second.consistencyPercent != null && it.second.scheduledOccurrences > 0 }
            .maxByOrNull { it.second.consistencyPercent ?: 0.0 }
        if (bestHabit != null) {
            val stats = bestHabit.second
            val percent = stats.consistencyPercent ?: 0.0
            if (percent >= 50.0) {
                return "${bestHabit.first.name} was your most consistent habit this week (${percent.roundToInt()}%)."
            }
        }
        val sessionLabel = if (current.completedSessions == 1) "session" else "sessions"
        return "You focused ${formatDuration(current.totalSeconds)} across ${current.completedSessions} completed $sessionLabel this week."
    }

    fun habitStats(
        habits: List<HabitFact>,
        completionsByHabit: Map<Long, List<com.focusflow.app.domain.habits.HabitCompletionFact>>,
        range: DateRange,
        today: LocalDate,
        weekStart: WeekStart,
    ): List<Pair<HabitFact, HabitPeriodStats>> =
        habits.filter { !it.archived }.map { habit ->
            habit to HabitCalculator.stats(
                habit,
                completionsByHabit[habit.id].orEmpty(),
                range,
                today,
                weekStart,
            )
        }
}
