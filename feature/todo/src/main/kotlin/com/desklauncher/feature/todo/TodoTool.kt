package com.desklauncher.feature.todo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.todo.navigation.TodoGraph
import com.desklauncher.feature.todo.navigation.todoGraph
import javax.inject.Inject


/** 도구 id — DB 키이자 탭 색 조회 키. 출시 후 변경 금지. */
const val TodoToolId = "todo"

/** 할일정리 도구. id "todo"는 출시 후 변경 금지. */
class TodoTool @Inject constructor() : DeskTool {
    override val id: String = TodoToolId
    override val titleRes: Int = R.string.todo_tool_title
    override val descriptionRes: Int = R.string.todo_tool_description
    override val icon: ImageVector = Icons.Outlined.Checklist
    override val defaultTabColor: DeskTabColor = DeskTabColor.Orange
    override val defaultEnabled: Boolean = true
    override val defaultOrder: Int = 0
    override val startDestination: Any = TodoGraph

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        todoGraph(onBack = { navController.popBackStack() })
    }
}
