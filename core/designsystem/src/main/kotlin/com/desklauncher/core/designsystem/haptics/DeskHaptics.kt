package com.desklauncher.core.designsystem.haptics

import android.os.SystemClock
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import com.desklauncher.core.designsystem.motion.DeskMotion

/** 설정의 햅틱 강도 (SHELL-11에서 DataStore와 연결). */
enum class HapticLevel { Off, Light, Default }

/** 앱이 설정값으로 제공한다. 기본값은 Default. */
val LocalDeskHapticLevel = staticCompositionLocalOf { HapticLevel.Default }

/**
 * 책상 햅틱 (SHELL-04, PRD §3.4). feature 모듈은 Vibrator를 직접 쓰지 않고 이 클래스만 쓴다.
 * View 햅틱은 시스템 '터치 피드백' 설정을, Vibrator 진동은 USAGE_TOUCH 강도 설정을 따른다.
 */
@Stable
class DeskHaptics internal constructor(
    private val view: View?,
    private val vibrator: Vibrator?,
    private val level: HapticLevel,
    private val uptimeMillis: () -> Long = SystemClock::uptimeMillis,
) {
    private var lastTickAt = Long.MIN_VALUE / 2

    private val thudSupported: Boolean =
        vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD) == true

    /** 탭 경계 통과. 연속으로 빠르게 오면 가벼운 틱으로 바꾸고, 너무 촘촘하면 건너뛴다. */
    fun segmentTick() {
        if (level == HapticLevel.Off || view == null) return
        val now = uptimeMillis()
        val since = now - lastTickAt
        if (since < DeskMotion.HapticMinIntervalMillis) return
        lastTickAt = now
        val light = level == HapticLevel.Light || since < DeskMotion.HapticFastIntervalMillis
        view.performHapticFeedback(
            if (light) HapticFeedbackConstants.SEGMENT_FREQUENT_TICK else HapticFeedbackConstants.SEGMENT_TICK,
        )
    }

    /** 첫/마지막 서류철을 넘어 당김(벽). 지원 기기에서는 묵직한 THUD primitive. */
    fun edge() {
        if (level == HapticLevel.Off) return
        if (level == HapticLevel.Default && thudSupported && vibrator != null) {
            vibrator.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(VibrationEffect.Composition.PRIMITIVE_THUD, EdgeScale)
                    .compose(),
                VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH),
            )
        } else {
            view?.performHapticFeedback(HapticFeedbackConstants.GESTURE_THRESHOLD_ACTIVATE)
        }
    }

    /** 키패드 누름 (계산기 등) */
    fun keyTap() {
        if (level == HapticLevel.Off) return
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /** 서류철 열기 확정. */
    fun confirm() {
        if (level == HapticLevel.Off) return
        view?.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    private companion object {
        const val EdgeScale = 0.6f
    }
}

@Composable
fun rememberDeskHaptics(): DeskHaptics {
    val view = LocalView.current
    val context = LocalContext.current
    val level = LocalDeskHapticLevel.current
    val inspection = LocalInspectionMode.current
    return remember(view, context, level, inspection) {
        if (inspection) {
            DeskHaptics(view = null, vibrator = null, level = HapticLevel.Off)
        } else {
            DeskHaptics(
                view = view,
                vibrator = context.getSystemService(VibratorManager::class.java)?.defaultVibrator,
                level = level,
            )
        }
    }
}
