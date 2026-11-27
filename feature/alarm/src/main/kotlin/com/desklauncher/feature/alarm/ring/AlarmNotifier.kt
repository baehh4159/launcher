package com.desklauncher.feature.alarm.ring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import com.desklauncher.feature.alarm.R
import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.receiver.AlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 울리는 알람 알림 (ALARM-04).
 * 소리·진동은 알람 채널(USAGE_ALARM)이 내고 FLAG_INSISTENT로 끌 때까지 반복한다.
 * 잠금화면/화면 꺼짐이면 전체 화면 인텐트로 [AlarmRingActivity]가 뜨고, 사용 중이면 헤드업 + 버튼.
 */
@Singleton
class AlarmNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val manager: NotificationManager = context.getSystemService(NotificationManager::class.java)

    /** Android 14+: 사용자가 끄면 헤드업 알림으로만 울린다 → 목록에서 안내 */
    fun canUseFullScreen(): Boolean = manager.canUseFullScreenIntent()

    fun showRinging(alarm: Alarm) {
        ensureChannels()
        val ringIntent = Intent(context, AlarmRingActivity::class.java)
            .putExtra(AlarmReceiver.ExtraAlarmId, alarm.id)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
        val ringPending = PendingIntent.getActivity(context, alarm.id.hashCode(), ringIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val title = alarm.label.ifBlank { context.getString(R.string.alarm_tool_title) }
        val time = alarm.time.format(DateTimeFormatter.ofPattern("HH:mm"))
        val notification = Notification.Builder(context, if (alarm.vibrate) ChannelVibrate else ChannelSilent)
            .setSmallIcon(R.drawable.ic_alarm_notification)
            .setContentTitle(title)
            .setContentText(time)
            .setCategory(Notification.CATEGORY_ALARM)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(ringPending, true)
            .setContentIntent(ringPending)
            .setTimeoutAfter(RingTimeoutMillis)
            .addAction(Notification.Action.Builder(null, context.getString(R.string.alarm_ring_snooze_short, alarm.snoozeMinutes), actionIntent(alarm.id, AlarmReceiver.ActionSnooze)).build())
            .addAction(Notification.Action.Builder(null, context.getString(R.string.alarm_ring_dismiss), actionIntent(alarm.id, AlarmReceiver.ActionDismiss)).build())
            .build()
            .apply { flags = flags or Notification.FLAG_INSISTENT }
        manager.notify(notificationId(alarm.id), notification)
    }

    fun cancel(alarmId: String) = manager.cancel(notificationId(alarmId))

    private fun actionIntent(alarmId: String, action: String): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).setAction(action).putExtra(AlarmReceiver.ExtraAlarmId, alarmId)
        return PendingIntent.getBroadcast(context, (alarmId + action).hashCode(), intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun ensureChannels() {
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        listOf(ChannelVibrate to true, ChannelSilent to false).forEach { (id, vibrate) ->
            val name = context.getString(if (vibrate) R.string.alarm_channel_vibrate else R.string.alarm_channel_sound_only)
            manager.createNotificationChannel(
                NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
                    setSound(sound, attributes)
                    enableVibration(vibrate)
                    if (vibrate) vibrationPattern = longArrayOf(0, 800, 600)
                    setBypassDnd(true)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                },
            )
        }
    }

    private companion object {
        const val ChannelVibrate = "alarm_ring_vibrate"
        const val ChannelSilent = "alarm_ring_sound_only"

        /** 끄지 않으면 10분 뒤 자동으로 멈춘다 */
        const val RingTimeoutMillis = 10 * 60_000L

        fun notificationId(alarmId: String): Int = 0x41_00_00 xor alarmId.hashCode()
    }
}
