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

    // v4 → v5: calendar_event 생성
    @Test
    fun migrate4To5_addsCalendarEvent() {
        helper.createDatabase(TestDb, 4).close()

        val db = helper.runMigrationsAndValidate(TestDb, 5, true)

        db.execSQL("INSERT INTO calendar_event (id, title, startAt, endAt, createdAt, updatedAt) VALUES ('e1', '회의', 10, 20, 0, 0)")
        db.query("SELECT allDay, note, recurrenceRule FROM calendar_event").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
            assertEquals("", cursor.getString(1))
            assertTrue(cursor.isNull(2))
        }
    }

    // v5 → v6: diary_entry 생성, 같은 날짜 두 번은 거부(UNIQUE)
    @Test
    fun migrate5To6_addsDiaryWithUniqueDate() {
        helper.createDatabase(TestDb, 5).close()

        val db = helper.runMigrationsAndValidate(TestDb, 6, true)

        db.execSQL("INSERT INTO diary_entry (id, date, createdAt, updatedAt) VALUES ('d1', 20000, 0, 0)")
        val duplicated = runCatching {
            db.execSQL("INSERT INTO diary_entry (id, date, createdAt, updatedAt) VALUES ('d2', 20000, 0, 0)")
        }
        assertTrue(duplicated.isFailure)
    }

    // v6 → v7: 폴더를 지우면 서류는 루트로 (SET NULL)
    @Test
    fun migrate6To7_addsDocumentsWithFolderSetNull() {
        helper.createDatabase(TestDb, 6).close()

        val db = helper.runMigrationsAndValidate(TestDb, 7, true)

        db.execSQL("PRAGMA foreign_keys = ON")
        db.execSQL("INSERT INTO document_folder (id, name, createdAt, updatedAt) VALUES ('f1', '영수증', 0, 0)")
        db.execSQL("INSERT INTO document (id, folderId, title, pdfPath, createdAt, updatedAt) VALUES ('d1', 'f1', '스캔', 'documents/d1.pdf', 0, 0)")
        db.execSQL("DELETE FROM document_folder WHERE id = 'f1'")
        db.query("SELECT folderId, source FROM document WHERE id = 'd1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.isNull(0))
            assertEquals("SCAN", cursor.getString(1))
        }
    }

    // v7 → v8: doodle 생성
    @Test
    fun migrate7To8_addsDoodle() {
        helper.createDatabase(TestDb, 7).close()

        val db = helper.runMigrationsAndValidate(TestDb, 8, true)

        db.execSQL("INSERT INTO doodle (id, strokesPath, thumbnailPath, canvasWidth, canvasHeight, createdAt, updatedAt) VALUES ('n1', 'doodles/n1.json', 'doodles/n1.png', 1080, 1800, 0, 0)")
        db.query("SELECT title FROM doodle").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("", cursor.getString(0))
        }
    }

    // v8 → v9: alarm 생성 (기본값: 켜짐·진동·5분 다시 울림)
    @Test
    fun migrate8To9_addsAlarm() {
        helper.createDatabase(TestDb, 8).close()

        val db = helper.runMigrationsAndValidate(TestDb, 9, true)

        db.execSQL("INSERT INTO alarm (id, hour, minute, createdAt, updatedAt) VALUES ('a1', 7, 30, 0, 0)")
        db.query("SELECT enabled, vibrate, snoozeMinutes, repeatDays FROM alarm").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
            assertEquals(1, cursor.getInt(1))
            assertEquals(5, cursor.getInt(2))
            assertEquals(0, cursor.getInt(3))
        }
    }

    // v9 → v10: file_bookmark 생성, 같은 URI 중복 거부
    @Test
    fun migrate9To10_addsFileBookmark() {
        helper.createDatabase(TestDb, 9).close()

        val db = helper.runMigrationsAndValidate(TestDb, 10, true)

        db.execSQL("INSERT INTO file_bookmark (id, uri, displayName, addedAt) VALUES ('b1', 'content://x/1', 'a.pdf', 0)")
        assertTrue(runCatching { db.execSQL("INSERT INTO file_bookmark (id, uri, displayName, addedAt) VALUES ('b2', 'content://x/1', 'a.pdf', 0)") }.isFailure)
    }

    // v10 → v11: plugin_data 생성, (pluginId, key) 기본키라 같은 키는 덮어쓰기 대상
    @Test
    fun migrate10To11_addsPluginData() {
        helper.createDatabase(TestDb, 10).close()

        val db = helper.runMigrationsAndValidate(TestDb, 11, true)

        db.execSQL("INSERT INTO plugin_data (pluginId, `key`, value, updatedAt) VALUES ('water', 'count', '3', 0)")
        db.execSQL("INSERT INTO plugin_data (pluginId, `key`, value, updatedAt) VALUES ('other', 'count', '9', 0)")
        assertTrue(runCatching { db.execSQL("INSERT INTO plugin_data (pluginId, `key`, value, updatedAt) VALUES ('water', 'count', '4', 0)") }.isFailure)
        db.query("SELECT value FROM plugin_data WHERE pluginId = 'water'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("3", cursor.getString(0))
        }
    }

    private companion object {
        const val TestDb = "migration-test.db"
    }
}
