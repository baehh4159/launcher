package com.desklauncher.feature.calendar.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class CalendarUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.of(2026, 1),
    val today: LocalDate = LocalDate.of(2026, 1, 1),
    val selected: LocalDate = LocalDate.of(2026, 1, 1),
    val cells: List<DayCell> = emptyList(),
    val agenda: List<AgendaItem> = emptyList(),
    /** 일정 편집 시트 */
    val editor: EventDraft? = null,
)

@Immutable
data class DayCell(
    val date: LocalDate,
    val inMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    /** 점으로 보일 일정 색 (최대 3개), null = 기본색 */
    val dots: List<String?>,
    val itemCount: Int,
)

sealed interface AgendaItem {
    val key: String

    data class Event(val event: CalendarEvent) : AgendaItem {
        override val key: String get() = "e_${event.id}"
    }
}

sealed interface CalendarAction {
    data object PrevMonth : CalendarAction
    data object NextMonth : CalendarAction
    data object GoToday : CalendarAction
    data class Select(val date: LocalDate) : CalendarAction
    data object NewEvent : CalendarAction
    data class EditEvent(val id: String) : CalendarAction
    data class SaveEvent(val draft: EventDraft) : CalendarAction
    data class DeleteEvent(val id: String) : CalendarAction
    data object DismissEditor : CalendarAction
    data object Back : CalendarAction
}
