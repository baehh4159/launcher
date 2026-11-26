package com.desklauncher.feature.todo.data

import com.desklauncher.core.database.dao.ProjectDao
import com.desklauncher.core.database.dao.TodoDao
import com.desklauncher.feature.todo.domain.model.ProjectRef
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.domain.model.TodoDraft
import com.desklauncher.feature.todo.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.util.UUID
import javax.inject.Inject

internal class RoomTodoRepository @Inject constructor(
    private val todoDao: TodoDao,
    projectDao: ProjectDao,
    private val clock: Clock,
) : TodoRepository {

    override val todos: Flow<List<Todo>> = todoDao.observeAll().map { list -> list.map { it.toDomain() } }

    override val projects: Flow<List<ProjectRef>> = projectDao.observeActive().map { list -> list.map { it.toRef() } }

    override suspend fun add(draft: TodoDraft): String {
        val now = clock.instant()
        val todo = Todo(
            id = UUID.randomUUID().toString(),
            title = draft.title.trim(),
            note = draft.note,
            priority = draft.priority,
            dueDate = draft.dueDate,
            dueTime = draft.dueTime,
            projectId = draft.projectId,
            createdAt = now,
            updatedAt = now,
        )
        todoDao.upsert(todo.toEntity())
        return todo.id
    }

    override suspend fun update(todo: Todo) {
        todoDao.upsert(todo.copy(title = todo.title.trim(), updatedAt = clock.instant()).toEntity())
    }

    override suspend fun delete(id: String): Todo? {
        val existing = todoDao.get(id)?.toDomain() ?: return null
        todoDao.delete(id)
        return existing
    }

    override suspend fun restore(todo: Todo) {
        todoDao.upsert(todo.toEntity())
    }

    override suspend fun setDone(id: String, done: Boolean) {
        val now = clock.millis()
        todoDao.setDone(id, done, completedAt = if (done) now else null, updatedAt = now)
    }
}
