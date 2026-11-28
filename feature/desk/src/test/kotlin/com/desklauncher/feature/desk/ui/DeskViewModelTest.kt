package com.desklauncher.feature.desk.ui

import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.datastore.StripLayout
import com.desklauncher.core.testing.FakeSettingsRepository
import com.desklauncher.core.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DeskViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 처음에는 훑기 안내, 훑기로 한 번 열면 거둔다
    @Test
    fun scrubHint_hiddenAfterFirstScrubOpen() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = DeskViewModel(repository)
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertTrue(viewModel.uiState.value.showScrubHint)

        viewModel.onScrubbedOpen()

        assertFalse(viewModel.uiState.value.showScrubHint)
        assertTrue(repository.settings.value.scrubHintDone)
    }

    // 설정의 서류철 배치를 따른다
    @Test
    fun stripLayout_followsSettings() = runTest {
        val viewModel = DeskViewModel(FakeSettingsRepository(DeskSettings(stripLayout = StripLayout.RightVisible)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
        assertEquals(StripLayout.RightVisible, viewModel.uiState.value.stripLayout)
    }
}
