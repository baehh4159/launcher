package com.desklauncher.feature.doodle.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DoodleStateTest {

    private fun stroke(vararg xy: Float) = Stroke(argb = 0, widthPx = 4f, points = xy.toList())

    // 빈 획은 추가하지 않는다
    @Test
    fun add_ignoresEmptyStroke() {
        assertTrue(DoodleState().add(Stroke(0, 4f)).isEmpty)
    }

    @Test
    fun add_keepsOrder() {
        val state = DoodleState().add(stroke(1f, 1f)).add(stroke(2f, 2f, 3f, 3f))
        assertEquals(listOf(1, 2), state.strokes.map { it.pointCount })
    }
}
