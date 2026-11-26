package com.desklauncher.feature.todo.ui

import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.todo.R
import com.desklauncher.feature.todo.domain.TodoFilter
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.ui.component.QuickAddBar
import com.desklauncher.feature.todo.ui.component.TodoEditorSheet
import com.desklauncher.feature.todo.ui.component.TodoMeta
import com.desklauncher.feature.todo.ui.component.TodoRow
import com.desklauncher.feature.todo.ui.preview.TodoPreviewData
import java.time.LocalDate

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
    today: LocalDate = LocalDate.now(),
) {
    val context = LocalContext.current
    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }
    val words = DueLabelFormatter.RelativeWords(
        stringResource(R.string.todo_due_yesterday),
        stringResource(R.string.todo_due_today),
        stringResource(R.string.todo_due_tomorrow),
    )
    val locale = LocalConfiguration.current.locales[0]
    val projectsById = remember(state.projects) { state.projects.associateBy { it.id } }
    val rowMeta: @Composable (Todo) -> Unit = { todo ->
        TodoMeta(
            due = todo.dueDate?.let { DueLabelFormatter.format(it, todo.dueTime, today, is24Hour, locale, words) },
            priority = todo.priority,
            project = todo.projectId?.let(projectsById::get),
        )
    }
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            TodoFilterRow(selected = state.filter, counts = state.counts, onSelect = { onAction(TodoAction.SelectFilter(it)) })
            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.items.isEmpty() && state.done.isEmpty() -> {
                        val (title, body) = emptyText(state.filter)
                        DeskEmptyState(
                            title = stringResource(title),
                            body = stringResource(body),
                            icon = Icons.Outlined.Checklist,
                            modifier = Modifier.align(Alignment.Center).padding(32.dp),
                        )
                    }
                    else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 8.dp)) {
                        items(state.items, key = { it.id }) { todo ->
                            TodoListRow(todo, onAction, rowMeta, Modifier.animateItem())
                        }
                        if (state.done.isNotEmpty()) {
                            item(key = "done_header") {
                                DoneSectionHeader(
                                    count = state.done.size,
                                    expanded = state.showDone,
                                    onToggle = { onAction(TodoAction.ToggleDoneSection) },
                                    modifier = Modifier.animateItem(),
                                )
                            }
                            if (state.showDone) {
                                items(state.done, key = { it.id }) { todo ->
                                    TodoListRow(todo, onAction, rowMeta, Modifier.animateItem())
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    state.editing?.let { todo ->
        TodoEditorSheet(
            todo = todo,
            projects = state.projects,
            today = today,
            is24Hour = is24Hour,
            onSave = { onAction(TodoAction.Save(it)) },
            onDelete = { onAction(TodoAction.Delete(todo.id)) },
            onDismiss = { onAction(TodoAction.DismissEditor) },
        )
    }
}

@Composable
private fun TodoListRow(
    todo: Todo,
    onAction: (TodoAction) -> Unit,
    meta: @Composable (Todo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        TodoRow(
            todo = todo,
            onClick = { onAction(TodoAction.OpenEditor(todo.id)) },
            onDoneChange = { done -> onAction(TodoAction.SetDone(todo.id, done)) },
            meta = { meta(todo) },
        )
        HorizontalDivider()
    }
}

@Composable
private fun DoneSectionHeader(count: Int, expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    val toggleLabel = stringResource(R.string.todo_done_section_toggle)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClickLabel = toggleLabel, onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore, contentDescription = null)
        Text(stringResource(R.string.todo_done_section, count), style = MaterialTheme.typography.titleSmall)
    }
}

@Composable
private fun TodoFilterRow(selected: TodoFilter, counts: Map<TodoFilter, Int>, onSelect: (TodoFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TodoFilter.entries.forEach { filter ->
            val label = stringResource(filter.labelRes())
            FilterChip(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                label = { Text(stringResource(R.string.todo_filter_with_count, label, counts[filter] ?: 0)) },
            )
        }
    }
}

@StringRes
private fun TodoFilter.labelRes(): Int = when (this) {
    TodoFilter.Today -> R.string.todo_filter_today
    TodoFilter.Upcoming -> R.string.todo_filter_upcoming
    TodoFilter.All -> R.string.todo_filter_all
    TodoFilter.Done -> R.string.todo_filter_done
}

private fun emptyText(filter: TodoFilter): Pair<Int, Int> = when (filter) {
    TodoFilter.Today -> R.string.todo_empty_today_title to R.string.todo_empty_today_body
    TodoFilter.Upcoming -> R.string.todo_empty_upcoming_title to R.string.todo_empty_upcoming_body
    TodoFilter.All -> R.string.todo_empty_title to R.string.todo_empty_body
    TodoFilter.Done -> R.string.todo_empty_done_title to R.string.todo_empty_done_body
}

private val ToolColor = DeskTabColor.Orange

@DeskPreviews
@Composable
private fun TodoScreenPreview() {
    DeskTheme { TodoScreen(state = TodoPreviewData.loaded, onAction = {}, today = TodoPreviewData.today) }
}

@DeskPreviews
@Composable
private fun TodoScreenDoneExpandedPreview() {
    DeskTheme { TodoScreen(state = TodoPreviewData.loaded.copy(filter = TodoFilter.All, showDone = true), onAction = {}, today = TodoPreviewData.today) }
}

@DeskPreviews
@Composable
private fun TodoScreenTodayPreview() {
    DeskTheme { TodoScreen(state = TodoPreviewData.todayView, onAction = {}, today = TodoPreviewData.today) }
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
