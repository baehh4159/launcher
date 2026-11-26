package com.desklauncher.feature.scanner.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.scanner.ui.cabinet.CabinetRoute
import kotlinx.serialization.Serializable

@Serializable
data object CabinetGraph

@Serializable
data object CabinetDestination

internal fun NavGraphBuilder.cabinetGraph(navController: NavController) {
    navigation<CabinetGraph>(startDestination = CabinetDestination) {
        composable<CabinetDestination> {
            CabinetRoute(onBack = { navController.popBackStack() }, onOpenDocument = { })
        }
    }
}
