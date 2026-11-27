package com.desklauncher.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTheme

/** 도구가 자체 미리보기를 제공하지 않을 때 쓰는 기본 DeskPreview (아이콘 + 제목). */
@Composable
fun ToolPreviewCard(
    icon: ImageVector,
    title: String,
    tabColor: DeskTabColor,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = tabColor.container,
        contentColor = tabColor.onContainer,
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(DeskLine.Hairline, DeskTheme.colors.line),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = title, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun ToolPreviewCardPreview() {
    DeskTheme {
        ToolPreviewCard(
            icon = Icons.Outlined.Checklist,
            title = "할일정리",
            tabColor = DeskTabColor.Orange,
            modifier = Modifier.padding(24.dp),
        )
    }
}
