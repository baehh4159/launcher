package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 일정 (PRD §8.3 calendar_event). 시점은 epoch millis(UTC). 종일 일정은 로컬 자정 기준으로 저장한다. */
@Entity(tableName = "calendar_event", indices = [Index("startAt")])
data class CalendarEventEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "startAt") val startAt: Long,
    @ColumnInfo(name = "endAt") val endAt: Long,
    @ColumnInfo(name = "allDay", defaultValue = "0") val allDay: Boolean = false,
    @ColumnInfo(name = "location") val location: String? = null,
    @ColumnInfo(name = "note", defaultValue = "") val note: String = "",
    @ColumnInfo(name = "color") val color: String? = null,
    /** 예약 컬럼 (CAL-05, P2). MVP에서는 항상 null */
    @ColumnInfo(name = "recurrenceRule") val recurrenceRule: String? = null,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)
