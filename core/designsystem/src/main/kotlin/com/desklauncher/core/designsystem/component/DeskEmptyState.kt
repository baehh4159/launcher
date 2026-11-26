package com.desklauncher.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme

/** 책상 위에 놓인 '종이 메모' 모양의 빈 상태 안내. */
@Composable
fun DeskEmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Inventory2,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = DeskTheme.colors
    Surface(
        modifier = modifier,
        color = colors.paper,
        contentColor = colors.onPaper,
        shape = MaterialTheme.shapes.large,
        shadowElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(imageVector = icon, contentDescription = null)
            Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            action?.invoke()
        }
    }
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun DeskEmptyStatePreview() {
    DeskTheme {
        DeskEmptyState(
            title = "아직 책상에 꺼내 둔 도구가 없어요",
            body = "도구함에서 필요한 도구를 켜면 오른쪽에 서류철이 꽂힙니다.",
            modifier = Modifier.padding(24.dp),
            action = { FilledTonalButton(onClick = {}) { Text("도구함 열기") } },
        )
    }
}
