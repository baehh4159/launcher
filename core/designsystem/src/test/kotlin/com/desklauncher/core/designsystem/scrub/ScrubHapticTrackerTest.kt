package com.desklauncher.core.designsystem.scrub

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScrubHapticTrackerTest {

    private val tracker = ScrubHapticTracker(hysteresis = 0.15f, edgeThreshold = 0.25f).apply { reset(0f, 5) }

    // 경계를 히스테리시스 이상 넘을 때 한 번만 틱
    @Test
    fun update_ticksOncePerBoundary() {
        assertNull(tracker.update(0.6f, 5))
        assertEquals(ScrubHapticEvent.Tick(1), tracker.update(0.7f, 5))
        assertNull(tracker.update(0.8f, 5))
        assertNull(tracker.update(1.2f, 5))
    }

    // 경계 근처에서 떨려도 중복 진동 없음
    @Test
    fun update_ignoresJitterAroundBoundary() {
        tracker.update(0.7f, 5)
        assertNull(tracker.update(0.45f, 5))
        assertNull(tracker.update(0.55f, 5))
        assertNull(tracker.update(0.4f, 5))
        assertEquals(ScrubHapticEvent.Tick(0), tracker.update(0.3f, 5))
    }

    // 끝을 넘어 당기면 벽 이벤트 1회, 안으로 돌아오면 재무장
    @Test
    fun update_emitsEdgeOnceAndRearms() {
        assertEquals(ScrubHapticEvent.Edge, tracker.update(-0.3f, 5))
        assertNull(tracker.update(-0.4f, 5))
        assertNull(tracker.update(0f, 5))
        assertEquals(ScrubHapticEvent.Edge, tracker.update(-0.3f, 5))
    }

    // 한 프레임에 여러 탭을 건너뛰면 도착 탭으로 한 번
    @Test
    fun update_reportsLandingIndexWhenSkipping() {
        assertEquals(ScrubHapticEvent.Tick(3), tracker.update(3f, 5))
    }

    @Test
    fun update_returnsNullWithoutItems() {
        assertNull(tracker.update(2f, 0))
    }
}
