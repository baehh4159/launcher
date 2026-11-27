package com.desklauncher.core.designsystem.component

/** 팝업 창 밀어 닫기 판정 (순수 함수). 단위: px, px/s. 오른쪽(+x)만 닫기 방향이다. */
object PopupSwipe {

    /** 끈 거리 → 0..1 진행률 */
    fun progress(offsetPx: Float, widthPx: Float): Float =
        if (widthPx <= 0f) 0f else (offsetPx / widthPx).coerceIn(0f, 1f)

    /**
     * 손을 뗐을 때 닫을지. 충분히 끌었거나 오른쪽으로 빠르게 튕겼으면 닫는다.
     * 왼쪽으로 되돌리며 놓으면(음의 속도) 많이 끌었어도 닫지 않는다 — 마음을 바꾼 것으로 본다.
     */
    fun shouldDismiss(
        offsetPx: Float,
        widthPx: Float,
        velocityPxPerSec: Float,
        dismissFraction: Float,
        dismissVelocityPxPerSec: Float,
    ): Boolean = when {
        velocityPxPerSec >= dismissVelocityPxPerSec -> true
        velocityPxPerSec <= -dismissVelocityPxPerSec -> false
        else -> progress(offsetPx, widthPx) >= dismissFraction
    }
}
