package com.desklauncher.core.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.R
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTheme

/** 책상 색상 토큰 선택 (프로젝트·일정 색 등). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeskColorPicker(
    selected: DeskTabColor,
    onSelect: (DeskTabColor) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        DeskTabColor.entries.forEach { color ->
            val label = stringResource(color.labelRes())
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.container, CircleShape)
                    .border(
                        width = if (color == selected) 2.dp else DeskLine.Hairline,
                        color = if (color == selected) DeskTheme.colors.onPaper else DeskTheme.colors.line,
                        shape = CircleShape,
                    )
                    .selectable(selected = color == selected, role = Role.RadioButton, onClick = { onSelect(color) })
                    .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                if (color == selected) Box(Modifier.size(14.dp).background(color.onContainer, CircleShape))
            }
        }
    }
}

/** 색 이름 (접근성 낭독용). */
@StringRes
fun DeskTabColor.labelRes(): Int = when (this) {
    DeskTabColor.Red -> R.string.desk_color_red
    DeskTabColor.Orange -> R.string.desk_color_orange
    DeskTabColor.Yellow -> R.string.desk_color_yellow
    DeskTabColor.Green -> R.string.desk_color_green
    DeskTabColor.Teal -> R.string.desk_color_teal
    DeskTabColor.Blue -> R.string.desk_color_blue
    DeskTabColor.Indigo -> R.string.desk_color_indigo
    DeskTabColor.Purple -> R.string.desk_color_purple
    DeskTabColor.Pink -> R.string.desk_color_pink
    DeskTabColor.Brown -> R.string.desk_color_brown
    DeskTabColor.Gray -> R.string.desk_color_gray
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun DeskColorPickerPreview() {
    DeskTheme { DeskColorPicker(selected = DeskTabColor.Blue, onSelect = {}, modifier = Modifier.padding(16.dp)) }
}
