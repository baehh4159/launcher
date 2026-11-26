package com.desklauncher.feature.apps.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desklauncher.feature.apps.ui.AppsRoute
import kotlinx.serialization.Serializable

/** 앱 서랍 — SHELL-07. */
@Serializable
data object AppsDestination

fun NavController.navigateToApps() = navigate(AppsDestination) { launchSingleTop = true }

fun NavGraphBuilder.appsScreen(
    onBack: () -> Unit,
    onOpenStore: () -> Unit,
) {
    composable<AppsDestination> {
        AppsRoute(onBack = onBack, onOpenStore = onOpenStore)
    }
}
