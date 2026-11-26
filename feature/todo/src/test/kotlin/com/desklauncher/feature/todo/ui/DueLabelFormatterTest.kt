package com.desklauncher.feature.todo.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.LocalTime
import java.util.Locale

class DueLabelFormatterTest {

    private val today = LocalDate.of(2026, 11, 26)
    private val words = DueLabelFormatter.RelativeWords("어제", "오늘", "내일")

    private fun label(date: LocalDate, time: LocalTime? = null, is24Hour: Boolean = true) =
        DueLabelFormatter.format(date, time, today, is24Hour, Locale.KOREAN, words)

    @Test
    fun format_usesRelativeWordsNearToday() {
        assertEquals("어제", label(today.minusDays(1)).text)
        assertEquals("오늘 09:30", label(today, LocalTime.of(9, 30)).text)
        assertEquals("내일 오후 3:00", label(today.plusDays(1), LocalTime.of(15, 0), is24Hour = false).text)
    }

    @Test
    fun format_usesDateWithWeekdayOtherwise() {
        assertEquals("11월 30일 (월)", label(LocalDate.of(2026, 11, 30)).text)
        assertEquals("2027년 1월 5일", label(LocalDate.of(2027, 1, 5)).text)
    }

    // 지난 마감만 overdue
    @Test
    fun format_marksOverdue() {
        assertTrue(label(today.minusDays(3)).isOverdue)
        assertFalse(label(today).isOverdue)
    }
}
