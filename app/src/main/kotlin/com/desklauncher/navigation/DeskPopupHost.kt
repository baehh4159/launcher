package com.desklauncher.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.desklauncher.core.designsystem.component.DeskPopupFrame
import com.desklauncher.core.designsystem.component.DeskPopupScrim
import com.desklauncher.core.designsystem.motion.DeskTransitions
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.core.toolapi.PluginStorageProvider
import com.desklauncher.core.toolapi.SimpleDeskTool
import com.desklauncher.core.toolapi.ToolRegistry
import com.desklauncher.feature.apps.ui.AppsRoute
import kotlinx.serialization.Serializable

/**
 * 책상 위 팝업 창 (SHELL-05). [popupKey]가 null이 아니면 어두운 막 위에 서류철 창을 펼친다.
 * 닫기: 막 누르기, 뒤로가기(도구 첫 화면에서), 홈 버튼(호출부), 도구의 '책상으로' 버튼.
 */
@Composable
internal fun DeskPopupHost(
    popupKey: String?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (key: String) -> Unit,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedVisibility(visible = popupKey != null, enter = DeskTransitions.scrimIn, exit = DeskTransitions.scrimOut) {
            DeskPopupScrim(onDismiss = onDismiss)
        }
        // 닫히는 동안에도 내용이 남아 있어야 하므로 AnimatedContent로 이전 키를 유지한다
        AnimatedContent(
            targetState = popupKey,
            transitionSpec = {
                when {
                    initialState == null -> DeskTransitions.popupOpen togetherWith ExitTransition.None
                    targetState == null -> EnterTransition.None togetherWith DeskTransitions.popupClose
                    else -> DeskTransitions.popupOpen togetherWith DeskTransitions.popupClose
                }
            },
            label = "deskPopup",
        ) { key ->
            if (key != null) {
                DeskPopupFrame { content(key) }
            }
        }
    }
}

/** 팝업 안의 빈 시작점. 도구가 첫 화면에서 뒤로 가 여기로 돌아오면 창을 닫는다. */
@Serializable
internal data object PopupRootDestination

/**
 * 도구 하나를 팝업 안 전용 내비게이션에 띄운다. 도구는 받은 navController로만 이동하므로
 * 도구 모듈을 고치지 않고 그대로 창 안에서 동작한다.
 */
@Composable
internal fun ToolPopupContent(
    tool: DeskTool,
    toolRegistry: ToolRegistry,
    pluginStorageProvider: PluginStorageProvider,
    onClose: () -> Unit,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = PopupRootDestination,
        // 창 자체가 펼쳐지는 전환이 있으므로, 시작점↔도구 첫 화면 사이에는 전환을 넣지 않는다
        enterTransition = {
            if (initialState.destination.hasRoute(PopupRootDestination::class)) EnterTransition.None else DeskTransitions.innerEnter
        },
        exitTransition = { DeskTransitions.innerExit },
        popEnterTransition = { DeskTransitions.innerPopEnter },
        // 도구 첫 화면에서 닫을 때는 창이 접히는 동안 빈 종이가 보이지 않게 내용이 함께 사라진다
        popExitTransition = {
            if (targetState.destination.hasRoute(PopupRootDestination::class)) DeskTransitions.innerExit else DeskTransitions.innerPopExit
        },
    ) {
        composable<PopupRootDestination> { }
        with(tool) { toolGraph(navController) }
        if (tool is SimpleDeskTool) {
            simpleToolScreen(
                toolRegistry = toolRegistry,
                pluginStorageProvider = pluginStorageProvider,
                onClose = { navController.popBackStack() },
            )
        }
    }

    LaunchedEffect(navController, tool.id) {
        // 회전 등으로 복원된 경우에는 이미 도구 화면에 있으므로 다시 넣지 않는다
        if (navController.currentDestination?.hasRoute(PopupRootDestination::class) == true) {
            navController.navigate(tool.startDestination)
        }
    }

    val entry by navController.currentBackStackEntryAsState()
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(entry) {
        val current = entry ?: return@LaunchedEffect
        val atRoot = current.destination.hasRoute(PopupRootDestination::class)
        when {
            !atRoot -> entered = true
            entered -> onClose()
        }
    }
}

/** 앱 서랍을 팝업 창 안에 띄운다 (SHELL-07). 뒤로가기는 서랍 안(검색 지우기 등)이 먼저 처리하고, 남으면 창을 닫는다. */
@Composable
internal fun AppDrawerPopupContent(
    onClose: () -> Unit,
    onOpenStore: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    BackHandler(onBack = onClose)
    AppsRoute(onBack = onClose, onOpenStore = onOpenStore, onOpenSettings = onOpenSettings)
}
