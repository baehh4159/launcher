package com.desklauncher.feature.calendar.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.feature.calendar.domain.MonthGrid
import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import com.desklauncher.feature.calendar.domain.repository.CalendarRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val repository: CalendarRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val today: LocalDate get() = LocalDate.now(clock)
    private val month = savedStateHandle.getStateFlow(MonthKey, YearMonth.now(clock).toString()).map(YearMonth::parse)
    private val selected = savedStateHandle.getStateFlow(SelectedKey, LocalDate.now(clock).toString()).map(LocalDate::parse)

    val uiState: StateFlow<CalendarUiState> = combine(month, selected, ::Pair)
        .flatMapLatest { (m, sel) ->
            val (from, to) = MonthGrid.gridRange(m, clock.zone)
            repository.events(from, to).map { events -> buildState(m, sel, events) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CalendarUiState())

    fun onAction(action: CalendarAction) {
        val current = YearMonth.parse(savedStateHandle[MonthKey] ?: YearMonth.now(clock).toString())
        when (action) {
            CalendarAction.PrevMonth -> savedStateHandle[MonthKey] = current.minusMonths(1).toString()
            CalendarAction.NextMonth -> savedStateHandle[MonthKey] = current.plusMonths(1).toString()
            CalendarAction.GoToday -> select(today)
            is CalendarAction.Select -> select(action.date)
            CalendarAction.Back -> Unit // Route가 처리
        }
    }

    private fun select(date: LocalDate) {
        savedStateHandle[SelectedKey] = date.toString()
        savedStateHandle[MonthKey] = YearMonth.from(date).toString()
    }

    private fun buildState(month: YearMonth, selected: LocalDate, events: List<CalendarEvent>): CalendarUiState {
        val zone = clock.zone
        val today = today
        val cells = MonthGrid.days(month).map { date ->
            val dayEvents = MonthGrid.eventsOn(date, events, zone)
            DayCell(
                date = date,
                inMonth = YearMonth.from(date) == month,
                isToday = date == today,
                isSelected = date == selected,
                dots = dayEvents.take(MaxDots).map { it.colorName },
                itemCount = dayEvents.size,
            )
        }
        return CalendarUiState(
            isLoading = false,
            month = month,
            today = today,
            selected = selected,
            cells = cells,
            agenda = MonthGrid.eventsOn(selected, events, zone).map(AgendaItem::Event),
        )
    }

    private companion object {
        const val MonthKey = "month"
        const val SelectedKey = "selected"
        const val MaxDots = 3
    }
}
