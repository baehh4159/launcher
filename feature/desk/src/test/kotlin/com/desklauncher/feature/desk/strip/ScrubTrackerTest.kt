package com.desklauncher.feature.desk.strip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrubTrackerTest {

    // 6칸, 칸 56, 구분 16. 훑기 영역 왼쪽 끝 x=300, 취소 거리 64, 슬롭 8
    private val geometry = StripGeometry(extent = 56f, gap = 16f, count = 6, gapBefore = 4)
    private fun tracker() = ScrubTracker(geometry, touchSlop = 8f, zoneLeft = 300f, cancelDistance = 64f)

    // 제자리에서 짧게 떼면 아무 사건도 없다 → 아래 항목(바로 적기)이 탭을 처리
    @Test
    fun shortTap_emitsNothing() {
        val t = tracker()
        t.down(330f, 320f)
        assertEquals(emptyList<ScrubEvent>(), t.move(332f, 322f))
        assertEquals(emptyList<ScrubEvent>(), t.up())
    }

    // 세로로 슬롭을 넘는 순간 그 자리 칸으로 탐색 시작
    @Test
    fun verticalMove_claimsAtCurrentSlot() {
        val t = tracker()
        t.down(330f, 100f)
        assertEquals(listOf(ScrubEvent.Claimed(1)), t.move(330f, 90f))
        assertTrue(t.isExploring)
    }

    // 머묾 시간이 지나면 누른 자리 칸으로 탐색 시작, 그대로 떼면 그 칸을 연다
    @Test
    fun hold_claimsDownSlot_andReleaseOpens() {
        val t = tracker()
        t.down(330f, 30f)
        assertEquals(listOf(ScrubEvent.Claimed(0)), t.holdElapsed())
        assertEquals(listOf(ScrubEvent.Open(0)), t.up())
    }

    // 가로 이동이 먼저 확정되면 서류철은 개입하지 않는다
    @Test
    fun horizontalFirst_abandons() {
        val t = tracker()
        t.down(330f, 100f)
        assertEquals(emptyList<ScrubEvent>(), t.move(345f, 104f))
        assertTrue(t.isAbandoned)
        assertEquals(emptyList<ScrubEvent>(), t.holdElapsed())
        assertEquals(emptyList<ScrubEvent>(), t.up())
    }

    // 탐색 중 칸이 바뀔 때만 사건, 구분 여백 위에서는 바뀌지 않음
    @Test
    fun exploring_emitsFocusChanges_skippingGap() {
        val t = tracker()
        t.down(330f, 180f)
        assertEquals(listOf(ScrubEvent.Claimed(3)), t.move(330f, 200f))
        assertEquals(emptyList<ScrubEvent>(), t.move(330f, 232f)) // 구분 여백
        assertEquals(listOf(ScrubEvent.FocusChanged(4)), t.move(330f, 260f))
    }

    // 본문 쪽으로 충분히 빼면 취소 대기, 떼면 열지 않는다
    @Test
    fun pullInward_arms_andReleaseCancels() {
        val t = tracker()
        t.down(330f, 100f)
        t.move(330f, 120f)
        assertEquals(listOf(ScrubEvent.CancelArmed(true)), t.move(230f, 120f))
        assertEquals(listOf(ScrubEvent.Cancelled), t.up())
    }

    // 취소 대기 후 다시 영역 쪽으로 돌아오면 해제되고 떼면 열린다
    @Test
    fun pullBack_disarms() {
        val t = tracker()
        t.down(330f, 100f)
        t.move(330f, 120f)
        t.move(230f, 120f)
        assertEquals(listOf(ScrubEvent.CancelArmed(false)), t.move(320f, 120f))
        assertEquals(listOf(ScrubEvent.Open(2)), t.up())
    }

    // 시스템이 입력을 취소하면 탐색 중이어도 아무것도 열지 않는다
    @Test
    fun systemCancel_whileExploring_cancels() {
        val t = tracker()
        t.down(330f, 100f)
        t.move(330f, 130f)
        assertEquals(listOf(ScrubEvent.Cancelled), t.cancel())
        assertFalse(t.isExploring)
    }

    // 머묾 시간 = 길게 누르기 × 0.75
    @Test
    fun holdMillis_isThreeQuartersOfLongPress() {
        assertEquals(300L, ScrubTracker.holdMillis(400L))
        assertEquals(750L, ScrubTracker.holdMillis(1000L))
    }
}
