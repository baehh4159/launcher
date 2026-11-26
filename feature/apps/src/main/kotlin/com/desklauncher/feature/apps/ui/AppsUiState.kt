package com.desklauncher.feature.apps.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.feature.apps.domain.model.InstalledApp

@Immutable
data class AppsUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val sections: List<AppSection> = emptyList(),
    val launchFailed: Boolean = false,
) {
    val indexKeys: List<String> get() = sections.map { it.key }
    val isSearching: Boolean get() = query.isNotBlank()
}

@Immutable
data class AppSection(
    val key: String,
    val apps: List<InstalledApp>,
)

sealed interface AppsAction {
    data class QueryChanged(val query: String) : AppsAction
    data class Launch(val app: InstalledApp) : AppsAction
    data object DismissLaunchError : AppsAction
    data object OpenStore : AppsAction
    data object Back : AppsAction
}
