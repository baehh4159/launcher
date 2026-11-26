package com.desklauncher.feature.todo.ui

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@Immutable
data class DueLabel(val text: String, val isOverdue: Boolean)

/** 마감 표시: 어제/오늘/내일, 그 외 "11월 30일 (월)", 시각이 있으면 덧붙인다. 지난 마감은 isOverdue. */
internal object DueLabelFormatter {

    fun format(
        date: LocalDate,
        time: LocalTime?,
        today: LocalDate,
        is24Hour: Boolean,
        locale: Locale,
        words: RelativeWords,
    ): DueLabel {
        val diff = ChronoUnit.DAYS.between(today, date)
        val day = when (diff) {
            -1L -> words.yesterday
            0L -> words.today
            1L -> words.tomorrow
            else -> date.format(DateTimeFormatter.ofPattern(if (date.year == today.year) "M월 d일 (E)" else "yyyy년 M월 d일", locale))
        }
        val timeText = time?.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "a h:mm", locale))
        return DueLabel(text = listOfNotNull(day, timeText).joinToString(" "), isOverdue = diff < 0)
    }

    data class RelativeWords(val yesterday: String, val today: String, val tomorrow: String)
}
