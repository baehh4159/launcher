package com.desklauncher.feature.memo.ui.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.memo.R
import com.desklauncher.feature.memo.domain.model.Project
import com.desklauncher.feature.memo.ui.component.ProjectEditorDialog
import com.desklauncher.feature.memo.ui.component.ProjectRow
import com.desklauncher.feature.memo.ui.preview.MemoPreviewData

@Composable
fun ProjectListRoute(
    onBack: () -> Unit,
    onOpenProject: (projectId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProjectListScreen(
        state = uiState,
        subtitle = { project -> stringResource(R.string.memo_count, uiState.memoCounts[project.id] ?: 0) },
        onAction = { action ->
            when (action) {
                ProjectListAction.Back -> onBack()
                is ProjectListAction.Open -> onOpenProject(action.project.id)
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun ProjectListScreen(
    state: ProjectListUiState,
    onAction: (ProjectListAction) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: @Composable (Project) -> String? = { null },
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = stringResource(R.string.memo_tool_title),
                tabColor = DeskTabColor.Yellow,
                backContentDescription = stringResource(R.string.memo_back),
                onBack = { onAction(ProjectListAction.Back) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(ProjectListAction.Add) }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.memo_project_add))
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.active.isEmpty() && state.archived.isEmpty() -> DeskEmptyState(
                    title = stringResource(R.string.memo_projects_empty_title),
                    body = stringResource(R.string.memo_projects_empty_body),
                    icon = Icons.AutoMirrored.Outlined.StickyNote2,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(state.active, key = { it.id }) { project ->
                        ProjectListItem(project, subtitle(project), onAction, Modifier.animateItem())
                    }
                    if (state.archived.isNotEmpty()) {
                        item(key = "archived_header") {
                            Text(
                                text = stringResource(R.string.memo_archived_section),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 4.dp).semantics { heading() },
                            )
                        }
                        items(state.archived, key = { it.id }) { project ->
                            ProjectListItem(project, subtitle(project), onAction, Modifier.animateItem())
                        }
                    }
                }
            }
        }
    }

    state.editor?.let { editor ->
        ProjectEditorDialog(
            editor = editor,
            onSave = { name, color -> onAction(ProjectListAction.SaveEditor(name, color)) },
            onDismiss = { onAction(ProjectListAction.DismissEditor) },
        )
    }
    state.confirmDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { onAction(ProjectListAction.DismissDelete) },
            title = { Text(stringResource(R.string.memo_project_delete_title)) },
            text = { Text(stringResource(R.string.memo_project_delete_body, project.name)) },
            confirmButton = {
                TextButton(onClick = { onAction(ProjectListAction.ConfirmDelete) }) {
                    Text(stringResource(R.string.memo_project_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { onAction(ProjectListAction.DismissDelete) }) { Text(stringResource(R.string.memo_cancel)) } },
        )
    }
}

@Composable
private fun ProjectListItem(project: Project, subtitle: String?, onAction: (ProjectListAction) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier) {
        ProjectRow(
            project = project,
            subtitle = subtitle,
            onClick = { onAction(ProjectListAction.Open(project)) },
            onEdit = { onAction(ProjectListAction.Edit(project)) },
            onToggleArchive = { onAction(ProjectListAction.SetArchived(project, !project.archived)) },
            onDelete = { onAction(ProjectListAction.RequestDelete(project)) },
        )
        HorizontalDivider()
    }
}

@DeskPreviews
@Composable
private fun ProjectListScreenPreview() {
    DeskTheme { ProjectListScreen(state = MemoPreviewData.projectList, onAction = {}) }
}

@DeskPreviews
@Composable
private fun ProjectListScreenEmptyPreview() {
    DeskTheme { ProjectListScreen(state = MemoPreviewData.projectListEmpty, onAction = {}) }
}
