package com.desklauncher.feature.desk.ui

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.datastore.StripLayout
import com.desklauncher.core.designsystem.haptics.rememberDeskHaptics
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.desk.clock.WallClockFormatter
import com.desklauncher.feature.desk.clock.WallClockText
import com.desklauncher.feature.desk.clock.rememberCurrentMinute
import com.desklauncher.feature.desk.strip.DefaultStrip
import com.desklauncher.feature.desk.strip.DeskStrip
import com.desklauncher.feature.desk.strip.ScrubEvent
import com.desklauncher.feature.desk.strip.ScrubTracker
import com.desklauncher.feature.desk.strip.StripDimens
import com.desklauncher.feature.desk.strip.StripGeometry
import com.desklauncher.feature.desk.strip.StripSide
import com.desklauncher.feature.desk.strip.StripSlot
import com.desklauncher.feature.desk.strip.StripVisualState
import com.desklauncher.feature.desk.strip.stripScrub
import com.desklauncher.feature.desk.ui.component.DefaultHomeCard
import com.desklauncher.feature.desk.ui.component.QuickWriteBar
import com.desklauncher.feature.desk.ui.component.ResumeCard
import com.desklauncher.feature.desk.ui.component.ScrubHint
import com.desklauncher.feature.desk.ui.component.WallLine
import com.desklauncher.feature.desk.ui.preview.DeskPreviewData
import java.time.ZoneId

/**
 * 책상 연결부. 칸을 열거나 바로 적기·이어서 쓰기를 누르면 어떤 창을 띄울지는 app이 정한다 (AGENTS.md §4.5).
 */
@Composable
fun DeskRoute(
    onOpenSlot: (StripSlot) -> Unit,
    onQuickWrite: () -> Unit,
    onResume: () -> Unit,
    onOpenSettings: () -> Unit,
    onSetDefaultHome: () -> Unit,
    modifier: Modifier = Modifier,
    /** 창이 열려 있으면 false — 책상 훑기 영역 비활성 (PRD §4.2) */
    scrubEnabled: Boolean = true,
    /** 이 앱이 기본 홈 역할을 가졌는지 — 아니면 설정 카드를 보인다 (HOME-01) */
    isDefaultHome: Boolean = true,
    viewModel: DeskViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var stripState by remember { mutableStateOf(StripVisualState.Idle) }
    val haptics = rememberDeskHaptics()
    val now = rememberCurrentMinute()
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val clock = remember(now, locale) {
        WallClockFormatter.format(now, ZoneId.systemDefault(), DateFormat.is24HourFormat(context), locale)
    }
    DeskScreen(
        state = state,
        clock = clock,
        onAction = { action ->
            when (action) {
                is DeskAction.OpenSlot -> state.slots.getOrNull(action.index)?.let { slot ->
                    // 직접 탭으로 열 때도 확정 진동 (PRD §4.2)
                    haptics.confirm()
                    onOpenSlot(slot)
                }
                DeskAction.QuickWrite -> onQuickWrite()
                DeskAction.Resume -> onResume()
                DeskAction.OpenSettings -> onOpenSettings()
                DeskAction.SetDefaultHome -> onSetDefaultHome()
            }
        },
        modifier = modifier,
        stripState = stripState,
        scrubEnabled = scrubEnabled,
        isDefaultHome = isDefaultHome,
        onScrubEvent = { event ->
            stripState = stripState.reduce(event)
            // 두 단계 촉각 (STRIP-03, PRD §5): 탐색 시작·칸 이동 = 약한 틱, 열기 = 확정. 취소에는 없음.
            when (event) {
                is ScrubEvent.Claimed, is ScrubEvent.FocusChanged -> haptics.tick()
                is ScrubEvent.Open -> {
                    haptics.confirm()
                    viewModel.onScrubbedOpen()
                    state.slots.getOrNull(event.index)?.let(onOpenSlot)
                }
                is ScrubEvent.CancelArmed, ScrubEvent.Cancelled -> Unit
            }
        },
    )
}

