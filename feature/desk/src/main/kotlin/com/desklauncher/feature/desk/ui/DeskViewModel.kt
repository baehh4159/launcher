package com.desklauncher.feature.desk.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.toolapi.ToolRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DeskViewModel @Inject constructor(
    toolRegistry: ToolRegistry,
) : ViewModel() {

    val uiState: StateFlow<DeskUiState> = toolRegistry.enabledTools
        .map { tools -> DeskUiState(isLoading = false, tabs = tools.map { it.toTabUi() }) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = DeskUiState(),
        )
}
