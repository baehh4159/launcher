package com.desklauncher.feature.settings.ui

import com.desklauncher.core.datastore.HapticSetting
import com.desklauncher.core.testing.FakeSettingsRepository
import com.desklauncher.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 촉각 설정을 바꾸면 저장소에 반영된다
    @Test
    fun setHaptics_updatesRepository() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.onAction(SettingsAction.SetHaptics(HapticSetting.ConfirmOnly))

        assertEquals(HapticSetting.ConfirmOnly, repository.settings.value.haptics)
    }
}
