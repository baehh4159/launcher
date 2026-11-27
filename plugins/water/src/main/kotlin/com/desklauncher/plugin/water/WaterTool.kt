package com.desklauncher.plugin.water

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskToolHost
import com.desklauncher.core.toolapi.SimpleDeskTool
import com.desklauncher.plugin.water.ui.WaterRoute
import javax.inject.Inject

/** 도구 id — DB 키. 출시 후 변경 금지. */
const val WaterToolId = "water"

/**
 * 예시 플러그인. [SimpleDeskTool]이라 라우트·그래프 없이 [Content]만 구현한다.
 * 기본으로 꺼져 있어 도구함에서 켜야 책상에 꽂힌다.
 */
class WaterTool @Inject constructor() : SimpleDeskTool() {
    override val id: String = WaterToolId
    override val titleRes: Int = R.string.water_tool_title
    override val descriptionRes: Int = R.string.water_tool_description
    override val icon: ImageVector = Icons.Outlined.LocalDrink
    override val defaultTabColor: DeskTabColor = DeskTabColor.Blue
    override val defaultEnabled: Boolean = false
    override val defaultOrder: Int = 100

    @Composable
    override fun Content(host: DeskToolHost, modifier: Modifier) {
        WaterRoute(host = host, modifier = modifier)
    }
}
