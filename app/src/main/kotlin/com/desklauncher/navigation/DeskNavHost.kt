package com.desklauncher.navigation

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.desklauncher.core.designsystem.motion.DeskTransitions
import com.desklauncher.core.toolapi.PluginStorageProvider
import com.desklauncher.core.toolapi.ToolRegistry
import com.desklauncher.feature.desk.navigation.DeskDestination
import com.desklauncher.feature.desk.navigation.deskScreen
import com.desklauncher.feature.settings.navigation.OnboardingDestination
import com.desklauncher.feature.settings.navigation.navigateToSettings
import com.desklauncher.feature.settings.navigation.onboardingScreen
import com.desklauncher.feature.settings.navigation.settingsScreen
import com.desklauncher.feature.store.navigation.navigateToStore
import com.desklauncher.feature.store.navigation.storeScreen
import kotlinx.coroutines.flow.Flow

/**
 * 앱 내비게이션 조립. 셸 화면은 전체 화면 목적지로, 도구는 책상 위 팝업 창(SHELL-05)으로 연다.
 * 도구 그래프는 팝업마다 전용 NavHost에 붙는다 ([ToolPopupContent]). 플러그인 추가 방법은 docs/PLUGIN_GUIDE.md.
 */
@Composable
fun DeskNavHost(
    toolRegistry: ToolRegistry,
    pluginStorageProvider: PluginStorageProvider,
    homePresses: Flow<Unit>,
    onboardingCompleted: Boolean?,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    // 팝업으로 펼친 도구 id (회전·프로세스 복원 시 유지)
    var popupKey by rememberSaveable { mutableStateOf<String?>(null) }

    // 첫 실행이면 책상 위에 온보딩을 띄운다 (SHELL-10). 설정을 읽기 전(null)에는 판단하지 않는다.
    LaunchedEffect(navController, onboardingCompleted) {
        if (onboardingCompleted == false &&
            navController.currentDestination?.hierarchy?.none { it.hasRoute(OnboardingDestination::class) } != false
        ) {
            navController.navigate(OnboardingDestination) { launchSingleTop = true }
        }
    }

    // 홈 버튼: 어떤 도구에 있든 책상으로 (SHELL-06, PRD §4.1). 책상 자체의 초기화는 deskScreen이 맡는다.
    LaunchedEffect(navController, homePresses) {
        homePresses.collect {
            popupKey = null
            if (navController.currentDestination?.isDesk() == false) {
                navController.popBackStack(DeskDestination, inclusive = false)
            }
        }
    }
    Box(modifier = modifier) {
        NavHost(
            navController = navController,
            startDestination = DeskDestination,
            enterTransition = {
                if (initialState.destination.isDesk()) DeskTransitions.folderOpen else DeskTransitions.innerEnter
            },
            exitTransition = {
                if (initialState.destination.isDesk()) DeskTransitions.deskRecede else DeskTransitions.innerExit
            },
            popEnterTransition = {
                if (targetState.destination.isDesk()) DeskTransitions.deskReturn else DeskTransitions.innerPopEnter
            },
            popExitTransition = {
                if (targetState.destination.isDesk()) DeskTransitions.folderClose else DeskTransitions.innerPopExit
            },
        ) {
            deskScreen(
                homePresses = homePresses,
                onOpenTool = { toolId ->
                    if (toolRegistry.findTool(toolId) != null) popupKey = toolId
                },
                onOpenToolStore = navController::navigateToStore,
                onOpenAppDrawer = { popupKey = AppDrawerPopupKey },
            )
            storeScreen(onBack = navController::popBackStack)
            settingsScreen(onBack = navController::popBackStack)
            onboardingScreen(onFinished = navController::popBackStack)
        }

        DeskPopupHost(popupKey = popupKey, onDismiss = { popupKey = null }) { key ->
            val tool = toolRegistry.findTool(key)
            if (key == AppDrawerPopupKey) {
                AppDrawerPopupContent(
                    onClose = { popupKey = null },
                    onOpenStore = {
                        popupKey = null
                        navController.navigateToStore()
                    },
                    onOpenSettings = {
                        popupKey = null
                        navController.navigateToSettings()
                    },
                )
            } else if (tool == null) {
                // 플러그인을 뺀 뒤 복원된 키 등 — 크래시 대신 안내
                MissingToolScreen(onClose = { popupKey = null })
            } else {
                ToolPopupContent(
                    tool = tool,
                    toolRegistry = toolRegistry,
                    pluginStorageProvider = pluginStorageProvider,
                    onClose = { popupKey = null },
                )
            }
        }
    }
}

private fun NavDestination.isDesk(): Boolean = hierarchy.any { it.hasRoute(DeskDestination::class) }

/** 앱 서랍 팝업 키. 도구 id 규칙([a-z]…)과 겹치지 않는다. */
private const val AppDrawerPopupKey = "@apps"
