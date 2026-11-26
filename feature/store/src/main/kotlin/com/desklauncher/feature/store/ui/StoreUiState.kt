package com.desklauncher.feature.store.ui

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.desklauncher.core.designsystem.text.UiText
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.ActiveTool

@Immutable
data class StoreUiState(
    val isLoading: Boolean = true,
    val tools: List<StoreToolUi> = emptyList(),
    /** 색상 선택 대화상자를 연 도구 */
    val colorPickerToolId: String? = null,
)

@Immutable
data class StoreToolUi(
    val id: String,
    val title: UiText,
    val description: UiText,
    val icon: ImageVector,
    val color: DeskTabColor,
    val enabled: Boolean,
)

sealed interface StoreAction {
    data class SetEnabled(val toolId: String, val enabled: Boolean) : StoreAction
    data class Reorder(val orderedToolIds: List<String>) : StoreAction
    data class OpenColorPicker(val toolId: String) : StoreAction
    data object DismissColorPicker : StoreAction

    /** null = 기본 색으로 되돌리기 */
    data class SetColor(val toolId: String, val color: DeskTabColor?) : StoreAction
    data object Back : StoreAction
}

internal fun ActiveTool.toStoreUi(): StoreToolUi = StoreToolUi(
    id = id,
    title = UiText.Res(tool.titleRes),
    description = UiText.Res(tool.descriptionRes),
    icon = tool.icon,
    color = tabColor,
    enabled = enabled,
)
