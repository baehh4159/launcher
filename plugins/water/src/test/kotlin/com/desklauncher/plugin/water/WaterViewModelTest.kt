package com.desklauncher.plugin.water

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.core.toolapi.InMemoryPluginStorage
import com.desklauncher.plugin.water.data.WaterStore
import com.desklauncher.plugin.water.domain.WaterDay
import com.desklauncher.plugin.water.ui.WaterAction
import com.desklauncher.plugin.water.ui.WaterUiState
import com.desklauncher.plugin.water.ui.WaterViewModel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class WaterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 11, 27)

    // 한 잔 마시면 오늘 칸에 저장되고 화면 상태가 바뀐다
    @Test
    fun drink_incrementsTodayCups() = runTest {
        val storage = InMemoryPluginStorage()
        val viewModel = WaterViewModel(WaterStore(storage), today = { today })
        collectInBackground(viewModel.uiState)

        viewModel.onAction(WaterAction.Drink)
        viewModel.onAction(WaterAction.Drink)

        assertEquals(2, (viewModel.uiState.value as WaterUiState.Ready).today.cups)
        assertEquals("2", storage.get("day:2026-11-27"))
    }

    // 0잔에서 되돌리기는 음수가 되지 않는다
    @Test
    fun undo_staysAtZero_whenEmpty() = runTest {
        val viewModel = WaterViewModel(WaterStore(InMemoryPluginStorage()), today = { today })
        collectInBackground(viewModel.uiState)

        viewModel.onAction(WaterAction.Undo)

        assertEquals(0, (viewModel.uiState.value as WaterUiState.Ready).today.cups)
    }

    // 저장된 목표가 없으면 기본 8잔, 목표 도달 여부 계산
    @Test
    fun state_usesDefaultGoal_andReportsReached() = runTest {
        val storage = InMemoryPluginStorage(mapOf("day:2026-11-27" to "8"))
        val viewModel = WaterViewModel(WaterStore(storage), today = { today })
        collectInBackground(viewModel.uiState)

        val day = (viewModel.uiState.value as WaterUiState.Ready).today
        assertEquals(WaterDay.DefaultGoal, day.goal)
        assertTrue(day.reachedGoal)
    }

    // 다른 날 기록은 오늘에 섞이지 않는다
    @Test
    fun state_ignoresOtherDays() = runTest {
        val storage = InMemoryPluginStorage(mapOf("day:2026-11-26" to "6"))
        val viewModel = WaterViewModel(WaterStore(storage), today = { today })
        collectInBackground(viewModel.uiState)

        assertEquals(0, (viewModel.uiState.value as WaterUiState.Ready).today.cups)
    }
}
