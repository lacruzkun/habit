package com.focusflow.app.data.repository

import com.focusflow.app.data.db.DistractionEntity
import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.domain.time.TimeSource
import kotlinx.coroutines.flow.Flow

class DistractionRepository(
    private val database: FocusFlowDatabase,
    private val timeSource: TimeSource,
) {
    private val dao = database.distractionDao()

    val allDistractions: Flow<List<DistractionEntity>> = dao.observeAll()

    suspend fun getAll(): List<DistractionEntity> = dao.getAll()

    suspend fun log(
        activity: String,
        startedAt: Long,
        endedAt: Long,
        note: String? = null,
    ): Long = dao.insert(
        DistractionEntity(
            activity = activity,
            startedAt = startedAt,
            endedAt = endedAt,
            note = note?.takeIf { it.isNotBlank() },
            createdAt = timeSource.wallMillis(),
        ),
    )

    suspend fun updateNote(id: Long, note: String?) {
        val current = dao.getAll().firstOrNull { it.id == id } ?: return
        dao.update(current.copy(note = note?.takeIf { it.isNotBlank() }))
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
