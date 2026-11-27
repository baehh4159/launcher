package com.desklauncher.feature.alarm

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.alarm.navigation.AlarmGraph
import com.desklauncher.feature.alarm.navigation.alarmGraph
import javax.inject.Inject


/** 도구 id — DB 키이자 탭 색 조회 키. 출시 후 변경 금지. */
const val AlarmToolId = "alarm"

/** 알람 도구. id "alarm"은 출시 후 변경 금지. */
class AlarmTool @Inject constructor() : DeskTool {
    override val id: String = AlarmToolId
    override val titleRes: Int = R.string.alarm_tool_title
    override val descriptionRes: Int = R.string.alarm_tool_description
    override val icon: ImageVector = Icons.Outlined.Alarm
    override val defaultTabColor: DeskTabColor = DeskTabColor.Indigo
    override val defaultEnabled: Boolean = true
    override val defaultOrder: Int = 7
    override val startDestination: Any = AlarmGraph

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        alarmGraph(navController)
    }
}
