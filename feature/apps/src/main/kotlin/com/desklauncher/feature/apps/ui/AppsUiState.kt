package com.desklauncher.feature.apps.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.feature.apps.domain.model.AppSection
import com.desklauncher.feature.apps.domain.model.InstalledApp

@Immutable
data class AppsUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val sections: List<AppSection> = emptyList(),
    /** 하단 고정 앱 줄 (APPS-02) */
    val pinned: List<InstalledApp> = emptyList(),
) {
    val isSearching: Boolean get() = query.isNotBlank()
    val indexKeys: List<String> get() = sections.map(AppSection::key)
}

sealed interface AppsAction {
    data class Query(val text: String) : AppsAction
    data class Launch(val app: InstalledApp) : AppsAction
    data class Pin(val app: InstalledApp) : AppsAction
    data class Unpin(val app: InstalledApp) : AppsAction
    /** -1 앞으로, +1 뒤로 */
    data class MovePinned(val app: InstalledApp, val delta: Int) : AppsAction
}

sealed interface AppsEvent {
    /** 앱을 실행했다 → 창을 닫는다 */
    data object Launched : AppsEvent
    data object LaunchFailed : AppsEvent
    /** 고정 줄이 가득 참 */
    data object PinFull : AppsEvent
}
