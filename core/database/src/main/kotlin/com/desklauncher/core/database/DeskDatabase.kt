package com.desklauncher.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.desklauncher.core.database.dao.AppPrefDao
import com.desklauncher.core.database.dao.ProjectDao
import com.desklauncher.core.database.dao.TodoDao
import com.desklauncher.core.database.dao.ToolConfigDao
import com.desklauncher.core.database.entity.AppPrefEntity
import com.desklauncher.core.database.entity.ProjectEntity
import com.desklauncher.core.database.entity.TodoEntity
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
        ProjectEntity::class,
        TodoEntity::class,
    ],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        // v2: app_pref 추가 (SHELL-08)
        AutoMigration(from = 1, to = 2),
        // v3: project, todo 추가 (TODO-01)
        AutoMigration(from = 2, to = 3),
    ],
)
abstract class DeskDatabase : RoomDatabase() {
    abstract fun toolConfigDao(): ToolConfigDao
    abstract fun appPrefDao(): AppPrefDao
    abstract fun projectDao(): ProjectDao
    abstract fun todoDao(): TodoDao

    companion object {
        const val NAME = "desk.db"
    }
}
