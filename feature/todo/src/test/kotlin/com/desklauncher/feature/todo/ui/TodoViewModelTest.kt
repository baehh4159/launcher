package com.desklauncher.feature.todo.ui

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class TodoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 빠른 입력: 공백은 무시하고 앞뒤 공백을 지운 제목으로 추가
    @Test
    fun quickAdd_addsTrimmedTitleAndIgnoresBlank() = runTest {
        val repository = FakeTodoRepository()
        val viewModel = TodoViewModel(repository)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(TodoAction.QuickAdd("   "))
        viewModel.onAction(TodoAction.QuickAdd("  우유 사기 "))

        assertEquals(listOf("우유 사기"), viewModel.uiState.value.items.map { it.title })
    }

    // 편집 저장이 반영되고 시트가 닫힌다
    @Test
    fun save_updatesTodoAndClosesEditor() = runTest {
        val repository = FakeTodoRepository(listOf(todo("1", "초안")))
        val viewModel = TodoViewModel(repository)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(TodoAction.OpenEditor("1"))
        assertEquals("1", viewModel.uiState.value.editing?.id)
        viewModel.onAction(TodoAction.Save(repository.current.first().copy(title = "완성", note = "메모")))

        assertNull(viewModel.uiState.value.editing)
        assertEquals("완성", viewModel.uiState.value.items.single().title)
        assertEquals("메모", viewModel.uiState.value.items.single().note)
    }

    // 삭제 후 실행 취소로 그대로 되살린다
    @Test
    fun delete_thenUndo_restoresTodo() = runTest {
        val repository = FakeTodoRepository(listOf(todo("1"), todo("2")))
        val viewModel = TodoViewModel(repository)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(TodoAction.Delete("1"))
        assertEquals(listOf("2"), viewModel.uiState.value.items.map { it.id })
        assertEquals("1", viewModel.uiState.value.recentlyDeleted?.id)

        viewModel.onAction(TodoAction.UndoDelete)
        assertEquals(setOf("1", "2"), viewModel.uiState.value.items.map { it.id }.toSet())
        assertNull(viewModel.uiState.value.recentlyDeleted)
    }
}
