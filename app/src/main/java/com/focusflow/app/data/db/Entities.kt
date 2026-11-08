package com.focusflow.app.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.focusflow.app.domain.model.HabitFrequency
import com.focusflow.app.domain.model.ReminderType
import com.focusflow.app.domain.model.SessionStatus
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.model.TaskPriority
import com.focusflow.app.domain.timer.TimerStatus
import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)],
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Long,
    val iconKey: String,
    val createdAt: Long,
)

@Serializable
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("categoryId"),
        Index("archivedAt"),
        Index("sortOrder"),
    ],
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val categoryId: Long? = null,
    val iconKey: String = "code",
    val colorArgb: Long = 0xFF6C4CF1,
    val dailyTargetMinutes: Int = 60,
    val weeklyTargetMinutes: Int = 300,
    val yearlyTargetMinutes: Int? = null,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val createdAt: Long = 0,
    val archivedAt: Long? = null,
    val active: Boolean = true,
    val scheduledDaysMask: Int = 0x7F,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int? = null,
    val focusMinutes: Int = 25,
    val shortBreakMinutes: Int = 5,
    val longBreakMinutes: Int = 15,
    val sessionsBeforeLongBreak: Int = 4,
    val sortOrder: Long = 0,
)

@Serializable
@Entity(
    tableName = "focus_sessions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index("taskId"),
        Index("startedAt"),
        Index("status"),
        Index("sessionType"),
    ],
)
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long? = null,
    val taskNameSnapshot: String,
    val sessionType: SessionType,
    val status: SessionStatus,
    val startedAt: Long,
    val endedAt: Long,
    val plannedSeconds: Int,
    val focusSeconds: Int,
    val note: String? = null,
    val createdAt: Long = 0,
)

@Serializable
@Entity(
    tableName = "focus_segments",
    foreignKeys = [
        ForeignKey(
            entity = FocusSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("sessionId"), Index("startedAt")],
)
data class FocusSegmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val startedAt: Long,
    val endedAt: Long,
)

@Serializable
@Entity(
    tableName = "habits",
    indices = [Index("archivedAt")],
)
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val iconKey: String = "self_improvement",
    val colorArgb: Long = 0xFF45C98A,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val daysMask: Int = 0x7F,
    val weeklyTargetCount: Int = 3,
    val targetValue: Double = 1.0,
    val unit: String = "times",
    val startEpochDay: Long = 0,
    val reminderEnabled: Boolean = false,
    val reminderMinutes: Int? = null,
    val createdAt: Long = 0,
    val archivedAt: Long? = null,
)

@Serializable
@Entity(
    tableName = "habit_completions",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["habitId", "epochDay"], unique = true),
        Index("epochDay"),
    ],
)
data class HabitCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val habitId: Long,
    val epochDay: Long,
    val value: Double,
    val targetValue: Double,
    val note: String? = null,
    val completedAt: Long = 0,
)

@Serializable
@Entity(
    tableName = "distractions",
    indices = [Index("startedAt")],
)
data class DistractionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activity: String,
    val startedAt: Long,
    val endedAt: Long,
    val note: String? = null,
    val createdAt: Long = 0,
)

@Serializable
@Entity(
    tableName = "reflections",
    indices = [Index(value = ["weekStartEpochDay"], unique = true)],
)
data class ReflectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val weekStartEpochDay: Long,
    val wentWell: String = "",
    val needsImprovement: String = "",
    val nextFocus: String = "",
    val updatedAt: Long = 0,
)

@Serializable
@Entity(
    tableName = "reminders",
    indices = [Index("type"), Index("enabled")],
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: ReminderType = ReminderType.CUSTOM,
    val timeMinutes: Int,
    val daysMask: Int = 0x7F,
    val enabled: Boolean = true,
    val createdAt: Long = 0,
)

@Serializable
@Entity(tableName = "active_timer")
data class ActiveTimerEntity(
    @PrimaryKey val id: Int = 1,
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
    val focusMinutes: Int,
    val shortBreakMinutes: Int,
    val longBreakMinutes: Int,
    val sessionsBeforeLongBreak: Int,
    val segmentsJson: String,
    val updatedAt: Long,
)

data class SessionSegmentRow(
    val sessionId: Long,
    val taskId: Long?,
    val taskName: String,
    val sessionType: SessionType,
    val sessionStatus: SessionStatus,
    val segmentStart: Long,
    val segmentEnd: Long,
)


