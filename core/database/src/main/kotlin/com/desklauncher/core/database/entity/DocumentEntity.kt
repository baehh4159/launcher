package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** 서류정리함 폴더 (PRD §8.3 document_folder). MVP는 1단계 — parentId 예약. */
@Entity(tableName = "document_folder")
data class DocumentFolderEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "parentId") val parentId: String? = null,
    @ColumnInfo(name = "sortOrder", defaultValue = "0") val sortOrder: Int = 0,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)

/** 서류 (PRD §8.3 document). 파일 본문은 내부 저장소, 여기엔 상대 경로만. 폴더를 지우면 루트로(SET NULL). */
@Entity(
    tableName = "document",
    foreignKeys = [
        ForeignKey(entity = DocumentFolderEntity::class, parentColumns = ["id"], childColumns = ["folderId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("folderId")],
)
data class DocumentEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "folderId") val folderId: String? = null,
    @ColumnInfo(name = "title") val title: String,
    @ColumnInfo(name = "pdfPath") val pdfPath: String,
    @ColumnInfo(name = "thumbnailPath") val thumbnailPath: String? = null,
    @ColumnInfo(name = "pageCount", defaultValue = "0") val pageCount: Int = 0,
    @ColumnInfo(name = "sizeBytes", defaultValue = "0") val sizeBytes: Long = 0,
    /** SCAN / IMPORT */
    @ColumnInfo(name = "source", defaultValue = "SCAN") val source: String = "SCAN",
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)
