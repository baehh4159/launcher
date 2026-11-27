package com.desklauncher.feature.alarm.ui

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.alarm.domain.AlarmScheduler
import com.desklauncher.feature.alarm.domain.AlarmSync
import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.domain.repository.AlarmRepository
import com.desklauncher.feature.alarm.ui.list.AlarmListAction
import com.desklauncher.feature.alarm.ui.list.AlarmListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(LocalDateTime.of(2026, 11, 26, 9, 0).atZone(zone).toInstant(), zone)
    private val scheduler = FakeAlarmScheduler()

    // 켜면 예약, 끄면 취소, 지우면 취소 — 저장과 예약이 항상 함께
    @Test
    fun changes_keepSystemScheduleInSync() = runTest {
        val repository = FakeAlarmRepository()
        val viewModel = AlarmListViewModel(repository, AlarmSync(repository, scheduler, clock), scheduler, clock)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(AlarmListAction.Add(LocalTime.of(7, 0)))
        val alarm = viewModel.uiState.value.items.single().alarm
        assertEquals(LocalDateTime.of(2026, 11, 27, 7, 0).atZone(zone).toInstant(), scheduler.scheduled[alarm.id])
        assertEquals(scheduler.scheduled[alarm.id], alarm.nextTriggerAt)

        viewModel.onAction(AlarmListAction.SetEnabled(alarm, false))
        assertNull(scheduler.scheduled[alarm.id])

        viewModel.onAction(AlarmListAction.SetEnabled(viewModel.uiState.value.items.single().alarm, true))
        viewModel.onAction(AlarmListAction.Delete(viewModel.uiState.value.items.single().alarm))
        assertNull(scheduler.scheduled[alarm.id])
    }

    // 권한이 없으면 배너, 허용하고 돌아오면 밀린 알람을 예약
    @Test
    fun missingPermission_showsBannerAndReschedulesWhenGranted() = runTest {
        val repository = FakeAlarmRepository()
        scheduler.allowed = false
        val viewModel = AlarmListViewModel(repository, AlarmSync(repository, scheduler, clock), scheduler, clock)
        collectInBackground(viewModel.uiState)
        viewModel.onAction(AlarmListAction.Add(LocalTime.of(7, 0)))
        assertTrue(viewModel.uiState.value.needsExactAlarmPermission)
        assertTrue(scheduler.scheduled.isEmpty())

        scheduler.allowed = true
        viewModel.onAction(AlarmListAction.RefreshPermission)
        assertEquals(1, scheduler.scheduled.size)
        assertEquals(false, viewModel.uiState.value.needsExactAlarmPermission)
    }

    // 추가하면 남은 시간이 계산되고, 끄면 남은 시간이 없다. 삭제 후 실행 취소
    @Test
    fun addToggleDeleteUndo() = runTest {
        val repository = FakeAlarmRepository()
        val viewModel = AlarmListViewModel(repository, AlarmSync(repository, scheduler, clock), scheduler, clock)
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

    // 편집 저장: 반복 요일·라벨이 반영되고 시트가 닫힌다
    @Test
    fun editAndSave() = runTest {
        val repository = FakeAlarmRepository()
        val viewModel = AlarmListViewModel(repository, AlarmSync(repository, scheduler, clock), scheduler, clock)
        collectInBackground(viewModel.uiState)
        viewModel.onAction(AlarmListAction.Add(LocalTime.of(7, 0)))
        val alarm = viewModel.uiState.value.items.single().alarm

        viewModel.onAction(AlarmListAction.Edit(alarm))
        assertEquals(alarm.id, viewModel.uiState.value.editing?.id)
        viewModel.onAction(AlarmListAction.Save(alarm.copy(label = "출근", repeatDays = setOf(DayOfWeek.FRIDAY))))

        assertNull(viewModel.uiState.value.editing)
        val saved = viewModel.uiState.value.items.single()
        assertEquals("출근", saved.alarm.label)
        // 목(9시) → 금 7시 = 22시간 후
        assertEquals(Duration.ofHours(22), saved.untilNext)
    }
}

internal class FakeAlarmScheduler : AlarmScheduler {
    var allowed = true
    val scheduled = mutableMapOf<String, Instant>()
    override fun canScheduleExact() = allowed
    override fun schedule(alarmId: String, at: Instant): Boolean {
        if (!allowed) return false
        scheduled[alarmId] = at
        return true
    }
    override fun scheduleSnooze(alarmId: String, at: Instant) = allowed
    override fun cancel(alarmId: String) {
        scheduled.remove(alarmId)
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
