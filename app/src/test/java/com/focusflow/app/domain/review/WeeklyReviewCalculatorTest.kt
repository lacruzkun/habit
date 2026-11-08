package com.focusflow.app.domain.review

import com.focusflow.app.domain.habits.HabitFact
import com.focusflow.app.domain.model.HabitFrequency
import com.focusflow.app.domain.stats.DayFocus
import com.focusflow.app.domain.stats.DateRange
import com.focusflow.app.domain.stats.FocusSummary
import com.focusflow.app.domain.stats.TaskFocus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class WeeklyReviewCalculatorTest {

    private val start = LocalDate.of(2026, 1, 5)
    private val range = DateRange(start, start.plusDays(7))

    private fun summary(
        daily: List<DayFocus> = emptyList(),
        tasks: List<TaskFocus> = emptyList(),
        total: Long = daily.sumOf { it.seconds },
        started: Int = 0,
    ) = FocusSummary(
        range = range,
        totalSeconds = total,
        completedSessions = started,
        startedSessions = started,
        focusRate = if (started == 0) null else 1.0,
        averageDailySeconds = total / 7,
        daily = daily,
        taskTotals = tasks,
        bestDay = daily.filter { it.seconds > 0 }.maxByOrNull { it.seconds },
        topTask = tasks.firstOrNull(),
    )

    private val habit = HabitFact(
        id = 1,
        name = "Read",
        frequency = HabitFrequency.DAILY,
        daysMask = 0x7F,
        weeklyTargetCount = 3,
        targetValue = 1.0,
        startEpochDay = start.toEpochDay(),
        archived = false,
    )

    @Test
    fun `weekly goal sums active task targets`() {
        val goal = WeeklyReviewCalculator.weeklyGoalSeconds(
            listOf(1L to 300, 2L to 150, 3L to 0),
        )
        assertEquals(450L * 60L, goal)
    }

    @Test
    fun `best and worst ignore days without focus`() {
        val daily = listOf(
            DayFocus(start, 0),
            DayFocus(start.plusDays(1), 600),
            DayFocus(start.plusDays(2), 1_800),
            DayFocus(start.plusDays(3), 0),
        )
        val (best, worst) = WeeklyReviewCalculator.bestAndWorst(summary(daily = daily))
        assertEquals(start.plusDays(2), best)
        assertEquals(start.plusDays(1), worst)
    }

    @Test
    fun `best and worst are null without data`() {
        val (best, worst) = WeeklyReviewCalculator.bestAndWorst(summary())
        assertNull(best)
        assertNull(worst)
    }

    @Test
    fun `insight acknowledges an empty week`() {
        val insight = WeeklyReviewCalculator.buildInsight(summary(), summary(), emptyList())
        assertTrue(insight.contains("No focus time", ignoreCase = true))
    }

    @Test
    fun `insight highlights growth in the top task`() {
        val current = summary(
            daily = listOf(DayFocus(start, 3_600)),
            tasks = listOf(TaskFocus(1, "Write", 3_600, 2)),
            started = 2,
        )
        val previous = summary(
            daily = listOf(DayFocus(start.minusDays(3), 1_800)),
            tasks = listOf(TaskFocus(1, "Write", 1_800, 1)),
            started = 1,
        )
        val insight = WeeklyReviewCalculator.buildInsight(current, previous, emptyList())
        assertTrue(insight.contains("Write"))
        assertTrue(insight.isNotBlank())
    }
}
