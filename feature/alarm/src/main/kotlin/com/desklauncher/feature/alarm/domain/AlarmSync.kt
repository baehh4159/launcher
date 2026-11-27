package com.desklauncher.feature.alarm.domain

import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.domain.repository.AlarmRepository
import java.time.Clock
import java.time.ZonedDateTime
import javax.inject.Inject

/**
 * 알람 저장과 시스템 예약을 항상 함께 맞춘다 (ALARM-03, 05).
 * - 켜짐: 다음 울림 시각을 계산해 예약하고 nextTriggerAt에 기록
 * - 꺼짐/삭제: 예약 취소
 * - 울린 뒤: 반복이면 다음 회차 예약, 한 번짜리면 끔
 */
class AlarmSync @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler,
    private val clock: Clock,
) {
    /** 저장 + 예약. 예약하지 못했으면(권한 없음) false — 화면이 권한을 안내한다. */
    suspend fun save(alarm: Alarm): Boolean {
        if (!alarm.enabled) {
            scheduler.cancel(alarm.id)
            repository.save(alarm.copy(nextTriggerAt = null))
            return true
        }
        val next = AlarmSchedule.nextTrigger(alarm, ZonedDateTime.now(clock)).toInstant()
        val scheduled = scheduler.schedule(alarm.id, next)
        repository.save(alarm.copy(nextTriggerAt = if (scheduled) next else null))
        return scheduled
    }

    suspend fun delete(alarm: Alarm) {
        scheduler.cancel(alarm.id)
        repository.delete(alarm.id)
    }

    /** 알람이 울린 직후 */
    suspend fun onFired(alarmId: String) {
        val alarm = repository.get(alarmId) ?: return
        if (alarm.isRepeating) save(alarm) else repository.save(alarm.copy(enabled = false, nextTriggerAt = null))
    }

    /** 재부팅·시간 변경·권한 허용 뒤 켜진 알람을 모두 다시 예약 */
    suspend fun rescheduleAll() {
        repository.getEnabled().forEach { save(it) }
    }
}
