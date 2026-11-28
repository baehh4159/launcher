package com.desklauncher.feature.apps.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PinnedAppsTest {

    // 끝에 붙고, 이미 있으면 그대로
    @Test
    fun pin_appendsOnce() {
        assertEquals(PinnedApps.Result.Updated(listOf("a", "b")), PinnedApps.pin(listOf("a"), "b"))
        assertEquals(PinnedApps.Result.Unchanged, PinnedApps.pin(listOf("a"), "a"))
    }

    // 5개가 차면 거절
    @Test
    fun pin_rejected_whenFull() {
        assertEquals(PinnedApps.Result.Full, PinnedApps.pin(listOf("1", "2", "3", "4", "5"), "6"))
    }

    // 앞으로·뒤로 한 칸, 끝에서는 변화 없음
    @Test
    fun move_shiftsWithinBounds() {
        val keys = listOf("a", "b", "c")
        assertEquals(PinnedApps.Result.Updated(listOf("b", "a", "c")), PinnedApps.move(keys, "b", -1))
        assertEquals(PinnedApps.Result.Updated(listOf("a", "c", "b")), PinnedApps.move(keys, "b", 1))
        assertEquals(PinnedApps.Result.Unchanged, PinnedApps.move(keys, "a", -1))
        assertEquals(PinnedApps.Result.Unchanged, PinnedApps.move(keys, "c", 1))
    }

    // 해제
    @Test
    fun unpin_removes() {
        assertEquals(PinnedApps.Result.Updated(listOf("a")), PinnedApps.unpin(listOf("a", "b"), "b"))
        assertEquals(PinnedApps.Result.Unchanged, PinnedApps.unpin(listOf("a"), "z"))
    }

    // 삭제된 앱은 빠지고 순서는 저장된 대로
    @Test
    fun resolve_keepsOrder_dropsMissing() {
        assertEquals(listOf(3, 1), PinnedApps.resolve(listOf("c", "x", "a"), mapOf("a" to 1, "b" to 2, "c" to 3)))
    }
}
