package com.desklauncher.feature.alarm.ui.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.feature.alarm.domain.AlarmSchedule
import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.domain.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.LocalTime
import java.time.ZonedDateTime
import java.util.UUID
import javax.inject.Inject

@Immutable
data class AlarmItem(val alarm: Alarm, val untilNext: Duration?)

@Immutable
data class AlarmListUiState(
    val isLoading: Boolean = true,
    val items: List<AlarmItem> = emptyList(),
    val recentlyDeleted: Alarm? = null,
)

sealed interface AlarmListAction {
    data class Add(val time: LocalTime) : AlarmListAction
    data class SetEnabled(val alarm: Alarm, val enabled: Boolean) : AlarmListAction
    data class Delete(val alarm: Alarm) : AlarmListAction
    data object UndoDelete : AlarmListAction
    data object DismissUndo : AlarmListAction
}

/** 알람 목록 (ALARM-01). 울림 예약은 ALARM-03에서 연결한다. */
@HiltViewModel
class AlarmListViewModel @Inject constructor(
    private val repository: AlarmRepository,
    private val clock: Clock,
) : ViewModel() {

    private val recentlyDeleted = MutableStateFlow<Alarm?>(null)

    val uiState: StateFlow<AlarmListUiState> = combine(repository.alarms, recentlyDeleted) { alarms, deleted ->
        val now = ZonedDateTime.now(clock)
        AlarmListUiState(
            isLoading = false,
            items = alarms.map { AlarmItem(it, if (it.enabled) AlarmSchedule.untilNext(it, now) else null) },
            recentlyDeleted = deleted,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlarmListUiState())

    fun onAction(action: AlarmListAction) {
        when (action) {
            is AlarmListAction.Add -> viewModelScope.launch {
                val now = clock.instant()
                repository.save(Alarm(id = UUID.randomUUID().toString(), time = action.time, createdAt = now, updatedAt = now))
            }
            is AlarmListAction.SetEnabled -> viewModelScope.launch { repository.save(action.alarm.copy(enabled = action.enabled)) }
            is AlarmListAction.Delete -> viewModelScope.launch {
                repository.delete(action.alarm.id)
                recentlyDeleted.value = action.alarm
            }
            AlarmListAction.UndoDelete -> {
                val alarm = recentlyDeleted.value ?: return
                recentlyDeleted.value = null
                viewModelScope.launch { repository.save(alarm) }
            }
            AlarmListAction.DismissUndo -> recentlyDeleted.value = null
        }
    }
}
