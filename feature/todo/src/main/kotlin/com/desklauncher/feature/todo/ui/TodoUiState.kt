package com.desklauncher.feature.todo.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.feature.todo.domain.model.ProjectRef
import com.desklauncher.feature.todo.domain.model.Todo

@Immutable
data class TodoUiState(
    val isLoading: Boolean = true,
    val items: List<Todo> = emptyList(),
    val projects: List<ProjectRef> = emptyList(),
    /** 편집 시트에 연 할일 */
    val editing: Todo? = null,
    /** 방금 지운 할일 — 스낵바 '실행 취소' */
    val recentlyDeleted: Todo? = null,
)

sealed interface TodoAction {
    data class QuickAdd(val title: String) : TodoAction
    data class OpenEditor(val id: String) : TodoAction
    data object DismissEditor : TodoAction
    data class Save(val todo: Todo) : TodoAction
    data class Delete(val id: String) : TodoAction
    data object UndoDelete : TodoAction
    data object DismissUndo : TodoAction
    data object Back : TodoAction
}
