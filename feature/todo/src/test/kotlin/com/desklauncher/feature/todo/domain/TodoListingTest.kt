package com.desklauncher.feature.todo.domain

import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.domain.model.TodoPriority
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

class TodoListingTest {

    private val day = LocalDate.of(2026, 11, 26)

    private fun todo(
        id: String,
        due: LocalDate? = null,
        time: LocalTime? = null,
        priority: TodoPriority = TodoPriority.None,
        createdAt: Long = 0,
        done: Boolean = false,
        completedAt: Long? = null,
    ) = Todo(
        id = id, title = id, isDone = done, priority = priority, dueDate = due, dueTime = time,
        completedAt = completedAt?.let(Instant::ofEpochMilli),
        createdAt = Instant.ofEpochMilli(createdAt), updatedAt = Instant.EPOCH,
    )

    // 마감일 → 시각 → 우선순위 → 생성 순, 마감 없는 항목은 맨 뒤
    @Test
    fun active_sortsByDueThenTimeThenPriority() {
        val result = TodoListing.active(
            listOf(
                todo("noDue", createdAt = 1),
                todo("tomorrow", due = day.plusDays(1)),
                todo("todayLate", due = day, time = LocalTime.of(18, 0)),
                todo("todayEarly", due = day, time = LocalTime.of(9, 0)),
                todo("todayNoTimeHigh", due = day, priority = TodoPriority.High),
                todo("todayNoTimeLow", due = day, priority = TodoPriority.Low),
                todo("done", due = day, done = true),
            ),
        )
        assertEquals(
            listOf("todayEarly", "todayLate", "todayNoTimeHigh", "todayNoTimeLow", "tomorrow", "noDue"),
            result.map { it.id },
        )
    }

    // 오늘 = 오늘까지(지난 것 포함), 예정 = 내일부터, 전체 = 미완료, 완료 = 완료
    @Test
    fun filter_splitsByDueDate() {
        val todos = listOf(
            todo("overdue", due = day.minusDays(2)),
            todo("today", due = day),
            todo("later", due = day.plusDays(5)),
            todo("noDue"),
            todo("doneToday", due = day, done = true, completedAt = 1),
        )
        assertEquals(listOf("overdue", "today"), TodoListing.filter(todos, TodoFilter.Today, day).map { it.id })
        assertEquals(listOf("later"), TodoListing.filter(todos, TodoFilter.Upcoming, day).map { it.id })
        assertEquals(listOf("overdue", "today", "later", "noDue"), TodoListing.filter(todos, TodoFilter.All, day).map { it.id })
        assertEquals(listOf("doneToday"), TodoListing.filter(todos, TodoFilter.Done, day).map { it.id })
        assertEquals(mapOf(TodoFilter.Today to 2, TodoFilter.Upcoming to 1, TodoFilter.All to 4, TodoFilter.Done to 1), TodoListing.counts(todos, day))
    }

    // 완료 목록은 최근 완료 순
    @Test
    fun done_sortsByCompletedAtDescending() {
        val result = TodoListing.done(
            listOf(todo("a", done = true, completedAt = 10), todo("b", done = true, completedAt = 30), todo("c")),
        )
        assertEquals(listOf("b", "a"), result.map { it.id })
    }
}
