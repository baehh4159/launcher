package com.desklauncher.feature.desk.ui

import android.text.format.DateFormat
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.component.DeskSurface
import com.desklauncher.core.designsystem.component.ToolPreviewCard
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.scrub.IndexScrubState
import com.desklauncher.core.designsystem.scrub.IndexTabItem
import com.desklauncher.core.designsystem.scrub.IndexTabStrip
import com.desklauncher.core.designsystem.scrub.ScrubPhase
import com.desklauncher.core.designsystem.scrub.rememberIndexScrubState
import com.desklauncher.core.designsystem.text.asString
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.desk.R
import com.desklauncher.feature.desk.clock.DeskClockFormatter
import com.desklauncher.feature.desk.clock.DeskClockText
import com.desklauncher.feature.desk.clock.rememberCurrentMinute
import com.desklauncher.feature.desk.ui.component.DeskClock
import com.desklauncher.feature.desk.ui.preview.DeskPreviewData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DeskRoute(
    onOpenTool: (toolId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeskViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val now = rememberCurrentMinute()
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val clock = remember(now, locale) {
        DeskClockFormatter.format(now, DateFormat.is24HourFormat(context), locale)
    }
    DeskScreen(
        state = uiState,
        clock = clock,
        onAction = { action ->
            when (action) {
                is DeskAction.OpenTool -> onOpenTool(action.toolId)
            }
        },
        modifier = modifier,
        toolPreview = { tab, previewModifier ->
            val tool = remember(tab.toolId) { viewModel.findTool(tab.toolId) }
            if (tool != null) {
                tool.DeskPreview(tabColor = tab.color, modifier = previewModifier)
            } else {
                DefaultToolPreview(tab, previewModifier)
            }
        },
    )
}

@Composable
fun DeskScreen(
    state: DeskUiState,
    clock: DeskClockText,
    onAction: (DeskAction) -> Unit,
    modifier: Modifier = Modifier,
    stripState: IndexScrubState = rememberIndexScrubState(),
    toolPreview: @Composable (tab: DeskTabUi, modifier: Modifier) -> Unit = { tab, previewModifier ->
        DefaultToolPreview(tab, previewModifier)
    },
) {
    val scope = rememberCoroutineScope()

    // 플링 후 선택 상태는 잠시 뒤 자동으로 접는다.
    LaunchedEffect(stripState.phase) {
        if (stripState.phase == ScrubPhase.Selected) {
            delay(DeskMotion.SelectedTimeoutMillis)
            stripState.reset()
        }
    }
    BackHandler(enabled = stripState.phase != ScrubPhase.Idle) {
        scope.launch { stripState.reset() }
    }

    DeskSurface(modifier = modifier) {
        Box(modifier = Modifier.safeDrawingPadding()) {
            DeskClock(
                clock = clock,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            )
            when {
                // 로딩 중에는 빈 책상만 보여 준다 (홈 복귀 시 깜빡임 방지).
                state.isLoading -> Unit
                state.tabs.isEmpty() -> DeskEmptyState(
                    title = stringResource(R.string.desk_empty_title),
                    body = stringResource(R.string.desk_empty_body),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 32.dp),
                )
                else -> {
                    val focusedTab = state.tabs.getOrNull(stripState.focusedIndex)
                        ?.takeIf { stripState.phase != ScrubPhase.Idle }
                    AnimatedContent(
                        targetState = focusedTab,
                        transitionSpec = { fadeIn(tween(120)) togetherWith fadeOut(tween(90)) },
                        contentKey = { it?.toolId },
                        label = "deskPreview",
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(start = 24.dp, end = 24.dp, top = 136.dp),
                    ) { tab ->
                        if (tab != null) toolPreview(tab, Modifier)
                    }

                    val items = state.tabs.map { tab ->
                        IndexTabItem(key = tab.toolId, label = tab.title.asString(), icon = tab.icon, color = tab.color)
                    }
                    IndexTabStrip(
                        items = items,
                        state = stripState,
                        onOpen = { index -> state.tabs.getOrNull(index)?.let { onAction(DeskAction.OpenTool(it.toolId)) } },
                        openActionLabel = stringResource(R.string.desk_tab_open_action),
                        // 엄지가 닿는 하단 2/3 (PRD §3.1). 그립 위치 설정은 SHELL-11.
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .fillMaxHeight(0.66f)
                            .padding(bottom = 24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun DefaultToolPreview(tab: DeskTabUi, modifier: Modifier = Modifier) {
    ToolPreviewCard(icon = tab.icon, title = tab.title.asString(), tabColor = tab.color, modifier = modifier)
}

@DeskPreviews
@Composable
private fun DeskScreenPreview() {
    DeskTheme {
        DeskScreen(state = DeskPreviewData.withTabs, clock = DeskPreviewData.clock, onAction = {})
    }
}

/** 스크럽 중 정지 스냅샷: 세 번째 서류철에 포커스 + 책상 위 미리보기. */
@DeskPreviews
@Composable
private fun DeskScreenScrubbingPreview() {
    DeskTheme {
        DeskScreen(
            state = DeskPreviewData.withTabs,
            clock = DeskPreviewData.clock,
            onAction = {},
            stripState = rememberIndexScrubState(initialFocus = 2.2f, initialPhase = ScrubPhase.Scrubbing),
        )
    }
}

@DeskPreviews
@Composable
private fun DeskScreenEmptyPreview() {
    DeskTheme {
        DeskScreen(state = DeskPreviewData.empty, clock = DeskPreviewData.clock, onAction = {})
    }
}

@DeskPreviews
@Composable
private fun DeskScreenLoadingPreview() {
    DeskTheme {
        DeskScreen(state = DeskPreviewData.loading, clock = DeskPreviewData.clock, onAction = {})
    }
}
