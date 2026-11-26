package com.desklauncher.feature.doodle.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.doodle.ui.editor.DoodleEditorRoute
import com.desklauncher.feature.doodle.ui.list.DoodleListRoute
import kotlinx.serialization.Serializable

@Serializable
data object DoodleGraph

@Serializable
data object DoodleListDestination

/** doodleId가 null이면 새 낙서 */
@Serializable
data class DoodleEditorDestination(val doodleId: String? = null)

internal fun NavGraphBuilder.doodleGraph(navController: NavController) {
    navigation<DoodleGraph>(startDestination = DoodleListDestination) {
        composable<DoodleListDestination> {
            DoodleListRoute(
                onBack = { navController.popBackStack() },
                onOpen = { id -> navController.navigate(DoodleEditorDestination(id)) },
            )
        }
        composable<DoodleEditorDestination> { DoodleEditorRoute(onBack = { navController.popBackStack() }) }
    }
}
