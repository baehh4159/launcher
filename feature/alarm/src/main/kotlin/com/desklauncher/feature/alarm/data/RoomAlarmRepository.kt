package com.desklauncher.feature.alarm.data

import com.desklauncher.core.database.dao.AlarmDao
import com.desklauncher.core.database.entity.AlarmEntity
import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.domain.model.RepeatDays
import com.desklauncher.feature.alarm.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.Instant
import java.time.LocalTime
import javax.inject.Inject

internal class RoomAlarmRepository @Inject constructor(
    private val dao: AlarmDao,
    private val clock: Clock,
) : AlarmRepository {
    override val alarms: Flow<List<Alarm>> = dao.observeAll().map { list -> list.map { it.toDomain() } }
    override suspend fun get(id: String): Alarm? = dao.get(id)?.toDomain()
    override suspend fun getEnabled(): List<Alarm> = dao.getEnabled().map { it.toDomain() }
    override suspend fun save(alarm: Alarm) = dao.upsert(alarm.copy(updatedAt = clock.instant()).toEntity())
    override suspend fun delete(id: String) = dao.delete(id)
}

private fun AlarmEntity.toDomain() = Alarm(
    id = id, time = LocalTime.of(hour, minute), repeatDays = RepeatDays.fromMask(repeatDays), label = label,
    enabled = enabled, vibrate = vibrate, snoozeMinutes = snoozeMinutes, ringtoneUri = ringtoneUri,
    nextTriggerAt = nextTriggerAt?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAt), updatedAt = Instant.ofEpochMilli(updatedAt),
)

private fun Alarm.toEntity() = AlarmEntity(
    id = id, hour = time.hour, minute = time.minute, repeatDays = RepeatDays.toMask(repeatDays), label = label,
    enabled = enabled, vibrate = vibrate, ringtoneUri = ringtoneUri, snoozeMinutes = snoozeMinutes,
    nextTriggerAt = nextTriggerAt?.toEpochMilli(), createdAt = createdAt.toEpochMilli(), updatedAt = updatedAt.toEpochMilli(),
)
