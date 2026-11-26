package com.desklauncher.feature.todo.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.domain.model.TodoDraft
import com.desklauncher.feature.todo.domain.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository,
) : ViewModel() {

    private val editingId = MutableStateFlow<String?>(null)
    private val recentlyDeleted = MutableStateFlow<Todo?>(null)

    val uiState: StateFlow<TodoUiState> = combine(
        repository.todos,
        repository.projects,
        editingId,
        recentlyDeleted,
    ) { todos, projects, editId, deleted ->
        TodoUiState(
            isLoading = false,
            items = todos.filterNot { it.isDone },
            projects = projects,
            editing = todos.firstOrNull { it.id == editId },
            recentlyDeleted = deleted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodoUiState())

    fun onAction(action: TodoAction) {
        when (action) {
            is TodoAction.QuickAdd -> {
                val title = action.title.trim()
                if (title.isNotEmpty()) viewModelScope.launch { repository.add(TodoDraft(title = title)) }
            }
            is TodoAction.OpenEditor -> editingId.value = action.id
            TodoAction.DismissEditor -> editingId.value = null
            is TodoAction.Save -> {
                editingId.value = null
                if (action.todo.title.isNotBlank()) viewModelScope.launch { repository.update(action.todo) }
            }
            is TodoAction.Delete -> {
                editingId.value = null
                viewModelScope.launch { recentlyDeleted.value = repository.delete(action.id) }
            }
            TodoAction.UndoDelete -> {
                val deleted = recentlyDeleted.value ?: return
                recentlyDeleted.value = null
                viewModelScope.launch { repository.restore(deleted) }
            }
            TodoAction.DismissUndo -> recentlyDeleted.value = null
            TodoAction.Back -> Unit // Route가 처리
        }
    }
}
