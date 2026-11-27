package com.desklauncher.feature.doodle.ui.list

import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.common.dispatcher.DeskDispatchers
import com.desklauncher.core.common.dispatcher.Dispatcher
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskDoodlePaper
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.storage.DeskStorage
import com.desklauncher.feature.doodle.R
import com.desklauncher.feature.doodle.domain.model.Doodle
import com.desklauncher.feature.doodle.domain.repository.DoodleRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Immutable
data class DoodleListUiState(val isLoading: Boolean = true, val doodles: List<Doodle> = emptyList())

/** 낙서 목록 (DOODLE-03) */
@HiltViewModel
class DoodleListViewModel @Inject constructor(private val repository: DoodleRepository) : ViewModel() {
    val uiState: StateFlow<DoodleListUiState> = repository.doodles
        .map { DoodleListUiState(isLoading = false, doodles = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DoodleListUiState())

    fun delete(id: String) {
        viewModelScope.launch { repository.delete(id) }
    }
}

/** 목록 썸네일 디코더 — 화면 크기 PNG를 1/4로 줄여 읽는다 */
class DoodleImageLoader @Inject internal constructor(
    private val storage: DeskStorage,
    @Dispatcher(DeskDispatchers.IO) private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun load(relativePath: String): ImageBitmap? = withContext(ioDispatcher) {
        storage.resolve(relativePath)?.takeIf { it.exists() }?.let { file ->
            BitmapFactory.decodeFile(file.path, BitmapFactory.Options().apply { inSampleSize = 4 })?.asImageBitmap()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface DoodleImageEntryPoint {
    fun imageLoader(): DoodleImageLoader
}

@Composable
fun DoodleListRoute(
    onBack: () -> Unit,
    onOpen: (doodleId: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DoodleListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val loader = remember(context) { EntryPointAccessors.fromApplication(context.applicationContext, DoodleImageEntryPoint::class.java).imageLoader() }
    DoodleListScreen(
        state = uiState,
        onBack = onBack,
        onOpen = onOpen,
        onDelete = viewModel::delete,
        imageLoader = loader::load,
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DoodleListScreen(
    state: DoodleListUiState,
    onBack: () -> Unit,
    onOpen: (doodleId: String?) -> Unit,
    onDelete: (String) -> Unit,
    modifier: Modifier = Modifier,
    imageLoader: suspend (String) -> ImageBitmap? = { null },
) {
    var confirmDelete by rememberSaveable { mutableStateOf<String?>(null) }
    val locale = LocalConfiguration.current.locales[0]
    val formatter = remember(locale) { DateTimeFormatter.ofPattern("M월 d일 HH:mm", locale) }
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = stringResource(R.string.doodle_tool_title),
                tabColor = DeskTabColor.Purple,
                backContentDescription = stringResource(R.string.doodle_back),
                onBack = onBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onOpen(null) }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.doodle_new))
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.doodles.isEmpty() -> DeskEmptyState(
                    title = stringResource(R.string.doodle_empty_title),
                    body = stringResource(R.string.doodle_empty_body),
                    icon = Icons.Outlined.Brush,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(150.dp),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.doodles, key = { it.id }) { doodle ->
                        val image by produceState<ImageBitmap?>(null, doodle.imagePath, doodle.updatedAt) { value = imageLoader(doodle.imagePath) }
                        val label = stringResource(R.string.doodle_item, doodle.updatedAt.atZone(ZoneId.systemDefault()).format(formatter))
                        val deleteLabel = stringResource(R.string.doodle_delete)
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DeskTheme.colors.paper),
                            border = BorderStroke(DeskLine.Hairline, DeskTheme.colors.line),
                            modifier = Modifier
                                .animateItem()
                                .combinedClickable(onClick = { onOpen(doodle.id) }, onLongClick = { confirmDelete = doodle.id })
                                .semantics {
                                    contentDescription = label
                                    customActions = listOf(CustomAccessibilityAction(deleteLabel) { confirmDelete = doodle.id; true })
                                },
                        ) {
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(doodle.canvasWidth.coerceAtLeast(1).toFloat() / doodle.canvasHeight.coerceAtLeast(1))
                                    .background(DeskDoodlePaper),
                            ) {
                                image?.let { Image(it, contentDescription = null, contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize()) }
                            }
                            Text(
                                doodle.updatedAt.atZone(ZoneId.systemDefault()).format(formatter),
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(8.dp),
                            )
                        }
                    }
                }
            }
        }
    }
    confirmDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            title = { Text(stringResource(R.string.doodle_delete_title)) },
            confirmButton = {
                TextButton(onClick = { onDelete(id); confirmDelete = null }) {
                    Text(stringResource(R.string.doodle_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = null }) { Text(stringResource(R.string.doodle_cancel)) } },
        )
    }
}

@DeskPreviews
@Composable
private fun DoodleListScreenPreview() {
    val now = Instant.parse("2026-11-26T06:00:00Z")
    DeskTheme {
        DoodleListScreen(
            state = DoodleListUiState(false, listOf(Doodle("1", "", "doodles/1.png", 1080, 1800, now, now), Doodle("2", "", "doodles/2.png", 1080, 1800, now, now))),
            onBack = {}, onOpen = {}, onDelete = {},
        )
    }
}

@DeskPreviews
@Composable
private fun DoodleListScreenEmptyPreview() {
    DeskTheme { DoodleListScreen(state = DoodleListUiState(false), onBack = {}, onOpen = {}, onDelete = {}) }
}
