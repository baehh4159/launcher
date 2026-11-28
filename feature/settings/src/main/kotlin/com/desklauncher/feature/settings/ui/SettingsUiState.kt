package com.desklauncher.feature.settings.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.datastore.HapticSetting
import com.desklauncher.core.datastore.StripLayout

@Immutable
data class SettingsUiState(val settings: DeskSettings = DeskSettings())

sealed interface SettingsAction {
    data class SetHaptics(val value: HapticSetting) : SettingsAction
    data class SetStripLayout(val value: StripLayout) : SettingsAction
    data object ReplayScrubHint : SettingsAction
}
