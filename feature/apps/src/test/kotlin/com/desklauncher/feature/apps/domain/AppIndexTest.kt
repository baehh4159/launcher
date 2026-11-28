package com.desklauncher.feature.apps.domain

import com.desklauncher.feature.apps.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIndexTest {

    // 쌍자음은 기본 자음 묶음으로, 영문은 대문자, 그 외는 #
    @Test
    fun keyOf_mapsToIndexKeys() {
        assertEquals("ㅋ", AppIndex.keyOf("카카오톡"))
        assertEquals("ㄱ", AppIndex.keyOf("꿀팁"))
        assertEquals("G", AppIndex.keyOf("gmail"))
        assertEquals("#", AppIndex.keyOf("1password"))
    }

    // 자음만 입력하면 초성 검색, 공백 무시
    @Test
    fun matches_choseongQuery() {
        assertTrue(AppIndex.matches("카카오톡", "ㅋㅋㅇ"))
        assertTrue(AppIndex.matches("삼성 노트", "ㅅㅅㄴ"))
        assertFalse(AppIndex.matches("카카오톡", "ㅋㅌ"))
    }

    // 일반 검색은 포함·대소문자 무시
    @Test
    fun matches_plainQuery() {
        assertTrue(AppIndex.matches("Chrome", "chr"))
        assertTrue(AppIndex.matches("삼성 노트", "성노"))
    }

    // 묶음은 초성 → 영문 → # 순서, 묶음 안은 한국어 정렬
    @Test
    fun sections_orderedByIndexThenCollation() {
        val apps = listOf("Gmail", "캘린더", "카카오톡", "1Password", "갤러리").map { InstalledApp(it, it, it) }
        val sections = AppIndex.sections(apps)
        assertEquals(listOf("ㄱ", "ㅋ", "G", "#"), sections.map { it.key })
        assertEquals(listOf("카카오톡", "캘린더"), sections[1].apps.map { it.label })
    }
}
