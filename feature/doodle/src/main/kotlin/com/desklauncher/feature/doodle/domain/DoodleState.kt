package com.desklauncher.feature.doodle.domain

/** 낙서 내용 (불변). 획 추가 → DOODLE-01 */
data class DoodleState(
    val strokes: List<Stroke> = emptyList(),
) {
    val isEmpty: Boolean get() = strokes.isEmpty()

    /** 점 하나뿐인 획(톡 찍기)도 점으로 남긴다. 점이 없으면 무시 */
    fun add(stroke: Stroke): DoodleState =
        if (stroke.pointCount == 0) this else copy(strokes = strokes + stroke)
}
