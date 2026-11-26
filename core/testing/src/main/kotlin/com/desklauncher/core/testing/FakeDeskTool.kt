package com.desklauncher.core.testing

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool

/** 화면·그래프가 없는 테스트용 도구. 아이콘은 빈 벡터. */
class FakeDeskTool(
    override val id: String,
    override val defaultOrder: Int = 0,
    override val defaultEnabled: Boolean = true,
    override val defaultTabColor: DeskTabColor = DeskTabColor.Gray,
    override val titleRes: Int = 0,
    override val descriptionRes: Int = 0,
) : DeskTool {
    override val icon: ImageVector = EmptyIcon
    override val startDestination: Any = id
    override fun NavGraphBuilder.toolGraph(navController: NavController) = Unit
}

private val EmptyIcon: ImageVector by lazy {
    ImageVector.Builder(defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f).build()
}
