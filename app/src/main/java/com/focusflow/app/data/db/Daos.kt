package com.focusflow.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.focusflow.app.domain.model.SessionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(items: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: CategoryEntity): Long

    @Update
    suspend fun update(item: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY sortOrder, createdAt")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE archivedAt IS NULL ORDER BY sortOrder, createdAt")
    fun observeCurrent(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE archivedAt IS NOT NULL ORDER BY archivedAt DESC")
    fun observeArchived(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun observeById(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks ORDER BY sortOrder, createdAt")
    suspend fun getAll(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE archivedAt IS NULL ORDER BY sortOrder, createdAt")
    suspend fun getCurrent(): List<TaskEntity>

    @Query("SELECT COALESCE(MAX(sortOrder), 0) FROM tasks")
    suspend fun maxSortOrder(): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TaskEntity>)

    @Update
    suspend fun update(item: TaskEntity)

    @Update
    suspend fun updateAll(items: List<TaskEntity>)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(item: FocusSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessions(items: List<FocusSessionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegments(items: List<FocusSegmentEntity>)

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    fun observeAllSessions(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC")
    suspend fun getAllSessions(): List<FocusSessionEntity>

    @Query("SELECT * FROM focus_sessions ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecentSessions(limit: Int): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE taskId = :taskId ORDER BY startedAt DESC LIMIT :limit")
    fun observeSessionsForTask(taskId: Long, limit: Int): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startedAt >= :start AND startedAt < :end ORDER BY startedAt")
    fun observeSessionsStarted(start: Long, end: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startedAt >= :start AND startedAt < :end ORDER BY startedAt")
    suspend fun getSessionsStarted(start: Long, end: Long): List<FocusSessionEntity>

    @Query(
        """
        SELECT s.id AS sessionId, s.taskId AS taskId, s.taskNameSnapshot AS taskName,
               s.sessionType AS sessionType, s.status AS sessionStatus,
               g.startedAt AS segmentStart, g.endedAt AS segmentEnd
        FROM focus_sessions s
        INNER JOIN focus_segments g ON g.sessionId = s.id
        WHERE g.endedAt > :start AND g.startedAt < :end
        ORDER BY g.startedAt
        """,
    )
    fun observeSegments(start: Long, end: Long): Flow<List<SessionSegmentRow>>

    @Query(
        """
        SELECT s.id AS sessionId, s.taskId AS taskId, s.taskNameSnapshot AS taskName,
               s.sessionType AS sessionType, s.status AS sessionStatus,
               g.startedAt AS segmentStart, g.endedAt AS segmentEnd
        FROM focus_sessions s
        INNER JOIN focus_segments g ON g.sessionId = s.id
        WHERE g.endedAt > :start AND g.startedAt < :end
        ORDER BY g.startedAt
        """,
    )
    suspend fun getSegments(start: Long, end: Long): List<SessionSegmentRow>

    @Query("SELECT * FROM focus_segments ORDER BY startedAt")
    suspend fun getAllSegments(): List<FocusSegmentEntity>

    @Query("SELECT COUNT(*) FROM focus_sessions")
    suspend fun count(): Int

    @Query("DELETE FROM focus_sessions")
    suspend fun deleteAllSessions()

    @Query("DELETE FROM focus_segments")
    suspend fun deleteAllSegments()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE archivedAt IS NULL ORDER BY createdAt")
    fun observeCurrent(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE archivedAt IS NOT NULL ORDER BY archivedAt DESC")
    fun observeArchived(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt")
    suspend fun getAll(): List<HabitEntity>

    @Query("SELECT * FROM habits WHERE id = :id")
    fun observeById(id: Long): Flow<HabitEntity?>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HabitEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<HabitEntity>)

    @Update
    suspend fun update(item: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM habit_completions ORDER BY epochDay DESC")
    fun observeAllCompletions(): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE epochDay BETWEEN :startDay AND :endDay ORDER BY epochDay")
    fun observeCompletionsBetween(startDay: Long, endDay: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY epochDay")
    fun observeCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY epochDay")
    suspend fun getCompletionsForHabit(habitId: Long): List<HabitCompletionEntity>

    @Query("SELECT * FROM habit_completions ORDER BY epochDay")
    suspend fun getAllCompletions(): List<HabitCompletionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompletion(item: HabitCompletionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(items: List<HabitCompletionEntity>)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND epochDay = :epochDay")
    suspend fun deleteCompletion(habitId: Long, epochDay: Long)

    @Query("DELETE FROM habit_completions")
    suspend fun deleteAllCompletions()

    @Query("DELETE FROM habits")
    suspend fun deleteAll()
}

@Dao
interface DistractionDao {
    @Query("SELECT * FROM distractions ORDER BY startedAt DESC")
    fun observeAll(): Flow<List<DistractionEntity>>

    @Query("SELECT * FROM distractions ORDER BY startedAt DESC")
    suspend fun getAll(): List<DistractionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DistractionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DistractionEntity>)

    @Update
    suspend fun update(item: DistractionEntity)

    @Query("DELETE FROM distractions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM distractions")
    suspend fun deleteAll()
}

@Dao
interface ReflectionDao {
    @Query("SELECT * FROM reflections WHERE weekStartEpochDay = :weekStart ORDER BY updatedAt DESC LIMIT 1")
    fun observeForWeek(weekStart: Long): Flow<ReflectionEntity?>

    @Query("SELECT * FROM reflections ORDER BY weekStartEpochDay DESC")
    fun observeAll(): Flow<List<ReflectionEntity>>

    @Query("SELECT * FROM reflections ORDER BY weekStartEpochDay")
    suspend fun getAll(): List<ReflectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: ReflectionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ReflectionEntity>)

    @Query("DELETE FROM reflections")
    suspend fun deleteAll()
}

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY timeMinutes")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders ORDER BY timeMinutes")
    suspend fun getAll(): List<ReminderEntity>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ReminderEntity>)

    @Update
    suspend fun update(item: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM reminders")
    suspend fun deleteAll()
}

@Dao
interface ActiveTimerDao {
    @Query("SELECT * FROM active_timer WHERE id = 1")
    suspend fun get(): ActiveTimerEntity?

    @Query("SELECT * FROM active_timer WHERE id = 1")
    fun observe(): Flow<ActiveTimerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(item: ActiveTimerEntity)

    @Query("DELETE FROM active_timer")
    suspend fun clear()
}
