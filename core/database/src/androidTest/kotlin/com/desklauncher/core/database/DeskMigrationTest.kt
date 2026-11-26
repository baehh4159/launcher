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

    private companion object {
        const val TestDb = "migration-test.db"
    }
}
