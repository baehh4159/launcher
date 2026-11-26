package com.desklauncher.feature.settings.onboarding

import com.desklauncher.core.testing.FakeSettingsRepository
import com.desklauncher.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 저장이 끝난 뒤에만 닫힘 콜백
    @Test
    fun complete_savesBeforeCallback() = runTest {
        val repository = FakeSettingsRepository()
        var savedWhenCalled = false

        OnboardingViewModel(repository).complete { savedWhenCalled = repository.settings.value.onboardingCompleted }

        assertTrue(savedWhenCalled)
    }
}
