package com.focusflow.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.focusflow.app.data.db.HabitEntity
import com.focusflow.app.data.db.ReminderEntity
import com.focusflow.app.data.db.TaskEntity
import com.focusflow.app.data.prefs.UserPreferencesRepository
import com.focusflow.app.data.repository.HabitRepository
import com.focusflow.app.data.repository.ReminderRepository
import com.focusflow.app.data.repository.TaskRepository
import com.focusflow.app.domain.model.ReminderSource
import com.focusflow.app.domain.model.maskHasDay
import java.time.ZonedDateTime

class ReminderScheduler(
    private val context: Context,
    private val reminderRepository: ReminderRepository,
    private val taskRepository: TaskRepository,
    private val habitRepository: HabitRepository,
    private val preferencesRepository: UserPreferencesRepository,
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun sync() {
        val reminders = reminderRepository.getAll()
        val tasks = taskRepository.getAllTasks()
        val habits = habitRepository.getAllHabits()
        reminders.forEach { cancel(ReminderSource.CUSTOM, it.id) }
        tasks.forEach { cancel(ReminderSource.TASK, it.id) }
        habits.forEach { cancel(ReminderSource.HABIT, it.id) }

        if (!preferencesRepository.snapshot().notificationsEnabled) return
        reminders.filter { it.enabled }.forEach { scheduleCustom(it) }
        tasks.filter { it.reminderEnabled && it.archivedAt == null && it.active }
            .forEach { scheduleTaskReminder(it) }
        habits.filter { it.reminderEnabled && it.archivedAt == null }
            .forEach { scheduleHabitReminder(it) }
    }

    suspend fun scheduleSource(source: ReminderSource, id: Long) {
        if (!preferencesRepository.snapshot().notificationsEnabled) return
        when (source) {
            ReminderSource.CUSTOM -> reminderRepository.getById(id)?.let { scheduleCustom(it) }
            ReminderSource.TASK -> taskRepository.getTask(id)?.let { scheduleTaskReminder(it) }
            ReminderSource.HABIT -> habitRepository.getHabit(id)?.let { scheduleHabitReminder(it) }
        }
    }

    suspend fun scheduleCustom(reminder: ReminderEntity) {
        if (!reminder.enabled) return
        schedule(
            source = ReminderSource.CUSTOM,
            id = reminder.id,
            timeMinutes = reminder.timeMinutes,
            daysMask = reminder.daysMask,
            title = reminder.title,
            message = reminder.message,
        )
    }

    suspend fun scheduleTaskReminder(task: TaskEntity) {
        val minutes = task.reminderMinutes ?: return
        if (!task.reminderEnabled || task.archivedAt != null || !task.active) return
        schedule(
            source = ReminderSource.TASK,
            id = task.id,
            timeMinutes = minutes,
            daysMask = task.scheduledDaysMask,
            title = "Time to focus",
            message = task.name,
        )
    }

    suspend fun scheduleHabitReminder(habit: HabitEntity) {
        val minutes = habit.reminderMinutes ?: return
        if (!habit.reminderEnabled || habit.archivedAt != null) return
        schedule(
            source = ReminderSource.HABIT,
            id = habit.id,
            timeMinutes = minutes,
            daysMask = habit.daysMask,
            title = "Habit reminder",
            message = "Keep your streak going: ${habit.name}",
        )
    }

    fun cancel(source: ReminderSource, id: Long) {
        alarmManager.cancel(pendingIntent(source, id, 0, "", ""))
    }

    private fun schedule(
        source: ReminderSource,
        id: Long,
        timeMinutes: Int,
        daysMask: Int,
        title: String,
        message: String,
    ) {
        val triggerAt = nextTriggerMillis(timeMinutes, daysMask) ?: return
        val intent = pendingIntent(source, id, triggerAt, title, message)
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && canExact ->
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAt,
                    intent,
                )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ->
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, intent)

            else -> alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAt, intent)
        }
    }

    private fun pendingIntent(
        source: ReminderSource,
        id: Long,
        triggerAt: Long,
        title: String,
        message: String,
    ): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_MESSAGE, message)
            putExtra(EXTRA_SOURCE, source.name)
            putExtra(EXTRA_SOURCE_ID, id)
            putExtra(EXTRA_NOTIFICATION_ID, notificationId(source, id))
        }
        val requestCode = requestCode(source, id)
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun nextTriggerMillis(timeMinutes: Int, daysMask: Int): Long? {
        if (timeMinutes !in 0..(24 * 60 - 1) || daysMask == 0) return null
        val now = ZonedDateTime.now()
        val nowMillis = now.toInstant().toEpochMilli()
        for (offset in 0..7) {
            val date = now.toLocalDate().plusDays(offset.toLong())
            if (!maskHasDay(daysMask, date.dayOfWeek)) continue
            val candidate = date
                .atTime(timeMinutes / 60, timeMinutes % 60)
                .atZone(now.zone)
            if (candidate.toInstant().toEpochMilli() > nowMillis + 1_000L) {
                return candidate.toInstant().toEpochMilli()
            }
        }
        return null
    }

    companion object {
        const val ACTION_REMINDER = "com.focusflow.app.REMINDER"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_SOURCE = "extra_source"
        const val EXTRA_SOURCE_ID = "extra_source_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

        fun requestCode(source: ReminderSource, id: Long): Int = when (source) {
            ReminderSource.CUSTOM -> (10_000 + id).toInt()
            ReminderSource.TASK -> (20_000 + id).toInt()
            ReminderSource.HABIT -> (30_000 + id).toInt()
        }

        fun notificationId(source: ReminderSource, id: Long): Int = when (source) {
            ReminderSource.CUSTOM -> (40_000 + id).toInt()
            ReminderSource.TASK -> (50_000 + id).toInt()
            ReminderSource.HABIT -> (60_000 + id).toInt()
        }
    }
}
