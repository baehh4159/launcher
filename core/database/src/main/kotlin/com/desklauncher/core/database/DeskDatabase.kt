package com.desklauncher.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.desklauncher.core.database.dao.ToolConfigDao
import com.desklauncher.core.database.entity.ToolConfigEntity

/**
 * 앱 전체 단일 Room DB (PRD §8).
 *
 * 스키마를 바꿀 때: [version] +1 → [com.desklauncher.core.database.migration.DeskMigrations]에
 * Migration 추가 → `schemas/` JSON 커밋 → 마이그레이션 테스트 추가 (AGENTS.md §4.4).
 */
@Database(
    entities = [
        ToolConfigEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class DeskDatabase : RoomDatabase() {
    abstract fun toolConfigDao(): ToolConfigDao

    companion object {
        const val NAME = "desk.db"
    }
}
