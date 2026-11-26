package com.desklauncher.feature.todo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.todo.R
import com.desklauncher.feature.todo.ui.component.QuickAddBar
import com.desklauncher.feature.todo.ui.component.TodoEditorSheet
import com.desklauncher.feature.todo.ui.component.TodoRow
import com.desklauncher.feature.todo.ui.preview.TodoPreviewData

@Composable
fun TodoRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    TodoScreen(
        state = uiState,
        onAction = { action -> if (action == TodoAction.Back) onBack() else viewModel.onAction(action) },
        modifier = modifier,
    )
}

@Composable
fun TodoScreen(
    state: TodoUiState,
    onAction: (TodoAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.todo_deleted)
    val undoLabel = stringResource(R.string.todo_undo)
    LaunchedEffect(state.recentlyDeleted) {
        if (state.recentlyDeleted != null) {
            val result = snackbarHostState.showSnackbar(deletedMessage, undoLabel, duration = SnackbarDuration.Short)
            onAction(if (result == SnackbarResult.ActionPerformed) TodoAction.UndoDelete else TodoAction.DismissUndo)
        }
    }

    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            ToolTopBar(
                title = stringResource(R.string.todo_tool_title),
                tabColor = ToolColor,
                backContentDescription = stringResource(R.string.todo_back),
                onBack = { onAction(TodoAction.Back) },
            )
        },
        bottomBar = { QuickAddBar(onAdd = { onAction(TodoAction.QuickAdd(it)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.items.isEmpty() -> DeskEmptyState(
                    title = stringResource(R.string.todo_empty_title),
                    body = stringResource(R.string.todo_empty_body),
                    icon = Icons.Outlined.Checklist,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 8.dp)) {
                    items(state.items, key = { it.id }) { todo ->
                        TodoRow(todo = todo, onClick = { onAction(TodoAction.OpenEditor(todo.id)) }, modifier = Modifier.animateItem())
                        HorizontalDivider()
                    }
                }
            }
        }
    }

    state.editing?.let { todo ->
        TodoEditorSheet(
            todo = todo,
            onSave = { onAction(TodoAction.Save(it)) },
            onDelete = { onAction(TodoAction.Delete(todo.id)) },
            onDismiss = { onAction(TodoAction.DismissEditor) },
        )
    }
}

private val ToolColor = DeskTabColor.Orange

@DeskPreviews
@Composable
private fun TodoScreenPreview() {
    DeskTheme { TodoScreen(state = TodoPreviewData.loaded, onAction = {}) }
}

@DeskPreviews
@Composable
private fun TodoScreenEmptyPreview() {
    DeskTheme { TodoScreen(state = TodoPreviewData.empty, onAction = {}) }
}

@DeskPreviews
@Composable
private fun TodoScreenLoadingPreview() {
    DeskTheme { TodoScreen(state = TodoPreviewData.loading, onAction = {}) }
}
