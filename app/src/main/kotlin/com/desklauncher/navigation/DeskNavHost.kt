package com.desklauncher.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.desklauncher.core.designsystem.motion.DeskTransitions
import com.desklauncher.core.toolapi.ToolRegistry
import com.desklauncher.feature.apps.navigation.AppsDestination
import com.desklauncher.feature.apps.navigation.appsScreen
import com.desklauncher.feature.apps.navigation.navigateToApps
import com.desklauncher.feature.desk.navigation.DeskDestination
import com.desklauncher.feature.desk.navigation.deskScreen
import com.desklauncher.feature.settings.navigation.navigateToSettings
import com.desklauncher.feature.settings.navigation.settingsScreen
import com.desklauncher.feature.store.navigation.navigateToStore
import com.desklauncher.feature.store.navigation.storeScreen
import kotlinx.coroutines.flow.Flow

/**
 * 앱 내비게이션 조립. 셸 화면 + 레지스트리에 등록된 모든 도구의 그래프를 붙인다.
 * 꺼진 도구도 그래프는 등록해 둔다 (책상 탭에서만 숨김).
 */
@Composable
fun DeskNavHost(
    toolRegistry: ToolRegistry,
    homePresses: Flow<Unit>,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    // 홈 버튼: 어떤 도구에 있든 책상으로 (SHELL-06, PRD §4.1). 책상 자체의 초기화는 deskScreen이 맡는다.
    LaunchedEffect(navController, homePresses) {
        homePresses.collect {
            if (navController.currentDestination?.isDesk() == false) {
                navController.popBackStack(DeskDestination, inclusive = false)
            }
        }
    }
    NavHost(
        navController = navController,
        startDestination = DeskDestination,
        modifier = modifier,
        enterTransition = {
            when {
                targetState.destination.isApps() -> DeskTransitions.drawerOpen
                initialState.destination.isDesk() -> DeskTransitions.folderOpen
                else -> DeskTransitions.innerEnter
            }
        },
        exitTransition = {
            if (initialState.destination.isDesk()) DeskTransitions.deskRecede else DeskTransitions.innerExit
        },
        popEnterTransition = {
            if (targetState.destination.isDesk()) DeskTransitions.deskReturn else DeskTransitions.innerPopEnter
        },
        popExitTransition = {
            when {
                initialState.destination.isApps() -> DeskTransitions.drawerClose
                targetState.destination.isDesk() -> DeskTransitions.folderClose
                else -> DeskTransitions.innerPopExit
            }
        },
    ) {
        deskScreen(
            homePresses = homePresses,
            onOpenTool = { toolId ->
                toolRegistry.findTool(toolId)?.let { tool ->
                    navController.navigate(tool.startDestination) { launchSingleTop = true }
                }
            },
            onOpenToolStore = navController::navigateToStore,
            onOpenAppDrawer = navController::navigateToApps,
        )
        appsScreen(
            onBack = navController::popBackStack,
            onOpenStore = navController::navigateToStore,
            onOpenSettings = navController::navigateToSettings,
        )
        storeScreen(onBack = navController::popBackStack)
        settingsScreen(onBack = navController::popBackStack)
        toolRegistry.registeredTools.forEach { tool ->
            with(tool) { toolGraph(navController) }
        }
    }
}

private fun NavDestination.isDesk(): Boolean = hierarchy.any { it.hasRoute(DeskDestination::class) }

private fun NavDestination.isApps(): Boolean = hierarchy.any { it.hasRoute(AppsDestination::class) }
