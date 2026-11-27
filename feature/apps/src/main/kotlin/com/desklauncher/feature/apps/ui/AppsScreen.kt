package com.desklauncher.feature.apps.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.motion.DeskMotion
import com.desklauncher.core.designsystem.scrub.IndexLetterStrip
import com.desklauncher.core.designsystem.scrub.IndexScrubState
import com.desklauncher.core.designsystem.scrub.ScrubPhase
import com.desklauncher.core.designsystem.scrub.rememberIndexScrubState
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.apps.R
import com.desklauncher.feature.apps.di.AppIconEntryPoint
import com.desklauncher.feature.apps.domain.model.InstalledApp
import com.desklauncher.feature.apps.ui.component.AppMenuAction
import com.desklauncher.feature.apps.ui.component.AppIconProvider
import com.desklauncher.feature.apps.ui.component.AppRow
import com.desklauncher.feature.apps.ui.preview.AppsPreviewData
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch

@Composable
fun AppsRoute(
    onBack: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AppsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val iconLoader = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, AppIconEntryPoint::class.java).appIconLoader()
    }
    AppsScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                is AppsAction.Launch -> if (viewModel.launch(action.app)) onBack()
                AppsAction.OpenStore -> onOpenStore()
                AppsAction.OpenSettings -> onOpenSettings()
                AppsAction.Back -> onBack()
                else -> viewModel.onAction(action)
            }
        },
        iconProvider = iconLoader::load,
        modifier = modifier,
    )
}

@Composable
fun AppsScreen(
    state: AppsUiState,
    onAction: (AppsAction) -> Unit,
    modifier: Modifier = Modifier,
    iconProvider: AppIconProvider = { _, _ -> null },
    scrubState: IndexScrubState = rememberIndexScrubState(),
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val launchFailedMessage = stringResource(R.string.apps_launch_failed)
    LaunchedEffect(state.launchFailed) {
        if (state.launchFailed) {
            snackbarHostState.showSnackbar(launchFailedMessage)
            onAction(AppsAction.DismissLaunchError)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            AppsSearchBar(
                query = state.query,
                autoFocus = state.autoFocusSearch && !state.isLoading,
                onAction = onAction,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding(),
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.sections.isEmpty() -> DeskEmptyState(
                    title = stringResource(if (state.isSearching) R.string.apps_no_result_title else R.string.apps_empty_title),
                    body = if (state.isSearching) {
                        stringResource(R.string.apps_no_result_body, state.query)
                    } else {
                        stringResource(R.string.apps_empty_body)
                    },
                    icon = Icons.Outlined.Search,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                else -> AppList(state, iconProvider, scrubState, onAction)
            }
        }
    }
}

@Composable
private fun AppsSearchBar(query: String, autoFocus: Boolean, onAction: (AppsAction) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(autoFocus) {
        if (autoFocus) focusRequester.requestFocus()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { onAction(AppsAction.QueryChanged(it)) },
            placeholder = { Text(stringResource(R.string.apps_search_placeholder)) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onAction(AppsAction.QueryChanged("")) }) {
                        Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.apps_search_clear))
                    }
                }
            },
            singleLine = true,
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
        )
        IconButton(onClick = { onAction(AppsAction.OpenStore) }) {
            Icon(Icons.Outlined.Inventory2, contentDescription = stringResource(R.string.apps_open_store))
        }
        IconButton(onClick = { onAction(AppsAction.OpenSettings) }) {
            Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.apps_open_settings))
        }
    }
}

