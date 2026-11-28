package com.desklauncher.feature.desk.strip

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp

/**
 * 훑기 관찰자 (STRIP-02, PRD §4.2 구현 규칙). 책상 **부모**에 붙인다.
 * Initial 단계에서 입력을 먼저 엿보기만 하다가, 탐색이 확정된 순간부터 소비한다.
 * 그 전에 떼면 아무것도 소비하지 않으므로 아래 항목(바로 적기 등)이 원래 탭을 처리하고,
 * 가져간 뒤(탐색·취소)에는 자식이 소비된 입력을 보고 대기 중인 클릭을 스스로 취소한다 — 한 터치에 한 결과.
 * 빈 책상에서 시작한 드래그도 지켜보다가 영역에 들어와 멈추면 탐색으로 전환한다.
 *
 * @param zoneRightInset 부모 오른쪽 끝에서 훑기 영역 오른쪽 끝까지(px) — 시스템 제스처 인셋 + 여유
 * @param zoneWidth 훑기 영역 폭(px)
 * @param stripBottomInset 부모 아래 끝에서 서류철 영역 아래 끝까지(px)
 * @param cancelLeft 영역 왼쪽 끝에서 이만큼 더 화면 가운데 쪽으로 빠지면 즉시 취소(px)
 */
internal fun Modifier.stripScrub(
    enabled: Boolean,
    geometry: StripGeometry,
    zoneRightInset: Float,
    zoneWidth: Float,
    stripBottomInset: Float,
    holdMillis: Long,
    cancelLeft: Float,
    onEvent: (ScrubEvent) -> Unit,
): Modifier = if (!enabled) this else pointerInput(geometry, zoneRightInset, zoneWidth, stripBottomInset, holdMillis, cancelLeft) {
    // 오른쪽 취소 거리: 영역 끝과 화면 끝 사이의 절반 — 화면 끝까지 밀기 전에 취소된다 (최소 4dp)
    val cancelRight = maxOf(zoneRightInset / 2f, MinRightCancel.toPx())
    awaitEachGesture {
        val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
        val zoneRight = size.width - zoneRightInset
        val stripTop = size.height - stripBottomInset - geometry.height
        val tracker = ScrubTracker(
            geometry = geometry,
            touchSlop = viewConfiguration.touchSlop,
            zoneLeft = zoneRight - zoneWidth,
            zoneRight = zoneRight,
            cancelLeft = cancelLeft,
            cancelRight = cancelRight,
            holdMillis = holdMillis,
        )
        tracker.down(down.position.x, down.position.y - stripTop, down.uptimeMillis)
        var lastUptime = down.uptimeMillis

        while (true) {
            val deadline = if (tracker.ownsInput) null else tracker.dwellDeadline()
            val event = if (deadline != null) {
                withTimeoutOrNull((deadline - lastUptime).coerceAtLeast(0L)) { awaitPointerEvent(PointerEventPass.Initial) }
            } else {
                awaitPointerEvent(PointerEventPass.Initial)
            }
            if (event == null) {
                // 가만히 머묾 → 탐색 시작
                lastUptime = deadline ?: lastUptime
                tracker.timeout(lastUptime).forEach(onEvent)
                continue
            }
            val change = event.changes.firstOrNull { it.id == down.id }
            if (change == null) {
                tracker.cancel().forEach(onEvent)
                return@awaitEachGesture
            }
            lastUptime = change.uptimeMillis
            // 탐색 중이거나 취소된 뒤에는 이 터치를 끝까지 가져간다 (손 뗀 자리의 버튼도 실행되지 않음)
            if (tracker.ownsInput) change.consume()
            if (!change.pressed) {
                tracker.up().forEach(onEvent)
                return@awaitEachGesture
            }
            val events = tracker.move(change.position.x, change.position.y - stripTop, change.uptimeMillis)
            if (tracker.isAbandoned) return@awaitEachGesture // 가로 이동 → 아래 항목 몫
            if (tracker.ownsInput) change.consume()
            events.forEach(onEvent)
        }
    }
}

private val MinRightCancel = 4.dp
