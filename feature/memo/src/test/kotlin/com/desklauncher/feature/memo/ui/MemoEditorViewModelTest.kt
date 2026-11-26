package com.desklauncher.feature.memo.ui

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.feature.memo.domain.model.Memo
import com.desklauncher.feature.memo.navigation.MemoEditorDestination
import com.desklauncher.feature.memo.ui.editor.MemoEditorViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class MemoEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val repository = FakeMemoRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-26T06:00:00Z"), ZoneOffset.UTC)

    // 입력을 멈추고 잠시 뒤 한 번 저장 (그 전에는 저장 안 함)
    @Test
    fun autosave_afterDebounce() = runTest {
        val viewModel = MemoEditorViewModel(MemoEditorDestination("p1"), repository, backgroundScope, clock)
        advanceUntilIdle()

        viewModel.onTitleChange("회의")
        viewModel.onBodyChange("안건 1")
        advanceTimeBy(MemoEditorViewModel.AutoSaveDelayMillis - 100)
        assertTrue(repository.memoState.value.isEmpty())

        advanceTimeBy(200)
        val saved = repository.memoState.value.single()
        assertEquals("회의", saved.title)
        assertEquals("안건 1", saved.body)
        assertEquals("p1", saved.projectId)
        assertTrue(viewModel.isSaved)
    }

    // 새 메모를 아무것도 적지 않고 지우면 만들지 않는다
    @Test
    fun blankNewMemo_isNotCreated() = runTest {
        val viewModel = MemoEditorViewModel(MemoEditorDestination("p1"), repository, backgroundScope, clock)
        viewModel.onTitleChange("a")
        viewModel.onTitleChange("")
        advanceUntilIdle()

        assertTrue(repository.memoState.value.isEmpty())
    }

    // 기존 메모를 불러와 수정
    @Test
    fun existingMemo_isLoadedAndUpdated() = runTest {
        repository.saveMemo(Memo("m1", "p1", title = "원래", body = "본문", createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH))
        val viewModel = MemoEditorViewModel(MemoEditorDestination("p1", "m1"), repository, backgroundScope, clock)
        advanceUntilIdle()
        assertEquals("원래", viewModel.title)

        viewModel.onBodyChange("고친 본문")
        advanceUntilIdle()

        assertEquals("고친 본문", repository.memoState.value.single().body)
        assertEquals(Instant.EPOCH, repository.memoState.value.single().createdAt)
    }
}
