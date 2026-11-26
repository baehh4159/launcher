package com.desklauncher.feature.doodle.domain

import kotlinx.serialization.Serializable

/**
 * 획 하나. 좌표는 캔버스 px, 굵기도 px (저장 시 캔버스 크기를 함께 남겨 다른 화면에서도 비율로 재현).
 * points = [x0, y0, x1, y1, …] — 객체를 점마다 만들지 않아 저장·그리기가 가볍다.
 */
@Serializable
data class Stroke(
    val argb: Int,
    val widthPx: Float,
    val eraser: Boolean = false,
    val points: List<Float> = emptyList(),
) {
    val pointCount: Int get() = points.size / 2
}

/** 펜 굵기 3단계 + 지우개 (dp) */
enum class PenWidth(val dp: Float) { Thin(2.5f), Medium(5f), Thick(10f) }

const val EraserWidthDp = 24f
