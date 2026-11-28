package com.desklauncher.feature.desk.strip

import kotlin.math.abs
import kotlin.math.hypot

/** 훑기 판정 결과 (PRD §4.2) */
sealed interface ScrubEvent {
    /** 탐색 시작 — 이 순간부터 부모가 입력을 가져가고 아래 항목의 대기 중 클릭은 취소된다 */
    data class Claimed(val focus: Int) : ScrubEvent
    data class FocusChanged(val focus: Int) : ScrubEvent
    /** 탐색 중 손을 뗌 → 이 칸 하나를 연다 */
    data class Open(val index: Int) : ScrubEvent
    /** 양옆으로 빠졌거나 시스템이 입력을 취소 — 즉시 탭을 접고, 손을 뗄 때까지 아무것도 열지 않는다 */
    data object Cancelled : ScrubEvent
}

/**
 * 오른쪽 훑기 영역의 한 번의 터치를 판정하는 순수 상태 기계 (STRIP-02, PRD §4.2).
 * 한 터치에는 한 결과(PRD 원칙 6): 탐색으로 넘어가기 전에 떼면 아무 사건도 내지 않아 아래 항목이 탭을 처리한다.
 *
 * - 영역 안에서 누름: 세로 슬롭을 넘거나 머묾 시간이 지나면 탐색, 가로가 먼저면 포기
 * - 빈 책상에서 누름: 끌다가 영역에 들어와 슬롭 안에서 머묾 시간만큼 멈추면 탐색. 스쳐 지나가면 무시
 * - 탐색 중 영역 왼쪽·오른쪽으로 충분히 빠지면 **즉시 취소**, 손을 뗄 때까지 취소 유지
 *
 * 좌표: x는 부모 기준 px, y는 서류철 영역 맨 위 기준 px([StripGeometry]와 같은 좌표). 시각은 ms.
 * @param cancelLeft 영역 왼쪽 끝에서 이만큼 더 왼쪽(화면 가운데 쪽)이면 취소
 * @param cancelRight 영역 오른쪽 끝에서 이만큼 더 오른쪽(화면 가장자리 쪽)이면 취소
 */