/** 훑기 사건 → 보이는 상태 (순수) */
internal fun StripVisualState.reduce(event: ScrubEvent): StripVisualState = when (event) {
    is ScrubEvent.Claimed -> StripVisualState(focused = event.focus, exploring = true, cancelArmed = false)
    is ScrubEvent.FocusChanged -> copy(focused = event.focus)
    is ScrubEvent.CancelArmed -> copy(cancelArmed = event.armed)
    is ScrubEvent.Open, ScrubEvent.Cancelled -> StripVisualState.Idle
}

/**
 * 책상 (HOME-02, PRD §3). 스크롤하지 않는다 — 넘치면 접는다(§3.2).
 * 서류철은 하단 기준 고정 간격이라 위쪽 내용이 바뀌어도 칸이 움직이지 않는다(§4.1).
 */
@Composable
fun DeskScreen(
    state: DeskUiState,
    clock: WallClockText,
    onAction: (DeskAction) -> Unit,
    modifier: Modifier = Modifier,
    stripState: StripVisualState = StripVisualState.Idle,
    /** 훑기 사건 (STRIP-02). 창이 열려 있으면 호출부가 [scrubEnabled]를 끈다 */
    onScrubEvent: (ScrubEvent) -> Unit = {},
    scrubEnabled: Boolean = true,
    isDefaultHome: Boolean = true,
) {
    val density = LocalDensity.current
    val geometry = remember(density, state.slots.size) {
        with(density) {
            StripGeometry(
                extent = StripDimens.Extent.toPx(),
                gap = StripDimens.Gap.toPx(),
                count = state.slots.size,
                gapBefore = DefaultStrip.GapBeforeIndex,
            )
        }
    }
    val side = if (state.stripLayout == StripLayout.RightVisible) StripSide.Right else StripSide.Left
    val rightTabInset = rememberGestureEdgeInset()
    // 내용이 서류철·비교군 탭을 피하는 여백
    val contentStart = if (side == StripSide.Left) StripDimens.SpineVisible + DeskSpacing.m else DeskSpacing.l
    val contentEnd = if (side == StripSide.Right) rightTabInset + StripDimens.RightTabWidth + DeskSpacing.s else DeskSpacing.m
    // 바로 적기는 책등이 튀어나온 상태에서도 닿지 않는 곳에서 시작한다 (PRD §3.1: 앱 ┆ 간격 ┆ 바로 적기)
    val barStart = if (side == StripSide.Left) StripDimens.SpineVisible + StripDimens.SpinePop + DeskSpacing.s else DeskSpacing.l
    val stripBand = with(density) { geometry.height.toDp() } + StripBottomPadding
    val holdMillis = ScrubTracker.holdMillis(LocalViewConfiguration.current.longPressTimeoutMillis)
    val scrub = with(density) {
        Modifier.stripScrub(
            enabled = scrubEnabled,
            geometry = geometry,
            zoneRightInset = rightTabInset.toPx(),
            // 분리형: 보이지 않는 64dp 영역 / 비교군: 오른쪽 탭 자체가 훑는 곳
            zoneWidth = (if (side == StripSide.Left) StripDimens.ScrubZoneWidth else StripDimens.RightTabWidth).toPx(),
            stripBottomInset = StripBottomPadding.toPx(),
            holdMillis = holdMillis,
            cancelDistance = StripDimens.CancelDistance.toPx(),
            onEvent = onScrubEvent,
        )
    }

    Box(modifier = modifier.fillMaxSize().background(DeskTheme.colors.desk)) {
        Box(modifier = Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).then(scrub)) {
            // 벽 줄과 서류철 영역 사이 남는 공간의 가운데에 펼친 공책 한 장 (화면 비율이 아니라 공간에 맞춤)
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxSize()
                    .padding(start = contentStart, end = contentEnd, top = DeskSpacing.xs, bottom = stripBand + DeskSpacing.m),
            ) {
                WallLine(clock = clock, onOpenSettings = { onAction(DeskAction.OpenSettings) })
                if (!isDefaultHome) {
                    DefaultHomeCard(onSet = { onAction(DeskAction.SetDefaultHome) })
                }
                Spacer(Modifier.weight(1f))
                ResumeCard(resume = state.resume, onClick = { onAction(DeskAction.Resume) })
                Spacer(Modifier.weight(1f))
            }

            // 첫 실행: 보이지 않는 훑기 영역 위에 안내 (서류철 구간 가운데)
            if (state.showScrubHint && side == StripSide.Left && !stripState.exploring) {
                ScrubHint(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = rightTabInset, bottom = with(density) { (geometry.height / 2f).toDp() }),
                )
            }

            // 맨 아래 한 줄: 앱 칸(서류철 마지막 칸) | 바로 적기 (PRD §3.1)
            QuickWriteBar(
                onClick = { onAction(DeskAction.QuickWrite) },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = barStart, end = contentEnd, bottom = StripBottomPadding)
                    .height(StripDimens.Extent - StripDimens.SlotInnerGap),
            )

            DeskStrip(
                slots = state.slots,
                geometry = geometry,
                state = stripState,
                onSlotClick = { index -> onAction(DeskAction.OpenSlot(index, viaScrub = false)) },
                side = side,
                edgeInset = rightTabInset,
                modifier = Modifier.align(Alignment.BottomStart).padding(bottom = StripBottomPadding),
            )
        }
    }
}

