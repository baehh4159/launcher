package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 낙서 (PRD §8.3 doodle). 획 데이터(JSON)·이미지(PNG)는 내부 저장소, 여기엔 상대 경로만. */
@Entity(tableName = "doodle", indices = [Index("updatedAt")])
data class DoodleEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "title", defaultValue = "") val title: String = "",
    @ColumnInfo(name = "strokesPath") val strokesPath: String,
    @ColumnInfo(name = "thumbnailPath") val thumbnailPath: String,
    @ColumnInfo(name = "canvasWidth") val canvasWidth: Int,
    @ColumnInfo(name = "canvasHeight") val canvasHeight: Int,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)