class ScrubTracker(
    private val geometry: StripGeometry,
    private val touchSlop: Float,
    private val zoneLeft: Float,
    private val zoneRight: Float,
    private val cancelLeft: Float,
    private val cancelRight: Float,
    private val holdMillis: Long,
    private val hysteresisFraction: Float = DefaultHysteresis,
) {
    private sealed interface Phase {
        data object Idle : Phase
        /** 영역 안에서 누르고 판정 대기 */
        data class PendingInZone(val downX: Float, val downY: Float, val downTime: Long) : Phase
        /** 빈 책상에서 누르고 끄는 중. 영역 안에서 멈추기 시작한 지점·시각 */
        data class Roaming(val dwellX: Float?, val dwellY: Float?, val dwellStart: Long?) : Phase
        data class Exploring(val focus: Int) : Phase
        /** 손을 뗄 때까지 유지되는 취소 */
        data object Cancelled : Phase
        /** 이 터치는 아래 항목 몫 */
        data object Abandoned : Phase
    }

    private var phase: Phase = Phase.Idle

    val isExploring: Boolean get() = phase is Phase.Exploring
    val isAbandoned: Boolean get() = phase is Phase.Abandoned

    /** 부모가 입력을 소비해야 하는 상태 — 탐색 중이거나 취소된 뒤 (아래 버튼이 실행되지 않게) */
    val ownsInput: Boolean get() = phase is Phase.Exploring || phase is Phase.Cancelled

    fun isInZone(x: Float, y: Float): Boolean = x in zoneLeft..zoneRight && y in 0f..geometry.height

    fun down(x: Float, y: Float, time: Long) {
        phase = if (isInZone(x, y)) Phase.PendingInZone(x, y, time) else Phase.Roaming(null, null, null)
    }

    /** 다음 머묾 판정 시각. 머물고 있지 않으면 null */
    fun dwellDeadline(): Long? = when (val current = phase) {
        is Phase.PendingInZone -> current.downTime + holdMillis
        is Phase.Roaming -> current.dwellStart?.plus(holdMillis)
        else -> null
    }

    fun move(x: Float, y: Float, time: Long): List<ScrubEvent> = when (val current = phase) {
        is Phase.PendingInZone -> {
            val dx = abs(x - current.downX)
            val dy = abs(y - current.downY)
            when {
                // 가로가 먼저면 서류철은 개입하지 않는다
                dx > touchSlop && dx >= dy -> {
                    phase = Phase.Abandoned
                    emptyList()
                }
                dy > touchSlop -> claim(y)
                time - current.downTime >= holdMillis -> claim(current.downY)
                else -> emptyList()
            }
        }
        is Phase.Roaming -> roam(current, x, y, time)
        is Phase.Exploring -> explore(current, x, y)
        Phase.Idle, Phase.Cancelled, Phase.Abandoned -> emptyList()
    }

    /** 머묾 시간이 지남 (이벤트 없이 손가락이 가만히 있을 때 부모가 부른다) */
    fun timeout(time: Long): List<ScrubEvent> = when (val current = phase) {
        is Phase.PendingInZone -> if (time - current.downTime >= holdMillis) claim(current.downY) else emptyList()
        is Phase.Roaming -> {
            val start = current.dwellStart
            val dwellY = current.dwellY
            if (start != null && dwellY != null && time - start >= holdMillis) claim(dwellY) else emptyList()
        }
        else -> emptyList()
    }

    fun up(): List<ScrubEvent> {
        val current = phase
        phase = Phase.Idle
        // 탐색 전이면 아래 항목의 탭, 취소된 뒤면 아무것도 — 서류철은 탐색 중일 때만 연다
        return if (current is Phase.Exploring) listOf(ScrubEvent.Open(current.focus)) else emptyList()
    }

    fun cancel(): List<ScrubEvent> {
        val current = phase
        phase = Phase.Idle
        return if (current is Phase.Exploring) listOf(ScrubEvent.Cancelled) else emptyList()
    }

    private fun claim(y: Float): List<ScrubEvent> {
        val focus = geometry.nextFocus(null, y, hysteresisFraction)
        phase = Phase.Exploring(focus)
        return listOf(ScrubEvent.Claimed(focus))
    }

    private fun roam(current: Phase.Roaming, x: Float, y: Float, time: Long): List<ScrubEvent> {
        if (!isInZone(x, y)) {
            phase = Phase.Roaming(null, null, null)
            return emptyList()
        }
        val start = current.dwellStart
        val anchorX = current.dwellX
        val anchorY = current.dwellY
        // 영역에 막 들어왔거나 슬롭 넘게 움직였으면 그 자리에서 다시 멈춤을 잰다 — 스쳐 지나가기는 전환하지 않음
        if (start == null || anchorX == null || anchorY == null || hypot(x - anchorX, y - anchorY) > touchSlop) {
            phase = Phase.Roaming(x, y, time)
            return emptyList()
        }
        return if (time - start >= holdMillis) claim(anchorY) else emptyList()
    }

    private fun explore(current: Phase.Exploring, x: Float, y: Float): List<ScrubEvent> {
        if (x < zoneLeft - cancelLeft || x > zoneRight + cancelRight) {
            phase = Phase.Cancelled
            return listOf(ScrubEvent.Cancelled)
        }
        val focus = geometry.nextFocus(current.focus, y, hysteresisFraction)
        if (focus == current.focus) return emptyList()
        phase = Phase.Exploring(focus)
        return listOf(ScrubEvent.FocusChanged(focus))
    }

    companion object {
        /** 경계 이동 여유 = 칸의 15% [실험] (PRD §4.2) */
        const val DefaultHysteresis = 0.15f

        /** 머묾 시간 = 시스템 길게 누르기 × 0.75 (기본 400→300ms). 접근성 지연을 늘린 사용자는 함께 길어진다 */
        fun holdMillis(longPressTimeoutMillis: Long): Long = longPressTimeoutMillis * 3 / 4
    }
}
