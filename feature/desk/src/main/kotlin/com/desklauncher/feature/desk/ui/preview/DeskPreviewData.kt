package com.desklauncher.feature.desk.ui.preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.StickyNote2
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.FolderOpen
import com.desklauncher.core.designsystem.text.UiText
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.feature.desk.clock.DeskClockText
import com.desklauncher.feature.desk.ui.DeskTabUi
import com.desklauncher.feature.desk.ui.DeskUiState

/** Preview 전용 가짜 데이터 (Hilt·DB 없이 동작). */
internal object DeskPreviewData {

    val clock = DeskClockText(time = "오후 3:24", date = "11월 26일 목요일")

    val loading = DeskUiState(isLoading = true)

    val empty = DeskUiState(isLoading = false, tabs = emptyList())

    val withTabs = DeskUiState(
        isLoading = false,
        tabs = listOf(
            DeskTabUi("todo", UiText.Plain("할일정리"), Icons.Outlined.Checklist, DeskTabColor.Orange),
            DeskTabUi("calendar", UiText.Plain("캘린더"), Icons.Outlined.CalendarMonth, DeskTabColor.Red),
            DeskTabUi("memo", UiText.Plain("프로젝트 메모"), Icons.AutoMirrored.Outlined.StickyNote2, DeskTabColor.Yellow),
            DeskTabUi("cabinet", UiText.Plain("서류정리함"), Icons.Outlined.FolderOpen, DeskTabColor.Brown),
            DeskTabUi("alarm", UiText.Plain("알람"), Icons.Outlined.Alarm, DeskTabColor.Indigo),
        ),
    )
}
