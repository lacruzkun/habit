package com.focusflow.app.domain.timer

import com.focusflow.app.domain.model.SessionType
import kotlinx.serialization.Serializable

@Serializable
data class TimerConfig(
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
) {
    fun minutesFor(type: SessionType): Int = when (type) {
        SessionType.FOCUS -> focusMinutes
        SessionType.SHORT_BREAK -> shortBreakMinutes
        SessionType.LONG_BREAK -> longBreakMinutes
    }

    fun secondsFor(type: SessionType): Int = minutesFor(type).coerceAtLeast(1) * 60
}

@Serializable
data class TimerSegment(
    val startMillis: Long,
    val endMillis: Long,
)

enum class TimerStatus {
    RUNNING,
    PAUSED,
}

data class TimerState(
    val taskId: Long?,
    val taskName: String,
    val sessionType: SessionType,
    val status: TimerStatus,
    val plannedSeconds: Int,
    val accumulatedMs: Long,
    val runStartedAtElapsedMs: Long?,
    val runStartedAtWallMs: Long?,
    val sessionStartedAtWallMs: Long,
    val cycleCount: Int,
    val config: TimerConfig,
    val segments: List<TimerSegment> = emptyList(),
) {
    val isBreak: Boolean get() = sessionType != SessionType.FOCUS
}

object TimerMath {

    fun elapsedMs(state: TimerState, nowElapsedMs: Long, nowWallMs: Long): Long {
        val base = state.accumulatedMs.coerceAtLeast(0)
        val runStartElapsed = state.runStartedAtElapsedMs
        val runStartWall = state.runStartedAtWallMs
        if (state.status != TimerStatus.RUNNING || runStartElapsed == null || runStartWall == null) {
            return base
        }
        val delta = if (nowElapsedMs >= runStartElapsed) {
            nowElapsedMs - runStartElapsed
        } else {
            // Device rebooted: elapsedRealtime restarted. Fall back to wall clock,
            // but never report a negative elapsed time.
            (nowWallMs - runStartWall).coerceAtLeast(0)
        }
        return base + delta
    }

    fun remainingSeconds(state: TimerState, nowElapsedMs: Long, nowWallMs: Long): Long {
        val elapsedSeconds = elapsedMs(state, nowElapsedMs, nowWallMs) / 1000L
        return (state.plannedSeconds - elapsedSeconds).coerceAtLeast(0)
    }

    fun progress(state: TimerState, nowElapsedMs: Long, nowWallMs: Long): Float {
        if (state.plannedSeconds <= 0) return 0f
        return (elapsedMs(state, nowElapsedMs, nowWallMs).toDouble() /
            (state.plannedSeconds * 1000.0)).coerceIn(0.0, 1.0).toFloat()
    }

    fun isComplete(state: TimerState, nowElapsedMs: Long, nowWallMs: Long): Boolean =
        elapsedMs(state, nowElapsedMs, nowWallMs) >= state.plannedSeconds * 1000L

    fun completedFocusSessionsInCycle(cycleCount: Int): Int = cycleCount
}
