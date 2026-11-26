package com.desklauncher.feature.todo.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.todo.R
import com.desklauncher.feature.todo.domain.model.ProjectRef
import com.desklauncher.feature.todo.domain.model.TodoPriority
import com.desklauncher.feature.todo.ui.DueLabel

/** 할일 줄 아래 작은 정보: 마감 · 우선순위 · 프로젝트 (TODO-04). */
@Composable
internal fun TodoMeta(
    due: DueLabel?,
    priority: TodoPriority,
    project: ProjectRef?,
    modifier: Modifier = Modifier,
) {
    if (due == null && priority == TodoPriority.None && project == null) return
    Row(
        modifier = modifier.padding(top = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val small = MaterialTheme.typography.labelSmall
        if (due != null) {
            val color = if (due.isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(Icons.Outlined.Event, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(due.text, style = small, color = color)
            }
        }
        if (priority != TodoPriority.None) {
            val color = priority.color()
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(Icons.Outlined.Flag, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                Text(stringResource(priority.labelRes()), style = small, color = color)
            }
        }
        if (project != null) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                val dot = DeskTabColor.fromNameOrNull(project.colorName) ?: DeskTabColor.Gray
                Box(Modifier.size(8.dp).background(dot.container, CircleShape))
                Text(project.name, style = small, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

internal fun TodoPriority.labelRes(): Int = when (this) {
    TodoPriority.None -> R.string.todo_priority_none
    TodoPriority.Low -> R.string.todo_priority_low
    TodoPriority.Medium -> R.string.todo_priority_medium
    TodoPriority.High -> R.string.todo_priority_high
}

@Composable
private fun TodoPriority.color(): Color = when (this) {
    TodoPriority.None -> MaterialTheme.colorScheme.onSurfaceVariant
    TodoPriority.Low -> DeskTabColor.Blue.container
    TodoPriority.Medium -> DeskTabColor.Orange.container
    TodoPriority.High -> MaterialTheme.colorScheme.error
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun TodoMetaPreview() {
    DeskTheme {
        TodoMeta(
            due = DueLabel("어제", isOverdue = true),
            priority = TodoPriority.High,
            project = ProjectRef("p1", "책상 런처", "Blue"),
            modifier = Modifier.padding(16.dp),
        )
    }
}
