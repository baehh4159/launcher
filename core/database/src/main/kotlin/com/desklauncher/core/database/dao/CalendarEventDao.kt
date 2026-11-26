package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.desklauncher.core.database.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {

    /** [from, to) 구간과 겹치는 일정 */
    @Query("SELECT * FROM calendar_event WHERE startAt < :toMillis AND endAt > :fromMillis ORDER BY allDay DESC, startAt ASC")
    fun observeOverlapping(fromMillis: Long, toMillis: Long): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_event WHERE id = :id")
    suspend fun get(id: String): CalendarEventEntity?

    @Upsert
    suspend fun upsert(event: CalendarEventEntity)

    @Query("DELETE FROM calendar_event WHERE id = :id")
    suspend fun delete(id: String)
}
