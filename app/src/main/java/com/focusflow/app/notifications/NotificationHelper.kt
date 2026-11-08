package com.focusflow.app.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.focusflow.app.MainActivity
import com.focusflow.app.R
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.timer.TimerMath
import com.focusflow.app.domain.timer.TimerState
import com.focusflow.app.domain.timer.TimerStatus
import com.focusflow.app.timer.TimerService

class NotificationHelper(private val context: Context) {
    private val manager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createChannels() {
        val timerChannel = NotificationChannel(
            CHANNEL_TIMER,
            "Focus timer",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Ongoing focus and break timer"
            setShowBadge(false)
        }
        val alertsChannel = NotificationChannel(
            CHANNEL_ALERTS,
            "Alerts and reminders",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "Focus completion, habit and planning reminders"
        }
        manager.createNotificationChannels(listOf(timerChannel, alertsChannel))
    }

    fun timerNotification(
        state: TimerState,
        elapsedRealtime: Long,
    ): Notification {
        val remaining = TimerMath.remainingSeconds(
            state,
            elapsedRealtime,
            System.currentTimeMillis(),
        )
        val minutes = remaining / 60
        val seconds = remaining % 60
        val timeText = "%d:%02d".format(minutes, seconds)
        val label = when (state.sessionType) {
            SessionType.FOCUS -> "Focusing on ${state.taskName}"
            SessionType.SHORT_BREAK -> "Short break"
            SessionType.LONG_BREAK -> "Long break"
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(MainActivity.EXTRA_OPEN_TIMER, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val builder = NotificationCompat.Builder(context, CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(label)
            .setContentText(
                if (state.status == TimerStatus.RUNNING) "$timeText remaining"
                else "Paused at $timeText",
            )
            .setContentIntent(contentIntent)
            .setOngoing(state.status == TimerStatus.RUNNING)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (state.status == TimerStatus.RUNNING) {
            builder.addAction(
                0,
                "Pause",
                servicePendingIntent(ACTION_PAUSE, 1),
            )
        } else {
            builder.addAction(
                0,
                "Resume",
                servicePendingIntent(ACTION_RESUME, 2),
            )
        }
        val endLabel = if (state.isBreak) "Skip break" else "End session"
        builder.addAction(0, endLabel, servicePendingIntent(ACTION_END, 3))
        return builder.build()
    }

    fun showReminder(title: String, message: String, notificationId: Int) {
        val contentIntent = PendingIntent.getActivity(
            context,
            notificationId,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        manager.notify(notificationId, notification)
    }

    private fun servicePendingIntent(action: String, requestCode: Int): PendingIntent =
        PendingIntent.getService(
            context,
            requestCode,
            Intent(context, TimerService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    fun cancel(notificationId: Int) = manager.cancel(notificationId)

    companion object {
        const val CHANNEL_TIMER = "focusflow_timer"
        const val CHANNEL_ALERTS = "focusflow_alerts"
        const val TIMER_NOTIFICATION_ID = 1001
        const val ACTION_PAUSE = "com.focusflow.app.timer.PAUSE"
        const val ACTION_RESUME = "com.focusflow.app.timer.RESUME"
        const val ACTION_END = "com.focusflow.app.timer.END"

        fun startTimerService(context: Context) {
            ContextCompat.startForegroundService(
                context,
                Intent(context, TimerService::class.java).setAction(TimerService.ACTION_SYNC),
            )
        }
    }
}
