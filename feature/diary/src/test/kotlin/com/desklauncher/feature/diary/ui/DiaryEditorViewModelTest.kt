package com.desklauncher.feature.diary.ui

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.feature.diary.domain.model.DiaryEntry
import com.desklauncher.feature.diary.domain.model.Mood
import com.desklauncher.feature.diary.domain.repository.DiaryRepository
import com.desklauncher.feature.diary.navigation.DiaryEditorDestination
import com.desklauncher.feature.diary.ui.editor.DiaryEditorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class DiaryEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val zone = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(Instant.parse("2026-11-26T03:00:00Z"), zone)
    private val repository = FakeDiaryRepository()

    // 날짜 없이 열면 오늘, 기분만 골라도 저장된다
    @Test
    fun opensTodayAndSavesMoodOnly() = runTest {
        val viewModel = DiaryEditorViewModel(DiaryEditorDestination(), repository, backgroundScope, clock)
        advanceUntilIdle()
        assertEquals(LocalDate.of(2026, 11, 26), viewModel.date)

        viewModel.onMoodChange(Mood.Good)
        advanceUntilIdle()

        assertEquals(Mood.Good, repository.state.value.single().mood)
    }

    // 같은 날짜를 다시 열면 기존 일기를 이어서 고친다 (하루 1건)
    @Test
    fun reopenSameDate_updatesExistingEntry() = runTest {
        val day = LocalDate.of(2026, 11, 20)
        repository.save(DiaryEntry("d1", day, Mood.Bad, "처음", Instant.EPOCH, Instant.EPOCH))
        val viewModel = DiaryEditorViewModel(DiaryEditorDestination(day.toEpochDay()), repository, backgroundScope, clock)
        advanceUntilIdle()
        assertEquals("처음", viewModel.body)

        viewModel.onBodyChange("고침")
        advanceUntilIdle()

        val entry = repository.state.value.single()
        assertEquals("d1", entry.id)
        assertEquals("고침", entry.body)
    }

    // 아무것도 안 적으면 만들지 않는다
    @Test
    fun emptyEntry_isNotCreated() = runTest {
        val viewModel = DiaryEditorViewModel(DiaryEditorDestination(), repository, backgroundScope, clock)
        viewModel.onBodyChange("a")
        viewModel.onBodyChange("")
        advanceUntilIdle()
        assertTrue(repository.state.value.isEmpty())
    }
}

internal class FakeDiaryRepository : DiaryRepository {
    val state = MutableStateFlow<List<DiaryEntry>>(emptyList())
    override val entries = state
    override suspend fun get(date: LocalDate) = state.value.firstOrNull { it.date == date }
    override suspend fun save(entry: DiaryEntry) {
        state.value = (state.value.filterNot { it.date == entry.date } + entry).sortedByDescending { it.date }
    }
    override suspend fun delete(date: LocalDate) {
        state.value = state.value.filterNot { it.date == date }
    }
}
