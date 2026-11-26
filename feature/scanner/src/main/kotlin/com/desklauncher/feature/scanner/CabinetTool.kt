package com.desklauncher.feature.scanner

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.scanner.navigation.CabinetGraph
import com.desklauncher.feature.scanner.navigation.cabinetGraph
import javax.inject.Inject

/** 서류정리함 도구 (모듈 feature:scanner). id "cabinet"은 출시 후 변경 금지. */
class CabinetTool @Inject constructor() : DeskTool {
    override val id: String = "cabinet"
    override val titleRes: Int = R.string.cabinet_tool_title
    override val descriptionRes: Int = R.string.cabinet_tool_description
    override val icon: ImageVector = Icons.Outlined.FolderOpen
    override val defaultTabColor: DeskTabColor = DeskTabColor.Brown
    override val defaultEnabled: Boolean = true
    override val defaultOrder: Int = 4
    override val startDestination: Any = CabinetGraph

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        cabinetGraph(navController)
    }
}
