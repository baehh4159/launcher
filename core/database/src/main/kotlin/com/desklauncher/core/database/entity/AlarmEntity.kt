package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 알람 (PRD §8.3 alarm). repeatDays: 월=1 … 일=64 비트마스크, 0 = 한 번만. */
@Entity(tableName = "alarm", indices = [Index("enabled")])
data class AlarmEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "hour") val hour: Int,
    @ColumnInfo(name = "minute") val minute: Int,
    @ColumnInfo(name = "repeatDays", defaultValue = "0") val repeatDays: Int = 0,
    @ColumnInfo(name = "label", defaultValue = "") val label: String = "",
    @ColumnInfo(name = "enabled", defaultValue = "1") val enabled: Boolean = true,
    @ColumnInfo(name = "vibrate", defaultValue = "1") val vibrate: Boolean = true,
    @ColumnInfo(name = "ringtoneUri") val ringtoneUri: String? = null,
    @ColumnInfo(name = "snoozeMinutes", defaultValue = "5") val snoozeMinutes: Int = 5,
    @ColumnInfo(name = "nextTriggerAt") val nextTriggerAt: Long? = null,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)
