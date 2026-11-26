package com.desklauncher.feature.scanner.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.scanner.ui.cabinet.CabinetRoute
import com.desklauncher.feature.scanner.ui.viewer.DocumentViewerRoute
import kotlinx.serialization.Serializable

@Serializable
data object CabinetGraph

@Serializable
data object CabinetDestination

@Serializable
data class DocumentViewerDestination(val documentId: String)

internal fun NavGraphBuilder.cabinetGraph(navController: NavController) {
    navigation<CabinetGraph>(startDestination = CabinetDestination) {
        composable<CabinetDestination> {
            CabinetRoute(
                onBack = { navController.popBackStack() },
                onOpenDocument = { id -> navController.navigate(DocumentViewerDestination(id)) },
            )
        }
        composable<DocumentViewerDestination> {
            DocumentViewerRoute(onBack = { navController.popBackStack() })
        }
    }
}
