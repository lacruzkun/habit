package com.focusflow.app.timer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.focusflow.app.data.db.FocusFlowDatabase
import com.focusflow.app.data.prefs.UserPreferencesRepository
import com.focusflow.app.data.repository.SessionRepository
import com.focusflow.app.domain.model.SessionStatus
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.time.TimeSource
import com.focusflow.app.domain.timer.TimerConfig
import com.focusflow.app.domain.timer.TimerStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.concurrent.Executor

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class TimerManagerTest {

    private lateinit var database: FocusFlowDatabase
    private val directExecutor = Executor { it.run() }

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FocusFlowDatabase::class.java)
            .allowMainThreadQueries()
            .setQueryExecutor(directExecutor)
            .setTransactionExecutor(directExecutor)
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `finishing a focus session starts a break and saves the session`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val time = FakeTimeSource()
        val manager = TimerManager(
            database = database,
            sessionRepository = SessionRepository(database),
            preferencesRepository = UserPreferencesRepository(context),
            timeSource = time,
            scope = backgroundScope,
        )

        manager.startFocus(
            taskId = null,
            taskName = "Deep work",
            config = TimerConfig(
                focusMinutes = 1,
                shortBreakMinutes = 1,
                longBreakMinutes = 1,
                sessionsBeforeLongBreak = 4,
            ),
        )

        repeat(65) {
            time.elapsedRealtime += 1_000
            time.wallMillis += 1_000
            advanceTimeBy(1_000)
        }
        advanceUntilIdle()
        repeat(50) {
            if (manager.state.value?.sessionType != SessionType.FOCUS) return@repeat
            withContext(Dispatchers.IO) { delay(20) }
        }

        val state = manager.state.value
        assertNotNull(state)
        assertEquals(SessionType.SHORT_BREAK, state?.sessionType)
        assertEquals(TimerStatus.RUNNING, state?.status)
        assertEquals(60, state?.plannedSeconds)

        val sessions = database.focusSessionDao().getAllSessions()
        assertEquals(1, sessions.size)
        assertEquals(SessionStatus.COMPLETED, sessions.single().status)
        assertEquals(60, sessions.single().focusSeconds)
    }

    private class FakeTimeSource : TimeSource {
        var elapsedRealtime: Long = 0
        var wallMillis: Long = 1_000_000

        override fun elapsedRealtime(): Long = elapsedRealtime
        override fun wallMillis(): Long = wallMillis
        override fun zoneId() = java.time.ZoneId.of("UTC")
    }
}
