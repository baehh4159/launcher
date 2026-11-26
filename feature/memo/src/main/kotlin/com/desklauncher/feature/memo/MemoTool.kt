package com.desklauncher.feature.memo

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.memo.navigation.MemoGraph
import com.desklauncher.feature.memo.navigation.memoGraph
import javax.inject.Inject

/** 프로젝트 메모 도구. id "memo"는 출시 후 변경 금지. */
class MemoTool @Inject constructor() : DeskTool {
    override val id: String = "memo"
    override val titleRes: Int = R.string.memo_tool_title
    override val descriptionRes: Int = R.string.memo_tool_description
    override val icon: ImageVector = Icons.AutoMirrored.Outlined.StickyNote2
    override val defaultTabColor: DeskTabColor = DeskTabColor.Yellow
    override val defaultEnabled: Boolean = true
    override val defaultOrder: Int = 2
    override val startDestination: Any = MemoGraph

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        memoGraph(navController)
    }
}
