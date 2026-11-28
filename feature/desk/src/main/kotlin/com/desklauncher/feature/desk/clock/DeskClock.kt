package com.desklauncher.feature.desk.clock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 벽 시계 문자열 */
@Immutable
data class WallClockText(val time: String, val date: String)

/** 벽 시계 서식 (순수). 24시간제 여부는 시스템 설정을 따른다. */
object WallClockFormatter {
    fun format(epochMillis: Long, zone: ZoneId, is24Hour: Boolean, locale: Locale): WallClockText {
        val dateTime = Instant.ofEpochMilli(epochMillis).atZone(zone)
        val time = DateTimeFormatter.ofPattern(if (is24Hour) "H:mm" else "a h:mm", locale).format(dateTime)
        val date = DateTimeFormatter.ofPattern("M월 d일 EEEE", locale).format(dateTime)
        return WallClockText(time = time, date = date)
    }
}

/** 분이 바뀔 때마다 갱신되는 현재 시각(epoch millis). 홈 복귀 시 다시 구성되므로 놓친 분은 즉시 맞춰진다. */
@Composable
fun rememberCurrentMinute(): Long {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            val current = System.currentTimeMillis()
            now = current
            delay(MinuteMillis - current % MinuteMillis)
        }
    }
    return now
}

private const val MinuteMillis = 60_000L
