package com.desklauncher.feature.desk.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * 분 단위 현재 시각. `delay` 루프 대신 시스템 TIME_TICK을 쓴다 —
 * 코루틴 delay는 기기 절전 중 멈춰 화면을 켰을 때 시계가 늦을 수 있기 때문.
 */
@Composable
internal fun rememberCurrentMinute(): LocalDateTime {
    val context = LocalContext.current
    var now by remember { mutableStateOf(currentMinute()) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                now = currentMinute()
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    // 화면이 꺼져 있던 동안 놓친 TICK 보정
    LifecycleResumeEffect(Unit) {
        now = currentMinute()
        onPauseOrDispose { }
    }
    return now
}

private fun currentMinute(): LocalDateTime = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES)
