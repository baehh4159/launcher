package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 개인일기 (PRD §8.3 diary_entry). 하루 1건 — date(epochDay) UNIQUE. */
@Entity(tableName = "diary_entry", indices = [Index(value = ["date"], unique = true)])
data class DiaryEntryEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "date") val date: Long,
    /** VERY_BAD … VERY_GOOD */
    @ColumnInfo(name = "mood") val mood: String? = null,
    @ColumnInfo(name = "body", defaultValue = "") val body: String = "",
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)
