package com.desklauncher.feature.settings.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.core.datastore.DeskSettings
import com.desklauncher.core.datastore.GripPosition
import com.desklauncher.core.datastore.HapticPreference
import com.desklauncher.core.datastore.ThemePreference

@Immutable
data class SettingsUiState(
    val isLoading: Boolean = true,
    val settings: DeskSettings = DeskSettings(),
)

sealed interface SettingsAction {
    data class SetHaptic(val value: HapticPreference) : SettingsAction
    data class SetGrip(val value: GripPosition) : SettingsAction
    data class SetTheme(val value: ThemePreference) : SettingsAction
    data class SetAutoFocusSearch(val value: Boolean) : SettingsAction
    data object Back : SettingsAction
}
