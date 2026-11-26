package com.desklauncher.feature.todo.ui

import androidx.lifecycle.SavedStateHandle
import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.todo.domain.TodoFilter
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class TodoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val clock = Clock.fixed(Instant.parse("2026-11-26T03:00:00Z"), ZoneId.of("Asia/Seoul"))
    private val today = LocalDate.of(2026, 11, 26)

    // '오늘' 보기에서 빠른 입력은 오늘 마감, 다른 보기에서는 마감 없음
    @Test
    fun quickAdd_setsDueTodayOnlyInTodayFilter() = runTest {
        val repository = FakeTodoRepository()
        val viewModel = TodoViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)

        viewModel.onAction(TodoAction.QuickAdd("오늘 것"))
        viewModel.onAction(TodoAction.SelectFilter(TodoFilter.All))
        viewModel.onAction(TodoAction.QuickAdd("언젠가"))

        assertEquals(today, repository.current.first { it.title == "오늘 것" }.dueDate)
        assertNull(repository.current.first { it.title == "언젠가" }.dueDate)
        assertEquals(2, viewModel.uiState.value.counts[TodoFilter.All])
        assertEquals(1, viewModel.uiState.value.counts[TodoFilter.Today])
    }

    // 빠른 입력: 공백은 무시하고 앞뒤 공백을 지운 제목으로 추가
    @Test
    fun quickAdd_addsTrimmedTitleAndIgnoresBlank() = runTest {
        val repository = FakeTodoRepository()
        val viewModel = TodoViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)
        viewModel.onAction(TodoAction.SelectFilter(TodoFilter.All))

        viewModel.onAction(TodoAction.QuickAdd("   "))
        viewModel.onAction(TodoAction.QuickAdd("  우유 사기 "))

        assertEquals(listOf("우유 사기"), viewModel.uiState.value.items.map { it.title })
    }

    // 편집 저장이 반영되고 시트가 닫힌다
    @Test
    fun save_updatesTodoAndClosesEditor() = runTest {
        val repository = FakeTodoRepository(listOf(todo("1", "초안")))
        val viewModel = TodoViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)
        viewModel.onAction(TodoAction.SelectFilter(TodoFilter.All))

        viewModel.onAction(TodoAction.OpenEditor("1"))
        assertEquals("1", viewModel.uiState.value.editing?.id)
        viewModel.onAction(TodoAction.Save(repository.current.first().copy(title = "완성", note = "메모")))

        assertNull(viewModel.uiState.value.editing)
        assertEquals("완성", viewModel.uiState.value.items.single().title)
        assertEquals("메모", viewModel.uiState.value.items.single().note)
    }

    // 완료하면 완료 목록으로, 되돌리면 다시 할 일로
    @Test
    fun setDone_movesBetweenActiveAndDone() = runTest {
        val repository = FakeTodoRepository(listOf(todo("1"), todo("2")))
        val viewModel = TodoViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)
        viewModel.onAction(TodoAction.SelectFilter(TodoFilter.All))

        viewModel.onAction(TodoAction.SetDone("1", true))
        assertEquals(listOf("2"), viewModel.uiState.value.items.map { it.id })
        assertEquals(listOf("1"), viewModel.uiState.value.done.map { it.id })

        viewModel.onAction(TodoAction.SetDone("1", false))
        assertEquals(setOf("1", "2"), viewModel.uiState.value.items.map { it.id }.toSet())
        assertEquals(emptyList<String>(), viewModel.uiState.value.done.map { it.id })
    }

    // 삭제 후 실행 취소로 그대로 되살린다
    @Test
    fun delete_thenUndo_restoresTodo() = runTest {
        val repository = FakeTodoRepository(listOf(todo("1"), todo("2")))
        val viewModel = TodoViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)
        viewModel.onAction(TodoAction.SelectFilter(TodoFilter.All))

        viewModel.onAction(TodoAction.Delete("1"))
        assertEquals(listOf("2"), viewModel.uiState.value.items.map { it.id })
        assertEquals("1", viewModel.uiState.value.recentlyDeleted?.id)

        viewModel.onAction(TodoAction.UndoDelete)
        assertEquals(setOf("1", "2"), viewModel.uiState.value.items.map { it.id }.toSet())
        assertNull(viewModel.uiState.value.recentlyDeleted)
    }
}
