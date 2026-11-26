package com.desklauncher.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** 스키마 버전마다 기존 데이터가 보존되는지 확인 (AGENTS.md §4.4). 기기/에뮬레이터에서 실행. */
@RunWith(AndroidJUnit4::class)
class DeskMigrationTest {

    @get:Rule
    val helper = MigrationTestHelper(InstrumentationRegistry.getInstrumentation(), DeskDatabase::class.java)

    // v1 → v2: 도구함 설정 보존 + app_pref 생성
    @Test
    fun migrate1To2_keepsToolConfigAndAddsAppPref() {
        helper.createDatabase(TestDb, 1).apply {
            execSQL("INSERT INTO tool_config (toolId, enabled, sortOrder, tabColor) VALUES ('todo', 0, 3, 'Green')")
            close()
        }

        val db = helper.runMigrationsAndValidate(TestDb, 2, true)

        db.query("SELECT enabled, sortOrder, tabColor FROM tool_config WHERE toolId = 'todo'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
            assertEquals(3, cursor.getInt(1))
            assertEquals("Green", cursor.getString(2))
        }
        db.execSQL("INSERT INTO app_pref (componentKey) VALUES ('pkg/.Main')")
        db.query("SELECT hidden, favorite, launchCount FROM app_pref").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
            assertEquals(0, cursor.getInt(1))
            assertEquals(0, cursor.getInt(2))
        }
    }

    // v2 → v3: project·todo 생성, 할일 삭제 없이 프로젝트 삭제 시 projectId가 비워짐(SET NULL)
    @Test
    fun migrate2To3_addsProjectAndTodo() {
        helper.createDatabase(TestDb, 2).close()

        val db = helper.runMigrationsAndValidate(TestDb, 3, true)

        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL("INSERT INTO project (id, name, color, createdAt, updatedAt) VALUES ('p1', '책상 런처', 'Blue', 0, 0)")
        db.execSQL("INSERT INTO todo (id, title, projectId, createdAt, updatedAt) VALUES ('t1', '스트립 튜닝', 'p1', 0, 0)")
        db.execSQL("DELETE FROM project WHERE id = 'p1'")
        db.query("SELECT projectId, isDone, priority FROM todo WHERE id = 't1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
            assertEquals(0, cursor.getInt(1))
            assertEquals("NONE", cursor.getString(2))
        }
    }

    // v3 → v4: memo 생성, 프로젝트 삭제 시 메모도 삭제(CASCADE)
    @Test
    fun migrate3To4_addsMemoWithCascade() {
        helper.createDatabase(TestDb, 3).apply {
            execSQL("INSERT INTO project (id, name, color, createdAt, updatedAt) VALUES ('p1', '책상', 'Blue', 0, 0)")
            close()
        }

        val db = helper.runMigrationsAndValidate(TestDb, 4, true)

        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL("INSERT INTO memo (id, projectId, createdAt, updatedAt) VALUES ('m1', 'p1', 0, 0)")
        db.execSQL("DELETE FROM project WHERE id = 'p1'")
        db.query("SELECT COUNT(*) FROM memo").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    private companion object {
        const val TestDb = "migration-test.db"
    }
}
