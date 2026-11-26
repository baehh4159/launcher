package com.desklauncher.feature.settings.ui

import com.desklauncher.core.datastore.GripPosition
import com.desklauncher.core.datastore.HapticPreference
import com.desklauncher.core.datastore.ThemePreference
import com.desklauncher.core.testing.FakeSettingsRepository
import com.desklauncher.core.testing.MainDispatcherRule
import com.desklauncher.core.testing.collectInBackground
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeSettingsRepository()

    // 각 설정 변경이 저장소에 반영되고 화면 상태로 돌아온다
    @Test
    fun actions_updateSettings() = runTest {
        val viewModel = SettingsViewModel(repository)
        collectInBackground(viewModel.uiState)

        viewModel.onAction(SettingsAction.SetHaptic(HapticPreference.Off))
        viewModel.onAction(SettingsAction.SetGrip(GripPosition.Top))
        viewModel.onAction(SettingsAction.SetTheme(ThemePreference.Dark))
        viewModel.onAction(SettingsAction.SetAutoFocusSearch(true))

        val settings = viewModel.uiState.value.settings
        assertEquals(HapticPreference.Off, settings.haptic)
        assertEquals(GripPosition.Top, settings.gripPosition)
        assertEquals(ThemePreference.Dark, settings.theme)
        assertTrue(settings.appDrawerAutoFocusSearch)
    }
}
