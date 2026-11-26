package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 할일 (PRD §8.3 todo). 날짜 = epochDay, 시각 = 자정 기준 분, 시점 = epoch millis. */
@Entity(
    tableName = "todo",
    foreignKeys = [
        ForeignKey(
            entity = ProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("projectId"), Index("isDone", "dueDate")],
)
data class TodoEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "note", defaultValue = "") val note: String = "",
    @ColumnInfo(name = "isDone", defaultValue = "0") val isDone: Boolean = false,
    /** NONE / LOW / MEDIUM / HIGH */
    @ColumnInfo(name = "priority", defaultValue = "NONE") val priority: String = "NONE",
    @ColumnInfo(name = "dueDate") val dueDate: Long? = null,
    @ColumnInfo(name = "dueTime") val dueTime: Int? = null,
    @ColumnInfo(name = "projectId") val projectId: String? = null,
    @ColumnInfo(name = "sortOrder", defaultValue = "0") val sortOrder: Int = 0,
    @ColumnInfo(name = "completedAt") val completedAt: Long? = null,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)
