package com.desklauncher.feature.todo.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.todo.R
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.ui.preview.TodoPreviewData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodoEditorSheet(
    todo: Todo,
    onSave: (Todo) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        TodoEditorContent(todo = todo, onSave = onSave, onDelete = onDelete)
    }
}

@Composable
internal fun TodoEditorContent(
    todo: Todo,
    onSave: (Todo) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable(todo.id) { mutableStateOf(todo.title) }
    var note by rememberSaveable(todo.id) { mutableStateOf(todo.note) }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.todo_editor_title), style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.todo_editor_name)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.todo_editor_note)) },
            minLines = 3,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.todo_editor_delete), color = MaterialTheme.colorScheme.error)
            }
            Button(onClick = { onSave(todo.copy(title = title, note = note)) }, enabled = title.isNotBlank()) {
                Text(stringResource(R.string.todo_editor_save))
            }
        }
    }
}

@DeskPreviews
@Composable
private fun TodoEditorContentPreview() {
    DeskTheme {
        TodoEditorContent(todo = TodoPreviewData.todos.first(), onSave = {}, onDelete = {})
    }
}
