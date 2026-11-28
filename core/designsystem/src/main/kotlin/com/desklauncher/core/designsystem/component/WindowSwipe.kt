package com.desklauncher.core.designsystem.component

/**
 * 창 밀어 닫기 판정 (순수, PRD §9). 오른쪽(+x)만 닫기 방향.
 * 기준은 beUI Bottom Sheet 해제 규칙(120px 또는 600px/s)을 dp로 옮긴 것 [실험].
 */
object WindowSwipe {
    /**
     * @param offsetPx 지금까지 끈 거리
     * @param velocityPxPerSec 놓을 때 가로 속도
     */
    fun shouldDismiss(
        offsetPx: Float,
        velocityPxPerSec: Float,
        distanceThresholdPx: Float,
        velocityThresholdPxPerSec: Float,
    ): Boolean = when {
        velocityPxPerSec >= velocityThresholdPxPerSec -> true
        // 왼쪽으로 되돌리며 놓으면 많이 끌었어도 닫지 않는다 — 마음을 바꾼 것으로 본다
        velocityPxPerSec <= -velocityThresholdPxPerSec -> false
        else -> offsetPx >= distanceThresholdPx
    }
}
