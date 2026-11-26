package com.desklauncher.feature.apps.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.datastore.SettingsRepository
import com.desklauncher.feature.apps.domain.AppIndex
import com.desklauncher.feature.apps.domain.model.InstalledApp
import com.desklauncher.feature.apps.domain.repository.AppsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val repository: AppsRepository,
    settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val query = savedStateHandle.getStateFlow(QueryKey, "")
    private val launchFailed = MutableStateFlow(false)

    private val sortedApps = repository.apps.map { AppIndex.sorted(it) }

    private val autoFocus = settingsRepository.settings.map { it.appDrawerAutoFocusSearch }

    val uiState: StateFlow<AppsUiState> = combine(sortedApps, query, launchFailed, autoFocus) { apps, q, failed, focus ->
        AppsUiState(
            isLoading = false,
            query = q,
            sections = buildSections(apps, q),
            launchFailed = failed,
            autoFocusSearch = focus,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppsUiState())

    fun onAction(action: AppsAction) {
        when (action) {
            is AppsAction.QueryChanged -> savedStateHandle[QueryKey] = action.query
            AppsAction.DismissLaunchError -> launchFailed.value = false
            is AppsAction.SetFavorite -> viewModelScope.launch { repository.setFavorite(action.app, action.favorite) }
            is AppsAction.SetHidden -> viewModelScope.launch { repository.setHidden(action.app, action.hidden) }
            is AppsAction.OpenAppInfo -> repository.openAppInfo(action.app)
            is AppsAction.Uninstall -> repository.requestUninstall(action.app)
            is AppsAction.Launch, AppsAction.OpenStore, AppsAction.OpenSettings, AppsAction.Back -> Unit // Route가 처리
        }
    }

    /** 성공하면 true — Route가 서랍을 닫는다. */
    fun launch(app: InstalledApp): Boolean {
        val launched = repository.launch(app)
        if (launched) {
            savedStateHandle[QueryKey] = ""
        } else {
            launchFailed.value = true
        }
        return launched
    }

    private companion object {
        const val QueryKey = "query"

        /**
         * 검색 중: 숨긴 앱도 포함해 일치하는 앱만 (숨긴 앱을 다시 찾을 수 있게).
         * 평소: 즐겨찾기 섹션 → 숨기지 않은 앱을 인덱스 키별로.
         */
        fun buildSections(apps: List<InstalledApp>, query: String): List<AppSection> {
            val searching = query.isNotBlank()
            val visible = apps.filter { if (searching) AppIndex.matches(it, query) else !it.isHidden }
            val grouped = visible.groupBy { AppIndex.keyOf(it.label) }.map { (key, list) -> AppSection(key, list) }
            val favorites = visible.filter { it.isFavorite }
            return if (searching || favorites.isEmpty()) grouped else listOf(AppSection(AppSection.FavoritesKey, favorites)) + grouped
        }
    }
}
