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

/** 설정의 햅틱 강도 (SHELL-11에서 DataStore와 연결). */
enum class HapticLevel { Off, Light, Default }

/** 앱이 설정값으로 제공한다. 기본값은 Default. */
val LocalDeskHapticLevel = staticCompositionLocalOf { HapticLevel.Default }

/**
 * 책상 햅틱 (SHELL-04, PRD §3.4) — '책을 툭 놓는' 둔탁한 질감.
 * S24+의 리니어 모터에서 PRIMITIVE_THUD를 우선 쓰고(넘김 약하게·확정 세게), 미지원이면 LOW_TICK → 단발 진동.
 * 모든 진동은 USAGE_TOUCH라 시스템 '터치 진동 세기' 설정을 따른다.
 * feature 모듈은 Vibrator를 직접 쓰지 않고 이 클래스만 쓴다.
 */
@Stable
class DeskHaptics internal constructor(
    private val view: View?,
    private val vibrator: Vibrator?,
    private val level: HapticLevel,
    private val uptimeMillis: () -> Long = SystemClock::uptimeMillis,
) {
    private val debouncer = HapticDebouncer()
    private val thudSupported = vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_THUD) == true
    private val lowTickSupported = vibrator?.areAllPrimitivesSupported(VibrationEffect.Composition.PRIMITIVE_LOW_TICK) == true
    private val touch = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_TOUCH)

    /** 탭 경계 통과: 짧고 둔탁한 THUD. 너무 촘촘하면 건너뛴다(잔향 방지). */
    fun segmentTick() {
        val event = debouncer.tick(uptimeMillis()) ?: return
        play(event)
    }

    /** 첫/마지막 서류철을 넘어 당김(벽) */
    fun edge() = play(HapticEvent.Edge)

    /** 서류철 열기 확정: 묵직한 THUD 한 번 */
    fun confirm() {
        debouncer.confirm(uptimeMillis())
        play(HapticEvent.Confirm)
    }

    /** 키패드 누름 (계산기 등) — 짧은 시스템 탭 */
    fun keyTap() {
        if (level == HapticLevel.Off) return
        view?.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    private fun play(event: HapticEvent) {
        val target = vibrator ?: return
        if (!target.hasVibrator()) return
        when (val plan = DeskHapticSpec.plan(event, level, thudSupported, lowTickSupported)) {
            HapticPlan.None -> Unit
            is HapticPlan.Primitive -> target.vibrate(
                VibrationEffect.startComposition()
                    .addPrimitive(plan.kind.toPrimitiveId(), plan.scale.coerceIn(0f, 1f))
                    .compose(),
                touch,
            )
            is HapticPlan.OneShot -> target.vibrate(
                VibrationEffect.createOneShot(plan.millis, if (target.hasAmplitudeControl()) plan.amplitude else VibrationEffect.DEFAULT_AMPLITUDE),
                touch,
            )
        }
    }

    private fun PrimitiveKind.toPrimitiveId(): Int = when (this) {
        PrimitiveKind.Thud -> VibrationEffect.Composition.PRIMITIVE_THUD
        PrimitiveKind.LowTick -> VibrationEffect.Composition.PRIMITIVE_LOW_TICK
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
