package com.desklauncher.feature.memo.ui

import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.memo.domain.model.Memo
import com.desklauncher.feature.memo.domain.model.Project
import com.desklauncher.feature.memo.domain.repository.MemoRepository
import com.desklauncher.feature.memo.ui.project.ProjectListAction
import com.desklauncher.feature.memo.ui.project.ProjectListViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import java.time.Instant

class ProjectListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMemoRepository()

    // 추가 → 이름 바꾸기 → 보관 → 삭제 확인
    @Test
    fun projectLifecycle() = runTest {
        val viewModel = ProjectListViewModel(repository)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(ProjectListAction.Add)
        viewModel.onAction(ProjectListAction.SaveEditor("  책상 런처 ", "Blue"))
        val created = viewModel.uiState.value.active.single()
        assertEquals("책상 런처", created.name)
        assertNull(viewModel.uiState.value.editor)

        viewModel.onAction(ProjectListAction.Edit(created))
        viewModel.onAction(ProjectListAction.SaveEditor("데스크", "Green"))
        assertEquals("데스크", viewModel.uiState.value.active.single().name)
        assertEquals("Green", viewModel.uiState.value.active.single().colorName)

        viewModel.onAction(ProjectListAction.SetArchived(viewModel.uiState.value.active.single(), true))
        assertEquals(1, viewModel.uiState.value.archived.size)

        viewModel.onAction(ProjectListAction.RequestDelete(viewModel.uiState.value.archived.single()))
        viewModel.onAction(ProjectListAction.ConfirmDelete)
        assertEquals(0, viewModel.uiState.value.archived.size + viewModel.uiState.value.active.size)
    }

    // 빈 이름은 저장하지 않는다
    @Test
    fun saveEditor_ignoresBlankName() = runTest {
        val viewModel = ProjectListViewModel(repository)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(ProjectListAction.Add)
        viewModel.onAction(ProjectListAction.SaveEditor("   ", "Blue"))

        assertEquals(0, viewModel.uiState.value.active.size)
    }
}

internal class FakeMemoRepository : MemoRepository {
    private val state = MutableStateFlow<List<Project>>(emptyList())
    val memoState = MutableStateFlow<List<Memo>>(emptyList())
    private var next = 1
    override val projects = state
    override val memoCounts = memoState.map { list -> list.groupingBy { it.projectId }.eachCount() }
    override fun project(id: String) = state.map { list -> list.firstOrNull { it.id == id } }
    override fun memos(projectId: String) = memoState.map { list -> list.filter { it.projectId == projectId } }
    override suspend fun getMemo(id: String) = memoState.value.firstOrNull { it.id == id }
    override suspend fun saveMemo(memo: Memo) {
        memoState.value = memoState.value.filterNot { it.id == memo.id } + memo
    }
    override suspend fun deleteMemo(id: String) {
        memoState.value = memoState.value.filterNot { it.id == id }
    }

    override suspend fun addProject(name: String, colorName: String): String {
        val id = "p${next++}"
        state.value = state.value + Project(id, name.trim(), colorName, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH)
        return id
    }

    override suspend fun updateProject(project: Project) {
        state.value = state.value.map { if (it.id == project.id) project else it }
    }

    override suspend fun deleteProject(id: String) {
        state.value = state.value.filterNot { it.id == id }
    }
}
