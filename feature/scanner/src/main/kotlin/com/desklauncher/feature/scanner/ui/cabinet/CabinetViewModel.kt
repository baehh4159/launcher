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
    /** 현재 보기([filter])의 서류 */
    val documents: List<Document> = emptyList(),
    val folders: List<DocumentFolder> = emptyList(),
    val filter: FolderFilter = FolderFilter.All,
    val isSaving: Boolean = false,
    val message: CabinetMessage? = null,
    val dialog: CabinetDialog? = null,
)

/** 서류 보기 기준 (DOC-03) */
sealed interface FolderFilter {
    data object All : FolderFilter
    data object Unfiled : FolderFilter
    data class Folder(val id: String) : FolderFilter
}

sealed interface CabinetDialog {
    data object NewFolder : CabinetDialog
    data class RenameFolder(val folder: DocumentFolder) : CabinetDialog
    data class DeleteFolder(val folder: DocumentFolder) : CabinetDialog
    data class RenameDocument(val document: Document) : CabinetDialog
    data class MoveDocument(val document: Document) : CabinetDialog
    data class DeleteDocument(val document: Document) : CabinetDialog
}

enum class CabinetMessage { Saved, SaveFailed, ScannerUnavailable }

sealed interface CabinetAction {
    data class Scanned(val result: ScanResult) : CabinetAction
    data object ScannerUnavailable : CabinetAction
    data object DismissMessage : CabinetAction
    data class SelectFilter(val filter: FolderFilter) : CabinetAction
    data class ShowDialog(val dialog: CabinetDialog) : CabinetAction
    data object DismissDialog : CabinetAction

    /** 이름 입력 대화상자 확인 (새 폴더·폴더 이름·서류 이름) */
    data class SubmitName(val name: String) : CabinetAction

    /** 옮기기 대화상자에서 폴더 선택 (null = 미분류) */
    data class SubmitMove(val folderId: String?) : CabinetAction
    data object ConfirmDelete : CabinetAction
    data class Open(val document: Document) : CabinetAction
    data object Back : CabinetAction
}

@HiltViewModel
class CabinetViewModel @Inject constructor(
    private val repository: DocumentRepository,
) : ViewModel() {

    private val isSaving = MutableStateFlow(false)
    private val message = MutableStateFlow<CabinetMessage?>(null)
    private val filter = MutableStateFlow<FolderFilter>(FolderFilter.All)
    private val dialog = MutableStateFlow<CabinetDialog?>(null)
    private val panel = combine(isSaving, message, filter, dialog, ::Panel)

    val uiState: StateFlow<CabinetUiState> = combine(repository.documents, repository.folders, panel) { docs, folders, p ->
        // 지워진 폴더를 보고 있었다면 전체로
        val current = p.filter.takeUnless { it is FolderFilter.Folder && folders.none { f -> f.id == it.id } } ?: FolderFilter.All
        CabinetUiState(
            isLoading = false,
            documents = docs.filter { doc ->
                when (current) {
                    FolderFilter.All -> true
                    FolderFilter.Unfiled -> doc.folderId == null
                    is FolderFilter.Folder -> doc.folderId == current.id
                }
            },
            folders = folders,
            filter = current,
            isSaving = p.isSaving,
            message = p.message,
            dialog = p.dialog,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CabinetUiState())

    private data class Panel(
        val isSaving: Boolean,
        val message: CabinetMessage?,
        val filter: FolderFilter,
        val dialog: CabinetDialog?,
    )

    fun onAction(action: CabinetAction) {
        when (action) {
            is CabinetAction.Scanned -> viewModelScope.launch {
                isSaving.value = true
                message.value = try {
                    // 폴더를 보고 있으면 그 폴더에 바로 넣는다
                    repository.importScan(action.result, folderId = (filter.value as? FolderFilter.Folder)?.id)
                    CabinetMessage.Saved
                } catch (_: Exception) {
                    CabinetMessage.SaveFailed
                } finally {
                    isSaving.value = false
                }
            }
            CabinetAction.ScannerUnavailable -> message.value = CabinetMessage.ScannerUnavailable
            CabinetAction.DismissMessage -> message.value = null
            is CabinetAction.SelectFilter -> filter.value = action.filter
            is CabinetAction.ShowDialog -> dialog.value = action.dialog
            CabinetAction.DismissDialog -> dialog.value = null
            is CabinetAction.SubmitName -> submitName(action.name)
            is CabinetAction.SubmitMove -> {
                val target = dialog.value as? CabinetDialog.MoveDocument ?: return
                dialog.value = null
                viewModelScope.launch { repository.moveDocument(target.document.id, action.folderId) }
            }
            CabinetAction.ConfirmDelete -> {
                val target = dialog.value
                dialog.value = null
                viewModelScope.launch {
                    when (target) {
                        is CabinetDialog.DeleteFolder -> repository.deleteFolder(target.folder.id)
                        is CabinetDialog.DeleteDocument -> repository.deleteDocument(target.document.id)
                        else -> Unit
                    }
                }
            }
            is CabinetAction.Open, CabinetAction.Back -> Unit // Route가 처리
        }
    }

    private fun submitName(name: String) {
        val target = dialog.value
        dialog.value = null
        if (name.isBlank()) return
        viewModelScope.launch {
            when (target) {
                CabinetDialog.NewFolder -> filter.value = FolderFilter.Folder(repository.addFolder(name))
                is CabinetDialog.RenameFolder -> repository.renameFolder(target.folder.id, name)
                is CabinetDialog.RenameDocument -> repository.renameDocument(target.document.id, name)
                else -> Unit
            }
        }
    }
}
