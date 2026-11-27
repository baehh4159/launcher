package com.desklauncher.feature.memo.ui.editor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.toolTabColor
import com.desklauncher.feature.memo.R
import com.desklauncher.feature.memo.MemoToolId

@Composable
fun MemoEditorRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MemoEditorViewModel = hiltViewModel(),
) {
    MemoEditorScreen(
        isLoading = viewModel.isLoading,
        title = viewModel.title,
        body = viewModel.body,
        pinned = viewModel.pinned,
        isSaved = viewModel.isSaved,
        onTitleChange = viewModel::onTitleChange,
        onBodyChange = viewModel::onBodyChange,
        onTogglePin = viewModel::togglePin,
        onDelete = { viewModel.delete(onBack) },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun MemoEditorScreen(
    isLoading: Boolean,
    title: String,
    body: String,
    pinned: Boolean,
    isSaved: Boolean,
    onTitleChange: (String) -> Unit,
    onBodyChange: (String) -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            ToolTopBar(
                title = if (isSaved) stringResource(R.string.memo_editor_saved) else "",
                tabColor = toolTabColor(MemoToolId, DeskTabColor.Yellow),
                backContentDescription = stringResource(R.string.memo_back),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onTogglePin) {
                        Icon(
                            Icons.Outlined.PushPin,
                            contentDescription = stringResource(if (pinned) R.string.memo_editor_unpin else R.string.memo_editor_pin),
                            tint = if (pinned) MaterialTheme.colorScheme.primary else toolTabColor(MemoToolId, DeskTabColor.Yellow).onContainer,
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.memo_editor_delete))
                    }
                },
            )
        },
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding)) { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
            return@Scaffold
        }
        val plain = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        )
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            TextField(
                value = title,
                onValueChange = onTitleChange,
                placeholder = { Text(stringResource(R.string.memo_editor_title_hint), style = MaterialTheme.typography.headlineSmall) },
                textStyle = MaterialTheme.typography.headlineSmall,
                colors = plain,
                modifier = Modifier.fillMaxWidth(),
            )
            TextField(
                value = body,
                onValueChange = onBodyChange,
                placeholder = { Text(stringResource(R.string.memo_editor_body_hint)) },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = plain,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            )
        }
    }
}

@DeskPreviews
@Composable
private fun MemoEditorScreenPreview() {
    DeskTheme {
        MemoEditorScreen(
            isLoading = false,
            title = "서류철 스트립 메모",
            body = "엣지 패널과 겹침 → 그립 위치 '가운데'가 제일 편함\n햅틱 간격 12ms 적당",
            pinned = true,
            isSaved = true,
            onTitleChange = {}, onBodyChange = {}, onTogglePin = {}, onDelete = {}, onBack = {},
        )
    }
}

@DeskPreviews
@Composable
private fun MemoEditorScreenEmptyPreview() {
    DeskTheme {
        MemoEditorScreen(
            isLoading = false, title = "", body = "", pinned = false, isSaved = true,
            onTitleChange = {}, onBodyChange = {}, onTogglePin = {}, onDelete = {}, onBack = {},
        )
    }
}
