package com.desklauncher.feature.desk.strip

import kotlin.math.abs

/** 훑기 판정 결과 (PRD §4.2) */
sealed interface ScrubEvent {
    /** 탐색 시작 — 이 순간부터 부모가 입력을 가져가고 아래 항목의 대기 중 클릭은 취소된다 */
    data class Claimed(val focus: Int) : ScrubEvent
    data class FocusChanged(val focus: Int) : ScrubEvent
    data class CancelArmed(val armed: Boolean) : ScrubEvent
    /** 탐색 중 손을 뗌 → 이 칸 하나를 연다 */
    data class Open(val index: Int) : ScrubEvent
    /** 취소 대기에서 뗐거나 시스템이 입력을 취소 — 아무것도 열지 않는다 */
    data object Cancelled : ScrubEvent
}

/**
 * 오른쪽 훑기 영역의 한 번의 터치를 판정하는 순수 상태 기계 (STRIP-02).
 * 한 터치에는 한 결과(PRD 원칙 6): 탐색으로 넘어가기 전에 떼면 아무 사건도 내지 않아 아래 항목이 탭을 처리한다.
 *
 * 좌표: x는 부모 기준 px, y는 서류철 영역 맨 위 기준 px([StripGeometry]와 같은 좌표).
 * @param zoneLeft 훑기 영역 왼쪽 끝 x — 이보다 [cancelDistance] 넘게 왼쪽(본문 쪽)으로 빼면 취소 대기
 */
class ScrubTracker(
    private val geometry: StripGeometry,
    private val touchSlop: Float,
    private val zoneLeft: Float,
    private val cancelDistance: Float,
    private val hysteresisFraction: Float = DefaultHysteresis,
) {
    private sealed interface Phase {
        data object Idle : Phase
        data class Pending(val downX: Float, val downY: Float) : Phase
        data class Exploring(val focus: Int, val cancelArmed: Boolean) : Phase
        /** 가로 이동이 먼저 확정 — 이 터치는 아래 항목 몫 */
        data object Abandoned : Phase
    }

    private var phase: Phase = Phase.Idle

    val isExploring: Boolean get() = phase is Phase.Exploring
    val isAbandoned: Boolean get() = phase is Phase.Abandoned

    fun down(x: Float, y: Float) {
        phase = Phase.Pending(x, y)
    }

    fun move(x: Float, y: Float): List<ScrubEvent> = when (val current = phase) {
        is Phase.Pending -> {
            val dx = abs(x - current.downX)
            val dy = abs(y - current.downY)
            when {
                // 가로가 먼저면 서류철은 개입하지 않는다
                dx > touchSlop && dx >= dy -> {
                    phase = Phase.Abandoned
                    emptyList()
                }
                dy > touchSlop -> {
                    val focus = geometry.nextFocus(null, y, hysteresisFraction)
                    phase = Phase.Exploring(focus, cancelArmed = false)
                    listOf(ScrubEvent.Claimed(focus))
                }
                else -> emptyList()
            }
        }
        is Phase.Exploring -> {
            val events = mutableListOf<ScrubEvent>()
            val focus = geometry.nextFocus(current.focus, y, hysteresisFraction)
            val armed = x < zoneLeft - cancelDistance
            if (focus != current.focus) events += ScrubEvent.FocusChanged(focus)
            if (armed != current.cancelArmed) events += ScrubEvent.CancelArmed(armed)
            phase = Phase.Exploring(focus, armed)
            events
        }
        Phase.Idle, Phase.Abandoned -> emptyList()
    }

    /** 슬롭 안에서 머묾 시간이 지남 → 누른 자리의 칸으로 탐색 시작 */
    fun holdElapsed(): List<ScrubEvent> {
        val current = phase as? Phase.Pending ?: return emptyList()
        val focus = geometry.nextFocus(null, current.downY, hysteresisFraction)
        phase = Phase.Exploring(focus, cancelArmed = false)
        return listOf(ScrubEvent.Claimed(focus))
    }

    fun up(): List<ScrubEvent> {
        val current = phase
        phase = Phase.Idle
        return when (current) {
            is Phase.Exploring -> listOf(if (current.cancelArmed) ScrubEvent.Cancelled else ScrubEvent.Open(current.focus))
            // 탐색 전이면 아래 항목의 탭 — 서류철은 아무것도 하지 않는다
            else -> emptyList()
        }
    }

    fun cancel(): List<ScrubEvent> {
        val current = phase
        phase = Phase.Idle
        return if (current is Phase.Exploring) listOf(ScrubEvent.Cancelled) else emptyList()
    }

    companion object {
        /** 경계 이동 여유 = 칸의 15% [실험] (PRD §4.2) */
        const val DefaultHysteresis = 0.15f

        /** 머묾 시간 = 시스템 길게 누르기 × 0.75 (기본 400→300ms). 접근성 지연을 늘린 사용자는 함께 길어진다 */
        fun holdMillis(longPressTimeoutMillis: Long): Long = longPressTimeoutMillis * 3 / 4
    }
}
