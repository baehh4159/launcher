package com.desklauncher.feature.store.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.toolapi.ToolRegistry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StoreViewModel @Inject constructor(
    private val toolRegistry: ToolRegistry,
) : ViewModel() {

    private val colorPickerToolId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<StoreUiState> = combine(toolRegistry.allTools, colorPickerToolId) { tools, pickerId ->
        StoreUiState(
            isLoading = false,
            tools = tools.map { it.toStoreUi() },
            colorPickerToolId = pickerId,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StoreUiState())

    fun onAction(action: StoreAction) {
        when (action) {
            is StoreAction.SetEnabled -> viewModelScope.launch { toolRegistry.setEnabled(action.toolId, action.enabled) }
            is StoreAction.Reorder -> viewModelScope.launch { toolRegistry.reorder(action.orderedToolIds) }
            is StoreAction.OpenColorPicker -> colorPickerToolId.value = action.toolId
            StoreAction.DismissColorPicker -> colorPickerToolId.value = null
            is StoreAction.SetColor -> {
                colorPickerToolId.value = null
                viewModelScope.launch { toolRegistry.setTabColor(action.toolId, action.color) }
            }
            StoreAction.Back -> Unit // Route가 처리
        }
    }
}
