package com.desklauncher.feature.calendar.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.io.Serializable
import java.util.UUID

/**
 * 일정 편집 시트의 입력값 (CAL-02). 로컬 날짜·시각으로 다루고 저장할 때만 Instant로 바꾼다.
 * rememberSaveable로 보존하기 위해 Serializable.
 */
@Immutable
data class EventDraft(
    /** null = 새 일정 */
    val id: String?,
    val title: String = "",
    val allDay: Boolean = false,
    val startDate: LocalDate,
    val startTime: LocalTime,
    val endDate: LocalDate,
    val endTime: LocalTime,
    val location: String = "",
    val note: String = "",
    val colorName: String? = null,
    val createdAt: Instant? = null,
) : Serializable {
    /** 끝이 시작보다 빠르지 않게 맞춘다: 시각 일정은 +1시간, 종일은 같은 날. */
    fun normalized(): EventDraft {
        if (allDay) return if (endDate.isBefore(startDate)) copy(endDate = startDate) else this
        val start = LocalDateTime.of(startDate, startTime)
        val end = LocalDateTime.of(endDate, endTime)
        if (end.isAfter(start)) return this
        val fixed = start.plusHours(1)
        return copy(endDate = fixed.toLocalDate(), endTime = fixed.toLocalTime())
    }

    fun toEvent(zone: ZoneId, now: Instant): CalendarEvent {
        val d = normalized()
        val start = if (d.allDay) d.startDate.atStartOfDay(zone) else LocalDateTime.of(d.startDate, d.startTime).atZone(zone)
        val end = if (d.allDay) d.endDate.plusDays(1).atStartOfDay(zone) else LocalDateTime.of(d.endDate, d.endTime).atZone(zone)
        return CalendarEvent(
            id = d.id ?: UUID.randomUUID().toString(),
            title = d.title.trim(),
            start = start.toInstant(),
            end = end.toInstant(),
            allDay = d.allDay,
            location = d.location.trim().ifEmpty { null },
            note = d.note,
            colorName = d.colorName,
            createdAt = d.createdAt ?: now,
            updatedAt = now,
        )
    }

    companion object {
        /** 새 일정: 선택한 날, 지금 다음 정시(오늘이 아니면 9시)부터 1시간 */
        fun new(day: LocalDate, now: LocalDateTime): EventDraft {
            val startHour = if (day == now.toLocalDate()) (now.hour + 1).coerceAtMost(23) else 9
            val start = LocalTime.of(startHour, 0)
            val end = if (startHour == 23) LocalTime.of(23, 59) else start.plusHours(1)
            return EventDraft(id = null, startDate = day, startTime = start, endDate = day, endTime = end)
        }

        fun from(event: CalendarEvent, zone: ZoneId): EventDraft {
            val start = event.start.atZone(zone)
            // 종일 일정의 끝은 '다음 날 자정'(배타)이므로 하루 빼서 보여 준다
            val end = if (event.allDay) event.end.atZone(zone).minusDays(1) else event.end.atZone(zone)
            return EventDraft(
                id = event.id, title = event.title, allDay = event.allDay,
                startDate = start.toLocalDate(), startTime = start.toLocalTime(),
                endDate = end.toLocalDate(), endTime = end.toLocalTime(),
                location = event.location.orEmpty(), note = event.note, colorName = event.colorName, createdAt = event.createdAt,
            )
        }
    }
}
