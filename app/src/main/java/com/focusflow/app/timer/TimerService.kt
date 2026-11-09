package com.focusflow.app.timer

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.focusflow.app.FocusFlowApp
import com.focusflow.app.R
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.notifications.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class TimerService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var notificationHelper: NotificationHelper
    private lateinit var timerManager: TimerManager
    private var observing = false
    private var foregroundStarted = false
    private var lastNotificationKey: String? = null

    override fun onCreate() {
        super.onCreate()
        notificationHelper = NotificationHelper(this)
        notificationHelper.createChannels()
        timerManager = (application as FocusFlowApp).container.timerManager
        startForegroundPlaceholder()
        if (!observing) {
            observing = true
            observeTimer()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            NotificationHelper.ACTION_PAUSE,
            NotificationHelper.ACTION_RESUME,
            -> scope.launch { timerManager.toggle() }

            NotificationHelper.ACTION_END -> scope.launch {
                if (timerManager.state.value?.isBreak == true) timerManager.skipBreak()
                else timerManager.endSession()
            }

            ACTION_SYNC -> Unit
        }
        return START_STICKY
    }

    private fun observeTimer() {
        scope.launch {
            combine(timerManager.state, timerManager.tick) { state, elapsed -> state to elapsed }
                .collectLatest { (state, elapsed) ->
                    if (state == null) {
                        stopForegroundNow()
                        stopSelf()
                        return@collectLatest
                    }
                    val remaining = com.focusflow.app.domain.timer.TimerMath.remainingSeconds(
                        state,
                        elapsed,
                        System.currentTimeMillis(),
                    )
                    val key = "${state.sessionType}|${state.status}|${state.taskName}|$remaining"
                    if (key == lastNotificationKey) return@collectLatest
                    lastNotificationKey = key
                    val notification = notificationHelper.timerNotification(state, elapsed)
                    if (!foregroundStarted) {
                        startForegroundCompat(notification)
                        foregroundStarted = true
                    } else {
                        val manager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
                        manager.notify(NotificationHelper.TIMER_NOTIFICATION_ID, notification)
                    }
                    if (remaining <= 0) {
                        notificationHelper.cancel(NotificationHelper.TIMER_NOTIFICATION_ID)
                    }
                }
        }
        scope.launch {
            timerManager.events.collect { event ->
                when (event) {
                    is TimerEvent.FocusCompleted -> {
                        val title = "Focus session complete"
                        val message = when (event.nextBreak) {
                            SessionType.LONG_BREAK -> "Great streak. Take a longer break to recharge."
                            else -> "Nice work. Take a short break."
                        }
                        notificationHelper.showReminder(title, message, COMPLETION_ID)
                    }

                    is TimerEvent.BreakCompleted -> {
                        notificationHelper.showReminder(
                            "Break finished",
                            "Ready for your next focus session?",
                            COMPLETION_ID + 1,
                        )
                    }

                    is TimerEvent.BreakStarted,
                    is TimerEvent.SessionEnded,
                    -> Unit
                }
            }
        }
    }

    private fun startForegroundPlaceholder() {
        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_TIMER)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("FocusFlow")
            .setContentText("Preparing timer")
            .setOngoing(true)
            .setSilent(true)
            .build()
        startForegroundCompat(notification)
    }

    private fun startForegroundCompat(notification: Notification) {
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        } else {
            0
        }
        ServiceCompat.startForeground(
            this,
            NotificationHelper.TIMER_NOTIFICATION_ID,
            notification,
            type,
        )
    }

    private fun stopForegroundNow() {
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_SYNC = "com.focusflow.app.timer.SYNC"
        private const val COMPLETION_ID = 2001
    }
}
