package com.desklauncher.feature.desk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.desklauncher.core.designsystem.component.pressScale
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskShape
import com.desklauncher.core.designsystem.theme.DeskSize
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.desk.R

/**
 * 맨 아래 한 줄의 '바로 적기' 알약 (PRD §3.1, Beautiful UI Prompt Bar의 Pill 변형).
 * 오른쪽 훑기 영역과 면적을 공유한다 — 짧은 탭은 여기로 전달되고, 세로로 밀면 서류철 탐색이 된다(PRD §4.2).
 */
@Composable
fun QuickWriteBar(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DeskTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(DeskShape.pill)
            .background(colors.paper)
            .border(DeskLine.thin, colors.lineStrong, DeskShape.pill)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = DeskSpacing.m),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DeskSpacing.xs),
    ) {
        Icon(Icons.Outlined.EditNote, contentDescription = null, tint = colors.muted, modifier = Modifier.size(DeskSize.icon))
        Text(stringResource(R.string.desk_quick_write), style = DeskType.label, color = colors.muted)
    }
}

@DeskFontScalePreviews
@Composable
private fun QuickWriteBarPreview() {
    DeskTheme { QuickWriteBar(onClick = {}, modifier = Modifier.padding(DeskSpacing.m)) }
}
