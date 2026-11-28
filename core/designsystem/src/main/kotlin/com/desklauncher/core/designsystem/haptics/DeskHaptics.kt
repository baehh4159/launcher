package com.desklauncher.core.designsystem.haptics

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/** 촉각 설정 (PRD §5) */
enum class HapticMode {
    /** 탐색 + 확정 (기본) */
    Full,
    /** 확정만 */
    ConfirmOnly,
    Off,
}

val LocalHapticMode = compositionLocalOf { HapticMode.Full }

/**
 * 두 단계 촉각 (PRD §5): 칸 이동·머묾 = [tick], 열기·꽂기 확정 = [confirm].
 * View.performHapticFeedback을 쓰므로 시스템 촉각 설정을 따르고 VIBRATE 권한이 필요 없다.
 * feature 모듈은 Vibrator·HapticFeedbackConstants를 직접 쓰지 않는다 (AGENTS.md §5).
 */
@Stable
class DeskHaptics internal constructor(
    private val view: View,
    private val mode: HapticMode,
) {
    fun tick() {
        if (mode == HapticMode.Full) view.performHapticFeedback(HapticFeedbackConstants.SEGMENT_FREQUENT_TICK)
    }

    fun confirm() {
        if (mode != HapticMode.Off) view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }
}

@Composable
fun rememberDeskHaptics(): DeskHaptics {
    val view = LocalView.current
    val mode = LocalHapticMode.current
    return remember(view, mode) { DeskHaptics(view, mode) }
}
