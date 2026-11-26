package com.desklauncher.feature.diary.ui.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.feature.diary.domain.model.DiaryEntry
import com.desklauncher.feature.diary.domain.model.Mood
import com.desklauncher.feature.diary.domain.repository.DiaryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters
import javax.inject.Inject

@Immutable
data class DiaryListUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.of(2026, 1, 1),
    val month: YearMonth = YearMonth.of(2026, 1),
    /** 일요일 시작 6주 */
    val days: List<LocalDate> = emptyList(),
    /** 날짜별 기분 (작성한 날만, 기분 없이 쓴 날은 null 값) */
    val written: Map<LocalDate, Mood?> = emptyMap(),
    val entries: List<DiaryEntry> = emptyList(),
)

sealed interface DiaryListAction {
    data object PrevMonth : DiaryListAction
    data object NextMonth : DiaryListAction
    data class Open(val date: LocalDate) : DiaryListAction
    data object Back : DiaryListAction
}

/** 일기 목록 + 작성일 달력 (DIARY-02). */
@HiltViewModel
class DiaryListViewModel @Inject constructor(
    repository: DiaryRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val month = savedStateHandle.getStateFlow(MonthKey, YearMonth.now(clock).toString()).map(YearMonth::parse)

    val uiState: StateFlow<DiaryListUiState> = combine(repository.entries, month) { entries, m ->
        DiaryListUiState(
            isLoading = false,
            today = LocalDate.now(clock),
            month = m,
            days = monthDays(m),
            written = entries.associate { it.date to it.mood },
            entries = entries,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DiaryListUiState())

    fun onAction(action: DiaryListAction) {
        val current = YearMonth.parse(savedStateHandle[MonthKey] ?: YearMonth.now(clock).toString())
        when (action) {
            DiaryListAction.PrevMonth -> savedStateHandle[MonthKey] = current.minusMonths(1).toString()
            DiaryListAction.NextMonth -> savedStateHandle[MonthKey] = current.plusMonths(1).toString()
            is DiaryListAction.Open, DiaryListAction.Back -> Unit // Route가 처리
        }
    }

    private companion object {
        const val MonthKey = "month"

        fun monthDays(month: YearMonth): List<LocalDate> {
            val start = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
            return List(42) { start.plusDays(it.toLong()) }
        }
    }
}
