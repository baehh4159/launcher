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

    /** Idle 상태에서 화면 가장자리에 보이는 탭 폭. 아이콘(20dp) + 여백. */
    val TabIdleVisibleWidth = 32.dp

    /** 파도 꼭대기(손가락 위치)에서 탭이 왼쪽으로 나오는 최대 거리. */
    val TabWaveShift = 44.dp

    /** 선택 탭 왼쪽 알약 인디케이터 높이·탭과의 간격 */
    val IndicatorHeight = 40.dp
    val IndicatorGap = 10.dp

    /** 앱 서랍 글자 스트립: 파도 최대 이동 거리, 원형 인디케이터 지름 */
    val LetterWaveShift = 64.dp
    val LetterIndicatorSize = 48.dp

    /** 터치를 받는 폭(가장자리부터). 뒤로가기 제스처 영역(약 20~30dp)보다 넓게 잡는다. */
    val StripTouchWidth = 72.dp

    /** 시스템이 한쪽 가장자리당 허용하는 제스처 제외 최대 높이 (Android 정책). */
    val GestureExclusionMaxHeight = 200.dp

    /** 스크럽 중 손가락을 스트립 왼쪽으로 이만큼 벗어나면 '취소'. */
    val CancelDistance = 96.dp

    // ── 스크럽 물리 ──────────────────────────────────────────
    /**
     * 파도 반경(탭 단위): 손가락에서 이만큼 떨어진 탭까지 휘어진다(올린 코사인 곡선).
     * 탭은 9개 안팎이라 3칸, 글자는 30개 안팎이라 6칸 — 스크린샷 레퍼런스처럼 넓고 완만한 곡선.
     */
    const val TabWaveRadius = 3.2f
    const val LetterWaveRadius = 6f

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

    /** 진동이 뭉개지지 않도록 하는 최소 간격. */
    const val HapticMinIntervalMillis = 12L

    /** 이보다 짧은 간격으로 연속되면 가벼운(FREQUENT) 틱 사용. */
    const val HapticFastIntervalMillis = 55L
}
