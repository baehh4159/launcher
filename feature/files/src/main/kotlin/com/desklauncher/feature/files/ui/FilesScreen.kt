package com.desklauncher.feature.files.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.desklauncher.feature.files.R
import com.desklauncher.feature.files.domain.model.FileCategory
import com.desklauncher.feature.files.domain.model.FileItem
import com.desklauncher.feature.files.ui.component.FileRow
import dagger.hilt.android.EntryPointAccessors
import java.time.Instant

@Composable
fun FilesRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FilesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // 사용자가 고른 파일만 읽는다 (MANAGE_EXTERNAL_STORAGE 미사용, PRD §5.10)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { viewModel.onAction(FilesAction.AddBookmark(it.toString())) }
    }
    val context = LocalContext.current
    val intents = remember(context) { EntryPointAccessors.fromApplication(context.applicationContext, FileIntentsEntryPoint::class.java).fileIntents() }
    val launch: (Intent?) -> Unit = { intent ->
        try {
            if (intent == null) viewModel.onAction(FilesAction.OpenFailed) else context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            viewModel.onAction(FilesAction.OpenFailed)
        }
    }
    FilesScreen(
        state = uiState,
        onAddBookmark = { picker.launch(arrayOf("*/*")) },
        onOpen = { launch(intents.open(it)) },
        rowTrailing = { file ->
            FileMenu(file = file, onShare = { launch(intents.share(file)) }, onRemove = { viewModel.onAction(FilesAction.Remove(file)) })
        },
        onAction = { if (it == FilesAction.Back) onBack() else viewModel.onAction(it) },
        modifier = modifier,
    )
}

@Composable
fun FilesScreen(
    state: FilesUiState,
    onAction: (FilesAction) -> Unit,
    modifier: Modifier = Modifier,
    onAddBookmark: () -> Unit = {},
    onOpen: (FileItem) -> Unit = {},
    rowTrailing: @Composable (FileItem) -> Unit = {},
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val failedMessage = stringResource(R.string.files_add_failed)
    val openFailedMessage = stringResource(R.string.files_open_failed)
    LaunchedEffect(state.addFailed, state.openFailed) {
        if (state.addFailed || state.openFailed) {
            snackbarHostState.showSnackbar(if (state.addFailed) failedMessage else openFailedMessage)
            onAction(FilesAction.DismissError)
        }
    }
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state.category == FileCategory.Bookmarks) {
                FloatingActionButton(onClick = onAddBookmark) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.files_add_bookmark))
                }
            }
        },
        topBar = {
            ToolTopBar(
                title = stringResource(R.string.files_tool_title),
                tabColor = DeskTabColor.Gray,
                backContentDescription = stringResource(R.string.files_back),
                onBack = { onAction(FilesAction.Back) },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FileCategory.entries.forEach { category ->
                    FilterChip(
                        selected = state.category == category,
                        onClick = { onAction(FilesAction.SelectCategory(category)) },
                        label = { Text(stringResource(category.labelRes())) },
                    )
                }
            }
            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.files.isEmpty() -> DeskEmptyState(
                        title = stringResource(R.string.files_empty_title),
                        body = stringResource(state.category.emptyRes()),
                        icon = Icons.AutoMirrored.Outlined.InsertDriveFile,
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    )
                    else -> LazyColumn(Modifier.fillMaxSize()) {
                        items(state.files, key = { "${it.category}_${it.id}" }) { file ->
                            Column(Modifier.animateItem()) {
                                FileRow(file = file, onClick = { onOpen(file) }, trailing = { rowTrailing(file) })
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 공유 · (꺼내 둔 파일만) 목록에서 빼기 */
@Composable
private fun FileMenu(file: FileItem, onShare: () -> Unit, onRemove: () -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { open = true }) {
            Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.files_menu, file.name))
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.files_share)) }, onClick = { open = false; onShare() })
            if (file.category == FileCategory.Bookmarks) {
                DropdownMenuItem(text = { Text(stringResource(R.string.files_remove)) }, onClick = { open = false; onRemove() })
            }
        }
    }
}

internal fun FileCategory.labelRes(): Int = when (this) {
    FileCategory.Scans -> R.string.files_category_scans
    FileCategory.Doodles -> R.string.files_category_doodles
    FileCategory.Bookmarks -> R.string.files_category_bookmarks
}

internal fun FileCategory.emptyRes(): Int = when (this) {
    FileCategory.Scans -> R.string.files_empty_scans
    FileCategory.Doodles -> R.string.files_empty_doodles
    FileCategory.Bookmarks -> R.string.files_empty_bookmarks
}

@DeskPreviews
@Composable
private fun FilesScreenPreview() {
    val now = Instant.parse("2026-11-26T06:00:00Z")
    DeskTheme {
        FilesScreen(
            state = FilesUiState(
                isLoading = false,
                files = listOf(
                    FileItem("1", "전세 계약서.pdf", FileCategory.Scans, "documents/1.pdf", "application/pdf", 1_240_000, now),
                    FileItem("2", "관리비 고지서.pdf", FileCategory.Scans, "documents/2.pdf", "application/pdf", 320_000, now),
                ),
            ),
            onAction = {},
        )
    }
}

@DeskPreviews
@Composable
private fun FilesScreenEmptyPreview() {
    DeskTheme { FilesScreen(state = FilesUiState(isLoading = false, category = FileCategory.Doodles), onAction = {}) }
}

@DeskPreviews
@Composable
private fun FilesScreenBookmarksEmptyPreview() {
    DeskTheme { FilesScreen(state = FilesUiState(isLoading = false, category = FileCategory.Bookmarks), onAction = {}) }
}
