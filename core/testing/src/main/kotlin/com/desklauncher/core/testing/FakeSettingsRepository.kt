package com.desklauncher.core.testing

import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.datastore.GripPosition
import com.desklauncher.core.datastore.HapticPreference
import com.desklauncher.core.datastore.SettingsRepository
import com.desklauncher.core.datastore.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(initial: DeskSettings = DeskSettings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: StateFlow<DeskSettings> = state

    override suspend fun setHaptic(value: HapticPreference) = state.update { it.copy(haptic = value) }
    override suspend fun setGripPosition(value: GripPosition) = state.update { it.copy(gripPosition = value) }
    override suspend fun setTheme(value: ThemePreference) = state.update { it.copy(theme = value) }
    override suspend fun setOnboardingCompleted(value: Boolean) = state.update { it.copy(onboardingCompleted = value) }
    override suspend fun setAppDrawerAutoFocusSearch(value: Boolean) = state.update { it.copy(appDrawerAutoFocusSearch = value) }
}
