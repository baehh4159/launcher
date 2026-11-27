package com.desklauncher.feature.files.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.feature.files.domain.model.FileCategory
import com.desklauncher.feature.files.domain.model.FileItem
import com.desklauncher.feature.files.domain.repository.FilesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@Immutable
data class FilesUiState(
    val isLoading: Boolean = true,
    val category: FileCategory = FileCategory.Scans,
    val files: List<FileItem> = emptyList(),
)

sealed interface FilesAction {
    data class SelectCategory(val category: FileCategory) : FilesAction
    data object Back : FilesAction
}

/** 내파일 (FILES-01) */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FilesViewModel @Inject constructor(
    private val repository: FilesRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val category = savedStateHandle.getStateFlow(CategoryKey, FileCategory.Scans)

    val uiState: StateFlow<FilesUiState> = category
        .flatMapLatest { c -> repository.files(c).map { FilesUiState(isLoading = false, category = c, files = it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FilesUiState())

    fun onAction(action: FilesAction) {
        when (action) {
            is FilesAction.SelectCategory -> savedStateHandle[CategoryKey] = action.category
            FilesAction.Back -> Unit // Route가 처리
        }
    }

    private companion object {
        const val CategoryKey = "category"
    }
}
