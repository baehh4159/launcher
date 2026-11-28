package com.desklauncher.feature.desk.strip

import kotlin.math.abs

/**
 * 서류철 칸의 세로 기하 (PRD §4.1). 왼쪽 책등 배치와 오른쪽 훑기 판정이 **이 객체 하나**를 함께 쓴다 —
 * 두 레이아웃을 따로 계산해 한 칸씩 어긋나는 일을 막기 위함.
 *
 * 좌표는 서류철 영역 맨 위가 0인 px. 칸 번호 0이 맨 위.
 * @param extent 칸 간격(px). 시작값 56dp ≈ 9mm (한 손 엄지 표적 연구, PRD §4.1)
 * @param gap 구분 여백(px). 진동 없는 빈 구간.
 * @param gapBefore 이 번호의 칸 바로 위에 구분이 들어간다(1..count-1일 때만 유효).
 */
class StripGeometry(
    val extent: Float,
    val gap: Float,
    val count: Int,
    val gapBefore: Int,
) {
    init {
        require(count > 0) { "칸이 하나 이상 있어야 합니다" }
        require(extent > 0f)
    }

    private val hasGap: Boolean = gapBefore in 1 until count && gap > 0f

    val height: Float = extent * count + if (hasGap) gap else 0f

    fun top(index: Int): Float = index * extent + if (hasGap && index >= gapBefore) gap else 0f

    fun bottom(index: Int): Float = top(index) + extent

    fun center(index: Int): Float = top(index) + extent / 2f

    /** y에 있는 칸. 구분 여백이면 null. 영역 밖은 가장 가까운 끝 칸(고무줄 없이 끝에 머문다). */
    fun slotAt(y: Float): Int? {
        if (y < 0f) return 0
        if (y >= height) return count - 1
        for (index in 0 until count) {
            if (y >= top(index) && y < bottom(index)) return index
        }
        return null
    }

    /** y에서 가장 가까운 칸 (구분 여백 위라도 하나를 고른다) */
    fun nearestSlot(y: Float): Int =
        slotAt(y) ?: (0 until count).minBy { abs(center(it) - y) }

    /**
     * 탐색 중 다음 포커스 (PRD §4.2 경계 이동 여유).
     * 현재 칸의 띠를 [hysteresisFraction]×칸 간격만큼 넓혀 그 안이면 유지하고, 구분 여백 위에서도 유지한다 —
     * 경계에서 손이 조금 떨려도 같은 두 칸을 왕복한 것으로 세지 않기 위함.
     */
    fun nextFocus(current: Int?, y: Float, hysteresisFraction: Float): Int {
        if (current == null || current !in 0 until count) return nearestSlot(y)
        val margin = extent * hysteresisFraction
        if (y >= top(current) - margin && y < bottom(current) + margin) return current
        return slotAt(y) ?: current
    }

    /**
     * 돌출 정도: **포커스 칸 하나만** 1, 나머지는 0 (PRD §3.1 — 1차 실기기 피드백).
     * 이웃까지 조금씩 튀어나오면 평상시 책등 폭이 들쭉날쭉해 보이고 맨 아래 줄이 바로 적기에 닿는다.
     */
    fun popLevel(index: Int, focused: Int?): Float = if (focused != null && index == focused) 1f else 0f

    /**
     * 이름표(탐색 미리보기)의 세로 중심. 맨 아래 칸(앱 — 바로 적기와 같은 줄)은 바로 적기를 가리지 않도록
     * 그 줄 위에 띄운다 (PRD §3.1).
     */
    fun labelCenter(index: Int, labelHeight: Float, lift: Float): Float =
        if (index == count - 1) top(index) - lift - labelHeight / 2f else center(index)
}
