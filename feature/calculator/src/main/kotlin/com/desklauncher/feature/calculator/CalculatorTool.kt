package com.desklauncher.feature.calculator

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.calculator.ui.CalculatorRoute
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data object CalculatorGraph

@Serializable
data object CalculatorDestination

/** 계산기 도구. id "calculator"는 출시 후 변경 금지. */
class CalculatorTool @Inject constructor() : DeskTool {
    override val id: String = "calculator"
    override val titleRes: Int = R.string.calculator_tool_title
    override val descriptionRes: Int = R.string.calculator_tool_description
    override val icon: ImageVector = Icons.Outlined.Calculate
    override val defaultTabColor: DeskTabColor = DeskTabColor.Teal
    override val defaultEnabled: Boolean = true
    override val defaultOrder: Int = 5
    override val startDestination: Any = CalculatorGraph

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        navigation<CalculatorGraph>(startDestination = CalculatorDestination) {
            composable<CalculatorDestination> { CalculatorRoute(onBack = { navController.popBackStack() }) }
        }
    }
}
