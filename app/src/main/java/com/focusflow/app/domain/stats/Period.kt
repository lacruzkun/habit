package com.focusflow.app.domain.stats

import com.focusflow.app.domain.model.WeekStart
import com.focusflow.app.domain.time.dateRangeLabel
import com.focusflow.app.domain.time.endOfDayExclusiveMillis
import com.focusflow.app.domain.time.startOfDayMillis
import com.focusflow.app.domain.time.startOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters

data class DateRange(
    val start: LocalDate,
    val endExclusive: LocalDate,
) {
    val days: Int
        get() = ChronoUnit.DAYS.between(start, endExclusive).toInt().coerceAtLeast(1)

    val lastDay: LocalDate
        get() = endExclusive.minusDays(1)

    fun startMillis(zone: ZoneId): Long = start.startOfDayMillis(zone)

    fun endMillis(zone: ZoneId): Long = endExclusive.startOfDayMillis(zone)

    fun contains(date: LocalDate): Boolean = !date.isBefore(start) && date.isBefore(endExclusive)

    fun label(): String = dateRangeLabel(start, endExclusive)
}

data class PeriodSelection(
    val type: PeriodType,
    val customStart: LocalDate? = null,
    val customEnd: LocalDate? = null,
) {
    companion object {
        fun today() = PeriodSelection(PeriodType.TODAY)
        fun week() = PeriodSelection(PeriodType.WEEK)
        fun month() = PeriodSelection(PeriodType.MONTH)
        fun year() = PeriodSelection(PeriodType.YEAR)
    }
}

enum class PeriodType(val label: String) {
    TODAY("Today"),
    WEEK("This Week"),
    MONTH("This Month"),
    YEAR("This Year"),
    CUSTOM("Custom"),
}

fun periodRange(
    selection: PeriodSelection,
    today: LocalDate,
    weekStart: WeekStart,
): DateRange = when (selection.type) {
    PeriodType.TODAY -> DateRange(today, today.plusDays(1))
    PeriodType.WEEK -> {
        val start = startOfWeek(today, weekStart)
        DateRange(start, start.plusDays(7))
    }
    PeriodType.MONTH -> {
        val start = today.withDayOfMonth(1)
        DateRange(start, start.plusMonths(1))
    }
    PeriodType.YEAR -> {
        val start = today.withDayOfYear(1)
        DateRange(start, start.plusYears(1))
    }
    PeriodType.CUSTOM -> {
        val start = selection.customStart ?: today
        val endInclusive = selection.customEnd ?: today
        val safeEnd = if (endInclusive.isBefore(start)) start else endInclusive
        DateRange(start, safeEnd.plusDays(1))
    }
}

fun previousRange(
    selection: PeriodSelection,
    current: DateRange,
    today: LocalDate,
    weekStart: WeekStart,
): DateRange = when (selection.type) {
    PeriodType.TODAY -> DateRange(current.start.minusDays(1), current.start)
    PeriodType.WEEK -> DateRange(current.start.minusWeeks(1), current.start)
    PeriodType.MONTH -> {
        val start = current.start.minusMonths(1)
        DateRange(start, start.plusMonths(1))
    }
    PeriodType.YEAR -> {
        val start = current.start.minusYears(1)
        DateRange(start, start.plusYears(1))
    }
    PeriodType.CUSTOM -> {
        val length = current.days.toLong()
        DateRange(current.start.minusDays(length), current.start)
    }
}

fun monthRange(today: LocalDate): DateRange =
    DateRange(today.withDayOfMonth(1), today.withDayOfMonth(1).plusMonths(1))

fun yearRange(today: LocalDate): DateRange =
    DateRange(today.withDayOfYear(1), today.withDayOfYear(1).plusYears(1))

fun weekRange(today: LocalDate, weekStart: WeekStart): DateRange {
    val start = startOfWeek(today, weekStart)
    return DateRange(start, start.plusDays(7))
}

fun LocalDate.monthStart(): LocalDate = withDayOfMonth(1)

fun LocalDate.yearStart(): LocalDate = withDayOfYear(1)

fun LocalDate.nextMonday(): LocalDate = with(TemporalAdjusters.next(java.time.DayOfWeek.MONDAY))

fun DateRange.startOfDayMillis(zone: ZoneId): Long = start.startOfDayMillis(zone)

fun DateRange.endOfDayMillis(zone: ZoneId): Long = lastDay.endOfDayExclusiveMillis(zone)
