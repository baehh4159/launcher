package com.desklauncher.feature.apps.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.feature.apps.domain.model.InstalledApp

@Immutable
data class AppsUiState(
    val isLoading: Boolean = true,
    val query: String = "",
    val sections: List<AppSection> = emptyList(),
    val launchFailed: Boolean = false,
    val autoFocusSearch: Boolean = false,
) {
    val indexKeys: List<String> get() = sections.map { it.key }
    val isSearching: Boolean get() = query.isNotBlank()
}

@Immutable
data class AppSection(
    val key: String,
    val apps: List<InstalledApp>,
) {
    val isFavorites: Boolean get() = key == FavoritesKey

    companion object {
        /** 즐겨찾기 섹션 (목록 맨 위, 인덱스 스트립 첫 칸) */
        const val FavoritesKey = "★"
    }
}

sealed interface AppsAction {
    data class QueryChanged(val query: String) : AppsAction
    data class Launch(val app: InstalledApp) : AppsAction
    data object DismissLaunchError : AppsAction
    data class SetFavorite(val app: InstalledApp, val favorite: Boolean) : AppsAction
    data class SetHidden(val app: InstalledApp, val hidden: Boolean) : AppsAction
    data class OpenAppInfo(val app: InstalledApp) : AppsAction
    data class Uninstall(val app: InstalledApp) : AppsAction
    data object OpenStore : AppsAction
    data object OpenSettings : AppsAction
    data object Back : AppsAction
}
