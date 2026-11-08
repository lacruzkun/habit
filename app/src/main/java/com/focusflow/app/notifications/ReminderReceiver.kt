package com.focusflow.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.focusflow.app.FocusFlowApp
import com.focusflow.app.domain.model.ReminderSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_REMINDER) return
        val title = intent.getStringExtra(ReminderScheduler.EXTRA_TITLE) ?: return
        val message = intent.getStringExtra(ReminderScheduler.EXTRA_MESSAGE) ?: return
        val notificationId = intent.getIntExtra(
            ReminderScheduler.EXTRA_NOTIFICATION_ID,
            title.hashCode(),
        )
        NotificationHelper(context).apply {
            createChannels()
            showReminder(title, message, notificationId)
        }

        val source = intent.getStringExtra(ReminderScheduler.EXTRA_SOURCE)
            ?.let { runCatching { ReminderSource.valueOf(it) }.getOrNull() }
            ?: return
        val sourceId = intent.getLongExtra(ReminderScheduler.EXTRA_SOURCE_ID, -1L)
        if (sourceId < 0) return

        val pendingResult = goAsync()
        val container = (context.applicationContext as FocusFlowApp).container
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                container.reminderScheduler.scheduleSource(source, sourceId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
