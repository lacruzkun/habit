package com.focusflow.app.data.export

import com.focusflow.app.data.db.CategoryEntity
import com.focusflow.app.data.db.DistractionEntity
import com.focusflow.app.data.db.FocusSegmentEntity
import com.focusflow.app.data.db.FocusSessionEntity
import com.focusflow.app.data.db.HabitCompletionEntity
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.db.ReflectionEntity
import com.focusflow.app.data.db.ReminderEntity
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.data.prefs.UserPreferences
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val schemaVersion: Int = 1,
    val exportedAt: Long = 0,
    val preferences: UserPreferences = UserPreferences(),
    val categories: List<CategoryEntity> = emptyList(),
    val tasks: List<TaskEntity> = emptyList(),
    val sessions: List<FocusSessionEntity> = emptyList(),
    val segments: List<FocusSegmentEntity> = emptyList(),
    val habits: List<HabitEntity> = emptyList(),
    val completions: List<HabitCompletionEntity> = emptyList(),
    val distractions: List<DistractionEntity> = emptyList(),
    val reflections: List<ReflectionEntity> = emptyList(),
    val reminders: List<ReminderEntity> = emptyList(),
)

data class BackupSummary(
    val tasks: Int,
    val sessions: Int,
    val habits: Int,
    val completions: Int,
    val reminders: Int,
)
