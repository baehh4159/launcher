package com.desklauncher.feature.scanner.ui.viewer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.scanner.R
import com.desklauncher.feature.scanner.di.ThumbnailEntryPoint
import com.desklauncher.feature.scanner.domain.model.Document
import com.desklauncher.feature.scanner.domain.repository.DocumentRepository
import com.desklauncher.feature.scanner.navigation.DocumentViewerDestination
import com.desklauncher.feature.scanner.ui.preview.CabinetPreviewData
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Immutable
data class DocumentViewerUiState(val isLoading: Boolean = true, val document: Document? = null)

@HiltViewModel
class DocumentViewerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: DocumentRepository,
) : ViewModel() {
    private val id = savedStateHandle.toRoute<DocumentViewerDestination>().documentId
    val uiState: StateFlow<DocumentViewerUiState> = repository.document(id)
        .map { DocumentViewerUiState(isLoading = false, document = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DocumentViewerUiState())
}

/** 페이지 그리기 함수. Preview에서는 null → 빈 종이. */
fun interface PageRenderer {
    suspend fun render(index: Int, widthPx: Int): ImageBitmap?
}

@Composable
fun DocumentViewerRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DocumentViewerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val support = remember(context) {
        EntryPointAccessors.fromApplication(context.applicationContext, ThumbnailEntryPoint::class.java).pdfSupport()
    }
    val path = uiState.document?.pdfPath
    // null = 여는 중, Failed = 파일 없음/손상
    val opened by produceState<PdfOpenState?>(null, path) {
        value = path?.let { support.open(it) }?.let(PdfOpenState::Opened) ?: PdfOpenState.Failed
    }
    val handle = (opened as? PdfOpenState.Opened)?.handle
    DisposableEffect(handle) { onDispose { handle?.close() } }

    DocumentViewerScreen(
        state = uiState,
        pageCount = handle?.pageCount ?: 0,
        isMissing = !uiState.isLoading && (uiState.document == null || opened == PdfOpenState.Failed),
        renderer = { index, width -> handle?.render(index, width) },
        onShare = {
            val document = uiState.document ?: return@DocumentViewerScreen
            support.shareIntent(document.pdfPath, document.title)?.let(context::startActivity)
        },
        onBack = onBack,
        modifier = modifier,
    )
}

private sealed interface PdfOpenState {
    data class Opened(val handle: PdfHandle) : PdfOpenState
    data object Failed : PdfOpenState
}

@Composable
fun DocumentViewerScreen(
    state: DocumentViewerUiState,
    pageCount: Int,
    isMissing: Boolean,
    renderer: PageRenderer,
    onShare: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val currentPage by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = state.document?.title.orEmpty(),
                tabColor = DeskTabColor.Brown,
                backContentDescription = stringResource(R.string.cabinet_back),
                onBack = onBack,
                actions = {
                    if (state.document != null) {
                        IconButton(onClick = onShare) {
                            Icon(Icons.Outlined.Share, contentDescription = stringResource(R.string.cabinet_viewer_share))
                        }
                    }
                },
            )
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                isMissing -> Text(stringResource(R.string.cabinet_viewer_missing), modifier = Modifier.align(Alignment.Center))
                else -> {
                    BoxWithConstraints(Modifier.fillMaxSize()) {
                        val widthPx = with(LocalDensity.current) { (maxWidth - 24.dp).roundToPx() }
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            items(count = pageCount, key = { it }) { index ->
                                PdfPage(index = index, widthPx = widthPx, renderer = renderer)
                            }
                        }
                    }
                    if (pageCount > 0) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.inverseSurface,
                            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        ) {
                            Text(
                                stringResource(R.string.cabinet_viewer_page, currentPage.coerceAtMost(pageCount), pageCount),
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPage(index: Int, widthPx: Int, renderer: PageRenderer) {
    val bitmap by produceState<ImageBitmap?>(null, index, widthPx) { value = renderer.render(index, widthPx) }
    val image = bitmap
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().aspectRatio(image.width.toFloat() / image.height),
        )
    } else {
        Box(Modifier.fillMaxWidth().aspectRatio(210f / 297f).background(Color.White))
    }
}

@DeskPreviews
@Composable
private fun DocumentViewerScreenPreview() {
    DeskTheme {
        DocumentViewerScreen(
            state = DocumentViewerUiState(isLoading = false, document = CabinetPreviewData.documents[2]),
            pageCount = 3,
            isMissing = false,
            renderer = { _, _ -> null },
            onShare = {},
            onBack = {},
        )
    }
}

@DeskPreviews
@Composable
private fun DocumentViewerScreenMissingPreview() {
    DeskTheme {
        DocumentViewerScreen(DocumentViewerUiState(isLoading = false), 0, isMissing = true, renderer = { _, _ -> null }, onShare = {}, onBack = {})
    }
}
