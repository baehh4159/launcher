package com.desklauncher.feature.scanner.ui.cabinet

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.feature.scanner.domain.model.Document
import com.desklauncher.feature.scanner.domain.model.DocumentFolder
import com.desklauncher.feature.scanner.domain.model.ScanResult
import com.desklauncher.feature.scanner.domain.repository.DocumentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class CabinetUiState(
    val isLoading: Boolean = true,
    val documents: List<Document> = emptyList(),
    val folders: List<DocumentFolder> = emptyList(),
    val isSaving: Boolean = false,
    val message: CabinetMessage? = null,
)

enum class CabinetMessage { Saved, SaveFailed, ScannerUnavailable }

sealed interface CabinetAction {
    data class Scanned(val result: ScanResult) : CabinetAction
    data object ScannerUnavailable : CabinetAction
    data object DismissMessage : CabinetAction
    data class Open(val document: Document) : CabinetAction
    data object Back : CabinetAction
}

@HiltViewModel
class CabinetViewModel @Inject constructor(
    private val repository: DocumentRepository,
) : ViewModel() {

    private val isSaving = MutableStateFlow(false)
    private val message = MutableStateFlow<CabinetMessage?>(null)

    val uiState: StateFlow<CabinetUiState> = combine(repository.documents, repository.folders, isSaving, message) { docs, folders, saving, msg ->
        CabinetUiState(isLoading = false, documents = docs, folders = folders, isSaving = saving, message = msg)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CabinetUiState())

    fun onAction(action: CabinetAction) {
        when (action) {
            is CabinetAction.Scanned -> viewModelScope.launch {
                isSaving.value = true
                message.value = try {
                    repository.importScan(action.result, folderId = null)
                    CabinetMessage.Saved
                } catch (_: Exception) {
                    CabinetMessage.SaveFailed
                } finally {
                    isSaving.value = false
                }
            }
            CabinetAction.ScannerUnavailable -> message.value = CabinetMessage.ScannerUnavailable
            CabinetAction.DismissMessage -> message.value = null
            is CabinetAction.Open, CabinetAction.Back -> Unit // Route가 처리
        }
    }
}
