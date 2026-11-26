package com.desklauncher.feature.doodle.domain

/**
 * 저장할 때와 다른 크기의 캔버스에서 열면 비율을 유지해 맞춘다 (가로세로 중 작은 배율).
 * 굵기도 같은 배율로.
 */
object StrokeScaling {
    fun fit(strokes: List<Stroke>, fromWidth: Int, fromHeight: Int, toWidth: Int, toHeight: Int): List<Stroke> {
        if (fromWidth <= 0 || fromHeight <= 0 || toWidth <= 0 || toHeight <= 0) return strokes
        val scale = minOf(toWidth.toFloat() / fromWidth, toHeight.toFloat() / fromHeight)
        if (scale == 1f) return strokes
        return strokes.map { s -> s.copy(widthPx = s.widthPx * scale, points = s.points.map { it * scale }) }
    }
}
