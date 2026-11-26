package com.desklauncher.feature.calendar.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class CalendarEvent(
    val id: String,
    val title: String,
    val start: Instant,
    /** 끝(배타). 종일 일정은 마지막 날 다음 날 자정 */
    val end: Instant,
    val allDay: Boolean = false,
    val location: String? = null,
    val note: String = "",
    /** DeskTabColor 이름, null = 기본 */
    val colorName: String? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

/** 마감일이 있는 할일 (할일정리 도구 데이터를 읽기 전용으로 — CAL-03) */
data class DueTodo(
    val id: String,
    val title: String,
    val date: LocalDate,
    val time: LocalTime?,
    val isDone: Boolean,
)
