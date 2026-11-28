package com.desklauncher.core.designsystem.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.unit.dp

/**
 * 모션 토큰 (DESIGN_LANGUAGE §4). beUI Motion 스프링(stiffness/damping/mass)을
 * dampingRatio = damping / (2·√(stiffness·mass)), stiffness = stiffness / mass 로 옮겼다. 1을 넘는 감쇠비는 1.0.
 */
object DeskMotion {

    // ── 스프링 ─────────────────────────────────────────────
    /** 누름 축소 (beUI SPRING_PRESS 500/30/0.6) */
    fun <T> press(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.87f, stiffness = 833f)

    /** 아이콘·라벨 교체 (SPRING_SWAP 460/30/0.55) */
    fun <T> swap(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.94f, stiffness = 836f)

    /** 팝오버·트레이 등장 (SPRING_PANEL 420/40/0.5 → 임계 감쇠) */
    fun <T> panel(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 840f)

    /** 카드 재배치·접기 (SPRING_LAYOUT 360/32/0.6) */
    fun <T> layout(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 600f)

    /** 선택 강조가 칸 사이를 미끄러짐 (SPRING_GLIDE 700/50/0.5) */
    fun <T> glide(): FiniteAnimationSpec<T> = spring(dampingRatio = 1f, stiffness = 1400f)

    /** 서류철 칸 → 창 펼침 (beUI Bloom menu 300/32/0.9) — 아주 살짝 넘침 */
    fun <T> folder(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.97f, stiffness = 333f)

    /** 서류철 돌출 — 칸을 넘는 순간 살짝 튀고 멈춘다(진동과 같은 박자) */
    fun <T> tabPop(): FiniteAnimationSpec<T> = spring(dampingRatio = 0.75f, stiffness = 900f)

    // ── 이징 ───────────────────────────────────────────────
    val EaseOut = CubicBezierEasing(0.16f, 1f, 0.3f, 1f)
    val EaseInOut = CubicBezierEasing(0.77f, 0f, 0.175f, 1f)
    /** 창·서랍 — 튀지 않음 (beUI EASE_DRAWER) */
    val EaseDrawer = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

    // ── 길이 (D2: 300ms 이하 기본) ─────────────────────────
    const val RevealMillis = 220
    const val SwapOutMillis = 120
    const val SwapInMillis = 180
    const val WindowCloseMillis = 280
    /** 움직임 줄이기 상태의 페이드 */
    const val ReducedFadeMillis = 150

    fun <T> reveal(): FiniteAnimationSpec<T> = tween(RevealMillis, easing = EaseOut)
    fun <T> windowClose(): FiniteAnimationSpec<T> = tween(WindowCloseMillis, easing = EaseDrawer)
    fun <T> reducedFade(): AnimationSpec<T> = tween(ReducedFadeMillis)

    // ── 거리 ───────────────────────────────────────────────
    /** 내용 등장 시 올라오는 거리 (beUI content reveal 8px) */
    val RevealOffset = 8.dp
    /** 내용 교체 이동 (beUI content swap 4px) */
    val SwapOffset = 4.dp
    /** 누름 축소 비율 (beUI whileTap 0.97) */
    const val PressScale = 0.97f
}
