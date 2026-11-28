package com.desklauncher.core.designsystem.component

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WindowSwipeTest {

    private fun dismiss(offset: Float, velocity: Float) =
        WindowSwipe.shouldDismiss(offset, velocity, distanceThresholdPx = 120f, velocityThresholdPxPerSec = 600f)

    // 거리 기준을 넘겨 놓으면 닫힌다
    @Test
    fun dismiss_whenDraggedPastDistance() {
        assertTrue(dismiss(130f, 0f))
        assertFalse(dismiss(100f, 0f))
    }

    // 조금만 끌어도 빠르게 튕기면 닫힌다
    @Test
    fun dismiss_whenFlungRight() {
        assertTrue(dismiss(20f, 900f))
    }

    // 많이 끌었어도 왼쪽으로 되돌리며 놓으면 닫지 않는다
    @Test
    fun keep_whenFlungBackLeft() {
        assertFalse(dismiss(300f, -900f))
    }
}
