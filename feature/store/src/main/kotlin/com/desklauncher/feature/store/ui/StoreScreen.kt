package com.desklauncher.feature.store.ui

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.haptics.rememberDeskHaptics
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.store.R
import com.desklauncher.feature.store.ui.component.ReorderableContentType
import com.desklauncher.feature.store.ui.component.StoreToolRow
import com.desklauncher.feature.store.ui.component.TabColorDialog
import com.desklauncher.feature.store.ui.component.rememberReorderState
import com.desklauncher.feature.store.ui.preview.StorePreviewData

@Composable
fun StoreRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StoreViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StoreScreen(
        state = uiState,
        onAction = { action -> if (action == StoreAction.Back) onBack() else viewModel.onAction(action) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(
    state: StoreUiState,
    onAction: (StoreAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.store_title)) },
                navigationIcon = {
                    IconButton(onClick = { onAction(StoreAction.Back) }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.store_back))
                    }
                },
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                state.tools.isEmpty() -> DeskEmptyState(
                    title = stringResource(R.string.store_empty_title),
                    body = stringResource(R.string.store_empty_body),
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                else -> StoreToolList(tools = state.tools, onAction = onAction)
            }
        }
    }

    val pickerTool = state.tools.firstOrNull { it.id == state.colorPickerToolId }
    if (pickerTool != null) {
        TabColorDialog(
            current = pickerTool.color,
            onSelect = { color -> onAction(StoreAction.SetColor(pickerTool.id, color)) },
            onDismiss = { onAction(StoreAction.DismissColorPicker) },
        )
    }
}

@Composable
private fun StoreToolList(
    tools: List<StoreToolUi>,
    onAction: (StoreAction) -> Unit,
) {
    // 드래그 중에는 화면 안에서만 순서를 바꾸고, 손을 뗄 때 한 번 저장한다.
    var localOrder by remember(tools) { mutableStateOf(tools) }
    val listState = rememberLazyListState()
    val haptics = rememberDeskHaptics()
    val reorder = rememberReorderState(listState) { fromKey, toKey ->
        val from = localOrder.indexOfFirst { it.id == fromKey }
        val to = localOrder.indexOfFirst { it.id == toKey }
        if (from >= 0 && to >= 0) {
            localOrder = localOrder.toMutableList().apply { add(to, removeAt(from)) }
            haptics.segmentTick()
        }
    }
    val moveUp = stringResource(R.string.store_move_up)
    val moveDown = stringResource(R.string.store_move_down)

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "guide", contentType = "guide") {
            Text(
                text = stringResource(R.string.store_guide),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            )
        }
        items(localOrder, key = { it.id }, contentType = { ReorderableContentType }) { tool ->
            val isDragging = reorder.draggingKey == tool.id
            val index = localOrder.indexOf(tool)
            StoreToolRow(
                tool = tool,
                isDragging = isDragging,
                onToggle = { enabled -> onAction(StoreAction.SetEnabled(tool.id, enabled)) },
                onColorClick = { onAction(StoreAction.OpenColorPicker(tool.id)) },
                handleModifier = Modifier.pointerInput(tool.id) {
                    detectDragGestures(
                        onDragStart = { reorder.start(tool.id) },
                        onDrag = { change, amount ->
                            change.consume()
                            reorder.drag(amount.y)
                        },
                        onDragEnd = {
                            reorder.end()
                            onAction(StoreAction.Reorder(localOrder.map { it.id }))
                        },
                        onDragCancel = {
                            reorder.end()
                            localOrder = tools
                        },
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(
                        if (isDragging) {
                            Modifier.zIndex(1f).graphicsLayer { translationY = reorder.dragOffset }
                        } else {
                            Modifier.animateItem()
                        },
                    )
                    .semantics {
                        customActions = buildList {
                            if (index > 0) {
                                add(CustomAccessibilityAction(moveUp) {
                                    onAction(StoreAction.Reorder(localOrder.moved(index, index - 1).map { it.id }))
                                    true
                                })
                            }
                            if (index < localOrder.lastIndex) {
                                add(CustomAccessibilityAction(moveDown) {
                                    onAction(StoreAction.Reorder(localOrder.moved(index, index + 1).map { it.id }))
                                    true
                                })
                            }
                        }
                    },
            )
            Spacer(Modifier.height(4.dp))
        }
    }
}

private fun <T> List<T>.moved(from: Int, to: Int): List<T> = toMutableList().apply { add(to, removeAt(from)) }

@DeskPreviews
@Composable
private fun StoreScreenPreview() {
    DeskTheme { StoreScreen(state = StorePreviewData.loaded, onAction = {}) }
}

@DeskPreviews
@Composable
private fun StoreScreenEmptyPreview() {
    DeskTheme { StoreScreen(state = StorePreviewData.empty, onAction = {}) }
}

@DeskPreviews
@Composable
private fun StoreScreenLoadingPreview() {
    DeskTheme { StoreScreen(state = StorePreviewData.loading, onAction = {}) }
}
