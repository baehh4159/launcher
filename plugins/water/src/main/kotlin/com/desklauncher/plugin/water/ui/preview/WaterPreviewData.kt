package com.desklauncher.plugin.water.ui.preview

import com.desklauncher.plugin.water.domain.WaterDay
import com.desklauncher.plugin.water.ui.WaterUiState
import java.time.LocalDate

internal object WaterPreviewData {
    private val day = LocalDate.of(2026, 11, 27)
    val halfway = WaterUiState.Ready(WaterDay(date = day, cups = 5, goal = 8))
    val empty = WaterUiState.Ready(WaterDay(date = day, cups = 0, goal = 8))
}
