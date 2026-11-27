package com.desklauncher.feature.alarm.domain.model

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime

data class Alarm(
    val id: String,
    val time: LocalTime,
    /** 비어 있으면 한 번만 */
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val label: String = "",
    val enabled: Boolean = true,
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 5,
    val ringtoneUri: String? = null,
    val nextTriggerAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    val isRepeating: Boolean get() = repeatDays.isNotEmpty()
}

/** 요일 비트마스크: 월=1 … 일=64 (PRD §8.3) */
object RepeatDays {
    fun toMask(days: Set<DayOfWeek>): Int = days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }
    fun fromMask(mask: Int): Set<DayOfWeek> = DayOfWeek.entries.filter { mask and (1 shl (it.value - 1)) != 0 }.toSet()
}
