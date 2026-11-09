package com.focusflow.app.timer

import com.focusflow.app.data.db.ActiveTimerEntity
import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.db.FocusSegmentEntity
import com.focusflow.app.data.db.FocusSessionEntity
import com.focusflow.app.data.prefs.UserPreferencesRepository
import com.focusflow.app.data.repository.SessionRepository
import com.focusflow.app.domain.model.SessionStatus
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.time.TimeSource
import com.focusflow.app.domain.timer.TimerConfig
import com.focusflow.app.domain.timer.TimerMath
import com.focusflow.app.domain.timer.TimerSegment
import com.focusflow.app.domain.timer.TimerState
import com.focusflow.app.domain.timer.TimerStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

sealed interface TimerEvent {
    data class FocusCompleted(
        val session: FocusSessionEntity,
        val nextBreak: SessionType,
    ) : TimerEvent

    data class BreakCompleted(val sessionType: SessionType) : TimerEvent

    data class BreakStarted(val sessionType: SessionType) : TimerEvent

    data class SessionEnded(val status: SessionStatus) : TimerEvent
}

class TimerManager(
    private val database: FocusFlowDatabase,
    private val sessionRepository: SessionRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val timeSource: TimeSource,
    private val scope: CoroutineScope,
) {
    private val activeTimerDao = database.activeTimerDao()
    private val json = Json { ignoreUnknownKeys = true }

    private val _state = MutableStateFlow<TimerState?>(null)
    val state: StateFlow<TimerState?> = _state.asStateFlow()

    private val _tick = MutableStateFlow(timeSource.elapsedRealtime())
    val tick: StateFlow<Long> = _tick.asStateFlow()

    private val _events = MutableSharedFlow<TimerEvent>(extraBufferCapacity = 16)
    val events = _events.asSharedFlow()

    private var tickerJob: Job? = null
    private val mutex = Mutex()

    fun restore() {
        scope.launch {
            val saved = activeTimerDao.get() ?: return@launch
            val restored = saved.toTimerState()
            val active = _state.value
            if (active != null) return@launch
            _state.value = restored
            _tick.value = timeSource.elapsedRealtime()
            if (restored.status == TimerStatus.RUNNING) {
                startTicker()
            }
        }
    }

    suspend fun startFocus(
        taskId: Long?,
        taskName: String,
        config: TimerConfig,
    ) {
        mutex.withLock {
            if (_state.value != null) return
            val cycle = preferencesRepository.snapshot().focusCycleCount
            val state = TimerState(
                taskId = taskId,
                taskName = taskName,
                sessionType = SessionType.FOCUS,
                status = TimerStatus.RUNNING,
                plannedSeconds = config.secondsFor(SessionType.FOCUS),
                accumulatedMs = 0,
                runStartedAtElapsedMs = timeSource.elapsedRealtime(),
                runStartedAtWallMs = timeSource.wallMillis(),
                sessionStartedAtWallMs = timeSource.wallMillis(),
                cycleCount = cycle,
                config = config,
            )
            _state.value = state
            persist(state)
            _tick.value = timeSource.elapsedRealtime()
            startTicker()
        }
    }

    suspend fun pause() {
        mutex.withLock {
            val current = _state.value ?: return
            if (current.status != TimerStatus.RUNNING) return
            val nowElapsed = timeSource.elapsedRealtime()
            val nowWall = timeSource.wallMillis()
            val elapsed = TimerMath.elapsedMs(current, nowElapsed, nowWall)
            val segments = current.segments + closedSegment(current, nowWall)
            val paused = current.copy(
                status = TimerStatus.PAUSED,
                accumulatedMs = elapsed,
                runStartedAtElapsedMs = null,
                runStartedAtWallMs = null,
                segments = segments,
            )
            _state.value = paused
            tickerJob?.cancel()
            persist(paused)
        }
    }

    suspend fun resume() {
        mutex.withLock {
            val current = _state.value ?: return
            if (current.status != TimerStatus.PAUSED) return
            val resumed = current.copy(
                status = TimerStatus.RUNNING,
                runStartedAtElapsedMs = timeSource.elapsedRealtime(),
                runStartedAtWallMs = timeSource.wallMillis(),
            )
            _state.value = resumed
            persist(resumed)
            startTicker()
        }
    }

    suspend fun toggle() {
        when (_state.value?.status) {
            TimerStatus.RUNNING -> pause()
            TimerStatus.PAUSED -> resume()
            null -> Unit
        }
    }

    suspend fun cancelActive() {
        mutex.withLock { clearLocked() }
    }

    suspend fun endSession() {
        mutex.withLock {
            val current = _state.value ?: return
            if (current.isBreak) {
                skipBreakLocked()
                return
            }
            val nowElapsed = timeSource.elapsedRealtime()
            val nowWall = timeSource.wallMillis()
            val elapsed = TimerMath.elapsedMs(current, nowElapsed, nowWall)
            val segments = current.segments.toMutableList()
            if (current.status == TimerStatus.RUNNING) {
                segments += closedSegment(current, nowWall)
            }
            val status = if (elapsed >= 60_000L) SessionStatus.INTERRUPTED else SessionStatus.ABANDONED
            if (elapsed >= 1_000L) {
                saveFocusSession(
                    current = current,
                    claimedFocusSeconds = (elapsed / 1000L).toInt(),
                    status = status,
                    segments = segments,
                    endedAt = nowWall,
                )
            }
            preferencesRepository.setFocusCycleCount(0)
            clearLocked()
            _events.emit(TimerEvent.SessionEnded(status))
        }
    }

    suspend fun skipBreak() {
        mutex.withLock {
            if (_state.value?.isBreak != true) return
            skipBreakLocked()
        }
    }

    private suspend fun skipBreakLocked() {
        clearLocked()
        _events.emit(TimerEvent.SessionEnded(SessionStatus.ABANDONED))
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive) {
                val current = _state.value ?: break
                if (current.status != TimerStatus.RUNNING) break
                val nowElapsed = timeSource.elapsedRealtime()
                val nowWall = timeSource.wallMillis()
                _tick.value = nowElapsed
                if (TimerMath.isComplete(current, nowElapsed, nowWall)) {
                    completeLocked(current, nowElapsed, nowWall)
                    break
                }
                delay(500)
            }
        }
    }

    private suspend fun completeLocked(current: TimerState, nowElapsed: Long, nowWall: Long) {
        mutex.withLock {
            if (_state.value !== current) return
            val elapsedMs = TimerMath.elapsedMs(current, nowElapsed, nowWall)
                .coerceAtMost(current.plannedSeconds * 1000L)
            val segments = current.segments.toMutableList()
            if (current.status == TimerStatus.RUNNING) {
                val endWall = (current.runStartedAtWallMs ?: nowWall) +
                    (current.plannedSeconds * 1000L - current.accumulatedMs)
                segments += TimerSegment(
                    startMillis = current.runStartedAtWallMs ?: nowWall,
                    endMillis = endWall.coerceAtMost(nowWall),
                )
            }

            if (current.isBreak) {
                val session = FocusSessionEntity(
                    taskId = null,
                    taskNameSnapshot = current.taskName,
                    sessionType = current.sessionType,
                    status = SessionStatus.COMPLETED,
                    startedAt = current.sessionStartedAtWallMs,
                    endedAt = nowWall,
                    plannedSeconds = current.plannedSeconds,
                    focusSeconds = (elapsedMs / 1000L).toInt(),
                    createdAt = nowWall,
                )
                sessionRepository.saveCompletedSession(
                    session,
                    segments.map { FocusSegmentEntity(sessionId = 0, startedAt = it.startMillis, endedAt = it.endMillis) },
                )
                if (current.sessionType == SessionType.LONG_BREAK) {
                    preferencesRepository.setFocusCycleCount(0)
                }
                clearLocked()
                _events.emit(TimerEvent.BreakCompleted(current.sessionType))
                return
            }

            val session = saveFocusSession(
                current = current,
                claimedFocusSeconds = (elapsedMs / 1000L).toInt(),
                status = SessionStatus.COMPLETED,
                segments = segments,
                endedAt = nowWall,
            )
            val nextCycle = current.cycleCount + 1
            val longBreak = nextCycle >= current.config.sessionsBeforeLongBreak
            val nextBreak = if (longBreak) SessionType.LONG_BREAK else SessionType.SHORT_BREAK
            preferencesRepository.setFocusCycleCount(nextCycle)
            val breakConfig = current.config.copy(
                sessionsBeforeLongBreak = current.config.sessionsBeforeLongBreak,
            )
            val breakState = TimerState(
                taskId = null,
                taskName = if (longBreak) "Long break" else "Short break",
                sessionType = nextBreak,
                status = TimerStatus.RUNNING,
                plannedSeconds = breakConfig.secondsFor(nextBreak),
                accumulatedMs = 0,
                runStartedAtElapsedMs = timeSource.elapsedRealtime(),
                runStartedAtWallMs = timeSource.wallMillis(),
                sessionStartedAtWallMs = timeSource.wallMillis(),
                cycleCount = nextCycle,
                config = breakConfig,
            )
            _state.value = breakState
            persist(breakState)
            _events.emit(TimerEvent.FocusCompleted(session, nextBreak))
            _events.emit(TimerEvent.BreakStarted(nextBreak))
            startTicker()
        }
    }

    private suspend fun saveFocusSession(
        current: TimerState,
        claimedFocusSeconds: Int,
        status: SessionStatus,
        segments: List<TimerSegment>,
        endedAt: Long,
    ): FocusSessionEntity {
        val session = FocusSessionEntity(
            taskId = current.taskId,
            taskNameSnapshot = current.taskName,
            sessionType = SessionType.FOCUS,
            status = status,
            startedAt = current.sessionStartedAtWallMs,
            endedAt = endedAt,
            plannedSeconds = current.plannedSeconds,
            focusSeconds = claimedFocusSeconds.coerceAtMost(current.plannedSeconds),
            createdAt = endedAt,
        )
        sessionRepository.saveCompletedSession(
            session,
            segments.map { FocusSegmentEntity(sessionId = 0, startedAt = it.startMillis, endedAt = it.endMillis) },
        )
        return session
    }

    private fun closedSegment(state: TimerState, endWall: Long): TimerSegment {
        val start = state.runStartedAtWallMs ?: endWall
        return TimerSegment(startMillis = start, endMillis = endWall.coerceAtLeast(start))
    }

    private suspend fun clearLocked() {
        _state.value = null
        activeTimerDao.clear()
    }

    private fun persist(state: TimerState) {
        scope.launch {
            activeTimerDao.save(state.toEntity())
        }
    }

    private fun TimerState.toEntity(): ActiveTimerEntity = ActiveTimerEntity(
        taskId = taskId,
        taskName = taskName,
        sessionType = sessionType,
        status = status,
        plannedSeconds = plannedSeconds,
        accumulatedMs = accumulatedMs,
        runStartedAtElapsedMs = runStartedAtElapsedMs,
        runStartedAtWallMs = runStartedAtWallMs,
        sessionStartedAtWallMs = sessionStartedAtWallMs,
        cycleCount = cycleCount,
        focusMinutes = config.focusMinutes,
        shortBreakMinutes = config.shortBreakMinutes,
        longBreakMinutes = config.longBreakMinutes,
        sessionsBeforeLongBreak = config.sessionsBeforeLongBreak,
        segmentsJson = json.encodeToString(segments),
        updatedAt = timeSource.wallMillis(),
    )

    private fun ActiveTimerEntity.toTimerState(): TimerState = TimerState(
        taskId = taskId,
        taskName = taskName,
        sessionType = sessionType,
        status = status,
        plannedSeconds = plannedSeconds,
        accumulatedMs = accumulatedMs,
        runStartedAtElapsedMs = runStartedAtElapsedMs,
        runStartedAtWallMs = runStartedAtWallMs,
        sessionStartedAtWallMs = sessionStartedAtWallMs,
        cycleCount = cycleCount,
        config = TimerConfig(
            focusMinutes = focusMinutes,
            shortBreakMinutes = shortBreakMinutes,
            longBreakMinutes = longBreakMinutes,
            sessionsBeforeLongBreak = sessionsBeforeLongBreak,
        ),
        segments = runCatching { json.decodeFromString<List<TimerSegment>>(segmentsJson) }
            .getOrDefault(emptyList()),
    )
}
