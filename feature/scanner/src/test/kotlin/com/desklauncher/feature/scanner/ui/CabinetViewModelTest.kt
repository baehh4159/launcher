package com.desklauncher.feature.scanner.ui

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.scanner.domain.model.Document
import com.desklauncher.feature.scanner.domain.model.DocumentFolder
import com.desklauncher.feature.scanner.domain.model.DocumentSource
import com.desklauncher.feature.scanner.domain.model.ScanResult
import com.desklauncher.feature.scanner.domain.repository.DocumentRepository
import com.desklauncher.feature.scanner.ui.cabinet.CabinetAction
import com.desklauncher.feature.scanner.ui.cabinet.CabinetMessage
import com.desklauncher.feature.scanner.ui.cabinet.CabinetViewModel
import kotlinx.coroutines.flow.MutableStateFlow
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
    private var next = 1

    override suspend fun importScan(result: ScanResult, folderId: String?): Document {
        if (fail) error("디스크 가득 참")
        val id = "d${next++}"
        val doc = Document(id, folderId, "스캔 $id", "documents/$id.pdf", null, result.pageCount, 1, DocumentSource.Scan, Instant.EPOCH, Instant.EPOCH)
        docs.value = listOf(doc) + docs.value
        return doc
    }
}
