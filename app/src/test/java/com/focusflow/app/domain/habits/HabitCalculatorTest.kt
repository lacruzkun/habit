package com.focusflow.app.domain.habits

import com.focusflow.app.domain.model.HabitFrequency
import com.focusflow.app.domain.model.WeekStart
import com.focusflow.app.domain.stats.DateRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class HabitCalculatorTest {

    private val start: LocalDate = LocalDate.of(2026, 1, 1)
    private val today: LocalDate = LocalDate.of(2026, 1, 5)

    private fun habit(
        frequency: HabitFrequency = HabitFrequency.DAILY,
        daysMask: Int = 0x7F,
        weeklyTarget: Int = 3,
        targetValue: Double = 1.0,
    ) = HabitFact(
        id = 1,
        name = "Read",
        frequency = frequency,
        daysMask = daysMask,
        weeklyTargetCount = weeklyTarget,
        targetValue = targetValue,
        startEpochDay = start.toEpochDay(),
        archived = false,
    )

    private fun done(vararg dates: LocalDate, value: Double = 1.0): List<HabitCompletionFact> =
        dates.map {
            HabitCompletionFact(
                habitId = 1,
                epochDay = it.toEpochDay(),
                value = value,
                targetValue = 1.0,
            )
        }

    @Test
    fun `daily stats count scheduled days and completions`() {
        val stats = HabitCalculator.stats(
            habit = habit(),
            completions = done(start, start.plusDays(1), start.plusDays(2)),
            range = DateRange(start, today.plusDays(1)),
            today = today,
        )
        assertEquals(5, stats.scheduledOccurrences)
        assertEquals(3, stats.completedOccurrences)
        assertEquals(60.0, stats.consistencyPercent!!, 0.0001)
        assertEquals(3, stats.longestStreak)
        assertEquals(0, stats.currentStreak)
    }

    @Test
    fun `current streak allows missing today but breaks on yesterday`() {
        val completions = done(today.minusDays(1), today.minusDays(2), today.minusDays(3))
        val stats = HabitCalculator.stats(
            habit = habit(),
            completions = completions,
            range = DateRange(start, today.plusDays(1)),
            today = today,
        )
        assertEquals(3, stats.currentStreak)
    }

    @Test
    fun `current streak counts today when completed`() {
        val stats = HabitCalculator.stats(
            habit = habit(),
            completions = done(today, today.minusDays(1)),
            range = DateRange(start, today.plusDays(1)),
            today = today,
        )
        assertEquals(2, stats.currentStreak)
    }

    @Test
    fun `partial values do not qualify`() {
        val partial = listOf(
            HabitCompletionFact(1, today.toEpochDay(), value = 0.5, targetValue = 1.0),
        )
        val stats = HabitCalculator.stats(
            habit = habit(),
            completions = partial,
            range = DateRange(start, today.plusDays(1)),
            today = today,
        )
        assertEquals(0, stats.completedOccurrences)
        assertEquals(false, stats.todayCompleted)
    }

    @Test
    fun `weekly count habit tracks met weeks`() {
        val completions = done(
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 6),
            LocalDate.of(2026, 1, 7),
        )
        val stats = HabitCalculator.stats(
            habit = habit(frequency = HabitFrequency.WEEKLY_COUNT, weeklyTarget = 3),
            completions = completions,
            range = DateRange(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 12)),
            today = LocalDate.of(2026, 1, 7),
            weekStart = WeekStart.MONDAY,
        )
        assertEquals(1, stats.currentStreak)
        assertEquals(3, stats.completedOccurrences)
        assertEquals("week", stats.streakUnit)
    }

    @Test
    fun `weekly completion count only counts qualifying days in week`() {
        val completions = done(
            LocalDate.of(2026, 1, 5),
            LocalDate.of(2026, 1, 6),
            LocalDate.of(2026, 1, 12),
        )
        assertEquals(
            2,
            HabitCalculator.weeklyCompletionCount(completions, LocalDate.of(2026, 1, 5)),
        )
    }

    @Test
    fun `specific day habit only schedules selected weekdays`() {
        val mondayWednesdayMask = (1 shl 0) or (1 shl 2)
        val stats = HabitCalculator.stats(
            habit = habit(
                frequency = HabitFrequency.WEEKLY_DAYS,
                daysMask = mondayWednesdayMask,
            ),
            completions = done(LocalDate.of(2026, 1, 5)),
            range = DateRange(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 12)),
            today = LocalDate.of(2026, 1, 7),
        )
        assertEquals(2, stats.scheduledOccurrences)
        assertEquals(1, stats.completedOccurrences)
        assertEquals(50.0, stats.consistencyPercent!!, 0.0001)
        assertEquals(true, stats.todayScheduled)
    }

    @Test
    fun `weekly target follows habit frequency`() {
        assertEquals(7, HabitCalculator.weeklyTarget(habit()))
        assertEquals(
            2,
            HabitCalculator.weeklyTarget(
                habit(
                    frequency = HabitFrequency.WEEKLY_DAYS,
                    daysMask = (1 shl 0) or (1 shl 2),
                ),
            ),
        )
        assertEquals(
            3,
            HabitCalculator.weeklyTarget(
                habit(frequency = HabitFrequency.WEEKLY_COUNT, weeklyTarget = 3),
            ),
        )
    }

    @Test
    fun `no scheduled days yields null consistency`() {
        val stats = HabitCalculator.stats(
            habit = habit(frequency = HabitFrequency.WEEKLY_DAYS, daysMask = 1 shl 6),
            completions = emptyList(),
            range = DateRange(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 10)),
            today = LocalDate.of(2026, 1, 7),
        )
        assertNull(stats.consistencyPercent)
        assertEquals(0, stats.scheduledOccurrences)
    }
}
