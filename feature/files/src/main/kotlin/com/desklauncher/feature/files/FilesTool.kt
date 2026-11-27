package com.desklauncher.feature.files

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.InsertDriveFile
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.files.ui.FilesRoute
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data object FilesGraph

@Serializable
data object FilesDestination


/** 도구 id — DB 키이자 탭 색 조회 키. 출시 후 변경 금지. */
const val FilesToolId = "files"

/** 내파일 도구. id "files"는 출시 후 변경 금지. */
class FilesTool @Inject constructor() : DeskTool {
    override val id: String = FilesToolId
    override val titleRes: Int = R.string.files_tool_title
    override val descriptionRes: Int = R.string.files_tool_description
    override val icon: ImageVector = Icons.AutoMirrored.Outlined.InsertDriveFile
    override val defaultTabColor: DeskTabColor = DeskTabColor.Gray
    override val defaultEnabled: Boolean = true
    override val defaultOrder: Int = 8
    override val startDestination: Any = FilesGraph

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        navigation<FilesGraph>(startDestination = FilesDestination) {
            composable<FilesDestination> { FilesRoute(onBack = { navController.popBackStack() }) }
        }
    }
}
