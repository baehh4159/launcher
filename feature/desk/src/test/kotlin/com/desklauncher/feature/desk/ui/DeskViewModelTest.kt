package com.desklauncher.feature.desk.ui

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import com.desklauncher.core.designsystem.text.UiText
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.toolapi.ActiveTool
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.core.testing.FakeSettingsRepository
import com.desklauncher.core.toolapi.ToolRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeskViewModelTest {

    private val registry = FakeToolRegistry()

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // 레지스트리가 값을 내기 전에는 로딩 상태다
    @Test
    fun isLoading_untilRegistryEmits() = runTest {
        val viewModel = DeskViewModel(registry, FakeSettingsRepository())

        assertTrue(viewModel.uiState.value.isLoading)
    }

    // 켜진 도구만 탭으로 순서대로 보여 준다
    @Test
    fun tabs_containOnlyEnabledToolsInOrder() = runTest {
        val viewModel = DeskViewModel(registry, FakeSettingsRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        registry.emit(
            ActiveTool(FakeTool("todo", titleRes = 1), enabled = true, sortOrder = 0, tabColor = DeskTabColor.Orange),
            ActiveTool(FakeTool("memo", titleRes = 2), enabled = false, sortOrder = 1, tabColor = DeskTabColor.Blue),
            ActiveTool(FakeTool("alarm", titleRes = 3), enabled = true, sortOrder = 2, tabColor = DeskTabColor.Indigo),
        )

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("todo", "alarm"), state.tabs.map { it.toolId })
        assertEquals(UiText.Res(3), state.tabs.last().title)
        assertEquals(DeskTabColor.Indigo, state.tabs.last().color)
    }

    // 켜진 도구가 없으면 빈 상태다
    @Test
    fun tabs_areEmpty_whenNoToolEnabled() = runTest {
        val viewModel = DeskViewModel(registry, FakeSettingsRepository())
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        registry.emit()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.tabs.isEmpty())
    }

    private class FakeToolRegistry : ToolRegistry {
        private val tools = MutableStateFlow<List<ActiveTool>?>(null)

        fun emit(vararg activeTools: ActiveTool) {
            tools.value = activeTools.toList()
        }

        override val registeredTools: List<DeskTool> get() = tools.value.orEmpty().map { it.tool }
        override val allTools: Flow<List<ActiveTool>> = tools.filterNotNull()
        override val enabledTools: Flow<List<ActiveTool>> = allTools.map { list -> list.filter { it.enabled } }

        override fun findTool(toolId: String): DeskTool? = registeredTools.firstOrNull { it.id == toolId }
        override suspend fun setEnabled(toolId: String, enabled: Boolean) = Unit
        override suspend fun reorder(orderedToolIds: List<String>) = Unit
        override suspend fun setTabColor(toolId: String, tabColor: DeskTabColor?) = Unit
    }

    private class FakeTool(override val id: String, override val titleRes: Int) : DeskTool {
        override val descriptionRes: Int = 0
        override val icon: ImageVector get() = FakeIcon
        override val defaultTabColor: DeskTabColor = DeskTabColor.Gray
        override val defaultEnabled: Boolean = true
        override val defaultOrder: Int = 0
        override val startDestination: Any = Unit
        override fun NavGraphBuilder.toolGraph(navController: NavController) = Unit
    }

    private companion object {
        val FakeIcon: ImageVector = ImageVector.Builder(
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).build()
    }
}
