package com.desklauncher.core.designsystem.scrub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class IndexScrubMathTest {

    // 탭 중앙(y = extent/2)이 정수 포커스
    @Test
    fun rawFocus_tabCenterIsInteger() {
        assertEquals(0f, IndexScrubMath.rawFocus(y = 24f, extent = 48f), 0.0001f)
        assertEquals(2f, IndexScrubMath.rawFocus(y = 120f, extent = 48f), 0.0001f)
    }

    // 범위 안은 그대로, 밖은 최대 overscroll까지만
    @Test
    fun rubberBand_limitsOverscroll() {
        assertEquals(1.5f, IndexScrubMath.rubberBand(1.5f, count = 5), 0.0001f)
        val farBelow = IndexScrubMath.rubberBand(100f, count = 5, maxOverscroll = 0.6f)
        assertTrue(farBelow > 4f && farBelow < 4.6f)
        val farAbove = IndexScrubMath.rubberBand(-100f, count = 5, maxOverscroll = 0.6f)
        assertTrue(farAbove < 0f && farAbove > -0.6f)
    }

    // 고무줄 시작 기울기는 1 (경계에서 끊김 없음)
    @Test
    fun rubberBand_isContinuousAtEdge() {
        val justBeyond = IndexScrubMath.rubberBand(4.001f, count = 5, maxOverscroll = 0.6f)
        assertEquals(4.001f, justBeyond, 0.0001f)
    }

    @Test
    fun nearestIndex_clampsToRange() {
        assertEquals(0, IndexScrubMath.nearestIndex(-0.4f, 5))
        assertEquals(4, IndexScrubMath.nearestIndex(4.5f, 5))
        assertEquals(2, IndexScrubMath.nearestIndex(2.49f, 5))
        assertEquals(0, IndexScrubMath.nearestIndex(3f, 0))
    }

    // 포커스에서 1, 좌우 대칭으로 감소
    @Test
    fun popFraction_peaksAtFocusAndIsSymmetric() {
        assertEquals(1f, IndexScrubMath.popFraction(2, 2f), 0.0001f)
        assertEquals(IndexScrubMath.popFraction(1, 2f), IndexScrubMath.popFraction(3, 2f), 0.0001f)
        assertTrue(IndexScrubMath.popFraction(4, 2f) < IndexScrubMath.popFraction(3, 2f))
    }

    // 포커스 위 탭은 위로, 아래 탭은 아래로 벌어진다
    @Test
    fun spreadOffset_pushesAwayFromFocus() {
        assertEquals(0f, IndexScrubMath.spreadOffset(2, 2f), 0.0001f)
        assertTrue(IndexScrubMath.spreadOffset(1, 2f) < 0f)
        assertTrue(IndexScrubMath.spreadOffset(3, 2f) > 0f)
    }

    // 플링 목표는 투영 후 범위로 제한
    @Test
    fun flingTarget_projectsAndClamps() {
        assertEquals(3, IndexScrubMath.flingTarget(focus = 1f, velocity = 10f, count = 9, projectionSeconds = 0.2f))
        assertEquals(8, IndexScrubMath.flingTarget(focus = 7f, velocity = 50f, count = 9, projectionSeconds = 0.2f))
        assertEquals(0, IndexScrubMath.flingTarget(focus = 1f, velocity = -50f, count = 9, projectionSeconds = 0.2f))
    }
}
