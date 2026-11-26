package com.desklauncher.feature.memo.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.memo.ui.detail.ProjectDetailRoute
import com.desklauncher.feature.memo.ui.editor.MemoEditorRoute
import com.desklauncher.feature.memo.ui.project.ProjectListRoute
import kotlinx.serialization.Serializable

@Serializable
data object MemoGraph

@Serializable
data object ProjectListDestination

@Serializable
data class ProjectDetailDestination(val projectId: String)

/** memoId가 null이면 새 메모 */
@Serializable
data class MemoEditorDestination(val projectId: String, val memoId: String? = null)

internal fun NavGraphBuilder.memoGraph(navController: NavController) {
    navigation<MemoGraph>(startDestination = ProjectListDestination) {
        composable<ProjectListDestination> {
            ProjectListRoute(
                onBack = { navController.popBackStack() },
                onOpenProject = { id -> navController.navigate(ProjectDetailDestination(id)) },
            )
        }
        composable<ProjectDetailDestination> {
            ProjectDetailRoute(
                onBack = { navController.popBackStack() },
                onOpenMemo = { projectId, memoId -> navController.navigate(MemoEditorDestination(projectId, memoId)) },
            )
        }
        composable<MemoEditorDestination> {
            MemoEditorRoute(onBack = { navController.popBackStack() })
        }
    }
}
