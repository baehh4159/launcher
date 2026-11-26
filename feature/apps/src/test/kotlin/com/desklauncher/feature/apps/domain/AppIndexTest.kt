package com.desklauncher.feature.apps.domain

import com.desklauncher.feature.apps.domain.model.InstalledApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppIndexTest {

    private fun app(label: String) = InstalledApp(componentKey = "pkg/$label", packageName = "pkg", label = label)

    // 한글은 초성, 쌍자음은 기본 자음, 영문은 대문자, 나머지는 #
    @Test
    fun keyOf_mapsFirstCharacter() {
        assertEquals("ㅋ", AppIndex.keyOf("카카오톡"))
        assertEquals("ㄱ", AppIndex.keyOf("꿀벌"))
        assertEquals("ㅅ", AppIndex.keyOf("쌍용"))
        assertEquals("G", AppIndex.keyOf("gmail"))
        assertEquals("#", AppIndex.keyOf("1Password"))
        assertEquals("#", AppIndex.keyOf(""))
    }

    @Test
    fun choseongString_extractsInitialConsonants() {
        assertEquals("ㅋㅋㅇㅌ", AppIndex.choseongString("카카오톡"))
        assertEquals("ㄴㅇㅂ map", AppIndex.choseongString("네이버 Map"))
    }

    // 자음만 입력하면 초성 검색
    @Test
    fun matches_supportsChoseongSearch() {
        assertTrue(AppIndex.matches(app("카카오톡"), "ㅋㅋ"))
        assertTrue(AppIndex.matches(app("카카오 택시"), "ㅋㅋㅇㅌ"))
        assertFalse(AppIndex.matches(app("카카오톡"), "ㄴㅇ"))
    }

    // 일반 검색은 대소문자·공백 무시 포함 검색
    @Test
    fun matches_isCaseAndSpaceInsensitive() {
        assertTrue(AppIndex.matches(app("Google Maps"), "googlem"))
        assertTrue(AppIndex.matches(app("카카오 택시"), "오택"))
        assertTrue(AppIndex.matches(app("아무거나"), "  "))
    }

    // 한글 초성 → 영문 → # 순서, 같은 키 안에서는 가나다순
    @Test
    fun sorted_ordersByIndexKeyThenLabel() {
        val result = AppIndex.sorted(listOf(app("Youtube"), app("1Password"), app("카메라"), app("갤러리"), app("Chrome"), app("계산기")))
        assertEquals(listOf("갤러리", "계산기", "카메라", "Chrome", "Youtube", "1Password"), result.map { it.label })
    }
}
