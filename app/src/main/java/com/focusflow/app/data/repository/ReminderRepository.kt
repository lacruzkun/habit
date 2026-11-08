package com.focusflow.app.data.repository

import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.db.ReminderEntity
import com.focusflow.app.domain.model.ReminderType
import com.focusflow.app.domain.time.TimeSource
import kotlinx.coroutines.flow.Flow

class ReminderRepository(
    private val database: FocusFlowDatabase,
    private val timeSource: TimeSource,
) {
    private val dao = database.reminderDao()

    val reminders: Flow<List<ReminderEntity>> = dao.observeAll()

    suspend fun getAll(): List<ReminderEntity> = dao.getAll()

    suspend fun getById(id: Long): ReminderEntity? = dao.getById(id)

    suspend fun seedDefault() {
        if (dao.getAll().isNotEmpty()) return
        dao.insert(
            ReminderEntity(
                title = "Plan tomorrow",
                message = "Take five minutes to choose tomorrow's focus.",
                type = ReminderType.DAILY_PLAN,
                timeMinutes = 21 * 60,
                daysMask = 0x7F,
                enabled = true,
                createdAt = timeSource.wallMillis(),
            ),
        )
    }

    suspend fun create(
        title: String,
        message: String,
        type: ReminderType,
        timeMinutes: Int,
        daysMask: Int,
    ): Long = dao.insert(
        ReminderEntity(
            title = title.trim(),
            message = message.trim(),
            type = type,
            timeMinutes = timeMinutes,
            daysMask = daysMask,
            enabled = true,
            createdAt = timeSource.wallMillis(),
        ),
    )

    suspend fun update(reminder: ReminderEntity) {
        dao.update(
            reminder.copy(
                title = reminder.title.trim(),
                message = reminder.message.trim(),
            ),
        )
    }

    suspend fun setEnabled(id: Long, enabled: Boolean) {
        val reminder = dao.getById(id) ?: return
        dao.update(reminder.copy(enabled = enabled))
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}
