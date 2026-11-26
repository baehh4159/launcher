package com.desklauncher.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.datastore.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = settingsRepository.settings
        .map { SettingsUiState(isLoading = false, settings = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun onAction(action: SettingsAction) {
        viewModelScope.launch {
            when (action) {
                is SettingsAction.SetHaptic -> settingsRepository.setHaptic(action.value)
                is SettingsAction.SetGrip -> settingsRepository.setGripPosition(action.value)
                is SettingsAction.SetTheme -> settingsRepository.setTheme(action.value)
                is SettingsAction.SetAutoFocusSearch -> settingsRepository.setAppDrawerAutoFocusSearch(action.value)
                SettingsAction.Back -> Unit // Route가 처리
            }
        }
    }
}
