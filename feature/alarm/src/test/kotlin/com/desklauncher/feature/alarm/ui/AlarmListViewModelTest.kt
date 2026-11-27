package com.desklauncher.feature.alarm.ui

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.domain.repository.AlarmRepository
import com.desklauncher.feature.alarm.ui.list.AlarmListAction
import com.desklauncher.feature.alarm.ui.list.AlarmListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(LocalDateTime.of(2026, 11, 26, 9, 0).atZone(zone).toInstant(), zone)

    // 추가하면 남은 시간이 계산되고, 끄면 남은 시간이 없다. 삭제 후 실행 취소
    @Test
    fun addToggleDeleteUndo() = runTest {
        val repository = FakeAlarmRepository()
        val viewModel = AlarmListViewModel(repository, clock)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(AlarmListAction.Add(LocalTime.of(9, 30)))
        val item = viewModel.uiState.value.items.single()
        assertEquals(Duration.ofMinutes(30), item.untilNext)

        viewModel.onAction(AlarmListAction.SetEnabled(item.alarm, false))
        assertNull(viewModel.uiState.value.items.single().untilNext)

        viewModel.onAction(AlarmListAction.Delete(viewModel.uiState.value.items.single().alarm))
        assertEquals(0, viewModel.uiState.value.items.size)
        viewModel.onAction(AlarmListAction.UndoDelete)
        assertEquals(1, viewModel.uiState.value.items.size)
    }
}

internal class FakeAlarmRepository : AlarmRepository {
    val state = MutableStateFlow<List<Alarm>>(emptyList())
    override val alarms = state.map { list -> list.sortedBy { it.time } }
    override suspend fun get(id: String) = state.value.firstOrNull { it.id == id }
    override suspend fun getEnabled() = state.value.filter { it.enabled }
    override suspend fun save(alarm: Alarm) {
        state.value = state.value.filterNot { it.id == alarm.id } + alarm
    }
    override suspend fun delete(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}
