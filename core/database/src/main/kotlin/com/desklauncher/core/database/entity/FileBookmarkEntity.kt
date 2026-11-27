package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 내파일의 외부 파일 즐겨찾기 (PRD §8.3 file_bookmark). SAF로 고른 content:// URI + 영구 읽기 권한. */
@Entity(tableName = "file_bookmark", indices = [Index(value = ["uri"], unique = true)])
data class FileBookmarkEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "uri") val uri: String,
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "mimeType") val mimeType: String? = null,
    @ColumnInfo(name = "sizeBytes") val sizeBytes: Long? = null,
    @ColumnInfo(name = "addedAt") val addedAt: Long,
)
