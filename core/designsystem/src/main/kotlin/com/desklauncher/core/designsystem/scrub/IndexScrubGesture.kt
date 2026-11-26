package com.desklauncher.core.designsystem.scrub

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

/**
 * 세로 스크럽 제스처. 이 Modifier를 붙인 요소의 상단이 첫 탭의 위쪽 경계여야 한다.
 *
 * - 누르는 즉시 그 위치의 탭에 포커스 (Niagara 방식, 지연 없음)
 * - 손가락이 요소 왼쪽으로 [cancelDistancePx] 이상 벗어나면 취소 대기
 * - 놓을 때 세로 속도로 열기/플링 판정 → [IndexScrubState.release]
 */
fun Modifier.indexScrubGesture(
    state: IndexScrubState,
    itemExtentPx: Float,
    cancelDistancePx: Float,
    onOpen: (index: Int) -> Unit,
): Modifier = pointerInput(state, itemExtentPx, cancelDistancePx) {
    coroutineScope {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            launch { state.stopAnimation() }
            state.beginDrag(focusAt(down.position.y, itemExtentPx, state.itemCount))
            val velocityTracker = VelocityTracker()
            velocityTracker.addPointerInputChange(down)
            down.consume()

            while (true) {
                val event = awaitPointerEvent()
                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                velocityTracker.addPointerInputChange(change)
                if (!change.pressed) break
                state.drag(
                    focus = focusAt(change.position.y, itemExtentPx, state.itemCount),
                    cancelArmed = change.position.x < -cancelDistancePx,
                )
                change.consume()
            }

            val velocity = velocityTracker.calculateVelocity().y / itemExtentPx
            launch { state.release(velocity, onOpen) }
        }
    }
}

private fun focusAt(y: Float, extent: Float, count: Int): Float =
    IndexScrubMath.rubberBand(IndexScrubMath.rawFocus(y, extent), count)
