package com.desklauncher.core.database.migration

import androidx.room.migration.Migration

/**
 * 모든 스키마 마이그레이션 목록. 버전을 올릴 때마다 여기에 추가한다.
 * `fallbackToDestructiveMigration`은 사용하지 않는다 (AGENTS.md §4.4).
 */
object DeskMigrations {
    val ALL: Array<Migration> = emptyArray()
}
