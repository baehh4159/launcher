package com.desklauncher.feature.doodle.domain

/**
 * 낙서 내용 + 편집 이력 (불변).
 * 획 추가 → DOODLE-01, 실행 취소·다시 실행·전체 지우기 → DOODLE-02.
 * 이력은 '그때의 획 목록'을 그대로 쌓는다 — 목록은 불변이라 서로 공유되어 복사 비용이 거의 없다.
 */
data class DoodleState(
    val strokes: List<Stroke> = emptyList(),
    private val past: List<List<Stroke>> = emptyList(),
    private val future: List<List<Stroke>> = emptyList(),
) {
    val isEmpty: Boolean get() = strokes.isEmpty()
    val canUndo: Boolean get() = past.isNotEmpty()
    val canRedo: Boolean get() = future.isNotEmpty()

    /** 점이 없는 획은 무시 */
    fun add(stroke: Stroke): DoodleState =
        if (stroke.pointCount == 0) this else record(strokes + stroke)

    fun undo(): DoodleState =
        if (past.isEmpty()) this else DoodleState(strokes = past.last(), past = past.dropLast(1), future = future + listOf(strokes))

    fun redo(): DoodleState =
        if (future.isEmpty()) this else DoodleState(strokes = future.last(), past = past + listOf(strokes), future = future.dropLast(1))

    /** 전체 지우기도 실행 취소할 수 있다 */
    fun clear(): DoodleState = if (strokes.isEmpty()) this else record(emptyList())

    private fun record(next: List<Stroke>): DoodleState =
        DoodleState(strokes = next, past = (past + listOf(strokes)).takeLast(MaxHistory), future = emptyList())

    private companion object {
        const val MaxHistory = 100
    }
}
