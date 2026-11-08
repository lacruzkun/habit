package com.focusflow.app.domain.stats

import com.focusflow.app.domain.model.SessionStatus
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.model.TaskPriority
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.math.min

data class FocusSegmentFact(
    val sessionId: Long,
    val taskId: Long?,
    val taskName: String,
    val sessionType: SessionType,
    val sessionStatus: SessionStatus,
    val startMillis: Long,
    val endMillis: Long,
)

data class FocusSessionFact(
    val id: Long,
    val taskId: Long?,
    val taskName: String,
    val sessionType: SessionType,
    val status: SessionStatus,
    val startedAt: Long,
    val endedAt: Long,
    val plannedSeconds: Int,
    val focusSeconds: Int,
    val note: String? = null,
)

data class DayFocus(
    val date: LocalDate,
    val seconds: Long,
)

data class TaskFocus(
    val taskId: Long?,
    val name: String,
    val seconds: Long,
    val sessions: Int = 0,
)

data class FocusSummary(
    val range: DateRange,
    val totalSeconds: Long,
    val completedSessions: Int,
    val startedSessions: Int,
    val focusRate: Double?,
    val averageDailySeconds: Long,
    val daily: List<DayFocus>,
    val taskTotals: List<TaskFocus>,
    val bestDay: DayFocus?,
    val topTask: TaskFocus?,
) {
    val hasData: Boolean get() = totalSeconds > 0 || startedSessions > 0
}

data class MetricChange(
    val current: Long,
    val previous: Long,
    val percent: Double?,
    val isNew: Boolean,
) {
    val improved: Boolean? = when {
        isNew -> true
        percent == null -> null
        percent > 0.0 -> true
        percent < 0.0 -> false
        else -> null
    }
}

fun metricChange(current: Long, previous: Long): MetricChange {
    val isNew = previous == 0L && current > 0L
    val percent = if (previous == 0L) {
        if (current == 0L) 0.0 else null
    } else {
        (current - previous).toDouble() / previous * 100.0
    }
    return MetricChange(current, previous, percent, isNew)
}

data class FocusStatsRequest(
    val range: DateRange,
    val segments: List<FocusSegmentFact>,
    val sessions: List<FocusSessionFact>,
    val zone: ZoneId,
)

object FocusStatsCalculator {

    fun dailyFocus(
        segments: List<FocusSegmentFact>,
        range: DateRange,
        zone: ZoneId,
        focusOnly: Boolean = true,
    ): Map<LocalDate, Long> {
        val totals = rangeDates(range).associateWith { 0L }.toMutableMap()
        val rangeStart = range.startMillis(zone)
        val rangeEnd = range.endMillis(zone)
        segments.forEach { segment ->
            if (focusOnly && segment.sessionType != SessionType.FOCUS) return@forEach
            val start = maxOf(segment.startMillis, rangeStart)
            val end = minOf(segment.endMillis, rangeEnd)
            if (end <= start) return@forEach
            var cursor = start
            while (cursor < end) {
                val date = Instant.ofEpochMilli(cursor).atZone(zone).toLocalDate()
                val nextMidnight = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
                val pieceEnd = min(end, nextMidnight)
                val pieceMillis = (pieceEnd - cursor).coerceAtLeast(0)
                if (pieceMillis > 0) {
                    totals[date] = (totals[date] ?: 0L) + pieceMillis
                }
                cursor = if (pieceEnd <= cursor) cursor + 86_400_000L else pieceEnd
            }
        }
        return totals.mapValues { (_, millis) -> millis / 1000L }
    }

    fun taskFocus(
        segments: List<FocusSegmentFact>,
        sessions: List<FocusSessionFact>,
        range: DateRange,
        zone: ZoneId,
    ): List<TaskFocus> {
        val rangeStart = range.startMillis(zone)
        val rangeEnd = range.endMillis(zone)
        val totals = linkedMapOf<Long?, Long>()
        val names = mutableMapOf<Long?, String>()
        segments.forEach { segment ->
            if (segment.sessionType != SessionType.FOCUS) return@forEach
            val start = maxOf(segment.startMillis, rangeStart)
            val end = minOf(segment.endMillis, rangeEnd)
            if (end <= start) return@forEach
            totals[segment.taskId] = (totals[segment.taskId] ?: 0L) + (end - start)
            names[segment.taskId] = segment.taskName
        }
        val sessionCounts = sessions
            .filter { it.sessionType == SessionType.FOCUS && it.startedAt in rangeStart until rangeEnd }
            .groupingBy { it.taskId }
            .eachCount()
        return totals.map { (taskId, millis) ->
            TaskFocus(
                taskId = taskId,
                name = names[taskId] ?: "Deleted task",
                seconds = millis / 1000L,
                sessions = sessionCounts[taskId] ?: 0,
            )
        }.sortedByDescending { it.seconds }
    }

    fun summarize(request: FocusStatsRequest): FocusSummary {
        val dailyMap = dailyFocus(request.segments, request.range, request.zone)
        val daily = dailyMap.map { DayFocus(it.key, it.value) }.sortedBy { it.date }
        val total = daily.sumOf { it.seconds }
        val focusSessions = request.sessions.filter {
            it.sessionType == SessionType.FOCUS &&
                it.startedAt >= request.range.startMillis(request.zone) &&
                it.startedAt < request.range.endMillis(request.zone)
        }
        val completed = focusSessions.count { it.status == SessionStatus.COMPLETED }
        val started = focusSessions.size
        val rate = if (started == 0) null else completed.toDouble() / started
        val taskTotals = taskFocus(request.segments, request.sessions, request.range, request.zone)
        return FocusSummary(
            range = request.range,
            totalSeconds = total,
            completedSessions = completed,
            startedSessions = started,
            focusRate = rate,
            averageDailySeconds = total / request.range.days.coerceAtLeast(1),
            daily = daily,
            taskTotals = taskTotals,
            bestDay = daily.filter { it.seconds > 0 }.maxByOrNull { it.seconds },
            topTask = taskTotals.firstOrNull(),
        )
    }

    fun taskSummary(
        taskId: Long,
        segments: List<FocusSegmentFact>,
        sessions: List<FocusSessionFact>,
        range: DateRange,
        zone: ZoneId,
    ): FocusSummary {
        val taskSegments = segments.filter { it.taskId == taskId }
        val taskSessions = sessions.filter { it.taskId == taskId }
        return summarize(
            FocusStatsRequest(
                range = range,
                segments = taskSegments,
                sessions = taskSessions,
                zone = zone,
            ),
        )
    }

    fun rangeDates(range: DateRange): List<LocalDate> =
        generateSequence(range.start) { date ->
            date.plusDays(1).takeIf { it.isBefore(range.endExclusive) }
        }.toList()

    fun bucketByMonth(range: DateRange, daily: List<DayFocus>): List<DayFocus> {
        val grouped = daily.groupBy { LocalDate.of(it.date.year, it.date.monthValue, 1) }
        return grouped.map { (month, values) -> DayFocus(month, values.sumOf { it.seconds }) }
            .sortedBy { it.date }
    }

    fun bucketByWeek(range: DateRange, daily: List<DayFocus>, weekStart: java.time.DayOfWeek): List<DayFocus> {
        val grouped = daily.groupBy {
            it.date.with(java.time.temporal.TemporalAdjusters.previousOrSame(weekStart))
        }
        return grouped.map { (week, values) -> DayFocus(week, values.sumOf { it.seconds }) }
            .sortedBy { it.date }
    }

    fun priorityWeight(priority: TaskPriority): Int = priority.level
}
