package com.desklauncher.feature.memo.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
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
import com.desklauncher.core.designsystem.component.DeskColorPicker
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.memo.R
import com.desklauncher.feature.memo.ui.project.ProjectEditor

@Composable
internal fun ProjectEditorDialog(
    editor: ProjectEditor,
    onSave: (name: String, colorName: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by rememberSaveable(editor.id) { mutableStateOf(editor.name) }
    var color by rememberSaveable(editor.id) { mutableStateOf(DeskTabColor.fromNameOrNull(editor.colorName) ?: DeskTabColor.Blue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (editor.id == null) R.string.memo_project_new_title else R.string.memo_project_edit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.memo_project_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(stringResource(R.string.memo_project_color), style = MaterialTheme.typography.labelLarge)
                DeskColorPicker(selected = color, onSelect = { color = it })
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, color.name) }, enabled = name.isNotBlank()) { Text(stringResource(R.string.memo_save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.memo_cancel)) } },
    )
}

@DeskPreviews
@Composable
private fun ProjectEditorDialogPreview() {
    DeskTheme { ProjectEditorDialog(ProjectEditor(null, "책상 런처", "Blue"), onSave = { _, _ -> }, onDismiss = {}) }
}
