package com.desklauncher.core.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreSettingsRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private fun TestScope.repository() = DataStoreSettingsRepository(
        PreferenceDataStoreFactory.create(scope = backgroundScope) { tmp.newFile("settings.preferences_pb") },
    )

    // 저장된 값이 없으면 기본값
    @Test
    fun settings_defaultsWhenEmpty() = runTest {
        assertEquals(DeskSettings(), repository().settings.first())
    }

    // 저장한 값이 그대로 읽힌다
    @Test
    fun settings_roundTrip() = runTest {
        val repository = repository()
        repository.setHaptic(HapticPreference.Light)
        repository.setGripPosition(GripPosition.Middle)
        repository.setTheme(ThemePreference.Dark)
        repository.setOnboardingCompleted(true)
        repository.setAppDrawerAutoFocusSearch(true)

        assertEquals(
            DeskSettings(
                haptic = HapticPreference.Light,
                gripPosition = GripPosition.Middle,
                theme = ThemePreference.Dark,
                onboardingCompleted = true,
                appDrawerAutoFocusSearch = true,
            ),
            repository.settings.first(),
        )
    }
}
