package com.desklauncher.feature.scanner.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.scanner.R
import com.desklauncher.feature.scanner.domain.model.DocumentFolder
import com.desklauncher.feature.scanner.ui.cabinet.CabinetAction
import com.desklauncher.feature.scanner.ui.cabinet.CabinetDialog
import com.desklauncher.feature.scanner.ui.preview.CabinetPreviewData

/** 서류정리함 대화상자 모음 (DOC-03) */
@Composable
internal fun CabinetDialogHost(
    dialog: CabinetDialog?,
    folders: List<DocumentFolder>,
    onAction: (CabinetAction) -> Unit,
) {
    val dismiss = { onAction(CabinetAction.DismissDialog) }
    when (dialog) {
        null -> Unit
        CabinetDialog.NewFolder -> NameDialog(stringResource(R.string.cabinet_folder_new), "", onAction, dismiss)
        is CabinetDialog.RenameFolder -> NameDialog(stringResource(R.string.cabinet_folder_rename), dialog.folder.name, onAction, dismiss)
        is CabinetDialog.RenameDocument -> NameDialog(stringResource(R.string.cabinet_doc_rename), dialog.document.title, onAction, dismiss)
        is CabinetDialog.MoveDocument -> MoveDialog(folders, dialog.document.folderId, onAction, dismiss)
        is CabinetDialog.DeleteFolder -> ConfirmDeleteDialog(
            title = stringResource(R.string.cabinet_folder_delete),
            body = stringResource(R.string.cabinet_folder_delete_body, dialog.folder.name),
            onAction = onAction,
            onDismiss = dismiss,
        )
        is CabinetDialog.DeleteDocument -> ConfirmDeleteDialog(
            title = stringResource(R.string.cabinet_doc_delete_title),
            body = stringResource(R.string.cabinet_doc_delete_body, dialog.document.title),
            onAction = onAction,
            onDismiss = dismiss,
        )
    }
}

@Composable
private fun NameDialog(title: String, initial: String, onAction: (CabinetAction) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable(initial) { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.cabinet_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(onClick = { onAction(CabinetAction.SubmitName(name)) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.cabinet_ok))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cabinet_cancel)) } },
    )
}

@Composable
private fun MoveDialog(folders: List<DocumentFolder>, current: String?, onAction: (CabinetAction) -> Unit, onDismiss: () -> Unit) {
    var selected by rememberSaveable { mutableStateOf(current) }
    val options = listOf<Pair<String?, String>>(null to stringResource(R.string.cabinet_filter_unfiled)) + folders.map { it.id to it.name }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.cabinet_doc_move)) },
        text = {
            Column(Modifier.selectableGroup()) {
                options.forEach { (id, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = selected == id, role = Role.RadioButton, onClick = { selected = id })
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = selected == id, onClick = null)
                        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = { onAction(CabinetAction.SubmitMove(selected)) }) { Text(stringResource(R.string.cabinet_ok)) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cabinet_cancel)) } },
    )
}

@Composable
private fun ConfirmDeleteDialog(title: String, body: String, onAction: (CabinetAction) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = {
            TextButton(onClick = { onAction(CabinetAction.ConfirmDelete) }) {
                Text(stringResource(R.string.cabinet_delete), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cabinet_cancel)) } },
    )
}

@DeskPreviews
@Composable
private fun MoveDialogPreview() {
    DeskTheme { CabinetDialogHost(CabinetDialog.MoveDocument(CabinetPreviewData.documents.first()), CabinetPreviewData.folders, onAction = {}) }
}

@DeskPreviews
@Composable
private fun NameDialogPreview() {
    DeskTheme { CabinetDialogHost(CabinetDialog.NewFolder, CabinetPreviewData.folders, onAction = {}) }
}
