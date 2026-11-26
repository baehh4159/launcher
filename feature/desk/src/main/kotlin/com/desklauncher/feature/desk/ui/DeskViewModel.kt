package com.desklauncher.feature.desk.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.datastore.SettingsRepository
import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.core.toolapi.ToolRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class DeskViewModel @Inject constructor(
    private val toolRegistry: ToolRegistry,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<DeskUiState> = combine(toolRegistry.enabledTools, settingsRepository.settings) { tools, settings ->
        DeskUiState(isLoading = false, tabs = tools.map { it.toTabUi() }, gripPosition = settings.gripPosition)
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = DeskUiState(),
        )

    fun findTool(toolId: String): DeskTool? = toolRegistry.findTool(toolId)
}
