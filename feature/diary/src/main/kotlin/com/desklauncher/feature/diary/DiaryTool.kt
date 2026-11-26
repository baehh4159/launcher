package com.desklauncher.feature.diary

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.diary.navigation.DiaryGraph
import com.desklauncher.feature.diary.navigation.diaryGraph
import javax.inject.Inject

/** 개인일기 도구. id "diary"는 출시 후 변경 금지. */
class DiaryTool @Inject constructor() : DeskTool {
    override val id: String = "diary"
    override val titleRes: Int = R.string.diary_tool_title
    override val descriptionRes: Int = R.string.diary_tool_description
    override val icon: ImageVector = Icons.Outlined.AutoStories
    override val defaultTabColor: DeskTabColor = DeskTabColor.Pink
    override val defaultEnabled: Boolean = true
    override val defaultOrder: Int = 3
    override val startDestination: Any = DiaryGraph

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        diaryGraph(navController)
    }
}
