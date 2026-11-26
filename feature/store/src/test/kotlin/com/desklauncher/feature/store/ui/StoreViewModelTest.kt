package com.desklauncher.feature.store.ui

import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.testing.FakeToolRegistry
import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.activeTool
import com.desklauncher.core.testing.collectInBackground
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

class StoreViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val registry = FakeToolRegistry().apply {
        emit(
            activeTool("todo", order = 0, color = DeskTabColor.Orange),
            activeTool("memo", order = 1, enabled = false),
            activeTool("alarm", order = 2),
        )
    }

    // 꺼진 도구도 도구함에는 모두 보인다
    @Test
    fun uiState_listsAllToolsIncludingDisabled() = runTest {
        val viewModel = StoreViewModel(registry)
        collectInBackground(viewModel.uiState)

        assertEquals(listOf("todo", "memo", "alarm"), viewModel.uiState.value.tools.map { it.id })
        assertEquals(listOf(true, false, true), viewModel.uiState.value.tools.map { it.enabled })
    }

    // 켜기/끄기가 레지스트리에 반영된다
    @Test
    fun setEnabled_updatesRegistry() = runTest {
        val viewModel = StoreViewModel(registry)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(StoreAction.SetEnabled("memo", true))

        assertEquals(true, viewModel.uiState.value.tools.first { it.id == "memo" }.enabled)
    }

    // 순서 변경이 저장된다
    @Test
    fun reorder_updatesOrder() = runTest {
        val viewModel = StoreViewModel(registry)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(StoreAction.Reorder(listOf("alarm", "todo", "memo")))

        assertEquals(listOf("alarm", "todo", "memo"), viewModel.uiState.value.tools.map { it.id })
    }

    // 색 선택 후 대화상자가 닫히고, null이면 기본 색
    @Test
    fun setColor_closesPickerAndAppliesColor() = runTest {
        val viewModel = StoreViewModel(registry)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(StoreAction.OpenColorPicker("todo"))
        assertEquals("todo", viewModel.uiState.value.colorPickerToolId)

        viewModel.onAction(StoreAction.SetColor("todo", DeskTabColor.Green))
        assertNull(viewModel.uiState.value.colorPickerToolId)
        assertEquals(DeskTabColor.Green, viewModel.uiState.value.tools.first().color)

        viewModel.onAction(StoreAction.SetColor("todo", null))
        assertEquals(DeskTabColor.Orange, viewModel.uiState.value.tools.first().color)
    }
}
