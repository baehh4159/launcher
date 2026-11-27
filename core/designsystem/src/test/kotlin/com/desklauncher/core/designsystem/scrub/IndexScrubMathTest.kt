package com.desklauncher.core.designsystem.scrub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    // 파도: 포커스에서 1, 좌우 대칭, 반경 밖 0, 반경 절반에서 0.5
    @Test
    fun waveFraction_raisedCosine() {
        assertEquals(1f, IndexScrubMath.waveFraction(2, 2f, radius = 3f), 0.0001f)
        assertEquals(IndexScrubMath.waveFraction(1, 2.2f, 3f), IndexScrubMath.waveFraction(3, 1.8f, 3f), 0.0001f)
        assertEquals(0.5f, IndexScrubMath.waveFraction(5, 3.5f, radius = 3f), 0.0001f)
        assertEquals(0f, IndexScrubMath.waveFraction(6, 2f, radius = 3f), 0.0001f)
        assertTrue(IndexScrubMath.waveFraction(3, 2f, 3f) > IndexScrubMath.waveFraction(4, 2f, 3f))
    }

    // 툭툭: 선택 탭 1, 옆 칸은 단계별로 줄고, 범위 밖은 0
    @Test
    fun popLevel_stepsByDistance() {
        val levels = listOf(1f, 0.3f, 0.08f)
        assertEquals(1f, IndexScrubMath.popLevel(4, 4, levels), 0f)
        assertEquals(0.3f, IndexScrubMath.popLevel(3, 4, levels), 0f)
        assertEquals(0.08f, IndexScrubMath.popLevel(6, 4, levels), 0f)
        assertEquals(0f, IndexScrubMath.popLevel(8, 4, levels), 0f)
    }

    // 플링 목표는 투영 후 범위로 제한
    @Test
    fun flingTarget_projectsAndClamps() {
        assertEquals(3, IndexScrubMath.flingTarget(focus = 1f, velocity = 10f, count = 9, projectionSeconds = 0.2f))
        assertEquals(8, IndexScrubMath.flingTarget(focus = 7f, velocity = 50f, count = 9, projectionSeconds = 0.2f))
        assertEquals(0, IndexScrubMath.flingTarget(focus = 1f, velocity = -50f, count = 9, projectionSeconds = 0.2f))
    }

    // 왼쪽 스트립은 오른쪽(화면 안쪽)으로 벗어나야 취소, 오른쪽 스트립은 왼쪽으로
    @Test
    fun isCancelArmed_followsInwardDirection_perSide() {
        assertTrue(IndexScrubMath.isCancelArmed(x = 300f, stripWidth = 100f, cancelDistance = 150f, side = StripSide.Left))
        assertFalse(IndexScrubMath.isCancelArmed(x = 200f, stripWidth = 100f, cancelDistance = 150f, side = StripSide.Left))
        assertFalse(IndexScrubMath.isCancelArmed(x = -300f, stripWidth = 100f, cancelDistance = 150f, side = StripSide.Left))
        assertTrue(IndexScrubMath.isCancelArmed(x = -200f, stripWidth = 100f, cancelDistance = 150f, side = StripSide.Right))
        assertFalse(IndexScrubMath.isCancelArmed(x = 400f, stripWidth = 100f, cancelDistance = 150f, side = StripSide.Right))
    }
}
