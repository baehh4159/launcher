package com.desklauncher.feature.calendar.ui

import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 일정 시간 표시: "종일" 또는 "15:00 – 16:00", 그 날 밖에서 시작/끝나면 "…" 표시. */
internal object EventTimeText {
    fun format(event: CalendarEvent, day: LocalDate, zone: ZoneId, is24Hour: Boolean, locale: Locale, allDayLabel: String): String {
        if (event.allDay) return allDayLabel
        val formatter = DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "a h:mm", locale)
        val start = event.start.atZone(zone)
        val end = event.end.atZone(zone)
        val startText = if (start.toLocalDate() == day) start.format(formatter) else "…"
        val endText = if (end.toLocalDate() == day) end.format(formatter) else "…"
        return "$startText – $endText"
    }
}
