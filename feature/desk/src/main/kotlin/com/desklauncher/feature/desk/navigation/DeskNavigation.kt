package com.desklauncher.feature.desk.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desklauncher.feature.desk.ui.DeskRoute
import kotlinx.serialization.Serializable

/** 책상 메인 목적지 (앱 시작 화면). */
@Serializable
data object DeskDestination

fun NavGraphBuilder.deskScreen(onOpenTool: (toolId: String) -> Unit) {
    composable<DeskDestination> {
        DeskRoute(onOpenTool = onOpenTool)
    }
}
