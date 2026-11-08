package com.focusflow.app.domain.timer

import com.focusflow.app.domain.model.SessionType
import org.junit.Assert.assertEquals
import org.junit.Test

class TimerMathTest {

    private val config = TimerConfig(
        focusMinutes = 25,
        shortBreakMinutes = 5,
        longBreakMinutes = 15,
        sessionsBeforeLongBreak = 4,
    )

    private fun state(
        status: TimerStatus = TimerStatus.RUNNING,
        accumulatedMs: Long = 0,
        runStartedAtElapsedMs: Long? = 10_000,
        runStartedAtWallMs: Long? = 1_700_000_000_000,
        plannedSeconds: Int = 1_500,
        cycleCount: Int = 0,
    ) = TimerState(
        taskId = 1,
        taskName = "Deep work",
        sessionType = SessionType.FOCUS,
        status = status,
        plannedSeconds = plannedSeconds,
        accumulatedMs = accumulatedMs,
        runStartedAtElapsedMs = runStartedAtElapsedMs,
        runStartedAtWallMs = runStartedAtWallMs,
        sessionStartedAtWallMs = 1_700_000_000_000,
        cycleCount = cycleCount,
        config = config,
    )

    @Test
    fun `paused timer keeps accumulated time`() {
        val paused = state(status = TimerStatus.PAUSED, accumulatedMs = 5_000)
        assertEquals(5_000L, TimerMath.elapsedMs(paused, nowElapsedMs = 99_000, nowWallMs = 0))
    }

    @Test
    fun `running timer adds elapsed realtime delta`() {
        val running = state(accumulatedMs = 1_000)
        assertEquals(
            6_000L,
            TimerMath.elapsedMs(running, nowElapsedMs = 15_000, nowWallMs = 1_700_000_005_000),
        )
    }

    @Test
    fun `reboot fallback uses wall clock and never goes negative`() {
        val running = state(accumulatedMs = 2_000)
        assertEquals(
            9_000L,
            TimerMath.elapsedMs(running, nowElapsedMs = 500, nowWallMs = 1_700_000_007_000),
        )
        assertEquals(
            2_000L,
            TimerMath.elapsedMs(running, nowElapsedMs = 500, nowWallMs = 1_699_999_999_000),
        )
    }

    @Test
    fun `remaining seconds clamps at zero`() {
        val running = state(plannedSeconds = 60, accumulatedMs = 0)
        assertEquals(
            0L,
            TimerMath.remainingSeconds(running, nowElapsedMs = 200_000, nowWallMs = 1_700_000_200_000),
        )
    }

    @Test
    fun `config exposes seconds for each session type`() {
        assertEquals(1_500, config.secondsFor(SessionType.FOCUS))
        assertEquals(300, config.secondsFor(SessionType.SHORT_BREAK))
        assertEquals(900, config.secondsFor(SessionType.LONG_BREAK))
    }
}
