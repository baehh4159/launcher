package com.desklauncher.feature.diary.ui

import androidx.lifecycle.SavedStateHandle
import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.diary.domain.model.DiaryEntry
import com.desklauncher.feature.diary.domain.model.Mood
import com.desklauncher.feature.diary.ui.list.DiaryListAction
import com.desklauncher.feature.diary.ui.list.DiaryListViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

class DiaryListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(Instant.parse("2026-11-26T03:00:00Z"), ZoneId.of("Asia/Seoul"))

    // 최신 순 목록 + 작성일·기분 지도, 달 이동
    @Test
    fun listsEntriesAndWrittenDays() = runTest {
        val repository = FakeDiaryRepository()
        repository.save(DiaryEntry("a", LocalDate.of(2026, 11, 20), Mood.Bad, "x", Instant.EPOCH, Instant.EPOCH))
        repository.save(DiaryEntry("b", LocalDate.of(2026, 11, 25), null, "y", Instant.EPOCH, Instant.EPOCH))
        val viewModel = DiaryListViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)

        val state = viewModel.uiState.value
        assertEquals(listOf("b", "a"), state.entries.map { it.id })
        assertEquals(mapOf(LocalDate.of(2026, 11, 25) to null, LocalDate.of(2026, 11, 20) to Mood.Bad), state.written)
        assertEquals(42, state.days.size)

        viewModel.onAction(DiaryListAction.NextMonth)
        assertEquals(YearMonth.of(2026, 12), viewModel.uiState.value.month)
    }
}
