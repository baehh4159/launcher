package com.desklauncher.feature.alarm.receiver

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.desklauncher.core.common.dispatcher.ApplicationScope
import com.desklauncher.feature.alarm.domain.AlarmSync
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * AlarmManager 예약은 재부팅·앱 업데이트 때 사라지고, 시각·시간대가 바뀌면 다음 울림이 달라진다 (ALARM-05).
 * 이때 켜진 알람을 모두 다시 계산해 예약한다. 정확한 알람 권한을 새로 받은 경우도 포함.
 * 주의: 첫 잠금 해제 전(Direct Boot)에는 DB를 읽을 수 없어 BOOT_COMPLETED(잠금 해제 후)에 처리된다.
 */
@AndroidEntryPoint
class AlarmRescheduleReceiver : BroadcastReceiver() {

    @Inject lateinit var alarmSync: AlarmSync

    @Inject @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HandledActions) return
        val pending = goAsync()
        appScope.launch {
            try {
                alarmSync.rescheduleAll()
            } finally {
                pending.finish()
            }
        }
    }

    private companion object {
        val HandledActions = setOf(
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED,
        )
    }
}