@Composable
private fun AppList(
    state: AppsUiState,
    iconProvider: AppIconProvider,
    scrubState: IndexScrubState,
    onAction: (AppsAction) -> Unit,
) {
    val listState = rememberLazyListState()
    // 각 섹션 머리말의 목록 위치 (인덱스 스트립 → 목록 이동용)
    val headerPositions = remember(state.sections) {
        state.sections.runningFold(0) { position, section -> position + 1 + section.apps.size }.dropLast(1)
    }
    JumpToSectionWhileScrubbing(scrubState, listState, headerPositions)
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            // 글자 스트립이 가장자리에서 안쪽으로 들어온 만큼 목록 오른쪽을 비운다 (뒤로가기 제스처 회피, SHELL-07)
            contentPadding = PaddingValues(end = if (state.isSearching) 0.dp else DeskMotion.LetterStripReservedWidth, bottom = 16.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            state.sections.forEach { section ->
                item(key = "header_${section.key}", contentType = "header") {
                    Text(
                        text = if (section.isFavorites) stringResource(R.string.apps_favorites_section) else section.key,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
                            .semantics { heading() },
                    )
                }
                // 즐겨찾기 앱은 자기 글자 섹션에도 있으므로 키를 구분한다.
                items(
                    section.apps,
                    key = { if (section.isFavorites) "fav_${it.componentKey}" else it.componentKey },
                    contentType = { "app" },
                ) { app ->
                    AppRow(
                        app = app,
                        iconProvider = iconProvider,
                        onClick = { onAction(AppsAction.Launch(app)) },
                        onMenuAction = { menu -> onAction(menu.toAction(app)) },
                    )
                }
            }
        }
        if (!state.isSearching) {
            IndexLetterStrip(
                letters = state.indexKeys,
                state = scrubState,
                // 스크럽 중에는 JumpToSectionWhileScrubbing이 이미 옮긴다. TalkBack 두 번 탭 등 직접 선택용.
                onOpen = { index -> headerPositions.getOrNull(index)?.let { scope.launch { listState.scrollToItem(it) } } },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight(0.85f),
            )
        }
    }
}

private fun AppMenuAction.toAction(app: InstalledApp): AppsAction = when (this) {
    AppMenuAction.ToggleFavorite -> AppsAction.SetFavorite(app, !app.isFavorite)
    AppMenuAction.ToggleHidden -> AppsAction.SetHidden(app, !app.isHidden)
    AppMenuAction.AppInfo -> AppsAction.OpenAppInfo(app)
    AppMenuAction.Uninstall -> AppsAction.Uninstall(app)
}

/** 스크럽 중 포커스된 글자의 섹션으로 즉시 이동 (Niagara 방식). */
@Composable
private fun JumpToSectionWhileScrubbing(
    scrubState: IndexScrubState,
    listState: LazyListState,
    headerPositions: List<Int>,
) {
    LaunchedEffect(scrubState, listState, headerPositions) {
        snapshotFlow { scrubState.focusedIndex to scrubState.phase }.collect { (index, phase) ->
            if (phase != ScrubPhase.Idle) {
                headerPositions.getOrNull(index)?.let { listState.scrollToItem(it) }
            }
        }
    }
}

@DeskPreviews
@Composable
private fun AppsScreenPreview() {
    DeskTheme { AppsScreen(state = AppsPreviewData.loaded, onAction = {}) }
}

@DeskPreviews
@Composable
private fun AppsScreenScrubbingPreview() {
    DeskTheme {
        AppsScreen(
            state = AppsPreviewData.loaded,
            onAction = {},
            scrubState = rememberIndexScrubState(initialFocus = 6f, initialPhase = ScrubPhase.Scrubbing),
        )
    }
}

@DeskPreviews
@Composable
private fun AppsScreenSearchingPreview() {
    DeskTheme { AppsScreen(state = AppsPreviewData.searching, onAction = {}) }
}

@DeskPreviews
@Composable
private fun AppsScreenNoResultPreview() {
    DeskTheme { AppsScreen(state = AppsPreviewData.noResult, onAction = {}) }
}

@DeskPreviews
@Composable
private fun AppsScreenLoadingPreview() {
    DeskTheme { AppsScreen(state = AppsPreviewData.loading, onAction = {}) }
}
