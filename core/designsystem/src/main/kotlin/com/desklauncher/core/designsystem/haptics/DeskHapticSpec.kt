package com.desklauncher.core.designsystem.haptics

import com.desklauncher.core.designsystem.motion.DeskMotion

/** 책상 햅틱 이벤트 */
enum class HapticEvent { Tick, FastTick, Edge, Confirm }

/** 실제로 낼 진동 (안드로이드 타입과 분리해 순수 로직으로 테스트) */
sealed interface HapticPlan {
    data class Primitive(val kind: PrimitiveKind, val scale: Float) : HapticPlan
    data class OneShot(val millis: Long, val amplitude: Int) : HapticPlan
    data object None : HapticPlan
}

enum class PrimitiveKind { Thud, LowTick }

/**
 * 이벤트 → 진동 선택 (순수).
 * 1순위 PRIMITIVE_THUD(둔탁·잔향 짧음), 미지원이면 PRIMITIVE_LOW_TICK, 둘 다 없으면 짧은 단발 진동.
 */
object DeskHapticSpec {

    fun plan(event: HapticEvent, level: HapticLevel, thudSupported: Boolean, lowTickSupported: Boolean): HapticPlan {
        if (level == HapticLevel.Off) return HapticPlan.None
        val factor = if (level == HapticLevel.Light) DeskMotion.LightLevelFactor else 1f
        val confirm = event == HapticEvent.Confirm
        return when {
            thudSupported -> HapticPlan.Primitive(PrimitiveKind.Thud, factor * thudScale(event))
            lowTickSupported -> HapticPlan.Primitive(
                PrimitiveKind.LowTick,
                factor * if (confirm || event == HapticEvent.Edge) DeskMotion.LowTickConfirmScale else DeskMotion.LowTickTickScale,
            )
            else -> HapticPlan.OneShot(
                millis = if (confirm) DeskMotion.OneShotConfirmMillis else DeskMotion.OneShotTickMillis,
                amplitude = (factor * if (confirm) DeskMotion.OneShotConfirmAmplitude else DeskMotion.OneShotTickAmplitude).toInt().coerceIn(1, 255),
            )
        }
    }

    private fun thudScale(event: HapticEvent): Float = when (event) {
        HapticEvent.Tick -> DeskMotion.ThudTickScale
        HapticEvent.FastTick -> DeskMotion.ThudTickFastScale
        HapticEvent.Edge -> DeskMotion.ThudEdgeScale
        HapticEvent.Confirm -> DeskMotion.ThudConfirmScale
    }
}

/**
 * 넘김 진동 디바운서 (순수). 최소 간격 안의 넘김은 버리고, 빠르게 이어지면 약한 넘김으로,
 * 확정 직후에는 잠시 조용히.
 */
class HapticDebouncer(
    private val minIntervalMillis: Long = DeskMotion.HapticMinIntervalMillis,
    private val fastIntervalMillis: Long = DeskMotion.HapticFastIntervalMillis,
    private val quietAfterConfirmMillis: Long = DeskMotion.HapticQuietAfterConfirmMillis,
) {
    private var lastTickAt = Long.MIN_VALUE / 2
    private var lastConfirmAt = Long.MIN_VALUE / 2

    /** 넘김을 낼지, 낸다면 보통/빠른 넘김인지. null = 내지 않음 */
    fun tick(now: Long): HapticEvent? {
        if (now - lastConfirmAt < quietAfterConfirmMillis) return null
        val since = now - lastTickAt
        if (since < minIntervalMillis) return null
        lastTickAt = now
        return if (since < fastIntervalMillis) HapticEvent.FastTick else HapticEvent.Tick
    }

    fun confirm(now: Long) {
        lastConfirmAt = now
    }
}
