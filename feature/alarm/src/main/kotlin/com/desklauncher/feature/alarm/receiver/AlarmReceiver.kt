package com.desklauncher.feature.alarm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.desklauncher.core.common.dispatcher.ApplicationScope
import com.desklauncher.feature.alarm.domain.AlarmSync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 예약한 시각에 AlarmManager가 깨운다. 다음 회차 예약/끄기를 처리한다 (울림 표시는 ALARM-04). */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmSync: AlarmSync

    @Inject @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ActionFire) return
        val alarmId = intent.getStringExtra(ExtraAlarmId) ?: return
        val pending = goAsync()
        appScope.launch {
            try {
                onFire(context, alarmId, snooze = intent.getBooleanExtra(ExtraSnooze, false))
            } finally {
                pending.finish()
            }
        }
    }

    private suspend fun onFire(@Suppress("UNUSED_PARAMETER") context: Context, alarmId: String, snooze: Boolean) {
        // 다시 울림 회차는 원래 예약에 영향을 주지 않는다
        if (!snooze) alarmSync.onFired(alarmId)
    }

    companion object {
        const val ActionFire = "com.desklauncher.feature.alarm.FIRE"
        const val ExtraAlarmId = "alarmId"
        const val ExtraSnooze = "snooze"
    }
}
