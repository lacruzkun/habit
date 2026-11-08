package com.focusflow.app.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.focusflow.app.domain.model.SessionStatus
import com.focusflow.app.domain.model.SessionType
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class FocusFlowDatabaseTest {

    private lateinit var database: FocusFlowDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FocusFlowDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `task keeps category until category is deleted`() = runTest {
        val categoryId = database.categoryDao().upsert(
            CategoryEntity(name = "Programming", colorArgb = 0xFF6C4CF1, iconKey = "code", createdAt = 1),
        )
        val taskId = database.taskDao().insert(
            TaskEntity(name = "Ship feature", categoryId = categoryId, createdAt = 1),
        )

        assertEquals(categoryId, database.taskDao().getById(taskId)?.categoryId)

        database.categoryDao().deleteById(categoryId)
        val task = database.taskDao().getById(taskId)
        assertEquals("Ship feature", task?.name)
        assertNull(task?.categoryId)
    }

    @Test
    fun `archiving hides task from current list but keeps it recoverable`() = runTest {
        val taskId = database.taskDao().insert(TaskEntity(name = "Read", createdAt = 1))
        val archived = database.taskDao().getById(taskId)!!.copy(archivedAt = 99)
        database.taskDao().update(archived)

        assertTrue(database.taskDao().getCurrent().isEmpty())
        assertEquals(1, database.taskDao().getAll().size)

        database.taskDao().update(archived.copy(archivedAt = null))
        assertEquals(1, database.taskDao().getCurrent().size)
    }

    @Test
    fun `deleting a session cascades to its segments`() = runTest {
        val sessionId = database.focusSessionDao().insertSession(
            FocusSessionEntity(
                taskId = null,
                taskNameSnapshot = "Write",
                sessionType = SessionType.FOCUS,
                status = SessionStatus.COMPLETED,
                startedAt = 1_000,
                endedAt = 2_000,
                plannedSeconds = 1_500,
                focusSeconds = 1_000,
            ),
        )
        database.focusSessionDao().insertSegments(
            listOf(
                FocusSegmentEntity(sessionId = sessionId, startedAt = 1_000, endedAt = 2_000),
            ),
        )
        assertEquals(1, database.focusSessionDao().getAllSegments().size)

        database.focusSessionDao().deleteAllSessions()
        assertTrue(database.focusSessionDao().getAllSegments().isEmpty())
    }

    @Test
    fun `segment query only returns overlapping ranges`() = runTest {
        val sessionId = database.focusSessionDao().insertSession(
            FocusSessionEntity(
                taskId = null,
                taskNameSnapshot = "Focus",
                sessionType = SessionType.FOCUS,
                status = SessionStatus.COMPLETED,
                startedAt = 1_000,
                endedAt = 5_000,
                plannedSeconds = 4,
                focusSeconds = 4,
            ),
        )
        database.focusSessionDao().insertSegments(
            listOf(
                FocusSegmentEntity(sessionId = sessionId, startedAt = 1_000, endedAt = 2_000),
                FocusSegmentEntity(sessionId = sessionId, startedAt = 4_000, endedAt = 5_000),
            ),
        )

        val overlapping = database.focusSessionDao().getSegments(2_500, 4_500)
        assertEquals(1, overlapping.size)
        assertEquals(4_000L, overlapping.single().segmentStart)
    }

    @Test
    fun `habit completion upsert replaces the same day`() = runTest {
        val habitId = database.habitDao().insert(
            HabitEntity(name = "Water", targetValue = 8.0, unit = "glasses", createdAt = 1),
        )
        database.habitDao().upsertCompletion(
            HabitCompletionEntity(habitId = habitId, epochDay = 100, value = 3.0, targetValue = 8.0),
        )
        database.habitDao().upsertCompletion(
            HabitCompletionEntity(habitId = habitId, epochDay = 100, value = 8.0, targetValue = 8.0),
        )

        val completions = database.habitDao().getCompletionsForHabit(habitId)
        assertEquals(1, completions.size)
        assertEquals(8.0, completions.single().value, 0.0001)
    }

    @Test
    fun `reflection upsert keeps one row per week`() = runTest {
        database.reflectionDao().upsert(
            ReflectionEntity(
                weekStartEpochDay = 50,
                wentWell = "Shipped",
                needsImprovement = "Sleep",
                nextFocus = "Deep work",
                updatedAt = 1,
            ),
        )
        database.reflectionDao().upsert(
            ReflectionEntity(
                weekStartEpochDay = 50,
                wentWell = "Shipped more",
                needsImprovement = "Sleep",
                nextFocus = "Deep work",
                updatedAt = 2,
            ),
        )

        val all = database.reflectionDao().getAll()
        assertEquals(1, all.size)
        assertEquals("Shipped more", all.single().wentWell)
    }
}
