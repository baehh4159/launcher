package com.desklauncher.core.testing

import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.datastore.HapticSetting
import com.desklauncher.core.datastore.SettingsRepository
import com.desklauncher.core.datastore.StripLayout
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class FakeSettingsRepository(initial: DeskSettings = DeskSettings()) : SettingsRepository {
    private val state = MutableStateFlow(initial)
    override val settings: StateFlow<DeskSettings> = state
    override suspend fun setHaptics(value: HapticSetting) = state.update { it.copy(haptics = value) }
    override suspend fun setStripLayout(value: StripLayout) = state.update { it.copy(stripLayout = value) }
    override suspend fun setScrubHintDone() = state.update { it.copy(scrubHintDone = true) }
}
