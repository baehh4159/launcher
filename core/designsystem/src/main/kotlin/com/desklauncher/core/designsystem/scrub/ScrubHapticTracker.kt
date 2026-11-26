package com.desklauncher.core.designsystem.scrub

import com.desklauncher.core.designsystem.motion.DeskMotion
import kotlin.math.abs

sealed interface ScrubHapticEvent {
    data class Tick(val index: Int) : ScrubHapticEvent
    data object Edge : ScrubHapticEvent
}

/**
 * 연속 포커스 → 햅틱 이벤트. 경계를 '넘는 순간'에만 1회 (PRD §3.4).
 * 히스테리시스: 현재 탭 중심에서 0.5 + [hysteresis] 이상 벗어나야 다음 탭으로 인정한다.
 */
class ScrubHapticTracker(
    private val hysteresis: Float = DeskMotion.HapticHysteresis,
    private val edgeThreshold: Float = DeskMotion.HapticEdgeThreshold,
) {
    var index: Int = 0
        private set
    private var atEdge = false

    fun reset(focus: Float, count: Int) {
        index = IndexScrubMath.nearestIndex(focus, count)
        atEdge = false
    }

    fun update(focus: Float, count: Int): ScrubHapticEvent? {
        if (count <= 0) return null
        val last = (count - 1).toFloat()
        val beyond = focus < -edgeThreshold || focus > last + edgeThreshold
        if (beyond && !atEdge) {
            atEdge = true
            return ScrubHapticEvent.Edge
        }
        if (atEdge && focus >= -edgeThreshold / 2f && focus <= last + edgeThreshold / 2f) {
            atEdge = false
        }
        val candidate = IndexScrubMath.nearestIndex(focus, count)
        if (candidate != index && abs(focus - index) >= 0.5f + hysteresis) {
            index = candidate
            return ScrubHapticEvent.Tick(candidate)
        }
        return null
    }
}
