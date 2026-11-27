package com.desklauncher.core.database

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import com.desklauncher.core.database.dao.AlarmDao
import com.desklauncher.core.database.dao.AppPrefDao
import com.desklauncher.core.database.dao.CalendarEventDao
import com.desklauncher.core.database.dao.DiaryDao
import com.desklauncher.core.database.dao.DocumentDao
import com.desklauncher.core.database.dao.DoodleDao
import com.desklauncher.core.database.dao.MemoDao
import com.desklauncher.core.database.dao.ProjectDao
import com.desklauncher.core.database.dao.TodoDao
import com.desklauncher.core.database.dao.ToolConfigDao
import com.desklauncher.core.database.entity.AlarmEntity
import com.desklauncher.core.database.entity.AppPrefEntity
import com.desklauncher.core.database.entity.CalendarEventEntity
import com.desklauncher.core.database.entity.DiaryEntryEntity
import com.desklauncher.core.database.entity.DocumentEntity
import com.desklauncher.core.database.entity.DocumentFolderEntity
import com.desklauncher.core.database.entity.DoodleEntity
import com.desklauncher.core.database.entity.MemoEntity
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
        MemoEntity::class,
        CalendarEventEntity::class,
        DiaryEntryEntity::class,
        DocumentFolderEntity::class,
        DocumentEntity::class,
        DoodleEntity::class,
        AlarmEntity::class,
    ],
    version = 9,
    exportSchema = true,
    autoMigrations = [
        // v2: app_pref 추가 (SHELL-08)
        AutoMigration(from = 1, to = 2),
        // v3: project, todo 추가 (TODO-01)
        AutoMigration(from = 2, to = 3),
        // v4: memo 추가 (MEMO-02)
        AutoMigration(from = 3, to = 4),
        // v5: calendar_event 추가 (CAL-01)
        AutoMigration(from = 4, to = 5),
        // v6: diary_entry 추가 (DIARY-01)
        AutoMigration(from = 5, to = 6),
        // v7: document_folder, document 추가 (DOC-01/02)
        AutoMigration(from = 6, to = 7),
        // v8: doodle 추가 (DOODLE-03)
        AutoMigration(from = 7, to = 8),
        // v9: alarm 추가 (ALARM-01)
        AutoMigration(from = 8, to = 9),
    ],
)
abstract class DeskDatabase : RoomDatabase() {
    abstract fun toolConfigDao(): ToolConfigDao
    abstract fun appPrefDao(): AppPrefDao
    abstract fun projectDao(): ProjectDao
    abstract fun todoDao(): TodoDao
    abstract fun memoDao(): MemoDao
    abstract fun calendarEventDao(): CalendarEventDao
    abstract fun diaryDao(): DiaryDao
    abstract fun documentDao(): DocumentDao
    abstract fun doodleDao(): DoodleDao
    abstract fun alarmDao(): AlarmDao

    companion object {
        const val NAME = "desk.db"
    }
}
