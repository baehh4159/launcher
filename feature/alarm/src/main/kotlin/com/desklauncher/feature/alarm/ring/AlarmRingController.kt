package com.desklauncher.feature.alarm.ring

import com.desklauncher.feature.alarm.domain.AlarmScheduler
import com.desklauncher.feature.alarm.domain.repository.AlarmRepository
import java.time.Clock
import java.time.Duration
import javax.inject.Inject

/** 울림 끄기 / 다시 울림 (알림 버튼과 울림 화면이 함께 쓴다) */
class AlarmRingController @Inject constructor(
    private val notifier: AlarmNotifier,
    private val scheduler: AlarmScheduler,
    private val repository: AlarmRepository,
    private val clock: Clock,
) {
    fun dismiss(alarmId: String) = notifier.cancel(alarmId)

    suspend fun snooze(alarmId: String) {
        notifier.cancel(alarmId)
        val minutes = repository.get(alarmId)?.snoozeMinutes ?: DefaultSnoozeMinutes
        scheduler.scheduleSnooze(alarmId, clock.instant().plus(Duration.ofMinutes(minutes.toLong())))
    }

    private companion object {
        const val DefaultSnoozeMinutes = 5
    }
}
