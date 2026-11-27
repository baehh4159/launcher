package com.desklauncher.feature.alarm.domain

import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.domain.model.RepeatDays
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class AlarmScheduleTest {

    private val zone = ZoneId.of("Asia/Seoul")
    // 2026-11-26 목요일 09:00
    private val now = ZonedDateTime.of(LocalDateTime.of(2026, 11, 26, 9, 0), zone)

    private fun alarm(hour: Int, minute: Int, vararg days: DayOfWeek) =
        Alarm("a", LocalTime.of(hour, minute), days.toSet(), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH)

    // 한 번만: 아직 안 지났으면 오늘, 지났거나 지금이면 내일
    @Test
    fun oneShot_todayOrTomorrow() {
        assertEquals(LocalDateTime.of(2026, 11, 26, 9, 30), AlarmSchedule.nextTrigger(alarm(9, 30), now).toLocalDateTime())
        assertEquals(LocalDateTime.of(2026, 11, 27, 9, 0), AlarmSchedule.nextTrigger(alarm(9, 0), now).toLocalDateTime())
        assertEquals(LocalDateTime.of(2026, 11, 27, 7, 0), AlarmSchedule.nextTrigger(alarm(7, 0), now).toLocalDateTime())
    }

    // 반복: 다음 해당 요일
    @Test
    fun repeating_nextMatchingDay() {
        // 평일 7시 → 내일(금)
        val weekdays = arrayOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
        assertEquals(LocalDateTime.of(2026, 11, 27, 7, 0), AlarmSchedule.nextTrigger(alarm(7, 0, *weekdays), now).toLocalDateTime())
        // 목요일 10시 → 오늘
        assertEquals(LocalDateTime.of(2026, 11, 26, 10, 0), AlarmSchedule.nextTrigger(alarm(10, 0, DayOfWeek.THURSDAY), now).toLocalDateTime())
        // 목요일 8시(이미 지남) → 다음 주 목요일
        assertEquals(LocalDateTime.of(2026, 12, 3, 8, 0), AlarmSchedule.nextTrigger(alarm(8, 0, DayOfWeek.THURSDAY), now).toLocalDateTime())
        // 일요일만 → 이번 일요일
        assertEquals(LocalDateTime.of(2026, 11, 29, 9, 0), AlarmSchedule.nextTrigger(alarm(9, 0, DayOfWeek.SUNDAY), now).toLocalDateTime())
    }

    @Test
    fun repeatMask_roundTrip() {
        val days = setOf(DayOfWeek.MONDAY, DayOfWeek.SUNDAY)
        assertEquals(65, RepeatDays.toMask(days))
        assertEquals(days, RepeatDays.fromMask(65))
    }
}
