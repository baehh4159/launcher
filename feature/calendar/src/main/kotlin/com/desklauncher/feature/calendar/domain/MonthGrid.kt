package com.desklauncher.feature.calendar.domain

import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** 월간 달력 계산 (순수). 한국 달력 관례대로 일요일 시작이 기본. */
object MonthGrid {

    /** 6주 × 7일 = 42칸. 앞뒤 달의 날짜로 채운다. */
    fun days(month: YearMonth, firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): List<LocalDate> {
        val start = month.atDay(1).with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
        return List(42) { start.plusDays(it.toLong()) }
    }

    /** 그 날(로컬 자정~다음 자정)과 겹치는 일정 */
    fun eventsOn(date: LocalDate, events: List<CalendarEvent>, zone: ZoneId): List<CalendarEvent> {
        val (from, to) = dayRange(date, zone)
        return events.filter { it.start < to && it.end > from }
    }

    fun dayRange(date: LocalDate, zone: ZoneId): Pair<Instant, Instant> =
        date.atStartOfDay(zone).toInstant() to date.plusDays(1).atStartOfDay(zone).toInstant()

    /** 격자 전체 구간 [첫 칸 자정, 마지막 칸 다음 자정) */
    fun gridRange(month: YearMonth, zone: ZoneId, firstDayOfWeek: DayOfWeek = DayOfWeek.SUNDAY): Pair<Instant, Instant> {
        val days = days(month, firstDayOfWeek)
        return days.first().atStartOfDay(zone).toInstant() to days.last().plusDays(1).atStartOfDay(zone).toInstant()
    }
}
