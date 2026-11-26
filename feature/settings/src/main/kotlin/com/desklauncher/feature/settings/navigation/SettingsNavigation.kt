package com.desklauncher.feature.settings.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desklauncher.feature.settings.onboarding.OnboardingRoute
import com.desklauncher.feature.settings.ui.SettingsRoute
import kotlinx.serialization.Serializable

/** 설정 — SHELL-11. */
@Serializable
data object SettingsDestination

fun NavController.navigateToSettings() = navigate(SettingsDestination) { launchSingleTop = true }

fun NavGraphBuilder.settingsScreen(onBack: () -> Unit) {
    composable<SettingsDestination> {
        SettingsRoute(onBack = onBack)
    }
}

/** 첫 실행 안내 — SHELL-10. */
@Serializable
data object OnboardingDestination

fun NavGraphBuilder.onboardingScreen(onFinished: () -> Unit) {
    composable<OnboardingDestination> {
        OnboardingRoute(onFinished = onFinished)
    }
}
