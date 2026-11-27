package com.desklauncher.feature.memo.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.toolTabColor
import com.desklauncher.feature.memo.R
import com.desklauncher.feature.memo.domain.model.Memo
import com.desklauncher.feature.memo.ui.preview.MemoPreviewData
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.desklauncher.feature.memo.MemoToolId

@Composable
fun ProjectDetailRoute(
    onBack: () -> Unit,
    onOpenMemo: (projectId: String, memoId: String?) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProjectDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    ProjectDetailScreen(
        state = uiState,
        onBack = onBack,
        onOpenMemo = { memoId -> onOpenMemo(viewModel.projectId, memoId) },
        modifier = modifier,
    )
}

/** 한 프로젝트의 메모 목록 (MEMO-02). */
@Composable
fun ProjectDetailScreen(
    state: ProjectDetailUiState,
    onBack: () -> Unit,
    onOpenMemo: (memoId: String?) -> Unit,
    modifier: Modifier = Modifier,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val color = state.project?.colorName?.let(DeskTabColor::fromNameOrNull) ?: toolTabColor(MemoToolId, DeskTabColor.Yellow)
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = state.project?.name ?: stringResource(R.string.memo_tool_title),
                tabColor = color,
                backContentDescription = stringResource(R.string.memo_back),
                onBack = onBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onOpenMemo(null) }) {
                Icon(Icons.Outlined.Edit, contentDescription = stringResource(R.string.memo_add))
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.memos.isEmpty() -> DeskEmptyState(
                    title = stringResource(R.string.memo_empty_title),
                    body = stringResource(R.string.memo_empty_body),
                    icon = Icons.Outlined.Edit,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(state.memos, key = { it.id }) { memo ->
                        MemoRow(memo = memo, zone = zone, onClick = { onOpenMemo(memo.id) }, modifier = Modifier.animateItem())
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoRow(memo: Memo, zone: ZoneId, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val updated = memo.updatedAt.atZone(zone).format(DateTimeFormatter.ofPattern("M월 d일 HH:mm", locale))
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (memo.pinned) Icon(Icons.Outlined.PushPin, contentDescription = stringResource(R.string.memo_editor_pin), modifier = Modifier.size(16.dp))
            Text(
                text = memo.displayTitle.ifBlank { stringResource(R.string.memo_untitled) },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val preview = memo.body.lineSequence().filter { it.isNotBlank() }.drop(if (memo.title.isBlank()) 1 else 0).firstOrNull()
        if (preview != null) {
            Text(preview, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Text(stringResource(R.string.memo_updated_at, updated), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@DeskPreviews
@Composable
private fun ProjectDetailScreenPreview() {
    DeskTheme { ProjectDetailScreen(state = MemoPreviewData.detail, onBack = {}, onOpenMemo = {}, zone = ZoneId.of("Asia/Seoul")) }
}

@DeskPreviews
@Composable
private fun ProjectDetailScreenEmptyPreview() {
    DeskTheme { ProjectDetailScreen(state = MemoPreviewData.detail.copy(memos = emptyList()), onBack = {}, onOpenMemo = {}) }
}
