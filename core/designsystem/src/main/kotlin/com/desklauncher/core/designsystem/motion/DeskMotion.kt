package com.desklauncher.core.designsystem.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.spring
import androidx.compose.ui.unit.dp

/**
 * 서류철 스트립 모션·치수 상수 (PRD §3). 튜닝은 이 파일에서만 한다.
 * 값의 근거는 S24+(384×832dp) 오른손 엄지 기준 — 실기기 검증 후 조정.
 */
object DeskMotion {

    // ── 치수 ────────────────────────────────────────────────
    /** 탭 하나의 세로 간격 하한/상한. 하한은 엄지 오차(약 7mm)를 고려한 최소값. */
    val TabMinExtent = 36.dp
    val TabMaxExtent = 52.dp
    val TabGap = 3.dp

    /**
     * Idle 상태에서 화면 가장자리에 보이는 탭 폭. 아이콘(18dp) + 여백.
     * 32dp는 뒤로가기 제스처 영역(약 20~30dp)에 아이콘이 다 들어가 버려 40dp로 넓혔다 (S24+ 실기기 피드백).
     */
    val TabIdleVisibleWidth = 40.dp

    /** 선택된 서류철이 화면 안쪽으로 '툭' 튀어나오는 거리. 이웃 탭은 [TabPopLevels] 비율만큼. */
    val TabPopShift = 48.dp

    /** 선택 탭 옆(화면 안쪽) 알약 인디케이터 높이·탭과의 간격 */
    val IndicatorHeight = 40.dp
    val IndicatorGap = 10.dp

    /** 앱 서랍 글자 스트립: 파도 최대 이동 거리, 원형 인디케이터 지름 */
    val LetterWaveShift = 64.dp
    val LetterIndicatorSize = 48.dp

    /**
     * 서류철 스트립이 터치를 받는 폭(가장자리부터). 튀어나온 탭 전체(40+48dp)를 덮고,
     * 엄지가 뒤로가기 제스처 영역(약 20~30dp) 바깥에서 훑을 수 있게 넉넉히 잡는다.
     */
    val StripTouchWidth = 96.dp

    /** 시스템이 한쪽 가장자리당 허용하는 제스처 제외 최대 높이 (Android 정책). */
    val GestureExclusionMaxHeight = 200.dp

    /** 스크럽 중 손가락을 스트립에서 화면 안쪽으로 이만큼 벗어나면 '취소'. */
    val CancelDistance = 96.dp

    // ── 스크럽 물리 ──────────────────────────────────────────
    /**
     * 파도 반경(탭 단위): 손가락에서 이만큼 떨어진 탭까지 휘어진다(올린 코사인 곡선).
     * 탭은 9개 안팎이라 3칸, 글자는 30개 안팎이라 6칸 — 스크린샷 레퍼런스처럼 넓고 완만한 곡선.
     */
    const val LetterWaveRadius = 6f

    /**
     * 서류철 '툭툭' 튀어나옴 단계: 선택 탭 1, 바로 옆 0.3, 두 칸 옆 0.08, 그 밖 0.
     * 손가락을 따라 흐르지 않고 칸이 바뀔 때마다 THUD 진동과 함께 한 번씩 튀어나온다.
     */
    val TabPopLevels: List<Float> = listOf(1f, 0.3f, 0.08f)

    /** 튀어나옴 스프링: 빠르고(강성 1100) 살짝 튕기며(감쇠 0.55) 멈춘다 — 종이철이 '툭' 걸리는 느낌 */
    const val TabPopDamping = 0.55f
    const val TabPopStiffness = 1100f

    /** 처음/끝을 넘어 당길 수 있는 최대 거리(탭 단위, 고무줄 저항). */
    const val OverscrollMax = 0.6f

    /** 이 속도(탭/초) 이상으로 놓으면 플링 — 열지 않고 넘겨서 멈춘다. */
    const val FlingVelocityThreshold = 4f

    /** 플링 투영 시간(초): 목표 = 현재 + 속도 × 이 값. */
    const val FlingProjectionSeconds = 0.18f

    /** 정수 인덱스로 스냅하는 스프링. 약간의 오버슈트로 '드르륵' 멈추는 느낌. */
    val SettleSpring: AnimationSpec<Float> = spring(dampingRatio = 0.7f, stiffness = 380f)

    /** 플링 후 선택(Selected) 상태를 유지하는 시간. */
    const val SelectedTimeoutMillis = 2_500L

    /** Idle ↔ 활성 전환 페이드 시간. */
    const val PhaseFadeMillis = 140

    // ── 햅틱 ────────────────────────────────────────────────
    /** 경계 통과 판정 히스테리시스(탭 단위). 경계에서 손 떨림으로 인한 중복 진동 방지. */
    const val HapticHysteresis = 0.15f

    /** 처음/끝을 이만큼(탭 단위) 넘기면 '벽' 진동. */
    const val HapticEdgeThreshold = 0.25f

    /**
     * 탭 넘김 진동 최소 간격. THUD는 꼬리가 30~40ms라 그보다 촘촘하면 진동이 뭉개져 '웅—' 하는 잔향이 남는다.
     * 빠른 플링에서는 몇 칸을 건너뛰더라도 또렷한 '툭'만 남긴다.
     */
    const val HapticMinIntervalMillis = 42L

    /** 이보다 짧은 간격으로 연속되면 빠른 넘김 — 한 단계 약한 THUD */
    const val HapticFastIntervalMillis = 90L

    /** 서류철을 열어 확정한 뒤 이 시간 동안은 넘김 진동을 내지 않는다 (확정 '툭'의 여운을 지키기 위함) */
    const val HapticQuietAfterConfirmMillis = 160L

    // ── 진동 세기 (VibrationEffect.Composition 스케일 0..1) ──
    /** 탭 넘김: 낮은 THUD로 짧고 둔탁하게 */
    const val ThudTickScale = 0.55f
    const val ThudTickFastScale = 0.45f
    /** 첫/마지막을 넘어 당김(벽) */
    const val ThudEdgeScale = 0.7f
    /** 서류철 확정: 책을 툭 내려놓는 묵직한 THUD */
    const val ThudConfirmScale = 0.92f
    /** 설정 '약하게'일 때 곱하는 값 */
    const val LightLevelFactor = 0.6f

    // THUD 미지원 기기 폴백
    const val LowTickTickScale = 0.8f
    const val LowTickConfirmScale = 1f
    /** 프리미티브가 모두 없을 때: 짧은 단발 진동(ms, 진폭 1..255) */
    const val OneShotTickMillis = 10L
    const val OneShotTickAmplitude = 90
    const val OneShotConfirmMillis = 24L
    const val OneShotConfirmAmplitude = 200
}
