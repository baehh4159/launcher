package com.desklauncher.feature.todo.domain

import com.desklauncher.feature.todo.domain.model.Todo

/** 할일 목록 정렬·분류 (순수 Kotlin). */
object TodoListing {

    /** 미완료: 마감일 빠른 순(없으면 뒤) → 마감 시각 → 우선순위 높은 순 → 만든 순 */
    private val activeOrder: Comparator<Todo> =
        compareBy<Todo, Long?>(nullsLast()) { it.dueDate?.toEpochDay() }
            .thenBy(nullsLast()) { it.dueTime?.toSecondOfDay() }
            .thenByDescending { it.priority.ordinal }
            .thenBy { it.createdAt }

    fun active(todos: List<Todo>): List<Todo> = todos.filterNot { it.isDone }.sortedWith(activeOrder)

    /** 완료: 최근에 끝낸 순 */
    fun done(todos: List<Todo>): List<Todo> =
        todos.filter { it.isDone }.sortedByDescending { it.completedAt }
}
