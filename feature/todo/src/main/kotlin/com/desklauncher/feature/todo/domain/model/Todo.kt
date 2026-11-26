package com.desklauncher.feature.todo.domain.model

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class Todo(
    val id: String,
    val title: String,
    val note: String = "",
    val isDone: Boolean = false,
    val priority: TodoPriority = TodoPriority.None,
    val dueDate: LocalDate? = null,
    /** dueDate가 있을 때만 의미가 있다 */
    val dueTime: LocalTime? = null,
    val projectId: String? = null,
    val sortOrder: Int = 0,
    val completedAt: Instant? = null,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class TodoPriority { None, Low, Medium, High }

/** 새 할일 입력값 */
data class TodoDraft(
    val title: String,
    val note: String = "",
    val priority: TodoPriority = TodoPriority.None,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val projectId: String? = null,
)

/** 할일에 연결할 수 있는 프로젝트 (프로젝트 메모 도구 소유 데이터를 읽기 전용으로). */
data class ProjectRef(
    val id: String,
    val name: String,
    /** DeskTabColor 이름 */
    val colorName: String,
)
