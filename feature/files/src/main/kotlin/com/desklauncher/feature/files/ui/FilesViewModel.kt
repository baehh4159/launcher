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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class FilesUiState(
    val isLoading: Boolean = true,
    val category: FileCategory = FileCategory.Scans,
    val files: List<FileItem> = emptyList(),
    val addFailed: Boolean = false,
    val openFailed: Boolean = false,
)

sealed interface FilesAction {
    data class SelectCategory(val category: FileCategory) : FilesAction
    data object Back : FilesAction
    data class AddBookmark(val uri: String) : FilesAction
    data object DismissError : FilesAction
    data class Remove(val file: FileItem) : FilesAction
    data object OpenFailed : FilesAction
}

/** 내파일 (FILES-01) */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class FilesViewModel @Inject constructor(
    private val repository: FilesRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val category = savedStateHandle.getStateFlow(CategoryKey, FileCategory.Scans)
    private val addFailed = MutableStateFlow(false)
    private val openFailed = MutableStateFlow(false)

    val uiState: StateFlow<FilesUiState> = category
        .flatMapLatest { c ->
            combine(repository.files(c), addFailed, openFailed) { files, add, open -> FilesUiState(false, c, files, add, open) }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FilesUiState())

    fun onAction(action: FilesAction) {
        when (action) {
            is FilesAction.SelectCategory -> savedStateHandle[CategoryKey] = action.category
            is FilesAction.AddBookmark -> viewModelScope.launch {
                if (repository.addBookmark(action.uri)) savedStateHandle[CategoryKey] = FileCategory.Bookmarks else addFailed.value = true
            }
            FilesAction.DismissError -> {
                addFailed.value = false
                openFailed.value = false
            }
            is FilesAction.Remove -> if (action.file.category == FileCategory.Bookmarks) {
                viewModelScope.launch { repository.removeBookmark(action.file.id) }
            }
            FilesAction.OpenFailed -> openFailed.value = true
            FilesAction.Back -> Unit // Route가 처리
        }
    }

    private companion object {
        const val CategoryKey = "category"
    }
}
