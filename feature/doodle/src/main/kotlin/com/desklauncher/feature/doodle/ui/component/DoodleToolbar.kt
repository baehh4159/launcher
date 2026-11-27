package com.desklauncher.feature.doodle.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material3.FilledIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskPenColor
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.doodle.R
import com.desklauncher.feature.doodle.domain.PenWidth

/** 펜/지우개 · 색 5종 · 굵기 3단계 (DOODLE-01). 하단(엄지 영역)에 둔다. */
@Composable
internal fun DoodleToolbar(
    color: DeskPenColor,
    width: PenWidth,
    eraser: Boolean,
    onColor: (DeskPenColor) -> Unit,
    onWidth: (PenWidth) -> Unit,
    onEraser: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    // 그림자·색 번짐 대신 위쪽 얇은 선으로 본문과 나눈다
    Surface(modifier = modifier, border = BorderStroke(DeskLine.Hairline, DeskTheme.colors.line)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            FilledIconToggleButton(checked = !eraser, onCheckedChange = { onEraser(false) }) {
                Icon(Icons.Outlined.Brush, contentDescription = stringResource(R.string.doodle_pen))
            }
            FilledIconToggleButton(checked = eraser, onCheckedChange = { onEraser(true) }) {
                Icon(Icons.Outlined.CleaningServices, contentDescription = stringResource(R.string.doodle_eraser))
            }
            VerticalDivider(Modifier.size(width = 1.dp, height = 28.dp))
            DeskPenColor.entries.forEach { pen ->
                val label = stringResource(pen.labelRes())
                val selected = pen == color && !eraser
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                        .padding(4.dp)
                        .background(pen.color, CircleShape)
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onColor(pen) })
                        .semantics { contentDescription = label },
                )
            }
            VerticalDivider(Modifier.size(width = 1.dp, height = 28.dp))
            PenWidth.entries.forEach { w ->
                val label = stringResource(w.labelRes())
                val selected = w == width
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                        .selectable(selected = selected, role = Role.RadioButton, onClick = { onWidth(w) })
                        .semantics { contentDescription = label },
                    contentAlignment = Alignment.Center,
                ) {
                    Box(Modifier.size((w.dp * 1.4f).dp).background(MaterialTheme.colorScheme.onSurface, CircleShape))
                }
            }
            trailing()
        }
    }
}

internal fun DeskPenColor.labelRes(): Int = when (this) {
    DeskPenColor.Ink -> R.string.doodle_color_ink
    DeskPenColor.Blue -> R.string.doodle_color_blue
    DeskPenColor.Red -> R.string.doodle_color_red
    DeskPenColor.Green -> R.string.doodle_color_green
    DeskPenColor.Yellow -> R.string.doodle_color_yellow
}

internal fun PenWidth.labelRes(): Int = when (this) {
    PenWidth.Thin -> R.string.doodle_width_thin
    PenWidth.Medium -> R.string.doodle_width_medium
    PenWidth.Thick -> R.string.doodle_width_thick
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun DoodleToolbarPreview() {
    DeskTheme { DoodleToolbar(DeskPenColor.Blue, PenWidth.Medium, eraser = false, onColor = {}, onWidth = {}, onEraser = {}) }
}
