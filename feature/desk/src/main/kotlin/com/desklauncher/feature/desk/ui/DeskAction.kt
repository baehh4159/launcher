package com.desklauncher.feature.desk.ui

sealed interface DeskAction {
    data class OpenTool(val toolId: String) : DeskAction
}
