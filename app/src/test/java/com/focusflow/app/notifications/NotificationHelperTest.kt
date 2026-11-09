package com.focusflow.app.notifications

import android.app.Notification
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.timer.TimerConfig
import com.focusflow.app.domain.timer.TimerState
import com.focusflow.app.domain.timer.TimerStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
class NotificationHelperTest {

    private val helper = NotificationHelper(ApplicationProvider.getApplicationContext<Context>())

    @Test
    fun `running notification shows remaining time and pause action`() {
        val state = baseState(
            status = TimerStatus.RUNNING,
            accumulatedMs = 0,
        )

        val notification = helper.timerNotification(state, elapsedRealtime = 60_000)

        assertEquals("Focusing on Write report", title(notification))
        assertEquals("24:00 remaining", text(notification))
        assertEquals("Pause", notification.actions[0].title)
    }

    @Test
    fun `paused notification shows paused time and resume action`() {
        val state = baseState(
            status = TimerStatus.PAUSED,
            accumulatedMs = 60_000,
        )

        val notification = helper.timerNotification(state, elapsedRealtime = 60_000)

        assertEquals("Focusing on Write report", title(notification))
        assertEquals("Paused at 24:00", text(notification))
        assertEquals("Resume", notification.actions[0].title)
    }

    @Test
    fun `break notification labels skip action`() {
        val state = baseState(
            status = TimerStatus.RUNNING,
            accumulatedMs = 0,
            sessionType = SessionType.SHORT_BREAK,
        )

        val notification = helper.timerNotification(state, elapsedRealtime = 0)

        assertEquals("Short break", title(notification))
        assertEquals("Skip break", notification.actions[1].title)
    }

    private fun baseState(
        status: TimerStatus,
        accumulatedMs: Long,
        sessionType: SessionType = SessionType.FOCUS,
    ) = TimerState(
        taskId = null,
        taskName = "Write report",
        sessionType = sessionType,
        status = status,
        plannedSeconds = 1_500,
        accumulatedMs = accumulatedMs,
        runStartedAtElapsedMs = if (status == TimerStatus.RUNNING) 0 else null,
        runStartedAtWallMs = if (status == TimerStatus.RUNNING) 0 else null,
        sessionStartedAtWallMs = 0,
        cycleCount = 0,
        config = TimerConfig(),
    )

    private fun title(notification: Notification): String? =
        notification.extras.getString(Notification.EXTRA_TITLE)

    private fun text(notification: Notification): String? =
        notification.extras.getString(Notification.EXTRA_TEXT)
}
