package com.desklauncher.feature.todo.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.feature.todo.domain.TodoFilter
import com.desklauncher.feature.todo.domain.TodoListing
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
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository,
    private val clock: Clock,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val filter = savedStateHandle.getStateFlow(FilterKey, TodoFilter.Today)

    private val editingId = MutableStateFlow<String?>(null)
    private val recentlyDeleted = MutableStateFlow<Todo?>(null)
    private val showDone = MutableStateFlow(false)

    private val panel = combine(editingId, recentlyDeleted, showDone, filter, ::Panel)

    val uiState: StateFlow<TodoUiState> = combine(repository.todos, repository.projects, panel) { todos, projects, p ->
        val today = LocalDate.now(clock)
        TodoUiState(
            isLoading = false,
            filter = p.filter,
            counts = TodoListing.counts(todos, today),
            items = TodoListing.filter(todos, p.filter, today),
            done = if (p.filter == TodoFilter.All) TodoListing.done(todos) else emptyList(),
            showDone = p.showDone,
            projects = projects,
            editing = todos.firstOrNull { it.id == p.editingId },
            recentlyDeleted = p.recentlyDeleted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), TodoUiState())

    fun onAction(action: TodoAction) {
        when (action) {
            is TodoAction.QuickAdd -> {
                val title = action.title.trim()
                // '오늘' 보기에서 적으면 오늘 마감으로 — 적자마자 목록에서 사라지지 않게
                val due = if (filter.value == TodoFilter.Today) LocalDate.now(clock) else null
                if (title.isNotEmpty()) viewModelScope.launch { repository.add(TodoDraft(title = title, dueDate = due)) }
            }
            is TodoAction.OpenEditor -> editingId.value = action.id
            TodoAction.DismissEditor -> editingId.value = null
            is TodoAction.SetDone -> viewModelScope.launch { repository.setDone(action.id, action.done) }
            TodoAction.ToggleDoneSection -> showDone.value = !showDone.value
            is TodoAction.SelectFilter -> savedStateHandle[FilterKey] = action.filter
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

    private data class Panel(
        val editingId: String?,
        val recentlyDeleted: Todo?,
        val showDone: Boolean,
        val filter: TodoFilter,
    )

    private companion object {
        const val FilterKey = "filter"
    }
}
