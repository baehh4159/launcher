package com.desklauncher.core.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PopupSwipeTest {

    // 진행률은 폭 기준 0..1로 잘린다
    @Test
    fun progress_isClampedFraction() {
        assertEquals(0.5f, PopupSwipe.progress(200f, 400f), 0f)
        assertEquals(0f, PopupSwipe.progress(-50f, 400f), 0f)
        assertEquals(1f, PopupSwipe.progress(900f, 400f), 0f)
        assertEquals(0f, PopupSwipe.progress(100f, 0f), 0f)
    }

    // 30% 이상 끌고 놓으면 닫힌다
    @Test
    fun shouldDismiss_true_whenDraggedPastFraction() {
        assertTrue(dismiss(offset = 130f, velocity = 0f))
        assertFalse(dismiss(offset = 100f, velocity = 0f))
    }

    // 조금만 끌어도 오른쪽으로 빠르게 튕기면 닫힌다
    @Test
    fun shouldDismiss_true_whenFlungRight() {
        assertTrue(dismiss(offset = 20f, velocity = 2000f))
    }

    // 많이 끌었어도 왼쪽으로 되돌리며 놓으면 닫지 않는다
    @Test
    fun shouldDismiss_false_whenFlungBackLeft() {
        assertFalse(dismiss(offset = 300f, velocity = -2000f))
    }

    private fun dismiss(offset: Float, velocity: Float) = PopupSwipe.shouldDismiss(
        offsetPx = offset,
        widthPx = 400f,
        velocityPxPerSec = velocity,
        dismissFraction = 0.3f,
        dismissVelocityPxPerSec = 1000f,
    )
}
