package com.desklauncher.feature.todo.ui

import com.desklauncher.feature.todo.domain.model.ProjectRef
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.domain.model.TodoDraft
import com.desklauncher.feature.todo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

internal class FakeTodoRepository(
    initial: List<Todo> = emptyList(),
    projects: List<ProjectRef> = emptyList(),
    private val now: Instant = Instant.parse("2026-11-26T06:00:00Z"),
) : TodoRepository {
    private val state = MutableStateFlow(initial)
    override val todos = state
    override val projects = MutableStateFlow(projects)
    private var nextId = 100

    val current: List<Todo> get() = state.value

    override suspend fun add(draft: TodoDraft): String {
        val id = (nextId++).toString()
        state.value = state.value + Todo(
            id = id, title = draft.title, note = draft.note, priority = draft.priority, dueDate = draft.dueDate,
            dueTime = draft.dueTime, projectId = draft.projectId, createdAt = now, updatedAt = now,
        )
        return id
    }

    override suspend fun update(todo: Todo) {
        state.value = state.value.map { if (it.id == todo.id) todo else it }
    }

    override suspend fun delete(id: String): Todo? {
        val existing = state.value.firstOrNull { it.id == id } ?: return null
        state.value = state.value - existing
        return existing
    }

    override suspend fun restore(todo: Todo) {
        state.value = state.value + todo
    }

    override suspend fun setDone(id: String, done: Boolean) {
        state.value = state.value.map { if (it.id == id) it.copy(isDone = done, completedAt = if (done) now else null) else it }
    }
}

internal fun todo(id: String, title: String = "할일 $id") =
    Todo(id = id, title = title, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH)
