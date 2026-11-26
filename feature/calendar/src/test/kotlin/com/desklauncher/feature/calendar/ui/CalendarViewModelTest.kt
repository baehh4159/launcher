package com.desklauncher.feature.calendar.ui

import androidx.lifecycle.SavedStateHandle
import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.calendar.domain.model.CalendarEvent
import com.desklauncher.feature.calendar.domain.model.DueTodo
import com.desklauncher.feature.calendar.domain.repository.CalendarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId

class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val zone = ZoneId.of("Asia/Seoul")
    private val clock = Clock.fixed(LocalDateTime.of(2026, 11, 26, 9, 0).atZone(zone).toInstant(), zone)
    private val repository = FakeCalendarRepository()

    // 오늘이 선택된 이번 달로 시작, 선택일 일정이 아젠다에
    @Test
    fun startsOnTodayWithAgenda() = runTest {
        repository.events.value = listOf(event("e1", 26, 15, 16), event("e2", 27, 9, 10))
        val viewModel = CalendarViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)

        val state = viewModel.uiState.value
        assertEquals(YearMonth.of(2026, 11), state.month)
        assertEquals(LocalDate.of(2026, 11, 26), state.selected)
        assertEquals(listOf("e_e1"), state.agenda.map { it.key })
        assertTrue(state.cells.first { it.date == LocalDate.of(2026, 11, 27) }.itemCount == 1)
    }

    // 다른 달 날짜를 고르면 그 달로 이동
    @Test
    fun selectingDateInOtherMonth_switchesMonth() = runTest {
        val viewModel = CalendarViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)

        viewModel.onAction(CalendarAction.Select(LocalDate.of(2026, 12, 3)))
        assertEquals(YearMonth.of(2026, 12), viewModel.uiState.value.month)

        viewModel.onAction(CalendarAction.PrevMonth)
        viewModel.onAction(CalendarAction.PrevMonth)
        assertEquals(YearMonth.of(2026, 10), viewModel.uiState.value.month)

        viewModel.onAction(CalendarAction.GoToday)
        assertEquals(YearMonth.of(2026, 11), viewModel.uiState.value.month)
        assertEquals(LocalDate.of(2026, 11, 26), viewModel.uiState.value.selected)
    }

    // 마감 할일이 일정 다음에 나오고 격자에 표시된다 (미완료 먼저)
    @Test
    fun dueTodos_appearInAgendaAndGrid() = runTest {
        val today = LocalDate.of(2026, 11, 26)
        repository.events.value = listOf(event("e1", 26, 9, 10))
        repository.todos.value = listOf(
            DueTodo("done", "끝낸 일", today, null, isDone = true),
            DueTodo("open", "할 일", today, java.time.LocalTime.of(18, 0), isDone = false),
        )
        val viewModel = CalendarViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)

        val state = viewModel.uiState.value
        assertEquals(listOf("e_e1", "t_open", "t_done"), state.agenda.map { it.key })
        assertTrue(state.cells.first { it.date == today }.hasOpenTodos)
        assertEquals(3, state.cells.first { it.date == today }.itemCount)
    }

    // 새 일정 → 저장 → 편집 → 삭제
    @Test
    fun eventLifecycle() = runTest {
        val viewModel = CalendarViewModel(repository, clock, SavedStateHandle())
        collectInBackground(viewModel.uiState)

        viewModel.onAction(CalendarAction.NewEvent)
        val draft = viewModel.uiState.value.editor!!
        assertEquals(LocalDate.of(2026, 11, 26), draft.startDate)
        viewModel.onAction(CalendarAction.SaveEvent(draft.copy(title = "점심")))
        assertEquals(null, viewModel.uiState.value.editor)
        val saved = viewModel.uiState.value.agenda.single() as AgendaItem.Event
        assertEquals("점심", saved.event.title)

        viewModel.onAction(CalendarAction.EditEvent(saved.event.id))
        assertEquals("점심", viewModel.uiState.value.editor?.title)
        viewModel.onAction(CalendarAction.DeleteEvent(saved.event.id))
        assertTrue(viewModel.uiState.value.agenda.isEmpty())
    }

    private fun event(id: String, day: Int, startHour: Int, endHour: Int) = CalendarEvent(
        id = id, title = id,
        start = LocalDateTime.of(2026, 11, day, startHour, 0).atZone(zone).toInstant(),
        end = LocalDateTime.of(2026, 11, day, endHour, 0).atZone(zone).toInstant(),
        createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH,
    )
}

internal class FakeCalendarRepository : CalendarRepository {
    val events = MutableStateFlow<List<CalendarEvent>>(emptyList())
    val todos = MutableStateFlow<List<DueTodo>>(emptyList())

    override fun events(from: Instant, to: Instant): Flow<List<CalendarEvent>> =
        events.map { list -> list.filter { it.start < to && it.end > from } }

    override fun dueTodos(from: LocalDate, to: LocalDate): Flow<List<DueTodo>> =
        todos.map { list -> list.filter { !it.date.isBefore(from) && !it.date.isAfter(to) } }

    override suspend fun getEvent(id: String) = events.value.firstOrNull { it.id == id }
    override suspend fun saveEvent(event: CalendarEvent) {
        events.value = events.value.filterNot { it.id == event.id } + event
    }
    override suspend fun deleteEvent(id: String) {
        events.value = events.value.filterNot { it.id == id }
    }
}
