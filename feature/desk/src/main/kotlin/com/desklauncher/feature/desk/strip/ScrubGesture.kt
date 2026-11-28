package com.desklauncher.feature.desk.strip

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput

/**
 * 훑기 관찰자 (STRIP-02, PRD §4.2 구현 규칙). 책상 **부모**에 붙인다.
 * Initial 단계에서 입력을 먼저 엿보기만 하다가, 세로 슬롭을 넘거나 머묾 시간이 지난 순간부터 소비한다.
 * 그 전에 떼면 아무것도 소비하지 않으므로 아래 항목(바로 적기 등)이 원래 탭을 처리하고,
 * 가져간 뒤에는 자식이 소비된 입력을 보고 대기 중인 클릭을 스스로 취소한다 — 한 터치에 한 결과.
 *
 * @param zoneRightInset 부모 오른쪽 끝에서 훑기 영역 오른쪽 끝까지(px) — 시스템 제스처 인셋 + 여유
 * @param zoneWidth 훑기 영역 폭(px)
 * @param stripBottomInset 부모 아래 끝에서 서류철 영역 아래 끝까지(px)
 */
internal fun Modifier.stripScrub(
    enabled: Boolean,
    geometry: StripGeometry,
    zoneRightInset: Float,
    zoneWidth: Float,
    stripBottomInset: Float,
    holdMillis: Long,
    cancelDistance: Float,
    onEvent: (ScrubEvent) -> Unit,
): Modifier = if (!enabled) this else pointerInput(geometry, zoneRightInset, zoneWidth, stripBottomInset, holdMillis, cancelDistance) {
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val zoneRight = size.width - zoneRightInset
        val zoneLeft = zoneRight - zoneWidth
        val stripTop = size.height - stripBottomInset - geometry.height
        val inZone = down.position.x in zoneLeft..zoneRight &&
            down.position.y in stripTop..(stripTop + geometry.height)
        if (!inZone) return@awaitEachGesture

        val tracker = ScrubTracker(geometry, viewConfiguration.touchSlop, zoneLeft, cancelDistance)
        tracker.down(down.position.x, down.position.y - stripTop)
        val deadline = down.uptimeMillis + holdMillis
        var lastUptime = down.uptimeMillis

        // 1단계: 판정 대기 — 아무것도 소비하지 않는다
        while (!tracker.isExploring) {
            val event = withTimeoutOrNull((deadline - lastUptime).coerceAtLeast(0L)) {
                awaitPointerEvent(PointerEventPass.Initial)
            }
            if (event == null) {
                tracker.holdElapsed().forEach(onEvent)
                break
            }
            val change = event.changes.firstOrNull { it.id == down.id } ?: return@awaitEachGesture
            lastUptime = change.uptimeMillis
            if (!change.pressed) return@awaitEachGesture // 짧은 탭 → 아래 항목 몫
            val events = tracker.move(change.position.x, change.position.y - stripTop)
            if (tracker.isAbandoned) return@awaitEachGesture // 가로 이동 → 아래 항목 몫
            if (tracker.isExploring) change.consume()
            events.forEach(onEvent)
        }

        // 2단계: 탐색 — 이 터치가 끝날 때까지 모두 가져간다 (관성 없음)
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change == null) {
                tracker.cancel().forEach(onEvent)
                return@awaitEachGesture
            }
            change.consume()
            if (!change.pressed) {
                tracker.up().forEach(onEvent)
                return@awaitEachGesture
            }
            tracker.move(change.position.x, change.position.y - stripTop).forEach(onEvent)
        }
    }
}
