package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 프로젝트 메모 (PRD §8.3 memo). 프로젝트를 지우면 함께 지워진다. */
@Entity(
    tableName = "memo",
    foreignKeys = [
        ForeignKey(entity = ProjectEntity::class, parentColumns = ["id"], childColumns = ["projectId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("projectId"), Index("updatedAt")],
)
data class MemoEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "projectId") val projectId: String,
    @ColumnInfo(name = "title", defaultValue = "") val title: String = "",
    @ColumnInfo(name = "body", defaultValue = "") val body: String = "",
    @ColumnInfo(name = "pinned", defaultValue = "0") val pinned: Boolean = false,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)

/** 프로젝트별 메모 수 */
data class ProjectMemoCount(
    @ColumnInfo(name = "projectId") val projectId: String,
    @ColumnInfo(name = "count") val count: Int,
)
