package com.desklauncher.feature.scanner.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.AssistChip
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.scanner.R
import com.desklauncher.feature.scanner.domain.model.DocumentFolder
import com.desklauncher.feature.scanner.ui.cabinet.CabinetAction
import com.desklauncher.feature.scanner.ui.cabinet.CabinetDialog
import com.desklauncher.feature.scanner.ui.cabinet.FolderFilter
import com.desklauncher.feature.scanner.ui.preview.CabinetPreviewData

/** [전체] [미분류] [폴더…] [+ 폴더]. 폴더 칩을 다시 누르면 이름 바꾸기/삭제 메뉴. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun FolderFilterRow(
    folders: List<DocumentFolder>,
    selected: FolderFilter,
    onAction: (CabinetAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilterChip(
            selected = selected == FolderFilter.All,
            onClick = { onAction(CabinetAction.SelectFilter(FolderFilter.All)) },
            label = { Text(stringResource(R.string.cabinet_filter_all)) },
        )
        FilterChip(
            selected = selected == FolderFilter.Unfiled,
            onClick = { onAction(CabinetAction.SelectFilter(FolderFilter.Unfiled)) },
            label = { Text(stringResource(R.string.cabinet_filter_unfiled)) },
        )
        folders.forEach { folder ->
            var menuOpen by remember { mutableStateOf(false) }
            val isSelected = selected == FolderFilter.Folder(folder.id)
            val renameLabel = stringResource(R.string.cabinet_folder_rename)
            val deleteLabel = stringResource(R.string.cabinet_folder_delete)
            Box {
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) menuOpen = true else onAction(CabinetAction.SelectFilter(FolderFilter.Folder(folder.id)))
                    },
                    label = { Text(folder.name) },
                    leadingIcon = { Icon(Icons.Outlined.Folder, contentDescription = null) },
                    modifier = Modifier.semantics {
                        customActions = listOf(
                            CustomAccessibilityAction(renameLabel) { onAction(CabinetAction.ShowDialog(CabinetDialog.RenameFolder(folder))); true },
                            CustomAccessibilityAction(deleteLabel) { onAction(CabinetAction.ShowDialog(CabinetDialog.DeleteFolder(folder))); true },
                        )
                    },
                )
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(text = { Text(renameLabel) }, onClick = {
                        menuOpen = false
                        onAction(CabinetAction.ShowDialog(CabinetDialog.RenameFolder(folder)))
                    })
                    DropdownMenuItem(text = { Text(deleteLabel, color = MaterialTheme.colorScheme.error) }, onClick = {
                        menuOpen = false
                        onAction(CabinetAction.ShowDialog(CabinetDialog.DeleteFolder(folder)))
                    })
                }
            }
        }
        AssistChip(
            onClick = { onAction(CabinetAction.ShowDialog(CabinetDialog.NewFolder)) },
            label = { Text(stringResource(R.string.cabinet_folder_add)) },
            leadingIcon = { Icon(Icons.Outlined.CreateNewFolder, contentDescription = null) },
        )
    }
}

@DeskPreviews
@Composable
private fun FolderFilterRowPreview() {
    DeskTheme { FolderFilterRow(CabinetPreviewData.folders, FolderFilter.Folder("f1"), onAction = {}) }
}
