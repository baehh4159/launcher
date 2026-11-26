package com.desklauncher.feature.memo.ui.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.desklauncher.feature.memo.domain.model.Memo
import com.desklauncher.feature.memo.domain.model.Project
import com.desklauncher.feature.memo.domain.repository.MemoRepository
import com.desklauncher.feature.memo.navigation.ProjectDetailDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Immutable
data class ProjectDetailUiState(
    val isLoading: Boolean = true,
    val project: Project? = null,
    val memos: List<Memo> = emptyList(),
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    repository: MemoRepository,
) : ViewModel() {

    val projectId: String = savedStateHandle.toRoute<ProjectDetailDestination>().projectId

    val uiState: StateFlow<ProjectDetailUiState> =
        combine(repository.project(projectId), repository.memos(projectId)) { project, memos ->
            ProjectDetailUiState(isLoading = false, project = project, memos = memos)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProjectDetailUiState())
}
