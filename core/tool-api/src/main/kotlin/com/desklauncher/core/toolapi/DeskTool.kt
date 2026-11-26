package com.desklauncher.core.toolapi

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.component.ToolPreviewCard
import com.desklauncher.core.designsystem.theme.DeskTabColor

/**
 * 책상 도구(플러그인) 계약. 각 feature 모듈이 하나씩 구현하고 Hilt `@IntoSet`으로 등록한다 (PRD §6).
 *
 * ```
 * @Module @InstallIn(SingletonComponent::class)
 * abstract class TodoToolModule {
 *     @Binds @IntoSet abstract fun bindTodoTool(tool: TodoTool): DeskTool
 * }
 * ```
 */
interface DeskTool {

    /** 영구 불변 키 (예: "todo"). DB `tool_config.toolId`와 매칭되므로 **출시 후 변경 금지**. */
    val id: String

    @get:StringRes
    val titleRes: Int

    @get:StringRes
    val descriptionRes: Int

    val icon: ImageVector

    val defaultTabColor: DeskTabColor

    /** 최초 설치 시 켜짐 여부. */
    val defaultEnabled: Boolean

    /** 최초 설치 시 탭 순서 (작을수록 위). */
    val defaultOrder: Int

    /**
     * 도구를 열 때 이동할 목적지. [toolGraph]에서 등록한 `@Serializable` 라우트 객체여야 한다.
     * 보통 도구의 중첩 그래프 라우트(예: `TodoGraph`)를 반환한다.
     */
    val startDestination: Any

    /** 이 도구의 화면들을 중첩 그래프로 등록한다. 다른 도구의 라우트를 등록하지 않는다. */
    fun NavGraphBuilder.toolGraph(navController: NavController)

    /** 서류철 스크럽 중 책상 위에 보일 미리보기. 기본 구현은 아이콘 + 제목. */
    @Composable
    fun DeskPreview(tabColor: DeskTabColor, modifier: Modifier) {
        ToolPreviewCard(
            icon = icon,
            title = stringResource(titleRes),
            tabColor = tabColor,
            modifier = modifier,
        )
    }
}
