package com.desklauncher.plugin.water.ui

import com.desklauncher.plugin.water.domain.WaterDay

sealed interface WaterUiState {
    data object Loading : WaterUiState
    data class Ready(val today: WaterDay) : WaterUiState
}

sealed interface WaterAction {
    data object Drink : WaterAction
    data object Undo : WaterAction
}
