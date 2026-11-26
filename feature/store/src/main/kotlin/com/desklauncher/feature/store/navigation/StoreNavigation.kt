package com.desklauncher.feature.store.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desklauncher.feature.store.ui.StoreRoute
import kotlinx.serialization.Serializable

/** 내 상점(도구함) — SHELL-09. */
@Serializable
data object StoreDestination

fun NavController.navigateToStore() = navigate(StoreDestination) { launchSingleTop = true }

fun NavGraphBuilder.storeScreen(onBack: () -> Unit) {
    composable<StoreDestination> {
        StoreRoute(onBack = onBack)
    }
}
