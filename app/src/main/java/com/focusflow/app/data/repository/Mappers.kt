package com.focusflow.app.data.repository

import com.focusflow.app.data.db.FocusSessionEntity
import com.focusflow.app.data.db.HabitCompletionEntity
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.db.SessionSegmentRow
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.domain.habits.HabitCompletionFact
import com.focusflow.app.domain.habits.HabitFact
import com.focusflow.app.domain.stats.FocusSegmentFact
import com.focusflow.app.domain.stats.FocusSessionFact

fun FocusSessionEntity.toFact(): FocusSessionFact = FocusSessionFact(
    id = id,
    taskId = taskId,
    taskName = taskNameSnapshot,
    sessionType = sessionType,
    status = status,
    startedAt = startedAt,
    endedAt = endedAt,
    plannedSeconds = plannedSeconds,
    focusSeconds = focusSeconds,
    note = note,
)

fun SessionSegmentRow.toFact(): FocusSegmentFact = FocusSegmentFact(
    sessionId = sessionId,
    taskId = taskId,
    taskName = taskName,
    sessionType = sessionType,
    sessionStatus = sessionStatus,
    startMillis = segmentStart,
    endMillis = segmentEnd,
)

fun HabitEntity.toFact(): HabitFact = HabitFact(
    id = id,
    name = name,
    frequency = frequency,
    daysMask = daysMask,
    weeklyTargetCount = weeklyTargetCount,
    targetValue = targetValue,
    startEpochDay = startEpochDay,
    archived = archivedAt != null,
)

fun HabitCompletionEntity.toFact(): HabitCompletionFact = HabitCompletionFact(
    habitId = habitId,
    epochDay = epochDay,
    value = value,
    targetValue = targetValue,
)

fun TaskEntity.matchesSchedule(date: java.time.LocalDate): Boolean =
    scheduledDaysMask == 0 || com.focusflow.app.domain.model.maskHasDay(scheduledDaysMask, date.dayOfWeek)
