package com.desklauncher.feature.scanner.ui.cabinet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.scanner.R
import com.desklauncher.feature.scanner.di.ThumbnailEntryPoint
import com.desklauncher.feature.scanner.domain.model.Document
import com.desklauncher.feature.scanner.ui.component.CabinetDialogHost
import com.desklauncher.feature.scanner.ui.component.DocumentCard
import com.desklauncher.feature.scanner.ui.component.FolderFilterRow
import com.desklauncher.feature.scanner.ui.component.ThumbnailLoader
import com.desklauncher.feature.scanner.ui.preview.CabinetPreviewData
import com.desklauncher.feature.scanner.ui.scan.rememberDocumentScannerLauncher
import dagger.hilt.android.EntryPointAccessors

@Composable
fun CabinetRoute(
    onBack: () -> Unit,
    onOpenDocument: (documentId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CabinetViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val thumbnailLoader = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, ThumbnailEntryPoint::class.java).thumbnailLoader()
    }
    val launchScanner = rememberDocumentScannerLauncher(
        onScanned = { viewModel.onAction(CabinetAction.Scanned(it)) },
        onUnavailable = { viewModel.onAction(CabinetAction.ScannerUnavailable) },
    )
    CabinetScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                CabinetAction.Back -> onBack()
                is CabinetAction.Open -> onOpenDocument(action.document.id)
                else -> viewModel.onAction(action)
            }
        },
        onScan = launchScanner,
        thumbnailLoader = thumbnailLoader,
        modifier = modifier,
    )
}

@Composable
fun CabinetScreen(
    state: CabinetUiState,
    onAction: (CabinetAction) -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnailLoader: ThumbnailLoader = ThumbnailLoader { null },
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val messages = mapOf(
        CabinetMessage.Saved to stringResource(R.string.cabinet_saved),
        CabinetMessage.SaveFailed to stringResource(R.string.cabinet_save_failed),
        CabinetMessage.ScannerUnavailable to stringResource(R.string.cabinet_scanner_unavailable),
    )
    LaunchedEffect(state.message) {
        state.message?.let {
            snackbarHostState.showSnackbar(messages.getValue(it))
            onAction(CabinetAction.DismissMessage)
        }
    }
    CabinetDialogHost(dialog = state.dialog, folders = state.folders, onAction = onAction)
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = stringResource(R.string.cabinet_tool_title),
                tabColor = DeskTabColor.Brown,
                backContentDescription = stringResource(R.string.cabinet_back),
                onBack = { onAction(CabinetAction.Back) },
            )
        },
        floatingActionButton = {
            val description = stringResource(R.string.cabinet_scan_description)
            ExtendedFloatingActionButton(
                onClick = onScan,
                icon = { Icon(Icons.Outlined.DocumentScanner, contentDescription = null) },
                text = { Text(stringResource(R.string.cabinet_scan)) },
                modifier = Modifier.semantics { contentDescription = description },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            FolderFilterRow(folders = state.folders, selected = state.filter, onAction = onAction)
            if (state.isSaving) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(stringResource(R.string.cabinet_saving), modifier = Modifier.padding(16.dp))
            }
            Box(Modifier.fillMaxSize()) {
                when {
                    state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                    state.documents.isEmpty() -> DeskEmptyState(
                        title = stringResource(R.string.cabinet_empty_title),
                        body = stringResource(R.string.cabinet_empty_body),
                        icon = Icons.Outlined.DocumentScanner,
                        modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    )
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(150.dp),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 96.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.documents, key = { it.id }) { document ->
                            DocumentCard(
                                document = document,
                                thumbnailLoader = thumbnailLoader,
                                onClick = { onAction(CabinetAction.Open(document)) },
                                modifier = Modifier.animateItem(),
                                menu = { DocumentMenu(document, onAction) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DocumentMenu(document: Document, onAction: (CabinetAction) -> Unit) {
    var open by remember { mutableStateOf(false) }
    IconButton(onClick = { open = true }) {
        Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.cabinet_doc_menu, document.title), tint = Color.DarkGray)
    }
    DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
        DropdownMenuItem(text = { Text(stringResource(R.string.cabinet_doc_rename)) }, onClick = {
            open = false
            onAction(CabinetAction.ShowDialog(CabinetDialog.RenameDocument(document)))
        })
        DropdownMenuItem(text = { Text(stringResource(R.string.cabinet_doc_move)) }, onClick = {
            open = false
            onAction(CabinetAction.ShowDialog(CabinetDialog.MoveDocument(document)))
        })
        DropdownMenuItem(text = { Text(stringResource(R.string.cabinet_doc_delete), color = MaterialTheme.colorScheme.error) }, onClick = {
            open = false
            onAction(CabinetAction.ShowDialog(CabinetDialog.DeleteDocument(document)))
        })
    }
}

@DeskPreviews
@Composable
private fun CabinetScreenPreview() {
    DeskTheme { CabinetScreen(state = CabinetPreviewData.loaded, onAction = {}, onScan = {}) }
}

@DeskPreviews
@Composable
private fun CabinetScreenEmptyPreview() {
    DeskTheme { CabinetScreen(state = CabinetPreviewData.empty, onAction = {}, onScan = {}) }
}

@DeskPreviews
@Composable
private fun CabinetScreenSavingPreview() {
    DeskTheme { CabinetScreen(state = CabinetPreviewData.loaded.copy(isSaving = true), onAction = {}, onScan = {}) }
}
