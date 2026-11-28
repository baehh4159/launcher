package com.desklauncher.feature.apps.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.feature.apps.domain.model.AppSection
import com.desklauncher.feature.apps.domain.model.InstalledApp

@Immutable
data class AppsUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val sections: List<AppSection> = emptyList(),
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val indexKeys: List<String> get() = sections.map(AppSection::key)
}

sealed interface AppsAction {
    data class Query(val text: String) : AppsAction
    data class Launch(val app: InstalledApp) : AppsAction
}

sealed interface AppsEvent {
    /** 앱을 실행했다 → 창을 닫는다 */
    data object Launched : AppsEvent
    data object LaunchFailed : AppsEvent
}
