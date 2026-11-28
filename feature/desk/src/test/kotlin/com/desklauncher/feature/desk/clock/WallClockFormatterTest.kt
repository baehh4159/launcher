package com.desklauncher.feature.desk.clock

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale

class WallClockFormatterTest {

    private val seoul = ZoneId.of("Asia/Seoul")
    private val afternoon = ZonedDateTime.of(2026, 11, 28, 15, 4, 0, 0, seoul).toInstant().toEpochMilli()

    // 12시간제는 오전/오후 표기, 날짜는 월·일·요일
    @Test
    fun format_usesKoreanAmPm_when12Hour() {
        val text = WallClockFormatter.format(afternoon, seoul, is24Hour = false, locale = Locale.KOREAN)
        assertEquals("오후 3:04", text.time)
        assertEquals("11월 28일 토요일", text.date)
    }

    // 24시간제
    @Test
    fun format_uses24Hour_whenSystemSaysSo() {
        val text = WallClockFormatter.format(afternoon, seoul, is24Hour = true, locale = Locale.KOREAN)
        assertEquals("15:04", text.time)
    }
}
