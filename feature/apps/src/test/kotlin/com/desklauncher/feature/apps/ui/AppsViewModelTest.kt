package com.desklauncher.feature.apps.ui

import androidx.lifecycle.SavedStateHandle
import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.testing.FakeSettingsRepository
import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.feature.apps.domain.AppsRepository
import com.desklauncher.feature.apps.domain.model.InstalledApp
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private class FakeRepository(apps: List<InstalledApp>) : AppsRepository {
        override val apps = MutableStateFlow(apps)
        var launched: InstalledApp? = null
        var result = true
        override fun launch(app: InstalledApp): Boolean = result.also { launched = app }
    }

    private val kakao = InstalledApp("k/.M", "k", "카카오톡")
    private val chrome = InstalledApp("c/.M", "c", "Chrome")

    // 검색어를 넣으면 맞는 앱만 남는다
    @Test
    fun query_filtersSections() = runTest {
        val viewModel = AppsViewModel(FakeRepository(listOf(kakao, chrome)), FakeSettingsRepository(), SavedStateHandle(), iconLoader = null)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onAction(AppsAction.Query("ㅋㅋ"))

        assertEquals(listOf("카카오톡"), viewModel.uiState.value.sections.flatMap { it.apps }.map { it.label })
    }

    // 실행에 성공하면 검색어를 비우고 창 닫기 사건을 보낸다
    @Test
    fun launch_success_clearsQuery_andEmitsLaunched() = runTest {
        val repository = FakeRepository(listOf(kakao))
        val viewModel = AppsViewModel(repository, FakeSettingsRepository(), SavedStateHandle(), iconLoader = null)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        viewModel.onAction(AppsAction.Query("카"))

        viewModel.onAction(AppsAction.Launch(kakao))

        assertEquals(kakao, repository.launched)
        assertEquals("", viewModel.uiState.value.query)
        assertEquals(AppsEvent.Launched, viewModel.eventFlow.first())
    }

    // 실행 실패 시 실패 사건, 검색어 유지
    @Test
    fun launch_failure_emitsFailed() = runTest {
        val repository = FakeRepository(listOf(kakao)).apply { result = false }
        val viewModel = AppsViewModel(repository, FakeSettingsRepository(), SavedStateHandle(), iconLoader = null)
        viewModel.onAction(AppsAction.Launch(kakao))
        assertEquals(AppsEvent.LaunchFailed, viewModel.eventFlow.first())
    }

    // 고정하면 저장 순서대로 하단 줄에, 앞으로 옮기기·해제
    @Test
    fun pin_move_unpin_updatesPinnedRow() = runTest {
        val settings = FakeSettingsRepository()
        val viewModel = AppsViewModel(FakeRepository(listOf(kakao, chrome)), settings, SavedStateHandle(), iconLoader = null)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        viewModel.onAction(AppsAction.Pin(kakao))
        viewModel.onAction(AppsAction.Pin(chrome))
        assertEquals(listOf(kakao, chrome), viewModel.uiState.value.pinned)

        viewModel.onAction(AppsAction.MovePinned(chrome, -1))
        assertEquals(listOf(chrome, kakao), viewModel.uiState.value.pinned)

        viewModel.onAction(AppsAction.Unpin(chrome))
        assertEquals(listOf(kakao), viewModel.uiState.value.pinned)
        assertEquals(listOf(kakao.key), settings.settings.value.pinnedApps)
    }

    // 5개가 차면 고정하지 않고 알린다
    @Test
    fun pin_whenFull_emitsPinFull() = runTest {
        val full = DeskSettings(pinnedApps = listOf("1", "2", "3", "4", "5"))
        val viewModel = AppsViewModel(FakeRepository(listOf(kakao)), FakeSettingsRepository(full), SavedStateHandle(), iconLoader = null)

        viewModel.onAction(AppsAction.Pin(kakao))

        assertEquals(AppsEvent.PinFull, viewModel.eventFlow.first())
    }
}
