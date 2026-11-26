package com.desklauncher.feature.todo.data

import com.desklauncher.core.database.entity.ProjectEntity
import com.desklauncher.core.database.entity.TodoEntity
import com.desklauncher.feature.todo.domain.model.ProjectRef
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.domain.model.TodoPriority
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

internal fun TodoEntity.toDomain(): Todo = Todo(
    id = id,
    title = title,
    note = note,
    isDone = isDone,
    priority = priorityFromDb(priority),
    dueDate = dueDate?.let(LocalDate::ofEpochDay),
    dueTime = dueTime?.takeIf { dueDate != null }?.let { LocalTime.ofSecondOfDay(it * 60L) },
    projectId = projectId,
    sortOrder = sortOrder,
    completedAt = completedAt?.let(Instant::ofEpochMilli),
    createdAt = Instant.ofEpochMilli(createdAt),
    updatedAt = Instant.ofEpochMilli(updatedAt),
)

internal fun Todo.toEntity(): TodoEntity = TodoEntity(
    id = id,
    title = title,
    note = note,
    isDone = isDone,
    priority = priority.toDb(),
    dueDate = dueDate?.toEpochDay(),
    dueTime = if (dueDate != null) dueTime?.let { it.toSecondOfDay() / 60 } else null,
    projectId = projectId,
    sortOrder = sortOrder,
    completedAt = completedAt?.toEpochMilli(),
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli(),
)

internal fun ProjectEntity.toRef(): ProjectRef = ProjectRef(id = id, name = name, colorName = color)

// DB 값은 대문자 이름 (PRD §8.3)
private fun TodoPriority.toDb(): String = name.uppercase()

private fun priorityFromDb(value: String): TodoPriority =
    TodoPriority.entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: TodoPriority.None
