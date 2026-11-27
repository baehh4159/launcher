package com.desklauncher.core.designsystem.haptics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeskHapticSpecTest {

    // THUD 지원: 넘김은 약하게(0.45~0.6), 확정은 세게(0.85~1.0)
    @Test
    fun plan_usesThudWithLightTickAndHeavyConfirm() {
        val tick = DeskHapticSpec.plan(HapticEvent.Tick, HapticLevel.Default, thudSupported = true, lowTickSupported = true) as HapticPlan.Primitive
        val confirm = DeskHapticSpec.plan(HapticEvent.Confirm, HapticLevel.Default, thudSupported = true, lowTickSupported = true) as HapticPlan.Primitive
        assertEquals(PrimitiveKind.Thud, tick.kind)
        assertTrue(tick.scale in 0.45f..0.6f)
        assertTrue(confirm.scale in 0.85f..1f)
        val fast = DeskHapticSpec.plan(HapticEvent.FastTick, HapticLevel.Default, true, true) as HapticPlan.Primitive
        assertTrue(fast.scale < tick.scale)
    }

    // THUD 미지원 → LOW_TICK, 둘 다 없으면 짧은 단발
    @Test
    fun plan_fallsBack() {
        assertEquals(PrimitiveKind.LowTick, (DeskHapticSpec.plan(HapticEvent.Tick, HapticLevel.Default, false, true) as HapticPlan.Primitive).kind)
        val oneShot = DeskHapticSpec.plan(HapticEvent.Confirm, HapticLevel.Default, false, false) as HapticPlan.OneShot
        assertTrue(oneShot.millis <= 30)
    }

    // 끔은 없음, 약하게는 세기를 줄인다
    @Test
    fun plan_respectsLevel() {
        assertEquals(HapticPlan.None, DeskHapticSpec.plan(HapticEvent.Confirm, HapticLevel.Off, true, true))
        val light = DeskHapticSpec.plan(HapticEvent.Confirm, HapticLevel.Light, true, true) as HapticPlan.Primitive
        val normal = DeskHapticSpec.plan(HapticEvent.Confirm, HapticLevel.Default, true, true) as HapticPlan.Primitive
        assertTrue(light.scale < normal.scale)
    }

    // 디바운스: 최소 간격 안은 버림, 빠른 연속은 FastTick, 확정 직후 잠시 조용
    @Test
    fun debouncer_dropsAndSoftensAndQuiets() {
        val d = HapticDebouncer(minIntervalMillis = 40, fastIntervalMillis = 90, quietAfterConfirmMillis = 160)
        assertEquals(HapticEvent.Tick, d.tick(1_000))
        assertNull(d.tick(1_030))
        assertEquals(HapticEvent.FastTick, d.tick(1_060))
        assertEquals(HapticEvent.Tick, d.tick(1_300))
        d.confirm(2_000)
        assertNull(d.tick(2_100))
        assertEquals(HapticEvent.Tick, d.tick(2_200))
    }
}
