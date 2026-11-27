package com.desklauncher.core.designsystem.scrub

import com.desklauncher.core.designsystem.motion.DeskMotion
import kotlin.math.abs
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt

/** 스크럽 계산 순수 함수. 단위: focus/인덱스 = 탭 단위, y·extent = px. */
object IndexScrubMath {

    /** 스트립 상단 기준 y → 연속 포커스 (탭 중앙이 정수). */
    fun rawFocus(y: Float, extent: Float): Float = if (extent <= 0f) 0f else y / extent - 0.5f

    /** 범위를 벗어난 부분에 고무줄 저항을 적용. 최대 [maxOverscroll]까지만 넘어간다. */
    fun rubberBand(raw: Float, count: Int, maxOverscroll: Float = DeskMotion.OverscrollMax): Float {
        if (count <= 0) return 0f
        val last = (count - 1).toFloat()
        return when {
            raw < 0f -> -damp(-raw, maxOverscroll)
            raw > last -> last + damp(raw - last, maxOverscroll)
            else -> raw
        }
    }

    fun nearestIndex(focus: Float, count: Int): Int =
        if (count <= 0) 0 else focus.roundToInt().coerceIn(0, count - 1)

    /**
     * 파도 곡선 0..1 (올린 코사인): 포커스에서 1, [radius]에서 0, 그 밖은 0.
     * 양 끝 기울기가 0이라 휘어진 구간과 곧은 구간 사이가 각지지 않는다.
     */
    fun waveFraction(index: Int, focus: Float, radius: Float): Float {
        val d = abs(index - focus)
        if (radius <= 0f || d >= radius) return 0f
        return 0.5f * (1f + cos(PI.toFloat() * d / radius))
    }

    /** 플링 목표 인덱스. */
    fun flingTarget(
        focus: Float,
        velocity: Float,
        count: Int,
        projectionSeconds: Float = DeskMotion.FlingProjectionSeconds,
    ): Int = nearestIndex(focus + velocity * projectionSeconds, count)

    // x가 커질수록 m에 수렴, 기울기는 0에서 1
    private fun damp(x: Float, m: Float): Float = x * m / (x + m)
}
