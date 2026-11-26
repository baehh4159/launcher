package com.desklauncher.feature.desk.clock

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Immutable
data class DeskClockText(
    val time: String,
    val date: String,
)

/** 책상 시계 문자열. 시스템 12/24시간 설정과 로케일을 따른다. */
internal object DeskClockFormatter {

    fun format(now: LocalDateTime, is24Hour: Boolean, locale: Locale): DeskClockText {
        val timePattern = if (is24Hour) "HH:mm" else "a h:mm"
        val datePattern = if (locale.language == Locale.KOREAN.language) "M월 d일 EEEE" else "EEEE, MMMM d"
        return DeskClockText(
            time = now.format(DateTimeFormatter.ofPattern(timePattern, locale)),
            date = now.format(DateTimeFormatter.ofPattern(datePattern, locale)),
        )
    }
}
