package com.desklauncher.feature.desk.clock

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.util.Locale

class DeskClockFormatterTest {

    private val afternoon = LocalDateTime.of(2026, 11, 26, 15, 4)

    // 24시간제는 두 자리 시:분
    @Test
    fun format_uses24HourPattern() {
        val text = DeskClockFormatter.format(afternoon, is24Hour = true, locale = Locale.KOREAN)
        assertEquals("15:04", text.time)
    }

    // 12시간제는 오전/오후 표시
    @Test
    fun format_uses12HourPatternWithMeridiem() {
        val text = DeskClockFormatter.format(afternoon, is24Hour = false, locale = Locale.KOREAN)
        assertEquals("오후 3:04", text.time)
    }

    // 한국어 날짜는 "M월 d일 요일"
    @Test
    fun format_koreanDate() {
        val text = DeskClockFormatter.format(afternoon, is24Hour = true, locale = Locale.KOREAN)
        assertEquals("11월 26일 목요일", text.date)
    }
}
