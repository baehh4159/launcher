package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 프로젝트 (PRD §8.3 project). 프로젝트 메모·할일이 함께 쓴다. */
@Entity(tableName = "project")
data class ProjectEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String,
    @ColumnInfo(name = "name") val name: String,
    /** DeskTabColor.name */
    @ColumnInfo(name = "color") val color: String,
    @ColumnInfo(name = "archived", defaultValue = "0") val archived: Boolean = false,
    @ColumnInfo(name = "sortOrder", defaultValue = "0") val sortOrder: Int = 0,
    @ColumnInfo(name = "createdAt") val createdAt: Long,
    @ColumnInfo(name = "updatedAt") val updatedAt: Long,
)
