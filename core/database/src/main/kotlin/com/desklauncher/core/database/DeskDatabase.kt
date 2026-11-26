package com.desklauncher.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.desklauncher.core.database.dao.AppPrefDao
import com.desklauncher.core.database.dao.ToolConfigDao
import com.desklauncher.core.database.entity.AppPrefEntity
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
        AppPrefEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        // v2: app_pref 추가 (SHELL-08)
        AutoMigration(from = 1, to = 2),
    ],
)
abstract class DeskDatabase : RoomDatabase() {
    abstract fun toolConfigDao(): ToolConfigDao
    abstract fun appPrefDao(): AppPrefDao

    companion object {
        const val NAME = "desk.db"
    }
}
