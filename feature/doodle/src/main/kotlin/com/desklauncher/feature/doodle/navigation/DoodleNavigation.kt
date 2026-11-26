package com.desklauncher.feature.doodle.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.doodle.ui.editor.DoodleEditorRoute
import kotlinx.serialization.Serializable

@Serializable
data object DoodleGraph

@Serializable
data object DoodleEditorDestination

internal fun NavGraphBuilder.doodleGraph(navController: NavController) {
    navigation<DoodleGraph>(startDestination = DoodleEditorDestination) {
        composable<DoodleEditorDestination> { DoodleEditorRoute(onBack = { navController.popBackStack() }) }
    }
}
