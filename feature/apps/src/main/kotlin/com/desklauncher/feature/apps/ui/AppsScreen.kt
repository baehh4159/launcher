package com.desklauncher.feature.apps.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.apps.R
import com.desklauncher.feature.apps.ui.component.AppIconProvider
import com.desklauncher.feature.apps.ui.component.AppRow
import com.desklauncher.feature.apps.ui.component.LetterIndex
import com.desklauncher.feature.apps.ui.component.SearchPill
import com.desklauncher.feature.apps.ui.preview.AppsPreviewData
import kotlinx.coroutines.launch

/** 앱 서랍 연결부. 창 틀은 app이 씌운다(DeskWindowHost). 앱을 실행하면 [onLaunched]로 창을 닫는다. */
@Composable
fun AppsRoute(
    onLaunched: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentOnLaunched by rememberUpdatedState(onLaunched)
    val failedText = stringResource(R.string.apps_launch_failed)
    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                AppsEvent.Launched -> currentOnLaunched()
                AppsEvent.LaunchFailed -> Toast.makeText(context, failedText, Toast.LENGTH_SHORT).show()
            }
        }
    }
    val loader = viewModel.iconLoader
    val iconProvider: AppIconProvider? = remember(loader) { loader?.let { l -> { key, size -> l.load(key, size) } } }
    AppsScreen(state = state, onAction = viewModel::onAction, iconProvider = iconProvider, modifier = modifier)
}

/** 앱 서랍 (APPS-01): 목록 + 오른쪽 초성 색인 + 하단 검색. 검색은 설치 앱만 대상(PRD §12). */
@Composable
fun AppsScreen(
    state: AppsUiState,
    onAction: (AppsAction) -> Unit,
    modifier: Modifier = Modifier,
    iconProvider: AppIconProvider? = null,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    // 묶음 머리글의 목록 위치 (색인 → 스크롤)
    val headerPositions = remember(state.sections) {
        var position = 0
        state.sections.map { section -> position.also { position += section.apps.size + 1 } }
    }
    val colors = DeskTheme.colors

    Column(modifier = modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.isLoading -> Unit
                state.sections.isEmpty() -> Text(
                    text = if (state.isSearching) stringResource(R.string.apps_no_result, state.query) else stringResource(R.string.apps_empty),
                    style = DeskType.label,
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(DeskSpacing.l),
                )
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(start = DeskSpacing.xs, end = IndexReserve, top = DeskSpacing.s, bottom = DeskSpacing.s),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        state.sections.forEach { section ->
                            item(key = "header_${section.key}", contentType = "header") {
                                Text(
                                    text = section.key,
                                    style = DeskType.caption,
                                    color = colors.muted,
                                    modifier = Modifier.padding(start = DeskSpacing.s, top = DeskSpacing.s, bottom = DeskSpacing.xxs),
                                )
                            }
                            items(section.apps, key = { it.key }, contentType = { "app" }) { app ->
                                AppRow(app = app, iconProvider = iconProvider, onClick = { onAction(AppsAction.Launch(app)) })
                            }
                        }
                    }
                    if (!state.isSearching) {
                        LetterIndex(
                            keys = state.indexKeys,
                            onSelect = { index -> headerPositions.getOrNull(index)?.let { scope.launch { listState.scrollToItem(it) } } },
                            modifier = Modifier.align(Alignment.CenterEnd).padding(vertical = DeskSpacing.m, horizontal = DeskSpacing.xxs),
                        )
                    }
                }
            }
        }
        HorizontalDivider(thickness = DeskLine.hairline, color = colors.line)
        SearchPill(
            query = state.query,
            onQueryChange = { onAction(AppsAction.Query(it)) },
            onSubmit = { state.sections.firstOrNull()?.apps?.firstOrNull()?.let { onAction(AppsAction.Launch(it)) } },
            modifier = Modifier.padding(DeskSpacing.s),
        )
    }
}

private val IndexReserve = DeskSpacing.xl + DeskSpacing.xxs

@DeskPreviews
@Composable
private fun AppsScreenPreview() {
    DeskTheme { AppsScreen(state = AppsPreviewData.list, onAction = {}) }
}

@DeskPreviews
@Composable
private fun AppsScreenSearchPreview() {
    DeskTheme { AppsScreen(state = AppsPreviewData.searching, onAction = {}) }
}

@DeskPreviews
@Composable
private fun AppsScreenEmptyPreview() {
    DeskTheme { AppsScreen(state = AppsPreviewData.noResult, onAction = {}) }
}
