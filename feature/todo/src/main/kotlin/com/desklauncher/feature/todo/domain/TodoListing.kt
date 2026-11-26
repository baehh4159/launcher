package com.desklauncher.feature.todo.domain

import com.desklauncher.feature.todo.domain.model.Todo
import java.time.LocalDate

/** 할일 보기 (TODO-03) */
enum class TodoFilter {
    /** 오늘까지 마감(지난 것 포함) */
    Today,

    /** 내일 이후 마감 */
    Upcoming,

    /** 미완료 전체 */
    All,

    /** 완료 */
    Done,
}

/** 할일 목록 정렬·분류 (순수 Kotlin). */
object TodoListing {

    /** 미완료: 마감일 빠른 순(없으면 뒤) → 마감 시각 → 우선순위 높은 순 → 만든 순 */
    private val activeOrder: Comparator<Todo> =
        compareBy<Todo, Long?>(nullsLast()) { it.dueDate?.toEpochDay() }
            .thenBy(nullsLast()) { it.dueTime?.toSecondOfDay() }
            .thenByDescending { it.priority.ordinal }
            .thenBy { it.createdAt }

    fun active(todos: List<Todo>): List<Todo> = todos.filterNot { it.isDone }.sortedWith(activeOrder)

    fun filter(todos: List<Todo>, filter: TodoFilter, today: LocalDate): List<Todo> = when (filter) {
        TodoFilter.Today -> active(todos).filter { it.dueDate != null && !it.dueDate.isAfter(today) }
        TodoFilter.Upcoming -> active(todos).filter { it.dueDate != null && it.dueDate.isAfter(today) }
        TodoFilter.All -> active(todos)
        TodoFilter.Done -> done(todos)
    }

    fun counts(todos: List<Todo>, today: LocalDate): Map<TodoFilter, Int> =
        TodoFilter.entries.associateWith { filter(todos, it, today).size }

    /** 완료: 최근에 끝낸 순 */
    fun done(todos: List<Todo>): List<Todo> =
        todos.filter { it.isDone }.sortedByDescending { it.completedAt }
}
