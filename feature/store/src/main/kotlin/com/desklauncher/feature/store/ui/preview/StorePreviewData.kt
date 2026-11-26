package com.desklauncher.feature.store.ui.preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Checklist
import com.desklauncher.core.designsystem.text.UiText
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.feature.store.ui.StoreToolUi
import com.desklauncher.feature.store.ui.StoreUiState

internal object StorePreviewData {
    val tools = listOf(
        StoreToolUi("todo", UiText.Plain("할일정리"), UiText.Plain("오늘 할 일과 마감일을 정리해요"), Icons.Outlined.Checklist, DeskTabColor.Orange, true),
        StoreToolUi("calendar", UiText.Plain("캘린더"), UiText.Plain("월간 일정과 할일 마감을 한눈에"), Icons.Outlined.CalendarMonth, DeskTabColor.Red, true),
        StoreToolUi("calculator", UiText.Plain("계산기"), UiText.Plain("사칙연산과 계산 기록"), Icons.Outlined.Calculate, DeskTabColor.Teal, false),
        StoreToolUi("alarm", UiText.Plain("알람"), UiText.Plain("반복 알람과 다시 울림"), Icons.Outlined.Alarm, DeskTabColor.Indigo, true),
    )
    val loaded = StoreUiState(isLoading = false, tools = tools)
    val empty = StoreUiState(isLoading = false, tools = emptyList())
    val loading = StoreUiState(isLoading = true)
}
