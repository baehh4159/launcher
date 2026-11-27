package com.desklauncher.feature.alarm.domain

import java.time.Instant

/** 시스템 알람 예약 창구 (구현: AlarmManager). 도메인은 이 계약만 안다. */
interface AlarmScheduler {
    /** 정확한 알람 권한(SCHEDULE_EXACT_ALARM)이 있는지 */
    fun canScheduleExact(): Boolean

    /** 권한이 없으면 false (예약하지 못함) */
    fun schedule(alarmId: String, at: Instant): Boolean

    /** 다시 울림 — 원래 예약과 겹치지 않는 별도 예약 */
    fun scheduleSnooze(alarmId: String, at: Instant): Boolean

    fun cancel(alarmId: String)
}
