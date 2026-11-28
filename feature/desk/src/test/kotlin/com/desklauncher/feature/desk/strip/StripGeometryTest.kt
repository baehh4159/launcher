package com.desklauncher.feature.desk.strip

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StripGeometryTest {

    // 6칸, 칸 56, 구분 16(문구 위) — 기본 배치
    private val geometry = StripGeometry(extent = 56f, gap = 16f, count = 6, gapBefore = 4)

    // 전체 높이 = 칸 6개 + 구분 1개
    @Test
    fun height_includesSingleGap() {
        assertEquals(56f * 6 + 16f, geometry.height, 0f)
    }

    // 구분 아래 칸은 구분 높이만큼 내려간다
    @Test
    fun top_shiftsSlotsBelowGap() {
        assertEquals(168f, geometry.top(3), 0f)
        assertEquals(224f + 16f, geometry.top(4), 0f)
        assertEquals(280f + 16f, geometry.top(5), 0f)
    }

    // 구분 여백 위는 어떤 칸도 아니다
    @Test
    fun slotAt_returnsNull_inGap() {
        assertNull(geometry.slotAt(230f))
        assertEquals(3, geometry.slotAt(223f))
        assertEquals(4, geometry.slotAt(241f))
    }

    // 영역 밖은 끝 칸에 머문다
    @Test
    fun slotAt_clampsOutsideToEnds() {
        assertEquals(0, geometry.slotAt(-40f))
        assertEquals(5, geometry.slotAt(10_000f))
    }

    // 경계를 이동 여유(15%) 안에서 넘나들면 포커스가 바뀌지 않는다
    @Test
    fun nextFocus_keepsCurrent_withinHysteresis() {
        // 칸 1의 아래 경계는 112. 여유 8.4px
        assertEquals(1, geometry.nextFocus(current = 1, y = 118f, hysteresisFraction = 0.15f))
        assertEquals(2, geometry.nextFocus(current = 1, y = 122f, hysteresisFraction = 0.15f))
    }

    // 구분 여백을 지나는 동안에는 이전 칸을 유지한다 (진동 없는 여백)
    @Test
    fun nextFocus_keepsCurrent_inGap() {
        assertEquals(3, geometry.nextFocus(current = 3, y = 236f, hysteresisFraction = 0f))
    }

    // 처음 포커스는 구분 위라도 가장 가까운 칸
    @Test
    fun nextFocus_picksNearest_whenNoCurrent() {
        assertEquals(3, geometry.nextFocus(current = null, y = 226f, hysteresisFraction = 0.15f))
        assertEquals(4, geometry.nextFocus(current = null, y = 238f, hysteresisFraction = 0.15f))
    }

    // 포커스 칸 하나만 튀어나온다 (평상시 책등 폭 통일)
    @Test
    fun popLevel_onlyFocusedSlot() {
        assertEquals(1f, geometry.popLevel(2, 2), 0f)
        assertEquals(0f, geometry.popLevel(3, 2), 0f)
        assertEquals(0f, geometry.popLevel(1, 2), 0f)
        assertEquals(0f, geometry.popLevel(2, null), 0f)
    }

    // 맨 아래(앱) 칸 이름표는 그 줄 위에, 다른 칸은 칸 가운데
    @Test
    fun labelCenter_liftsBottomRowAboveItself() {
        assertEquals(geometry.center(2), geometry.labelCenter(2, labelHeight = 36f, lift = 4f), 0f)
        assertEquals(geometry.top(5) - 4f - 18f, geometry.labelCenter(5, labelHeight = 36f, lift = 4f), 0f)
    }

    // 구분이 없으면 칸이 이어진다
    @Test
    fun noGap_whenGapBeforeOutOfRange() {
        val plain = StripGeometry(extent = 50f, gap = 16f, count = 3, gapBefore = 0)
        assertEquals(150f, plain.height, 0f)
        assertEquals(100f, plain.top(2), 0f)
    }
}
