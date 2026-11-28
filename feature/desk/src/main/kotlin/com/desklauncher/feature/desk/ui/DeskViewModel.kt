package com.desklauncher.feature.desk.ui

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
class DeskViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<DeskUiState> = settingsRepository.settings
        .map { settings ->
            DeskUiState(stripLayout = settings.stripLayout, showScrubHint = !settings.scrubHintDone)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(StopTimeoutMillis), DeskUiState())

    /** 훑기로 처음 열었다 → 첫 실행 안내를 거둔다 (PRD §4.2) */
    fun onScrubbedOpen() {
        if (uiState.value.showScrubHint) viewModelScope.launch { settingsRepository.setScrubHintDone(true) }
    }

    private companion object {
        const val StopTimeoutMillis = 5_000L
    }
}
