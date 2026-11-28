package com.desklauncher.feature.desk.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.datastore.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DeskViewModel @Inject constructor(
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<DeskUiState> = settingsRepository.settings
        .map { settings -> DeskUiState(stripLayout = settings.stripLayout) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(StopTimeoutMillis), DeskUiState())

    private companion object {
        const val StopTimeoutMillis = 5_000L
    }
}
