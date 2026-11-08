package com.focusflow.app.data.repository

import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.db.HabitCompletionEntity
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.domain.time.TimeSource
import kotlinx.coroutines.flow.Flow

class HabitRepository(
    private val database: FocusFlowDatabase,
    private val timeSource: TimeSource,
) {
    private val habitDao = database.habitDao()

    val currentHabits: Flow<List<HabitEntity>> = habitDao.observeCurrent()
    val allHabits: Flow<List<HabitEntity>> = habitDao.observeAll()
    val archivedHabits: Flow<List<HabitEntity>> = habitDao.observeArchived()
    val allCompletions: Flow<List<HabitCompletionEntity>> = habitDao.observeAllCompletions()

    fun observeHabit(id: Long): Flow<HabitEntity?> = habitDao.observeById(id)

    fun observeCompletionsForHabit(id: Long): Flow<List<HabitCompletionEntity>> =
        habitDao.observeCompletionsForHabit(id)

    suspend fun getHabit(id: Long): HabitEntity? = habitDao.getById(id)

    suspend fun getAllHabits(): List<HabitEntity> = habitDao.getAll()

    suspend fun getAllCompletions(): List<HabitCompletionEntity> = habitDao.getAllCompletions()

    suspend fun createHabit(habit: HabitEntity): Long =
        habitDao.insert(
            habit.copy(
                createdAt = if (habit.createdAt == 0L) timeSource.wallMillis() else habit.createdAt,
            ),
        )

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.update(habit)
    }

    suspend fun archiveHabit(id: Long) {
        val habit = habitDao.getById(id) ?: return
        habitDao.update(habit.copy(archivedAt = timeSource.wallMillis()))
    }

    suspend fun restoreHabit(id: Long) {
        val habit = habitDao.getById(id) ?: return
        habitDao.update(habit.copy(archivedAt = null))
    }

    suspend fun deleteHabitPermanently(id: Long) {
        habitDao.deleteById(id)
    }

    suspend fun setCompletion(habitId: Long, epochDay: Long, value: Double) {
        val habit = habitDao.getById(habitId) ?: return
        val clamped = value.coerceAtLeast(0.0)
        if (clamped <= 0.0) {
            habitDao.deleteCompletion(habitId, epochDay)
            return
        }
        habitDao.upsertCompletion(
            HabitCompletionEntity(
                habitId = habitId,
                epochDay = epochDay,
                value = clamped,
                targetValue = habit.targetValue,
                completedAt = timeSource.wallMillis(),
            ),
        )
    }

    suspend fun toggleCompletion(habitId: Long, epochDay: Long): Boolean {
        val habit = habitDao.getById(habitId) ?: return false
        val existing = habitDao.getCompletionsForHabit(habitId)
            .firstOrNull { it.epochDay == epochDay }
        return if (existing != null) {
            habitDao.deleteCompletion(habitId, epochDay)
            false
        } else {
            habitDao.upsertCompletion(
                HabitCompletionEntity(
                    habitId = habitId,
                    epochDay = epochDay,
                    value = habit.targetValue,
                    targetValue = habit.targetValue,
                    completedAt = timeSource.wallMillis(),
                ),
            )
            true
        }
    }
}
