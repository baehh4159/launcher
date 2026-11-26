package com.desklauncher.feature.desk.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.desklauncher.feature.desk.ui.DeskRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

/** 책상 메인 목적지 (앱 시작 화면). */
@Serializable
data object DeskDestination

fun NavGraphBuilder.deskScreen(
    homePresses: Flow<Unit>,
    onOpenTool: (toolId: String) -> Unit,
) {
    composable<DeskDestination> {
        DeskRoute(homePresses = homePresses, onOpenTool = onOpenTool)
    }
}
