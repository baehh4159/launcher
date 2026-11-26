package com.desklauncher.feature.memo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.memo.R
import com.desklauncher.feature.memo.domain.model.Project
import com.desklauncher.feature.memo.ui.preview.MemoPreviewData

/** 프로젝트 = 작은 서류철 탭 + 이름 + 메뉴(편집/보관/삭제). */
@Composable
internal fun ProjectRow(
    project: Project,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onToggleArchive: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    var menuOpen by remember { mutableStateOf(false) }
    val color = DeskTabColor.fromNameOrNull(project.colorName) ?: DeskTabColor.Gray
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(onClick = onClick)
            .padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(Modifier.size(width = 14.dp, height = 40.dp).background(color.container, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)))
        Column(Modifier.weight(1f)) {
            Text(project.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Box {
            IconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Outlined.MoreVert, contentDescription = stringResource(R.string.memo_project_menu, project.name))
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.memo_project_edit)) }, onClick = { menuOpen = false; onEdit() })
                DropdownMenuItem(
                    text = { Text(stringResource(if (project.archived) R.string.memo_project_unarchive else R.string.memo_project_archive)) },
                    onClick = { menuOpen = false; onToggleArchive() },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.memo_project_delete), color = MaterialTheme.colorScheme.error) },
                    onClick = { menuOpen = false; onDelete() },
                )
            }
        }
    }
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun ProjectRowPreview() {
    DeskTheme {
        ProjectRow(MemoPreviewData.projects.first(), onClick = {}, onEdit = {}, onToggleArchive = {}, onDelete = {}, subtitle = "메모 3개")
    }
}
