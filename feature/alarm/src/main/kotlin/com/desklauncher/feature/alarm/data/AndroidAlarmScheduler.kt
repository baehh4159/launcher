package com.desklauncher.feature.alarm.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.desklauncher.feature.alarm.domain.AlarmScheduler
import com.desklauncher.feature.alarm.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Instant
import javax.inject.Inject

/**
 * setAlarmClock: 절전(Doze) 중에도 정시에 울리고, 상태바에 다음 알람이 표시된다.
 * 정확한 알람 권한이 없으면 SecurityException — 예약하지 않고 false.
 */
internal class AndroidAlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) : AlarmScheduler {

    private val alarmManager: AlarmManager = context.getSystemService(AlarmManager::class.java)

    override fun canScheduleExact(): Boolean = alarmManager.canScheduleExactAlarms()

    override fun schedule(alarmId: String, at: Instant): Boolean = set(alarmId, at, snooze = false)

    override fun scheduleSnooze(alarmId: String, at: Instant): Boolean = set(alarmId, at, snooze = true)

    override fun cancel(alarmId: String) {
        alarmManager.cancel(firePendingIntent(alarmId, snooze = false))
        alarmManager.cancel(firePendingIntent(alarmId, snooze = true))
    }

    private fun set(alarmId: String, at: Instant, snooze: Boolean): Boolean {
        if (!canScheduleExact()) return false
        return try {
            val show = context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
                PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
            }
            alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(at.toEpochMilli(), show), firePendingIntent(alarmId, snooze))
            true
        } catch (_: SecurityException) {
            false
        }
    }

    private fun firePendingIntent(alarmId: String, snooze: Boolean): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
            .setAction(AlarmReceiver.ActionFire)
            .putExtra(AlarmReceiver.ExtraAlarmId, alarmId)
            .putExtra(AlarmReceiver.ExtraSnooze, snooze)
        return PendingIntent.getBroadcast(context, requestCode(alarmId, snooze), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        /** 알람마다 고유한 요청 코드 (다시 울림은 짝수/홀수로 구분) */
        fun requestCode(alarmId: String, snooze: Boolean): Int = alarmId.hashCode() * 2 + if (snooze) 1 else 0
    }
}
