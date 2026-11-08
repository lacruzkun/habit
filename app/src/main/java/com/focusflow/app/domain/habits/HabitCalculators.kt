package com.focusflow.app.domain.habits

import com.focusflow.app.domain.model.HabitFrequency
import com.focusflow.app.domain.model.WeekStart
import com.focusflow.app.domain.model.maskHasDay
import com.focusflow.app.domain.stats.DateRange
import com.focusflow.app.domain.time.epochDayToDate
import com.focusflow.app.domain.time.startOfWeek
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class HabitFact(
    val id: Long,
    val name: String,
    val frequency: HabitFrequency,
    val daysMask: Int,
    val weeklyTargetCount: Int,
    val targetValue: Double,
    val startEpochDay: Long,
    val archived: Boolean,
)

data class HabitCompletionFact(
    val habitId: Long,
    val epochDay: Long,
    val value: Double,
    val targetValue: Double,
)

data class HabitPeriodStats(
    val scheduledOccurrences: Int,
    val completedOccurrences: Int,
    val consistencyPercent: Double?,
    val currentStreak: Int,
    val longestStreak: Int,
    val streakUnit: String,
    val todayScheduled: Boolean,
    val todayCompleted: Boolean,
)

object HabitCalculator {

    fun isScheduled(habit: HabitFact, date: LocalDate): Boolean = when (habit.frequency) {
        HabitFrequency.DAILY -> true
        HabitFrequency.WEEKLY_DAYS -> maskHasDay(habit.daysMask, date.dayOfWeek)
        HabitFrequency.WEEKLY_COUNT -> false
    }

    fun completionQualifies(completion: HabitCompletionFact): Boolean =
        completion.value >= completion.targetValue && completion.targetValue > 0.0

    fun completedDates(completions: List<HabitCompletionFact>): Set<Long> =
        completions.filter { completionQualifies(it) }.map { it.epochDay }.toSet()

    fun stats(
        habit: HabitFact,
        completions: List<HabitCompletionFact>,
        range: DateRange,
        today: LocalDate,
        weekStart: WeekStart = WeekStart.MONDAY,
    ): HabitPeriodStats {
        if (habit.frequency == HabitFrequency.WEEKLY_COUNT) {
            return weeklyCountStats(habit, completions, range, today, weekStart)
        }
        val completed = completedDates(completions)
        val startDate = epochDayToDate(habit.startEpochDay)
        val effectiveStart = maxOf(range.start, startDate)
        val effectiveEnd = range.endExclusive.minusDays(1)
        if (effectiveEnd.isBefore(effectiveStart)) {
            return HabitPeriodStats(0, 0, null, 0, 0, "day", false, today.toEpochDay() in completed)
        }
        var scheduled = 0
        var done = 0
        var date = effectiveStart
        while (!date.isAfter(effectiveEnd)) {
            if (isScheduled(habit, date)) {
                scheduled++
                if (date.toEpochDay() in completed) done++
            }
            date = date.plusDays(1)
        }
        val consistency = if (scheduled == 0) null else done.toDouble() / scheduled * 100.0
        val current = currentDayStreak(habit, completed, today)
        val longest = longestDayStreak(habit, completed, today)
        return HabitPeriodStats(
            scheduledOccurrences = scheduled,
            completedOccurrences = done,
            consistencyPercent = consistency,
            currentStreak = current,
            longestStreak = longest,
            streakUnit = "day",
            todayScheduled = isScheduled(habit, today),
            todayCompleted = today.toEpochDay() in completed,
        )
    }

    private fun currentDayStreak(
        habit: HabitFact,
        completed: Set<Long>,
        today: LocalDate,
    ): Int {
        val startDate = epochDayToDate(habit.startEpochDay)
        if (today.isBefore(startDate)) return 0
        var streak = 0
        var date = today
        var isToday = true
        while (!date.isBefore(startDate)) {
            if (isScheduled(habit, date)) {
                val done = date.toEpochDay() in completed
                if (done) {
                    streak++
                } else if (!isToday) {
                    break
                }
            }
            date = date.minusDays(1)
            isToday = false
        }
        return streak
    }

    private fun longestDayStreak(
        habit: HabitFact,
        completed: Set<Long>,
        today: LocalDate,
    ): Int {
        val startDate = epochDayToDate(habit.startEpochDay)
        if (today.isBefore(startDate)) return 0
        var longest = 0
        var running = 0
        var date = startDate
        while (!date.isAfter(today)) {
            if (isScheduled(habit, date)) {
                if (date.toEpochDay() in completed) {
                    running++
                    if (running > longest) longest = running
                } else if (date != today) {
                    running = 0
                }
            }
            date = date.plusDays(1)
        }
        return longest
    }

