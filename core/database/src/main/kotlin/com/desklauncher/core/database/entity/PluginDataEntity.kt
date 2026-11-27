package com.desklauncher.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * 플러그인 공용 키-값 저장소 (PRD §8.3 plugin_data).
 * 플러그인이 자기 표와 마이그레이션 없이 데이터를 남길 수 있게 한다. 도구를 꺼도 지우지 않는다.
 */
@Entity(tableName = "plugin_data", primaryKeys = ["pluginId", "key"])
data class PluginDataEntity(
    /** `DeskTool.id` */
    @ColumnInfo(name = "pluginId")
    val pluginId: String,
    @ColumnInfo(name = "key")
    val key: String,
    @ColumnInfo(name = "value")
    val value: String,
    /** epoch millis */
    @ColumnInfo(name = "updatedAt")
    val updatedAt: Long,
)
