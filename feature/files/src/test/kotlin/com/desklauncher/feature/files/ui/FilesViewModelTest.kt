package com.desklauncher.feature.files.ui

import androidx.lifecycle.SavedStateHandle
import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.files.domain.model.FileCategory
import com.desklauncher.feature.files.domain.model.FileItem
import com.desklauncher.feature.files.domain.repository.FilesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class FilesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 분류를 바꾸면 그 분류의 파일만
    @Test
    fun selectCategory_switchesList() = runTest {
        val repository = object : FilesRepository {
            override fun files(category: FileCategory): Flow<List<FileItem>> =
                flowOf(listOf(FileItem(category.name, "${category.name}.x", category, "p", "image/png", null, Instant.EPOCH)))
            override suspend fun addBookmark(uri: String) = true
        }
        val viewModel = FilesViewModel(repository, SavedStateHandle())
        collectInBackground(viewModel.uiState)
        assertEquals(listOf("Scans"), viewModel.uiState.value.files.map { it.id })

        viewModel.onAction(FilesAction.SelectCategory(FileCategory.Doodles))
        assertEquals(FileCategory.Doodles, viewModel.uiState.value.category)
        assertEquals(listOf("Doodles"), viewModel.uiState.value.files.map { it.id })
    }

    // 꺼내 두기 성공 → 꺼내 둔 파일 보기로, 실패 → 안내
    @Test
    fun addBookmark_switchesOrReportsFailure() = runTest {
        var succeed = true
        val repository = object : FilesRepository {
            override fun files(category: FileCategory): Flow<List<FileItem>> = flowOf(emptyList())
            override suspend fun addBookmark(uri: String) = succeed
        }
        val viewModel = FilesViewModel(repository, SavedStateHandle())
        collectInBackground(viewModel.uiState)

        viewModel.onAction(FilesAction.AddBookmark("content://x/1"))
        assertEquals(FileCategory.Bookmarks, viewModel.uiState.value.category)

        succeed = false
        viewModel.onAction(FilesAction.AddBookmark("content://x/2"))
        assertEquals(true, viewModel.uiState.value.addFailed)
    }
}
