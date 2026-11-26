package com.desklauncher.feature.calendar.domain

import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

class MonthGridTest {

    private val zone = ZoneId.of("Asia/Seoul")

    // 2026년 11월 1일은 일요일 → 첫 칸이 11/1, 42칸
    @Test
    fun days_startOnSundayAndFillSixWeeks() {
        val days = MonthGrid.days(YearMonth.of(2026, 11))
        assertEquals(42, days.size)
        assertEquals(LocalDate.of(2026, 11, 1), days.first())
        assertEquals(LocalDate.of(2026, 12, 12), days.last())
    }

    // 월요일 시작이면 앞 달 날짜로 채운다
    @Test
    fun days_supportsMondayStart() {
        assertEquals(LocalDate.of(2026, 10, 26), MonthGrid.days(YearMonth.of(2026, 11), DayOfWeek.MONDAY).first())
    }

    // 자정을 넘기는 일정은 두 날 모두에 보인다, 끝 시각 정각은 다음 날에 안 걸린다
    @Test
    fun eventsOn_includesOverlappingEvents() {
        fun at(d: Int, h: Int) = LocalDateTime.of(2026, 11, d, h, 0).atZone(zone).toInstant()
        val overnight = event("overnight", at(26, 22), at(27, 2))
        val endsAtMidnight = event("endsAtMidnight", at(26, 20), LocalDate.of(2026, 11, 27).atStartOfDay(zone).toInstant())

        assertEquals(listOf("overnight", "endsAtMidnight"), MonthGrid.eventsOn(LocalDate.of(2026, 11, 26), listOf(overnight, endsAtMidnight), zone).map { it.id })
        assertEquals(listOf("overnight"), MonthGrid.eventsOn(LocalDate.of(2026, 11, 27), listOf(overnight, endsAtMidnight), zone).map { it.id })
    }

    private fun event(id: String, start: Instant, end: Instant) =
        CalendarEvent(id = id, title = id, start = start, end = end, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH)
}
