package com.desklauncher.feature.store.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.desklauncher.core.designsystem.component.DeskColorPicker
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.store.R

/** 탭 색 고르기 — 디자인시스템 공용 [DeskColorPicker]를 쓴다 (프로젝트·일정 색과 같은 선택기). */
@Composable
internal fun TabColorDialog(
    current: DeskTabColor,
    onSelect: (DeskTabColor?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.store_color_dialog_title)) },
        text = { DeskColorPicker(selected = current, onSelect = onSelect) },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.store_color_close)) }
        },
        dismissButton = {
            TextButton(onClick = { onSelect(null) }) { Text(stringResource(R.string.store_color_default)) }
        },
    )
}

@DeskPreviews
@Composable
private fun TabColorDialogPreview() {
    DeskTheme {
        TabColorDialog(current = DeskTabColor.Blue, onSelect = {}, onDismiss = {})
    }
}
