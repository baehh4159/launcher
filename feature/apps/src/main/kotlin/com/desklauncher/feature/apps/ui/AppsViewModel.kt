package com.desklauncher.feature.apps.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val repository: AppsRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val query = savedStateHandle.getStateFlow(QueryKey, "")
    private val launchFailed = MutableStateFlow(false)

    private val sortedApps = repository.apps.map { AppIndex.sorted(it) }

    val uiState: StateFlow<AppsUiState> = combine(sortedApps, query, launchFailed) { apps, q, failed ->
        val visible = apps.filter { AppIndex.matches(it, q) }
        AppsUiState(
            isLoading = false,
            query = q,
            sections = visible.groupBy { AppIndex.keyOf(it.label) }.map { (key, list) -> AppSection(key, list) },
            launchFailed = failed,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppsUiState())

    fun onAction(action: AppsAction) {
        when (action) {
            is AppsAction.QueryChanged -> savedStateHandle[QueryKey] = action.query
            AppsAction.DismissLaunchError -> launchFailed.value = false
            is AppsAction.Launch, AppsAction.OpenStore, AppsAction.Back -> Unit // Route가 처리
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
    }
}
