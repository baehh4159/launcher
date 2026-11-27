package com.desklauncher.feature.alarm.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.alarm.ui.list.AlarmListRoute
import kotlinx.serialization.Serializable

@Serializable
data object AlarmGraph

@Serializable
data object AlarmListDestination

internal fun NavGraphBuilder.alarmGraph(navController: NavController) {
    navigation<AlarmGraph>(startDestination = AlarmListDestination) {
        composable<AlarmListDestination> { AlarmListRoute(onBack = { navController.popBackStack() }) }
    }
}
