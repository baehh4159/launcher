package com.desklauncher.feature.settings.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskShape
import com.desklauncher.core.designsystem.theme.DeskSize
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType

/** 하나를 고르는 설정 행 — 선택은 잉크색 점, 테두리는 얇은 선 (DESIGN_LANGUAGE D5) */
@Composable
fun ChoiceRow(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = DeskTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = DeskSize.control)
            .clip(DeskShape.control)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = DeskSpacing.s, vertical = DeskSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DeskSpacing.s),
    ) {
        Box(
            modifier = Modifier.size(20.dp).border(DeskLine.thin, if (selected) colors.ink else colors.lineStrong, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) Box(Modifier.size(10.dp).background(colors.ink, CircleShape))
        }
        Column(Modifier.weight(1f)) {
            Text(title, style = DeskType.label, color = colors.onPaper)
            Text(description, style = DeskType.caption, color = colors.muted)
        }
    }
}

@DeskFontScalePreviews
@Composable
private fun ChoiceRowPreview() {
    DeskTheme {
        Column {
            ChoiceRow("탐색 + 확정", "칸을 넘을 때 약하게, 열 때 분명하게", selected = true, onClick = {})
            ChoiceRow("끔", "진동 없이 화면 강조만", selected = false, onClick = {})
        }
    }
}
