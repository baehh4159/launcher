package com.desklauncher.feature.desk.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.desk.R
import com.desklauncher.feature.desk.clock.DeskClockFormatter
import com.desklauncher.feature.desk.clock.DeskClockText
import com.desklauncher.feature.desk.clock.rememberCurrentMinute
import com.desklauncher.feature.desk.ui.component.DeskClock
import com.desklauncher.feature.desk.ui.component.PlaceholderTabColumn
import com.desklauncher.feature.desk.ui.preview.DeskPreviewData

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
    )
}

@Composable
fun DeskScreen(
    state: DeskUiState,
    clock: DeskClockText,
    onAction: (DeskAction) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                else -> PlaceholderTabColumn(
                    tabs = state.tabs,
                    onTabClick = { onAction(DeskAction.OpenTool(it)) },
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
    }
}

@DeskPreviews
@Composable
private fun DeskScreenPreview() {
    DeskTheme {
        DeskScreen(state = DeskPreviewData.withTabs, clock = DeskPreviewData.clock, onAction = {})
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
