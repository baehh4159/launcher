package com.desklauncher.feature.desk.ui

import com.desklauncher.feature.desk.strip.ScrubEvent
import com.desklauncher.feature.desk.strip.StripVisualState
import org.junit.Assert.assertEquals
import org.junit.Test

class StripVisualStateReduceTest {

    // 탐색 시작 → 칸 이동 → 취소되면 즉시 Idle(탭이 접힌다)
    @Test
    fun reduce_followsScrubLifecycle() {
        val claimed = StripVisualState.Idle.reduce(ScrubEvent.Claimed(2))
        assertEquals(StripVisualState(focused = 2, exploring = true), claimed)
        val moved = claimed.reduce(ScrubEvent.FocusChanged(3))
        assertEquals(3, moved.focused)
        assertEquals(StripVisualState.Idle, moved.reduce(ScrubEvent.Cancelled))
    }

    // 열기 사건 뒤에는 돌출이 접힌다
    @Test
    fun reduce_open_returnsIdle() {
        val exploring = StripVisualState(focused = 1, exploring = true)
        assertEquals(StripVisualState.Idle, exploring.reduce(ScrubEvent.Open(1)))
    }
}
