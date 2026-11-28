package com.desklauncher.feature.desk.strip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScrubTrackerTest {

    // 6칸, 칸 56, 구분 16(높이 352). 훑기 영역 x 300..364, 왼쪽 취소 56, 오른쪽 취소 12, 슬롭 8, 머묾 300ms
    private val geometry = StripGeometry(extent = 56f, gap = 16f, count = 6, gapBefore = 4)
    private fun tracker() = ScrubTracker(
        geometry = geometry,
        touchSlop = 8f,
        zoneLeft = 300f,
        zoneRight = 364f,
        cancelLeft = 56f,
        cancelRight = 12f,
        holdMillis = 300L,
    )

    // 영역 안에서 제자리 짧게 떼면 아무 사건도 없다 → 아래 항목(바로 적기)이 탭을 처리
    @Test
    fun shortTap_inZone_emitsNothing() {
        val t = tracker()
        t.down(330f, 320f, time = 0L)
        assertEquals(emptyList<ScrubEvent>(), t.move(332f, 322f, time = 80L))
        assertEquals(emptyList<ScrubEvent>(), t.up())
        assertFalse(t.ownsInput)
    }

    // 영역 안 세로 이동이 슬롭을 넘으면 그 자리 칸으로 탐색
    @Test
    fun verticalMove_inZone_claims() {
        val t = tracker()
        t.down(330f, 100f, time = 0L)
        assertEquals(listOf(ScrubEvent.Claimed(1)), t.move(330f, 90f, time = 40L))
        assertTrue(t.ownsInput)
    }

    // 영역 안에서 머묾 시간이 지나면 누른 칸으로 탐색, 떼면 그 칸 하나만 연다
    @Test
    fun hold_inZone_claims_andReleaseOpens() {
        val t = tracker()
        t.down(330f, 330f, time = 0L)
        assertEquals(300L, t.dwellDeadline())
        assertEquals(listOf(ScrubEvent.Claimed(5)), t.timeout(300L))
        assertEquals(listOf(ScrubEvent.Open(5)), t.up())
    }

    // 영역 안에서 가로가 먼저면 포기
    @Test
    fun horizontalFirst_inZone_abandons() {
        val t = tracker()
        t.down(330f, 100f, time = 0L)
        t.move(345f, 104f, time = 30L)
        assertTrue(t.isAbandoned)
        assertEquals(emptyList<ScrubEvent>(), t.timeout(400L))
        assertEquals(emptyList<ScrubEvent>(), t.up())
    }

    // 빈 책상에서 끌다가 영역에 들어와 멈추면 손을 떼지 않고 탐색으로 전환, 떼면 그 공책 하나만
    @Test
    fun dragFromDesk_thenDwellInZone_claimsAndOpens() {
        val t = tracker()
        t.down(150f, 60f, time = 0L)
        assertNull(t.dwellDeadline())
        assertEquals(emptyList<ScrubEvent>(), t.move(250f, 70f, time = 60L)) // 아직 영역 밖
        assertEquals(emptyList<ScrubEvent>(), t.move(320f, 75f, time = 120L)) // 영역에 들어옴 → 멈춤 측정 시작
        assertEquals(420L, t.dwellDeadline())
        assertEquals(listOf(ScrubEvent.Claimed(1)), t.timeout(420L))
        assertEquals(listOf(ScrubEvent.Open(1)), t.up())
    }

    // 영역을 스쳐 지나가기만 하면 전환하지 않는다
    @Test
    fun dragFromDesk_passingThrough_doesNotClaim() {
        val t = tracker()
        t.down(150f, 60f, time = 0L)
        t.move(320f, 70f, time = 100L) // 들어옴
        t.move(340f, 90f, time = 250L) // 슬롭 넘게 계속 움직임 → 멈춤 다시 측정
        assertEquals(550L, t.dwellDeadline())
        t.move(380f, 110f, time = 380L) // 영역 밖으로 나감
        assertNull(t.dwellDeadline())
        assertEquals(emptyList<ScrubEvent>(), t.timeout(700L))
        assertEquals(emptyList<ScrubEvent>(), t.up())
    }

    // 탐색 중 오른쪽 가장자리 쪽으로 빠지면 즉시 취소, 다시 들어와도 살아나지 않고 떼도 열리지 않는다
    @Test
    fun exploring_exitRight_cancelsImmediately_andStays() {
        val t = tracker()
        t.down(330f, 100f, time = 0L)
        t.move(330f, 120f, time = 30L)
        assertEquals(listOf(ScrubEvent.Cancelled), t.move(380f, 120f, time = 60L))
        assertTrue(t.ownsInput) // 손 뗄 때까지 입력을 가져가 아래 버튼이 실행되지 않게
        assertEquals(emptyList<ScrubEvent>(), t.move(330f, 120f, time = 90L))
        assertEquals(emptyList<ScrubEvent>(), t.up())
    }

    // 왼쪽(화면 가운데) 쪽으로 빠져도 똑같이 즉시 취소
    @Test
    fun exploring_exitLeft_cancelsImmediately() {
        val t = tracker()
        t.down(330f, 100f, time = 0L)
        t.move(330f, 120f, time = 30L)
        assertEquals(listOf(ScrubEvent.Cancelled), t.move(240f, 120f, time = 60L))
        assertEquals(emptyList<ScrubEvent>(), t.up())
    }

    // 세로로 훑는 중 작은 대각선 흔들림(영역 밖 조금)은 취소가 아니다
    @Test
    fun exploring_smallDiagonalWobble_keepsExploring() {
        val t = tracker()
        t.down(330f, 30f, time = 0L)
        t.move(330f, 50f, time = 20L)
        assertEquals(listOf(ScrubEvent.FocusChanged(1)), t.move(290f, 90f, time = 40L))
        assertEquals(emptyList<ScrubEvent>(), t.move(370f, 100f, time = 60L))
        assertTrue(t.isExploring)
    }

    // 구분 여백 위에서는 칸이 바뀌지 않는다
    @Test
    fun exploring_skipsGap() {
        val t = tracker()
        t.down(330f, 180f, time = 0L)
        assertEquals(listOf(ScrubEvent.Claimed(3)), t.move(330f, 200f, time = 20L))
        assertEquals(emptyList<ScrubEvent>(), t.move(330f, 232f, time = 40L))
        assertEquals(listOf(ScrubEvent.FocusChanged(4)), t.move(330f, 260f, time = 60L))
    }

    // 시스템이 입력을 취소하면 탐색 중이어도 아무것도 열지 않는다
    @Test
    fun systemCancel_whileExploring_cancels() {
        val t = tracker()
        t.down(330f, 100f, time = 0L)
        t.move(330f, 130f, time = 30L)
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
