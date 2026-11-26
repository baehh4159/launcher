package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** 도구함 상태 (PRD §8.3 tool_config). */
@Entity(tableName = "tool_config")
data class ToolConfigEntity(
    @PrimaryKey
    @ColumnInfo(name = "toolId")
    val toolId: String,
    @ColumnInfo(name = "enabled")
    val enabled: Boolean,
    @ColumnInfo(name = "sortOrder")
    val sortOrder: Int,
    /** `DeskTabColor.name`. null이면 도구 기본 색. */
    @ColumnInfo(name = "tabColor")
    val tabColor: String?,
)
