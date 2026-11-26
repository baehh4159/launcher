package com.desklauncher.feature.memo.ui.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.feature.memo.domain.model.Project
import com.desklauncher.feature.memo.domain.repository.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProjectListViewModel @Inject constructor(
    private val repository: MemoRepository,
) : ViewModel() {

    private val editor = MutableStateFlow<ProjectEditor?>(null)
    private val confirmDelete = MutableStateFlow<Project?>(null)

    val uiState: StateFlow<ProjectListUiState> = combine(repository.projects, editor, confirmDelete) { projects, edit, delete ->
        ProjectListUiState(
            isLoading = false,
            active = projects.filterNot { it.archived },
            archived = projects.filter { it.archived },
            editor = edit,
            confirmDelete = delete,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectListUiState())

    fun onAction(action: ProjectListAction) {
        when (action) {
            ProjectListAction.Add -> {
                // 새 프로젝트 색은 순서대로 돌려 서로 구분되게
                val color = DeskTabColor.entries[(uiState.value.active.size + uiState.value.archived.size) % DeskTabColor.entries.size]
                editor.value = ProjectEditor(id = null, name = "", colorName = color.name)
            }
            is ProjectListAction.Edit -> editor.value = ProjectEditor(action.project.id, action.project.name, action.project.colorName)
            is ProjectListAction.SaveEditor -> {
                val current = editor.value ?: return
                editor.value = null
                val name = action.name.trim()
                if (name.isEmpty()) return
                viewModelScope.launch {
                    val existing = current.id?.let { id -> uiState.value.let { it.active + it.archived }.firstOrNull { it.id == id } }
                    if (existing == null) {
                        repository.addProject(name, action.colorName)
                    } else {
                        repository.updateProject(existing.copy(name = name, colorName = action.colorName))
                    }
                }
            }
            ProjectListAction.DismissEditor -> editor.value = null
            is ProjectListAction.SetArchived -> viewModelScope.launch {
                repository.updateProject(action.project.copy(archived = action.archived))
            }
            is ProjectListAction.RequestDelete -> confirmDelete.value = action.project
            ProjectListAction.ConfirmDelete -> {
                val target = confirmDelete.value ?: return
                confirmDelete.value = null
                viewModelScope.launch { repository.deleteProject(target.id) }
            }
            ProjectListAction.DismissDelete -> confirmDelete.value = null
            is ProjectListAction.Open, ProjectListAction.Back -> Unit // Route가 처리
        }
    }
}
