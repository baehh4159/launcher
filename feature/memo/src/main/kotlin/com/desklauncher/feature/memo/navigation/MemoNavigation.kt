package com.desklauncher.feature.memo.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.memo.ui.project.ProjectListRoute
import kotlinx.serialization.Serializable

@Serializable
data object MemoGraph

@Serializable
data object ProjectListDestination

internal fun NavGraphBuilder.memoGraph(navController: NavController) {
    navigation<MemoGraph>(startDestination = ProjectListDestination) {
        composable<ProjectListDestination> {
            ProjectListRoute(onBack = { navController.popBackStack() }, onOpenProject = { })
        }
    }
}
