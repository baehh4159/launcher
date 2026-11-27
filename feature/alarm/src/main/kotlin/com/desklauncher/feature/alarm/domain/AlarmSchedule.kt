package com.desklauncher.feature.alarm.domain

import com.desklauncher.feature.alarm.domain.model.Alarm
import java.time.Duration
import java.time.ZonedDateTime

/** 다음 울림 시각 계산 (순수). 시간대·서머타임은 ZonedDateTime이 처리한다. */
object AlarmSchedule {

    /** [now] 이후(같은 분은 제외) 처음 울릴 시각. 반복 없으면 오늘 또는 내일. */
    fun nextTrigger(alarm: Alarm, now: ZonedDateTime): ZonedDateTime {
        val today = now.toLocalDate()
        for (offset in 0..7L) {
            val date = today.plusDays(offset)
            if (alarm.isRepeating && date.dayOfWeek !in alarm.repeatDays) continue
            val candidate = ZonedDateTime.of(date, alarm.time, now.zone)
            if (candidate.isAfter(now)) return candidate
        }
        // 반복 요일이 이번 주 지금 이후로 없으면(지금 시각이 이미 지난 같은 요일) 다음 주 같은 요일
        return ZonedDateTime.of(today.plusDays(7), alarm.time, now.zone)
    }

    /** "n시간 m분 후" 표시용 */
    fun untilNext(alarm: Alarm, now: ZonedDateTime): Duration = Duration.between(now, nextTrigger(alarm, now))
}
