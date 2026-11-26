package com.desklauncher.core.designsystem.scrub

import com.desklauncher.core.designsystem.motion.DeskMotion
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sign

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

    /** 0..1. 포커스 위치에서 1, 멀어질수록 가우시안으로 감소. */
    fun popFraction(index: Int, focus: Float, sigma: Float = DeskMotion.PopSigma): Float {
        val d = index - focus
        return exp(-(d * d) / (2f * sigma * sigma))
    }

    /** 포커스에서 멀어지는 방향으로 벌어지는 양(탭 단위). 포커스 탭이 커질 자리를 만든다. */
    fun spreadOffset(index: Int, focus: Float, maxSpread: Float = DeskMotion.SpreadMax): Float {
        val d = index - focus
        return sign(d) * maxSpread * (1f - exp(-abs(d)))
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
