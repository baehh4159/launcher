package com.desklauncher.feature.todo.domain.repository

import com.desklauncher.feature.todo.domain.model.ProjectRef
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.domain.model.TodoDraft
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    val todos: Flow<List<Todo>>
    val projects: Flow<List<ProjectRef>>

    /** 새 할일 id */
    suspend fun add(draft: TodoDraft): String
    suspend fun update(todo: Todo)

    /** 지운 항목(실행 취소용). 없으면 null */
    suspend fun delete(id: String): Todo?

    /** 실행 취소: 지운 할일을 그대로 되살린다 */
    suspend fun restore(todo: Todo)
    suspend fun setDone(id: String, done: Boolean)
}
