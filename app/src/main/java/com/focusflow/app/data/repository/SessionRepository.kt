package com.focusflow.app.data.repository

import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.db.FocusSegmentEntity
import com.focusflow.app.data.db.FocusSessionEntity
import com.focusflow.app.data.db.SessionSegmentRow
import com.focusflow.app.domain.model.SessionType
import kotlinx.coroutines.flow.Flow

class SessionRepository(private val database: FocusFlowDatabase) {
    private val sessionDao = database.focusSessionDao()

    val allSessions: Flow<List<FocusSessionEntity>> = sessionDao.observeAllSessions()

    fun recentSessions(limit: Int = 20): Flow<List<FocusSessionEntity>> =
        sessionDao.observeRecentSessions(limit)

    fun sessionsForTask(taskId: Long, limit: Int = 20): Flow<List<FocusSessionEntity>> =
        sessionDao.observeSessionsForTask(taskId, limit)

    fun sessionsStarted(start: Long, end: Long): Flow<List<FocusSessionEntity>> =
        sessionDao.observeSessionsStarted(start, end)

    fun segments(start: Long, end: Long): Flow<List<SessionSegmentRow>> =
        sessionDao.observeSegments(start, end)

    suspend fun allSessionsSnapshot(): List<FocusSessionEntity> = sessionDao.getAllSessions()

    suspend fun allSegmentsSnapshot(): List<FocusSegmentEntity> = sessionDao.getAllSegments()

    suspend fun sessionsStartedSnapshot(start: Long, end: Long): List<FocusSessionEntity> =
        sessionDao.getSessionsStarted(start, end)

    suspend fun segmentsSnapshot(start: Long, end: Long): List<SessionSegmentRow> =
        sessionDao.getSegments(start, end)

    suspend fun saveCompletedSession(
        session: FocusSessionEntity,
        segments: List<FocusSegmentEntity>,
    ): Long {
        val id = sessionDao.insertSession(session.copy(id = 0))
        if (segments.isNotEmpty()) {
            sessionDao.insertSegments(segments.map { it.copy(sessionId = id, id = 0) })
        }
        return id
    }

    suspend fun saveSessions(sessions: List<FocusSessionEntity>, segments: List<FocusSegmentEntity>) {
        if (sessions.isEmpty()) return
        sessionDao.insertSessions(sessions)
        if (segments.isNotEmpty()) sessionDao.insertSegments(segments)
    }

    fun isFocusCompleted(session: FocusSessionEntity): Boolean =
        session.sessionType == SessionType.FOCUS &&
            session.status == com.focusflow.app.domain.model.SessionStatus.COMPLETED
}
