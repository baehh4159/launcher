package com.desklauncher.feature.calendar.ui.preview

import com.desklauncher.feature.calendar.domain.MonthGrid
import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import com.desklauncher.feature.calendar.ui.AgendaItem
import com.desklauncher.feature.calendar.ui.CalendarUiState
import com.desklauncher.feature.calendar.ui.DayCell
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

internal object CalendarPreviewData {
    val zone: ZoneId = ZoneId.of("Asia/Seoul")
    private val month = YearMonth.of(2026, 11)
    private val today = LocalDate.of(2026, 11, 26)
    private fun at(d: Int, h: Int) = LocalDateTime.of(2026, 11, d, h, 0).atZone(zone).toInstant()

    val events = listOf(
        CalendarEvent("e1", "디자인 리뷰", at(26, 15), at(26, 16), location = "회의실 B", colorName = "Blue", createdAt = at(1, 0), updatedAt = at(1, 0)),
        CalendarEvent("e2", "치과", at(26, 18), at(26, 19), colorName = "Pink", createdAt = at(1, 0), updatedAt = at(1, 0)),
        CalendarEvent("e3", "제주 여행", LocalDate.of(2026, 11, 28).atStartOfDay(zone).toInstant(), LocalDate.of(2026, 12, 1).atStartOfDay(zone).toInstant(), allDay = true, colorName = "Green", createdAt = at(1, 0), updatedAt = at(1, 0)),
    )

    val state = CalendarUiState(
        isLoading = false,
        month = month,
        today = today,
        selected = today,
        cells = MonthGrid.days(month).map { date ->
            val dayEvents = MonthGrid.eventsOn(date, events, zone)
            DayCell(date, YearMonth.from(date) == month, date == today, date == today, dayEvents.map { it.colorName }, dayEvents.size)
        },
        agenda = MonthGrid.eventsOn(today, events, zone).map(AgendaItem::Event),
    )
}
