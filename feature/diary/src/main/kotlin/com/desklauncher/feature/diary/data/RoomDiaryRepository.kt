package com.desklauncher.feature.diary.data

import com.desklauncher.core.database.dao.DiaryDao
import com.desklauncher.core.database.entity.DiaryEntryEntity
import com.desklauncher.feature.diary.domain.model.DiaryEntry
import com.desklauncher.feature.diary.domain.model.Mood
import com.desklauncher.feature.diary.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

internal class RoomDiaryRepository @Inject constructor(
    private val dao: DiaryDao,
    private val clock: Clock,
) : DiaryRepository {

    override val entries: Flow<List<DiaryEntry>> = dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun get(date: LocalDate): DiaryEntry? = dao.getByDate(date.toEpochDay())?.toDomain()

    override suspend fun save(entry: DiaryEntry) {
        dao.upsert(entry.copy(updatedAt = clock.instant()).toEntity())
    }

    override suspend fun delete(date: LocalDate) = dao.deleteByDate(date.toEpochDay())
}

private fun DiaryEntryEntity.toDomain() = DiaryEntry(
    id = id, date = LocalDate.ofEpochDay(date), mood = Mood.fromDb(mood), body = body,
    createdAt = Instant.ofEpochMilli(createdAt), updatedAt = Instant.ofEpochMilli(updatedAt),
)

private fun DiaryEntry.toEntity() = DiaryEntryEntity(
    id = id, date = date.toEpochDay(), mood = mood?.dbName, body = body,
    createdAt = createdAt.toEpochMilli(), updatedAt = updatedAt.toEpochMilli(),
)
