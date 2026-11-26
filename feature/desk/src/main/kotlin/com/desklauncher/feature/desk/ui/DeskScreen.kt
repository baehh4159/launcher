package com.desklauncher.feature.desk.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.component.DeskSurface
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.desk.R
import com.desklauncher.feature.desk.ui.component.PlaceholderTabColumn
import com.desklauncher.feature.desk.ui.preview.DeskPreviewData

@Composable
fun DeskRoute(
    onOpenTool: (toolId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DeskViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DeskScreen(
        state = uiState,
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
    onAction: (DeskAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    DeskSurface(modifier = modifier) {
        Box(modifier = Modifier.safeDrawingPadding()) {
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
        DeskScreen(state = DeskPreviewData.withTabs, onAction = {})
    }
}

@DeskPreviews
@Composable
private fun DeskScreenEmptyPreview() {
    DeskTheme {
        DeskScreen(state = DeskPreviewData.empty, onAction = {})
    }
}

@DeskPreviews
@Composable
private fun DeskScreenLoadingPreview() {
    DeskTheme {
        DeskScreen(state = DeskPreviewData.loading, onAction = {})
    }
}
