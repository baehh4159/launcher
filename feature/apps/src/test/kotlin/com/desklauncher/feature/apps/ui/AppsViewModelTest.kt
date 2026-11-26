package com.desklauncher.feature.apps.ui

import androidx.lifecycle.SavedStateHandle
import com.desklauncher.core.testing.FakeSettingsRepository
import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import com.desklauncher.feature.apps.domain.model.InstalledApp
import com.desklauncher.feature.apps.domain.repository.AppsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AppsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeAppsRepository(listOf("카카오톡", "카메라", "갤러리", "Chrome"))

    // 초성 섹션 → 영문 섹션 순으로 묶인다
    @Test
    fun uiState_groupsAppsBySection() = runTest {
        val viewModel = AppsViewModel(repository, FakeSettingsRepository(), SavedStateHandle())
        collectInBackground(viewModel.uiState)

        val state = viewModel.uiState.value
        assertEquals(listOf("ㄱ", "ㅋ", "C"), state.indexKeys)
        assertEquals(listOf("카메라", "카카오톡"), state.sections[1].apps.map { it.label })
    }

    // 초성 검색으로 걸러진다
    @Test
    fun query_filtersByChoseong() = runTest {
        val viewModel = AppsViewModel(repository, FakeSettingsRepository(), SavedStateHandle())
        collectInBackground(viewModel.uiState)

        viewModel.onAction(AppsAction.QueryChanged("ㅋㅋ"))

        assertEquals(listOf("카카오톡"), viewModel.uiState.value.sections.flatMap { it.apps }.map { it.label })
        assertTrue(viewModel.uiState.value.isSearching)
    }

    // 실행 실패 시 오류 상태, 성공 시 검색어 초기화
    @Test
    fun launch_reportsFailureAndClearsQueryOnSuccess() = runTest {
        val viewModel = AppsViewModel(repository, FakeSettingsRepository(), SavedStateHandle())
        collectInBackground(viewModel.uiState)
        val app = repository.current.first()

        repository.launchResult = false
        assertFalse(viewModel.launch(app))
        assertTrue(viewModel.uiState.value.launchFailed)

        viewModel.onAction(AppsAction.DismissLaunchError)
        viewModel.onAction(AppsAction.QueryChanged("카"))
        repository.launchResult = true
        assertTrue(viewModel.launch(app))
        assertFalse(viewModel.uiState.value.launchFailed)
        assertEquals("", viewModel.uiState.value.query)
    }

    private class FakeAppsRepository(labels: List<String>) : AppsRepository {
        val current = labels.map { InstalledApp("pkg.$it/.Main", "pkg.$it", it) }
        var launchResult = true
        override val apps: Flow<List<InstalledApp>> = MutableStateFlow(current)
        override fun launch(app: InstalledApp): Boolean = launchResult
    }
}
