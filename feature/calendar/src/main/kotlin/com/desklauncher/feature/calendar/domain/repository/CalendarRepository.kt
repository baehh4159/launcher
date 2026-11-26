package com.desklauncher.feature.calendar.domain.repository

import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import com.desklauncher.feature.calendar.domain.model.DueTodo
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate

interface CalendarRepository {
    /** [from, to)와 겹치는 일정 */
    fun events(from: Instant, to: Instant): Flow<List<CalendarEvent>>

    /** 마감일이 [from, to] 사이인 할일 (읽기 전용) */
    fun dueTodos(from: LocalDate, to: LocalDate): Flow<List<DueTodo>>

    suspend fun getEvent(id: String): CalendarEvent?
    suspend fun saveEvent(event: CalendarEvent)
    suspend fun deleteEvent(id: String)
}
