package com.focusflow.app.data.repository

import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.db.ReflectionEntity
import com.focusflow.app.domain.time.TimeSource
import kotlinx.coroutines.flow.Flow

class ReflectionRepository(
    private val database: FocusFlowDatabase,
    private val timeSource: TimeSource,
) {
    private val dao = database.reflectionDao()

    fun observeWeek(weekStartEpochDay: Long): Flow<ReflectionEntity?> =
        dao.observeForWeek(weekStartEpochDay)

    val allReflections: Flow<List<ReflectionEntity>> = dao.observeAll()

    suspend fun getAll(): List<ReflectionEntity> = dao.getAll()

    suspend fun save(
        weekStartEpochDay: Long,
        wentWell: String,
        needsImprovement: String,
        nextFocus: String,
    ): Long = dao.upsert(
        ReflectionEntity(
            weekStartEpochDay = weekStartEpochDay,
            wentWell = wentWell.trim(),
            needsImprovement = needsImprovement.trim(),
            nextFocus = nextFocus.trim(),
            updatedAt = timeSource.wallMillis(),
        ),
    )
}
