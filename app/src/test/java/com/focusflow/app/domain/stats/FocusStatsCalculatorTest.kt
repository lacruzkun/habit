package com.focusflow.app.domain.stats

import com.focusflow.app.domain.model.SessionStatus
import com.focusflow.app.domain.model.SessionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class FocusStatsCalculatorTest {

    private val zone: ZoneId = ZoneId.of("UTC")
    private val weekStartDate: LocalDate = LocalDate.of(2026, 1, 5)
    private val range = DateRange(weekStartDate, weekStartDate.plusDays(7))

    private fun millis(date: LocalDate, hour: Int, minute: Int): Long =
        date.atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()

    private fun segment(
        taskId: Long?,
        start: Long,
        end: Long,
        type: SessionType = SessionType.FOCUS,
    ) = FocusSegmentFact(
        sessionId = 1,
        taskId = taskId,
        taskName = "Write",
        sessionType = type,
        sessionStatus = SessionStatus.COMPLETED,
        startMillis = start,
        endMillis = end,
    )

    private fun session(
        id: Long,
        taskId: Long?,
        startedAt: Long,
        status: SessionStatus,
        type: SessionType = SessionType.FOCUS,
    ) = FocusSessionFact(
        id = id,
        taskId = taskId,
        taskName = "Write",
        sessionType = type,
        status = status,
        startedAt = startedAt,
        endedAt = startedAt + 1_800_000,
        plannedSeconds = 1_800,
        focusSeconds = 1_800,
    )

    @Test
    fun `summary counts only focus segments and completed focus sessions`() {
        val started = millis(weekStartDate, 9, 0)
        val summary = FocusStatsCalculator.summarize(
            FocusStatsRequest(
                range = range,
                segments = listOf(
                    segment(1, started, started + 1_800_000),
                    segment(1, started + 1_800_000, started + 2_100_000, SessionType.SHORT_BREAK),
                ),
                sessions = listOf(
                    session(1, 1, started, SessionStatus.COMPLETED),
                    session(2, 1, started + 2_100_000, SessionStatus.INTERRUPTED),
                ),
                zone = zone,
            ),
        )

        assertEquals(1_800L, summary.totalSeconds)
        assertEquals(1, summary.completedSessions)
        assertEquals(2, summary.startedSessions)
        assertEquals(0.5, summary.focusRate!!, 0.0001)
        assertEquals(1_800L / 7, summary.averageDailySeconds)
        assertEquals("Write", summary.taskTotals.single().name)
        assertEquals(weekStartDate, summary.bestDay?.date)
    }

    @Test
    fun `empty history produces null focus rate`() {
        val summary = FocusStatsCalculator.summarize(
            FocusStatsRequest(range, emptyList(), emptyList(), zone),
        )
        assertNull(summary.focusRate)
        assertEquals(0L, summary.totalSeconds)
        assertTrue(summary.daily.isEmpty() || summary.daily.all { it.seconds == 0L })
    }

    @Test
    fun `segments spanning midnight are split across days`() {
        val start = millis(weekStartDate, 23, 30)
        val end = millis(weekStartDate.plusDays(1), 0, 30)
        val daily = FocusStatsCalculator.dailyFocus(
            listOf(segment(1, start, end)),
            range,
            zone,
        )
        assertEquals(1_800L, daily[weekStartDate])
        assertEquals(1_800L, daily[weekStartDate.plusDays(1)])
    }

    @Test
    fun `bucket by month aggregates days`() {
        val daily = listOf(
            DayFocus(LocalDate.of(2026, 1, 2), 100),
            DayFocus(LocalDate.of(2026, 1, 20), 200),
            DayFocus(LocalDate.of(2026, 2, 3), 50),
        )
        val buckets = FocusStatsCalculator.bucketByMonth(
            DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 1)),
            daily,
        )
        assertEquals(300L, buckets[0].seconds)
        assertEquals(50L, buckets[1].seconds)
    }

    @Test
    fun `metric change flags new activity and handles zero baseline`() {
        val fresh = metricChange(current = 10, previous = 0)
        assertTrue(fresh.isNew)
        assertNull(fresh.percent)

        val flat = metricChange(current = 0, previous = 0)
        assertEquals(0.0, flat.percent!!, 0.0001)

        val up = metricChange(current = 150, previous = 100)
        assertEquals(50.0, up.percent!!, 0.0001)
        assertEquals(true, up.improved)
    }

    @Test
    fun `task summary filters other tasks`() {
        val started = millis(weekStartDate, 9, 0)
        val summary = FocusStatsCalculator.taskSummary(
            taskId = 1,
            segments = listOf(
                segment(1, started, started + 600_000),
                segment(2, started, started + 900_000),
            ),
            sessions = listOf(
                session(1, 1, started, SessionStatus.COMPLETED),
                session(2, 2, started, SessionStatus.COMPLETED),
            ),
            range = range,
            zone = zone,
        )
        assertEquals(600L, summary.totalSeconds)
        assertEquals(1, summary.completedSessions)
    }
}
