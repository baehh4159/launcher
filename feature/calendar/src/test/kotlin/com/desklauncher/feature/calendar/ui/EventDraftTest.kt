package com.desklauncher.feature.calendar.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class EventDraftTest {

    private val zone = ZoneId.of("Asia/Seoul")
    private val day = LocalDate.of(2026, 11, 26)

    // 오늘이면 다음 정시부터, 다른 날이면 9시부터 1시간
    @Test
    fun new_startsAtNextHourOrNine() {
        val now = LocalDateTime.of(2026, 11, 26, 14, 20)
        assertEquals(LocalTime.of(15, 0), EventDraft.new(day, now).startTime)
        assertEquals(LocalTime.of(16, 0), EventDraft.new(day, now).endTime)
        assertEquals(LocalTime.of(9, 0), EventDraft.new(day.plusDays(1), now).startTime)
    }

    // 끝이 시작보다 빠르면 시작 + 1시간으로 고친다 (자정 넘김 포함)
    @Test
    fun normalized_fixesEndBeforeStart() {
        val draft = EventDraft(null, startDate = day, startTime = LocalTime.of(23, 30), endDate = day, endTime = LocalTime.of(10, 0))
        val fixed = draft.normalized()
        assertEquals(day.plusDays(1), fixed.endDate)
        assertEquals(LocalTime.of(0, 30), fixed.endTime)
    }

    // 종일 일정: 끝은 마지막 날 다음 자정으로 저장, 다시 열면 마지막 날로 보임
    @Test
    fun allDay_roundTrip() {
        val draft = EventDraft(null, title = " 여행 ", allDay = true, startDate = day, startTime = LocalTime.MIDNIGHT, endDate = day.plusDays(2), endTime = LocalTime.MIDNIGHT)
        val event = draft.toEvent(zone, Instant.EPOCH)
        assertEquals("여행", event.title)
        assertEquals(day.plusDays(3).atStartOfDay(zone).toInstant(), event.end)

        val reopened = EventDraft.from(event, zone)
        assertEquals(day.plusDays(2), reopened.endDate)
    }
}
