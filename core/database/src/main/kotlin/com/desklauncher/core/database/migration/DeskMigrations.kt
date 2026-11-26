package com.desklauncher.core.database.migration

import androidx.room.migration.Migration

/**
 * 수동 스키마 마이그레이션 목록. 테이블·컬럼 '추가'만 있는 변경은 DeskDatabase의 AutoMigration을 쓰고,
 * 이름 변경·데이터 변환이 필요한 변경은 여기에 Migration을 추가한다.
 *
 * 이력: v1 tool_config / v2 app_pref(Auto) / v3 project·todo(Auto) / v4 memo(Auto)
 * `fallbackToDestructiveMigration`은 사용하지 않는다 (AGENTS.md §4.4).
 */
object DeskMigrations {
    val ALL: Array<Migration> = emptyArray()
}
