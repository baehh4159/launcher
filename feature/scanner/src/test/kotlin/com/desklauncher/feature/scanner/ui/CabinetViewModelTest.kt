package com.desklauncher.feature.scanner.ui

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.scanner.domain.model.Document
import com.desklauncher.feature.scanner.domain.model.DocumentFolder
import com.desklauncher.feature.scanner.domain.model.DocumentSource
import com.desklauncher.feature.scanner.domain.model.ScanResult
import com.desklauncher.feature.scanner.domain.repository.DocumentRepository
import com.desklauncher.feature.scanner.ui.cabinet.CabinetAction
import com.desklauncher.feature.scanner.ui.cabinet.CabinetDialog
import com.desklauncher.feature.scanner.ui.cabinet.FolderFilter
import com.desklauncher.feature.scanner.ui.cabinet.CabinetMessage
import com.desklauncher.feature.scanner.ui.cabinet.CabinetViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class CabinetViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 스캔 결과 저장 성공 → 목록에 추가 + 안내
    @Test
    fun scanned_savesDocument() = runTest {
        val repository = FakeDocumentRepository()
        val viewModel = CabinetViewModel(repository)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(CabinetAction.Scanned(ScanResult("content://scan/1.pdf", 3, null)))

        assertEquals(3, viewModel.uiState.value.documents.single().pageCount)
        assertEquals(CabinetMessage.Saved, viewModel.uiState.value.message)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    // 새 폴더 → 그 폴더 보기 → 스캔은 그 폴더에, 옮기기·폴더 삭제 시 미분류로
    @Test
    fun foldersAndMoving() = runTest {
        val repository = FakeDocumentRepository()
        val viewModel = CabinetViewModel(repository)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(CabinetAction.ShowDialog(CabinetDialog.NewFolder))
        viewModel.onAction(CabinetAction.SubmitName("영수증"))
        val folder = viewModel.uiState.value.folders.single()
        assertEquals(FolderFilter.Folder(folder.id), viewModel.uiState.value.filter)

        viewModel.onAction(CabinetAction.Scanned(ScanResult("content://1", 1, null)))
        val doc = viewModel.uiState.value.documents.single()
        assertEquals(folder.id, doc.folderId)

        viewModel.onAction(CabinetAction.ShowDialog(CabinetDialog.MoveDocument(doc)))
        viewModel.onAction(CabinetAction.SubmitMove(null))
        assertEquals(0, viewModel.uiState.value.documents.size)

        viewModel.onAction(CabinetAction.SelectFilter(FolderFilter.Unfiled))
        assertEquals(listOf(doc.id), viewModel.uiState.value.documents.map { it.id })

        viewModel.onAction(CabinetAction.ShowDialog(CabinetDialog.DeleteFolder(folder)))
        viewModel.onAction(CabinetAction.ConfirmDelete)
        assertEquals(0, viewModel.uiState.value.folders.size)
    }

    // 저장 실패 시 안내만 하고 목록은 그대로
    @Test
    fun scanned_reportsFailure() = runTest {
        val repository = FakeDocumentRepository(fail = true)
        val viewModel = CabinetViewModel(repository)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(CabinetAction.Scanned(ScanResult("content://scan/1.pdf", 1, null)))

        assertEquals(CabinetMessage.SaveFailed, viewModel.uiState.value.message)
        assertEquals(0, viewModel.uiState.value.documents.size)
    }
}

internal class FakeDocumentRepository(private val fail: Boolean = false) : DocumentRepository {
    val docs = MutableStateFlow<List<Document>>(emptyList())
    val folderState = MutableStateFlow<List<DocumentFolder>>(emptyList())
    override val documents = docs
    override val folders = folderState
    override fun document(id: String) = docs.map { list -> list.firstOrNull { it.id == id } }
    private var next = 1

    override suspend fun renameDocument(id: String, title: String) = update(id) { it.copy(title = title.trim()) }
    override suspend fun moveDocument(id: String, folderId: String?) = update(id) { it.copy(folderId = folderId) }
    override suspend fun deleteDocument(id: String) {
        docs.value = docs.value.filterNot { it.id == id }
    }
    override suspend fun addFolder(name: String): String {
        val id = "f${next++}"
        folderState.value = folderState.value + DocumentFolder(id, name, Instant.EPOCH)
        return id
    }
    override suspend fun renameFolder(id: String, name: String) {
        folderState.value = folderState.value.map { if (it.id == id) it.copy(name = name) else it }
    }
    override suspend fun deleteFolder(id: String) {
        folderState.value = folderState.value.filterNot { it.id == id }
        docs.value = docs.value.map { if (it.folderId == id) it.copy(folderId = null) else it }
    }
    private fun update(id: String, transform: (Document) -> Document) {
        docs.value = docs.value.map { if (it.id == id) transform(it) else it }
    }

    override suspend fun importScan(result: ScanResult, folderId: String?): Document {
        if (fail) error("디스크 가득 참")
        val id = "d${next++}"
        val doc = Document(id, folderId, "스캔 $id", "documents/$id.pdf", null, result.pageCount, 1, DocumentSource.Scan, Instant.EPOCH, Instant.EPOCH)
        docs.value = listOf(doc) + docs.value
        return doc
    }
}
