package com.desklauncher.feature.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.desklauncher.core.common.dispatcher.ApplicationScope
import com.desklauncher.feature.alarm.domain.AlarmSync
import com.desklauncher.feature.alarm.domain.repository.AlarmRepository
import com.desklauncher.feature.alarm.ring.AlarmNotifier
import com.desklauncher.feature.alarm.ring.AlarmRingController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * - FIRE: AlarmManager가 깨움 → 울림 알림(전체 화면) 표시 → 다음 회차 예약/끄기
 * - DISMISS / SNOOZE: 알림 버튼
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmSync: AlarmSync

    @Inject lateinit var repository: AlarmRepository

    @Inject lateinit var notifier: AlarmNotifier

    @Inject lateinit var controller: AlarmRingController

    @Inject @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getStringExtra(ExtraAlarmId) ?: return
        val pending = goAsync()
        appScope.launch {
            try {
                when (intent.action) {
                    ActionFire -> {
                        repository.get(alarmId)?.let(notifier::showRinging)
                        // 다시 울림 회차는 원래 예약에 영향을 주지 않는다
                        if (!intent.getBooleanExtra(ExtraSnooze, false)) alarmSync.onFired(alarmId)
                    }
                    ActionDismiss -> controller.dismiss(alarmId)
                    ActionSnooze -> controller.snooze(alarmId)
                }
            } finally {
                pending.finish()
            }
        }
    }

    companion object {
        const val ActionFire = "com.desklauncher.feature.alarm.FIRE"
        const val ActionDismiss = "com.desklauncher.feature.alarm.DISMISS"
        const val ActionSnooze = "com.desklauncher.feature.alarm.SNOOZE"
        const val ExtraAlarmId = "alarmId"
        const val ExtraSnooze = "snooze"
    }
}
