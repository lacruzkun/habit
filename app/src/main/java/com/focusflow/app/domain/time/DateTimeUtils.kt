package com.focusflow.app.domain.time

import com.focusflow.app.domain.model.WeekStart
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

fun LocalDate.startOfDayMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    atStartOfDay(zone).toInstant().toEpochMilli()

fun LocalDate.endOfDayExclusiveMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()

fun LocalDate.atTimeMinutes(minutesOfDay: Int): LocalDateTime =
    atTime(LocalTime.of(minutesOfDay / 60, minutesOfDay % 60))

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

fun Long.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()): LocalDateTime =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDateTime()

fun Long.toLocalTime(zone: ZoneId = ZoneId.systemDefault()): LocalTime =
    Instant.ofEpochMilli(this).atZone(zone).toLocalTime()

fun LocalDate.epochDayLong(): Long = toEpochDay()

fun epochDayToDate(epochDay: Long): LocalDate = LocalDate.ofEpochDay(epochDay)

fun startOfWeek(date: LocalDate, weekStart: WeekStart): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(weekStart.dayOfWeek))

fun safeStartOfWeek(date: LocalDate, firstDay: DayOfWeek): LocalDate =
    date.with(TemporalAdjusters.previousOrSame(firstDay))

fun formatDuration(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

fun formatDurationFromMinutes(minutes: Int): String = formatDuration(minutes * 60L)

fun formatDurationOrZero(seconds: Long): String =
    if (seconds <= 0) "0m" else formatDuration(seconds)

fun formatTimer(seconds: Long): String {
    val safe = seconds.coerceAtLeast(0)
    val hours = safe / 3600
    val minutes = (safe % 3600) / 60
    val secs = safe % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format(Locale.US, "%02d:%02d", minutes, secs)
    }
}

fun formatClock(minutesOfDay: Int): String {
    val normalized = ((minutesOfDay % 1440) + 1440) % 1440
    val time = LocalTime.of(normalized / 60, normalized % 60)
    return time.format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
}

fun formatDateLong(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d", Locale.getDefault()))

fun formatDateShort(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("MMM d", Locale.getDefault()))

fun formatMonth(date: LocalDate): String =
    date.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()))

fun formatWeekdayShort(day: DayOfWeek): String =
    day.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())

fun greeting(hour: Int): String = when (hour) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    in 17..21 -> "Good evening"
    else -> "Good night"
}

fun percentLabel(value: Double?): String =
    if (value == null) "—" else "${value.roundToInt()}%"

fun progressPercent(completed: Long, target: Long): Double =
    if (target <= 0) 0.0 else (completed.toDouble() / target).coerceIn(0.0, 1.0)

fun percentageChange(current: Long, previous: Long): Double? {
    if (previous == 0L) return if (current == 0L) 0.0 else null
    return (current - previous).toDouble() / previous * 100.0
}

fun daysBetweenInclusive(start: LocalDate, endInclusive: LocalDate): Long =
    ChronoUnit.DAYS.between(start, endInclusive) + 1

fun minutesToSeconds(minutes: Int): Int = (minutes.coerceAtLeast(1)) * 60

fun formatCount(value: Double): String {
    val rounded = value.roundToInt()
    return if (abs(value - rounded) < 0.001) rounded.toString() else {
        String.format(Locale.getDefault(), "%.1f", value)
    }
}

fun dateRangeLabel(start: LocalDate, endExclusive: LocalDate): String {
    val last = endExclusive.minusDays(1)
    return if (start == last) formatDateShort(start) else {
        "${formatDateShort(start)} – ${formatDateShort(last)}"
    }
}
