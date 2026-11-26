package com.desklauncher.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.desklauncher.core.toolapi.ToolRegistry
import com.desklauncher.feature.desk.navigation.DeskDestination
import com.desklauncher.feature.desk.navigation.deskScreen

/**
 * 앱 내비게이션 조립. 셸 화면 + 레지스트리에 등록된 모든 도구의 그래프를 붙인다.
 * 꺼진 도구도 그래프는 등록해 둔다 (책상 탭에서만 숨김).
 */
@Composable
fun DeskNavHost(
    toolRegistry: ToolRegistry,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = DeskDestination,
        modifier = modifier,
    ) {
        deskScreen(
            onOpenTool = { toolId ->
                toolRegistry.findTool(toolId)?.let { tool -> navController.navigate(tool.startDestination) }
            },
        )
        toolRegistry.registeredTools.forEach { tool ->
            with(tool) { toolGraph(navController) }
        }
    }
}
