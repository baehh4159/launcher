package com.desklauncher.feature.todo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.haptics.rememberDeskHaptics
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.todo.R
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.ui.preview.TodoPreviewData
import kotlinx.coroutines.launch

/**
 * 할일 한 줄. 체크박스 또는 오른쪽으로 밀어 완료/되돌리기 (TODO-02).
 * 밀어도 줄은 사라지지 않고 제자리로 돌아온다 — 목록 이동은 상태 변화로 처리.
 */
@Composable
internal fun TodoRow(
    todo: Todo,
    onClick: () -> Unit,
    onDoneChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    meta: @Composable () -> Unit = {},
) {
    val haptics = rememberDeskHaptics()
    val currentOnDoneChange by rememberUpdatedState(onDoneChange)
    val scope = rememberCoroutineScope()
    val swipeState = rememberSwipeToDismissBoxState()
    SwipeToDismissBox(
        state = swipeState,
        enableDismissFromEndToStart = false,
        modifier = modifier,
        onDismiss = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                haptics.confirm()
                currentOnDoneChange(!todo.isDone)
            }
            scope.launch { swipeState.reset() }
        },
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterStart,
            ) {
                Icon(
                    imageVector = if (todo.isDone) Icons.AutoMirrored.Outlined.Undo else Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        },
    ) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .clickable(onClick = onClick)
                    .padding(start = 4.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                val checkLabel = stringResource(if (todo.isDone) R.string.todo_mark_undone else R.string.todo_mark_done, todo.title)
                Checkbox(
                    checked = todo.isDone,
                    onCheckedChange = { checked ->
                        haptics.confirm()
                        onDoneChange(checked)
                    },
                    modifier = Modifier.semantics { contentDescription = checkLabel },
                )
                Column(Modifier.weight(1f)) {
                    Text(
                        text = todo.title,
                        style = MaterialTheme.typography.bodyLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (todo.isDone) TextDecoration.LineThrough else null,
                        color = if (todo.isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                    if (todo.note.isNotBlank()) {
                        Text(
                            text = todo.note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    meta()
                }
            }
        }
    }
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun TodoRowPreview() {
    DeskTheme {
        Column {
            TodoRow(todo = TodoPreviewData.todos.first(), onClick = {}, onDoneChange = {})
            TodoRow(todo = TodoPreviewData.todos.last(), onClick = {}, onDoneChange = {})
        }
    }
}
