package com.desklauncher.feature.doodle.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StrokeScalingTest {

    // 작은 배율(가로·세로 중)로 좌표와 굵기를 함께 줄인다
    @Test
    fun fit_scalesByLimitingAxis() {
        val strokes = listOf(Stroke(0, 10f, points = listOf(100f, 200f)))
        val fitted = StrokeScaling.fit(strokes, fromWidth = 1000, fromHeight = 2000, toWidth = 500, toHeight = 2000)
        assertEquals(listOf(50f, 100f), fitted.single().points)
        assertEquals(5f, fitted.single().widthPx)
    }

    @Test
    fun fit_keepsStrokesForSameOrInvalidSize() {
        val strokes = listOf(Stroke(0, 10f, points = listOf(1f, 2f)))
        assertEquals(strokes, StrokeScaling.fit(strokes, 100, 100, 100, 100))
        assertEquals(strokes, StrokeScaling.fit(strokes, 0, 100, 100, 100))
    }
}
