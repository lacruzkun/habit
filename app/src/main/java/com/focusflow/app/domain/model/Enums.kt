package com.focusflow.app.domain.model

import kotlinx.serialization.Serializable
import java.time.DayOfWeek

@Serializable
enum class SessionType {
    FOCUS,
    SHORT_BREAK,
    LONG_BREAK,
}

@Serializable
enum class SessionStatus {
    COMPLETED,
    INTERRUPTED,
    ABANDONED,
}

@Serializable
enum class TaskPriority(val level: Int) {
    LOW(0),
    MEDIUM(1),
    HIGH(2),
    URGENT(3),
}

@Serializable
enum class HabitFrequency {
    DAILY,
    WEEKLY_DAYS,
    WEEKLY_COUNT,
}

@Serializable
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

@Serializable
enum class WeekStart(val dayOfWeek: DayOfWeek) {
    MONDAY(DayOfWeek.MONDAY),
    SUNDAY(DayOfWeek.SUNDAY),
}

@Serializable
enum class ReminderType {
    TASK,
    HABIT,
    DAILY_PLAN,
    WEEKLY_REVIEW,
    CUSTOM,
}

@Serializable
enum class ReminderSource {
    TASK,
    HABIT,
    CUSTOM,
}

@Serializable
enum class AvatarStyle {
    MOON,
    LEAF,
    BOLT,
    WAVE,
    STAR,
    CAT,
}

val DayOfWeek.maskBit: Int
    get() = 1 shl (value - 1)

fun maskHasDay(mask: Int, dayOfWeek: DayOfWeek): Boolean =
    mask and dayOfWeek.maskBit != 0

fun maskDays(mask: Int): List<DayOfWeek> =
    DayOfWeek.entries.filter { maskHasDay(mask, it) }

const val ALL_DAYS_MASK = 0x7F
