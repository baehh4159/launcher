package com.desklauncher.feature.alarm.ring

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.domain.repository.AlarmRepository
import com.desklauncher.feature.alarm.receiver.AlarmReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/** 잠금화면 위 알람 울림 화면 (ALARM-04). 소리는 알림 채널이 내므로 여기서는 끄기/다시 울림만. */
@AndroidEntryPoint
class AlarmRingActivity : ComponentActivity() {

    @Inject lateinit var repository: AlarmRepository

    @Inject lateinit var controller: AlarmRingController

    private var alarm by mutableStateOf<Alarm?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val alarmId = intent.getStringExtra(AlarmReceiver.ExtraAlarmId)
        if (alarmId == null) {
            finish()
            return
        }
        lifecycleScope.launch { alarm = repository.get(alarmId) }
        setContent {
            DeskTheme(darkTheme = true) {
                AlarmRingScreen(
                    alarm = alarm,
                    onDismiss = {
                        controller.dismiss(alarmId)
                        finish()
                    },
                    onSnooze = {
                        lifecycleScope.launch {
                            controller.snooze(alarmId)
                            finish()
                        }
                    },
                )
            }
        }
    }
}
