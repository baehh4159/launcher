package com.desklauncher.feature.store.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.text.UiText
import com.desklauncher.core.designsystem.text.asString
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.store.R
import com.desklauncher.feature.store.ui.StoreToolUi

/** 도구함 한 줄: [손잡이] [탭 색 견본+아이콘] 이름/설명 [스위치] */
@Composable
internal fun StoreToolRow(
    tool: StoreToolUi,
    isDragging: Boolean,
    onToggle: (Boolean) -> Unit,
    onColorClick: () -> Unit,
    modifier: Modifier = Modifier,
    handleModifier: Modifier = Modifier,
) {
    val title = tool.title.asString()
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        // 끄는 중에는 그림자 대신 진한 선으로 들어 올린 느낌
        border = BorderStroke(if (isDragging) DeskLine.Thin else DeskLine.Hairline, if (isDragging) DeskTheme.colors.lineStrong else DeskTheme.colors.line),
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val handleLabel = stringResource(R.string.store_reorder_handle, title)
            Box(
                modifier = handleModifier
                    .size(48.dp)
                    .semantics { contentDescription = handleLabel },
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.DragHandle, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val colorLabel = stringResource(R.string.store_tab_color, title)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(tool.color.container, RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp))
                    .clickable(onClick = onColorClick)
                    .semantics { contentDescription = colorLabel },
                contentAlignment = Alignment.Center,
            ) {
                Icon(tool.icon, contentDescription = null, tint = tool.color.onContainer)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = tool.description.asString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = tool.enabled,
                onCheckedChange = onToggle,
                modifier = Modifier.semantics { contentDescription = title },
            )
        }
    }
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun StoreToolRowPreview() {
    DeskTheme {
        StoreToolRow(
            tool = StoreToolUi(
                id = "todo",
                title = UiText.Plain("할일정리"),
                description = UiText.Plain("오늘 할 일과 마감일을 정리해요"),
                icon = Icons.Outlined.Checklist,
                color = DeskTabColor.Orange,
                enabled = true,
            ),
            isDragging = false,
            onToggle = {},
            onColorClick = {},
            modifier = Modifier.padding(8.dp),
        )
    }
}
