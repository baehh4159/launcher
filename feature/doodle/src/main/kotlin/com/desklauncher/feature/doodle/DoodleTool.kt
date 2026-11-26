package com.desklauncher.feature.doodle

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.doodle.navigation.DoodleGraph
import com.desklauncher.feature.doodle.navigation.doodleGraph
import javax.inject.Inject

/** 낙서장 도구. id "doodle"은 출시 후 변경 금지. */
class DoodleTool @Inject constructor() : DeskTool {
    override val id: String = "doodle"
    override val titleRes: Int = R.string.doodle_tool_title
    override val descriptionRes: Int = R.string.doodle_tool_description
    override val icon: ImageVector = Icons.Outlined.Brush
    override val defaultTabColor: DeskTabColor = DeskTabColor.Purple
    override val defaultEnabled: Boolean = true
    override val defaultOrder: Int = 6
    override val startDestination: Any = DoodleGraph

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        doodleGraph(navController)
    }
}
