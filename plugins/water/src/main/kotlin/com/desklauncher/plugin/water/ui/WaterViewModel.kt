package com.desklauncher.plugin.water.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.plugin.water.data.WaterStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * 단순 플러그인은 Hilt 없이 호스트 저장소로 직접 만든다 (WaterRoute의 `viewModel { }`).
 * 날짜는 화면을 연 시점 기준 — 자정을 넘겨 열어 두는 경우는 다시 열면 갱신된다.
 */
class WaterViewModel(
    private val store: WaterStore,
    today: () -> LocalDate,
) : ViewModel() {

    private val date: LocalDate = today()

    val uiState: StateFlow<WaterUiState> = store.observeDay(date)
        .map<_, WaterUiState>(WaterUiState::Ready)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(StopTimeoutMillis), WaterUiState.Loading)

    fun onAction(action: WaterAction) {
        val delta = when (action) {
            WaterAction.Drink -> 1
            WaterAction.Undo -> -1
        }
        viewModelScope.launch { store.addCups(date, delta) }
    }

    private companion object {
        const val StopTimeoutMillis = 5_000L
    }
}
