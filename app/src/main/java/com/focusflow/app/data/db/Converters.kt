package com.focusflow.app.data.db

import androidx.room.TypeConverter
import com.focusflow.app.domain.model.HabitFrequency
import com.focusflow.app.domain.model.ReminderType
import com.focusflow.app.domain.model.SessionStatus
import com.focusflow.app.domain.model.SessionType
import com.focusflow.app.domain.model.TaskPriority
import com.focusflow.app.domain.timer.TimerStatus

class Converters {
    @TypeConverter
    fun sessionTypeToString(value: SessionType?): String? = value?.name

    @TypeConverter
    fun stringToSessionType(value: String?): SessionType? =
        value?.let { SessionType.valueOf(it) }

    @TypeConverter
    fun sessionStatusToString(value: SessionStatus?): String? = value?.name

    @TypeConverter
    fun stringToSessionStatus(value: String?): SessionStatus? =
        value?.let { SessionStatus.valueOf(it) }

    @TypeConverter
    fun taskPriorityToString(value: TaskPriority?): String? = value?.name

    @TypeConverter
    fun stringToTaskPriority(value: String?): TaskPriority? =
        value?.let { TaskPriority.valueOf(it) }

    @TypeConverter
    fun habitFrequencyToString(value: HabitFrequency?): String? = value?.name

    @TypeConverter
    fun stringToHabitFrequency(value: String?): HabitFrequency? =
        value?.let { HabitFrequency.valueOf(it) }

    @TypeConverter
    fun reminderTypeToString(value: ReminderType?): String? = value?.name

    @TypeConverter
    fun stringToReminderType(value: String?): ReminderType? =
        value?.let { ReminderType.valueOf(it) }

    @TypeConverter
    fun timerStatusToString(value: TimerStatus?): String? = value?.name

    @TypeConverter
    fun stringToTimerStatus(value: String?): TimerStatus? =
        value?.let { TimerStatus.valueOf(it) }
}
