package com.desklauncher.feature.store.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.store.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TabColorDialog(
    current: DeskTabColor,
    onSelect: (DeskTabColor?) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.store_color_dialog_title)) },
        text = {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DeskTabColor.entries.forEach { color ->
                    ColorSwatch(
                        color = color,
                        selected = color == current,
                        label = stringResource(color.labelRes()),
                        onClick = { onSelect(color) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.store_color_close)) }
        },
        dismissButton = {
            TextButton(onClick = { onSelect(null) }) { Text(stringResource(R.string.store_color_default)) }
        },
    )
}

@StringRes
internal fun DeskTabColor.labelRes(): Int = when (this) {
    DeskTabColor.Red -> R.string.store_color_red
    DeskTabColor.Orange -> R.string.store_color_orange
    DeskTabColor.Yellow -> R.string.store_color_yellow
    DeskTabColor.Green -> R.string.store_color_green
    DeskTabColor.Teal -> R.string.store_color_teal
    DeskTabColor.Blue -> R.string.store_color_blue
    DeskTabColor.Indigo -> R.string.store_color_indigo
    DeskTabColor.Purple -> R.string.store_color_purple
    DeskTabColor.Pink -> R.string.store_color_pink
    DeskTabColor.Brown -> R.string.store_color_brown
    DeskTabColor.Gray -> R.string.store_color_gray
}

@DeskPreviews
@Composable
private fun TabColorDialogPreview() {
    DeskTheme {
        TabColorDialog(current = DeskTabColor.Blue, onSelect = {}, onDismiss = {})
    }
}
