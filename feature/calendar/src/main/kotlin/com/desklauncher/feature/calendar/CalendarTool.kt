package com.desklauncher.feature.calendar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.calendar.navigation.CalendarGraph
import com.desklauncher.feature.calendar.navigation.calendarGraph
import javax.inject.Inject


/** 도구 id — DB 키이자 탭 색 조회 키. 출시 후 변경 금지. */
const val CalendarToolId = "calendar"

/** 캘린더 도구. id "calendar"는 출시 후 변경 금지. */
class CalendarTool @Inject constructor() : DeskTool {
    override val id: String = CalendarToolId
    override val titleRes: Int = R.string.calendar_tool_title
    override val descriptionRes: Int = R.string.calendar_tool_description
    override val icon: ImageVector = Icons.Outlined.CalendarMonth
    override val defaultTabColor: DeskTabColor = DeskTabColor.Red
    override val defaultEnabled: Boolean = true
    override val defaultOrder: Int = 1
    override val startDestination: Any = CalendarGraph

    override fun NavGraphBuilder.toolGraph(navController: NavController) {
        calendarGraph(onBack = { navController.popBackStack() })
    }
}