    private fun weeklyCountStats(
        habit: HabitFact,
        completions: List<HabitCompletionFact>,
        range: DateRange,
        today: LocalDate,
        weekStart: WeekStart,
    ): HabitPeriodStats {
        val target = habit.weeklyTargetCount.coerceAtLeast(1)
        val startDate = epochDayToDate(habit.startEpochDay)
        val completed = completedDates(completions)
        val firstWeek = earliestWeekStart(maxOf(startDate, range.start), weekStart)
        val lastWeek = earliestWeekStart(yesterdayOrToday(today), weekStart)
        if (lastWeek.isBefore(firstWeek)) {
            return HabitPeriodStats(0, 0, null, 0, 0, "week", isScheduled(habit, today), today.toEpochDay() in completed)
        }
        var totalWeeks = 0
        var metWeeks = 0
        var week = firstWeek
        while (!week.isAfter(lastWeek)) {
            totalWeeks++
            if (countInWeek(completed, week) >= target) metWeeks++
            week = week.plusWeeks(1)
        }
        val consistency = if (totalWeeks == 0) null else metWeeks.toDouble() / totalWeeks * 100.0
        var current = 0
        var cursor = lastWeek
        var isCurrentWeek = true
        while (!cursor.isBefore(firstWeek)) {
            val met = countInWeek(completed, cursor) >= target
            if (met) {
                current++
            } else if (isCurrentWeek && cursor == lastWeek && countInWeek(completed, cursor) < target) {
                // The current week is still in progress.
            } else {
                break
            }
            cursor = cursor.minusWeeks(1)
            isCurrentWeek = false
        }
        var longest = 0
        var running = 0
        week = firstWeek
        while (!week.isAfter(lastWeek)) {
            if (countInWeek(completed, week) >= target) {
                running++
                if (running > longest) longest = running
            } else if (week != lastWeek) {
                running = 0
            }
            week = week.plusWeeks(1)
        }
        return HabitPeriodStats(
            scheduledOccurrences = totalWeeks * target,
            completedOccurrences = metWeeks * target,
            consistencyPercent = consistency,
            currentStreak = current,
            longestStreak = longest,
            streakUnit = "week",
            todayScheduled = isScheduled(habit, today),
            todayCompleted = today.toEpochDay() in completed,
        )
    }

    private fun earliestWeekStart(date: LocalDate, weekStart: WeekStart): LocalDate =
        startOfWeek(date, weekStart)

    private fun yesterdayOrToday(today: LocalDate): LocalDate = today

    private fun countInWeek(completed: Set<Long>, weekStartDate: LocalDate): Int {
        var count = 0
        for (offset in 0..6) {
            if (weekStartDate.plusDays(offset.toLong()).toEpochDay() in completed) count++
        }
        return count
    }

    fun weeklyCompletionCount(
        completions: List<HabitCompletionFact>,
        weekStartDate: LocalDate,
    ): Int = countInWeek(completedDates(completions), weekStartDate)

    fun bestHabit(
        habits: List<HabitFact>,
        completionsByHabit: Map<Long, List<HabitCompletionFact>>,
        range: DateRange,
        today: LocalDate,
        weekStart: WeekStart,
    ): Pair<HabitFact, HabitPeriodStats>? =
        habits.filter { !it.archived }
            .map { habit ->
                habit to stats(habit, completionsByHabit[habit.id].orEmpty(), range, today, weekStart)
            }
            .filter { it.second.consistencyPercent != null && it.second.scheduledOccurrences > 0 }
            .maxByOrNull { it.second.consistencyPercent ?: 0.0 }

    fun latestStreak(habit: HabitFact, completions: List<HabitCompletionFact>, today: LocalDate): Int =
        stats(
            habit,
            completions,
            DateRange(epochDayToDate(habit.startEpochDay), today.plusDays(1)),
            today,
        ).currentStreak

    fun monthRangeFor(date: LocalDate): DateRange =
        DateRange(date.withDayOfMonth(1), date.withDayOfMonth(1).plusMonths(1))

    fun lastNDaysRange(today: LocalDate, days: Long): DateRange =
        DateRange(today.minusDays(days - 1), today.plusDays(1))

    fun daysSinceStart(habit: HabitFact, today: LocalDate): Long =
        ChronoUnit.DAYS.between(epochDayToDate(habit.startEpochDay), today).coerceAtLeast(0)
}
