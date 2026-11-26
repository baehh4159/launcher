package com.desklauncher.feature.calendar.data

import com.desklauncher.core.database.dao.CalendarEventDao
import com.desklauncher.core.database.dao.TodoDao
import com.desklauncher.core.database.entity.CalendarEventEntity
import com.desklauncher.core.database.entity.TodoEntity
import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import com.desklauncher.feature.calendar.domain.model.DueTodo
import com.desklauncher.feature.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

internal class RoomCalendarRepository @Inject constructor(
    private val eventDao: CalendarEventDao,
    private val todoDao: TodoDao,
    private val clock: Clock,
) : CalendarRepository {

    override fun events(from: Instant, to: Instant): Flow<List<CalendarEvent>> =
        eventDao.observeOverlapping(from.toEpochMilli(), to.toEpochMilli()).map { list -> list.map { it.toDomain() } }

    override fun dueTodos(from: LocalDate, to: LocalDate): Flow<List<DueTodo>> =
        todoDao.observeDueBetween(from.toEpochDay(), to.toEpochDay()).map { list -> list.mapNotNull { it.toDueTodo() } }

    override suspend fun getEvent(id: String): CalendarEvent? = eventDao.get(id)?.toDomain()

    override suspend fun saveEvent(event: CalendarEvent) {
        eventDao.upsert(event.copy(title = event.title.trim(), updatedAt = clock.instant()).toEntity())
    }

    override suspend fun deleteEvent(id: String) = eventDao.delete(id)
}

private fun CalendarEventEntity.toDomain() = CalendarEvent(
    id = id, title = title, start = Instant.ofEpochMilli(startAt), end = Instant.ofEpochMilli(endAt), allDay = allDay,
    location = location, note = note, colorName = color,
    createdAt = Instant.ofEpochMilli(createdAt), updatedAt = Instant.ofEpochMilli(updatedAt),
)

private fun CalendarEvent.toEntity() = CalendarEventEntity(
    id = id, title = title, startAt = start.toEpochMilli(), endAt = end.toEpochMilli(), allDay = allDay,
    location = location?.takeIf { it.isNotBlank() }, note = note, color = colorName,
    createdAt = createdAt.toEpochMilli(), updatedAt = updatedAt.toEpochMilli(),
)

private fun TodoEntity.toDueTodo(): DueTodo? {
    val day = dueDate ?: return null
    return DueTodo(
        id = id,
        title = title,
        date = LocalDate.ofEpochDay(day),
        time = dueTime?.let { LocalTime.ofSecondOfDay(it * 60L) },
        isDone = isDone,
    )
}
