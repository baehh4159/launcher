package com.desklauncher.feature.apps.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.core.datastore.SettingsRepository
import com.desklauncher.feature.apps.domain.AppIndex
import com.desklauncher.feature.apps.domain.AppsRepository
import com.desklauncher.feature.apps.domain.PinnedApps
import com.desklauncher.feature.apps.ui.icon.AppIconLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppsViewModel @Inject constructor(
    private val repository: AppsRepository,
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle,
    /** UI 전용 아이콘 캐시 — 화면에 그대로 건넨다 */
    val iconLoader: AppIconLoader?,
) : ViewModel() {

    private val query = savedStateHandle.getStateFlow(QueryKey, "")
    private val events = Channel<AppsEvent>(Channel.BUFFERED)
    val eventFlow: Flow<AppsEvent> = events.receiveAsFlow()

    val uiState: StateFlow<AppsUiState> = combine(repository.apps, query, settingsRepository.settings) { apps, text, settings ->
        AppsUiState(
            isLoading = false,
            query = text,
            sections = AppIndex.sections(apps.filter { AppIndex.matches(it.label, text) }),
            pinned = PinnedApps.resolve(settings.pinnedApps, apps.associateBy { it.key }),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(StopTimeoutMillis), AppsUiState())

    fun onAction(action: AppsAction) {
        when (action) {
            is AppsAction.Query -> savedStateHandle[QueryKey] = action.text
            is AppsAction.Launch -> {
                val launched = repository.launch(action.app)
                if (launched) savedStateHandle[QueryKey] = ""
                events.trySend(if (launched) AppsEvent.Launched else AppsEvent.LaunchFailed)
            }
            is AppsAction.Pin -> updatePinned { PinnedApps.pin(it, action.app.key) }
            is AppsAction.Unpin -> updatePinned { PinnedApps.unpin(it, action.app.key) }
            is AppsAction.MovePinned -> updatePinned { PinnedApps.move(it, action.app.key, action.delta) }
        }
    }

    private fun updatePinned(change: (List<String>) -> PinnedApps.Result) {
        viewModelScope.launch {
            val current = settingsRepository.settings.first().pinnedApps
            when (val result = change(current)) {
                is PinnedApps.Result.Updated -> settingsRepository.setPinnedApps(result.keys)
                PinnedApps.Result.Full -> events.send(AppsEvent.PinFull)
                PinnedApps.Result.Unchanged -> Unit
            }
        }
    }

    private companion object {
        const val QueryKey = "query"
        const val StopTimeoutMillis = 5_000L
    }
}