/** 서류철 아래 여백 — 시스템 내비게이션 위로 살짝 띄운다 */
private val StripBottomPadding = 8.dp

/** 오른쪽 가장자리에서 훑기·비교군 탭을 띄우는 거리 = 시스템 제스처 인셋(안전 영역 밖 부분) + 여유 8dp (PRD §4.1) */
@Composable
internal fun rememberGestureEdgeInset(): Dp {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    val gestures = WindowInsets.systemGestures.getRight(density, direction)
    val safe = WindowInsets.safeDrawing.getRight(density, direction)
    return with(density) { (gestures - safe).coerceAtLeast(0).toDp() } + GestureInsetMargin
}

private val GestureInsetMargin = 8.dp

@DeskPreviews
@Composable
private fun DeskScreenPreview() {
    DeskTheme { DeskScreen(state = DeskPreviewData.empty, clock = DeskPreviewData.clock, onAction = {}) }
}

@DeskPreviews
@Composable
private fun DeskScreenResumePreview() {
    DeskTheme { DeskScreen(state = DeskPreviewData.withWork, clock = DeskPreviewData.clock, onAction = {}) }
}

/** 정지 스냅샷: 맨 아래 앱 칸 탐색 — 앱 책등이 튀어나와도 바로 적기와 간격이 남고 이름표는 줄 위 */
@DeskPreviews
@Composable
private fun DeskScreenBottomRowPreview() {
    DeskTheme {
        DeskScreen(
            state = DeskPreviewData.withWork,
            clock = DeskPreviewData.clock,
            onAction = {},
            stripState = StripVisualState(focused = 5, exploring = true),
        )
    }
}

/** 정지 스냅샷: 엄지가 두 번째 공책 칸에 있다 */
@DeskPreviews
@Composable
private fun DeskScreenScrubbingPreview() {
    DeskTheme {
        DeskScreen(
            state = DeskPreviewData.withWork,
            clock = DeskPreviewData.clock,
            onAction = {},
            stripState = StripVisualState(focused = 1, exploring = true),
        )
    }
}

/** 첫 실행: 기본 홈 설정 카드 + 훑기 안내 */
@DeskPreviews
@Composable
private fun DeskScreenFirstRunPreview() {
    DeskTheme {
        DeskScreen(
            state = DeskPreviewData.empty.copy(showScrubHint = true),
            clock = DeskPreviewData.clock,
            onAction = {},
            isDefaultHome = false,
        )
    }
}
