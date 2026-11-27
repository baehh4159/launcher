package com.desklauncher.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTheme

/**
 * 도구 화면 공통 '서류철 헤더' (PRD §4.6). 탭 색상으로 칠한 상단 바.
 * [onBack]이 null이면 뒤로 버튼을 숨긴다(도구의 첫 화면에서도 보통은 표시 — 책상으로 돌아감).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolTopBar(
    title: String,
    tabColor: DeskTabColor,
    backContentDescription: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    // 그림자 대신 아래 얇은 선으로 본문과 나눈다
    Column(modifier) {
        TopAppBar(
            title = { Text(text = title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = backContentDescription)
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = tabColor.container,
                titleContentColor = tabColor.onContainer,
                navigationIconContentColor = tabColor.onContainer,
                actionIconContentColor = tabColor.onContainer,
            ),
        )
        HorizontalDivider(thickness = DeskLine.Hairline, color = DeskTheme.colors.line)
    }
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun ToolTopBarPreview() {
    DeskTheme {
        ToolTopBar(
            title = "할일정리",
            tabColor = DeskTabColor.Orange,
            backContentDescription = "책상으로",
            onBack = {},
            actions = { IconButton(onClick = {}) { Icon(Icons.Outlined.Add, contentDescription = "추가") } },
        )
    }
}
