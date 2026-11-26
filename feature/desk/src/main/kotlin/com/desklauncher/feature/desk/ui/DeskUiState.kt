package com.desklauncher.feature.desk.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.desklauncher.core.designsystem.text.UiText
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.ActiveTool

@Immutable
data class DeskUiState(
    val isLoading: Boolean = true,
    val tabs: List<DeskTabUi> = emptyList(),
)

/** 인덱스 탭 하나의 화면 모델. */
@Immutable
data class DeskTabUi(
    val toolId: String,
    val title: UiText,
    val icon: ImageVector,
    val color: DeskTabColor,
)

internal fun ActiveTool.toTabUi(): DeskTabUi = DeskTabUi(
    toolId = id,
    title = UiText.Res(tool.titleRes),
    icon = tool.icon,
    color = tabColor,
)
