package com.desklauncher.feature.alarm.ui.list

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desklauncher.feature.alarm.domain.AlarmSchedule
import com.desklauncher.feature.alarm.domain.AlarmScheduler
import com.desklauncher.feature.alarm.domain.AlarmSync
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
    /** 정확한 알람 권한 없음 → 안내 배너 */
    val needsExactAlarmPermission: Boolean = false,
    val items: List<AlarmItem> = emptyList(),
    val recentlyDeleted: Alarm? = null,
    val editing: Alarm? = null,
)

sealed interface AlarmListAction {
    data class Add(val time: LocalTime) : AlarmListAction
    data class SetEnabled(val alarm: Alarm, val enabled: Boolean) : AlarmListAction
    data class Delete(val alarm: Alarm) : AlarmListAction
    data object UndoDelete : AlarmListAction
    data object DismissUndo : AlarmListAction
    data class Edit(val alarm: Alarm) : AlarmListAction
    data class Save(val alarm: Alarm) : AlarmListAction
    data object DismissEditor : AlarmListAction

    /** 화면 재개 시 권한 상태 다시 확인 (설정에서 돌아온 경우) */
    data object RefreshPermission : AlarmListAction
}

/** 알람 목록 (ALARM-01~03). 모든 변경은 [AlarmSync]로 저장과 시스템 예약을 함께 맞춘다. */
@HiltViewModel
class AlarmListViewModel @Inject constructor(
    repository: AlarmRepository,
    private val alarmSync: AlarmSync,
    private val scheduler: AlarmScheduler,
    private val clock: Clock,
) : ViewModel() {

    private val recentlyDeleted = MutableStateFlow<Alarm?>(null)
    private val editing = MutableStateFlow<Alarm?>(null)
    private val needsPermission = MutableStateFlow(!scheduler.canScheduleExact())

    val uiState: StateFlow<AlarmListUiState> = combine(repository.alarms, recentlyDeleted, editing, needsPermission) { alarms, deleted, edit, perm ->
        val now = ZonedDateTime.now(clock)
        AlarmListUiState(
            isLoading = false,
            needsExactAlarmPermission = perm,
            items = alarms.map { AlarmItem(it, if (it.enabled) AlarmSchedule.untilNext(it, now) else null) },
            recentlyDeleted = deleted,
            editing = edit,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AlarmListUiState())

    fun onAction(action: AlarmListAction) {
        when (action) {
            is AlarmListAction.Add -> viewModelScope.launch {
                val now = clock.instant()
                alarmSync.save(Alarm(id = UUID.randomUUID().toString(), time = action.time, createdAt = now, updatedAt = now))
            }
            is AlarmListAction.SetEnabled -> viewModelScope.launch { alarmSync.save(action.alarm.copy(enabled = action.enabled)) }
            is AlarmListAction.Delete -> viewModelScope.launch {
                editing.value = null
                alarmSync.delete(action.alarm)
                recentlyDeleted.value = action.alarm
            }
            AlarmListAction.UndoDelete -> {
                val alarm = recentlyDeleted.value ?: return
                recentlyDeleted.value = null
                viewModelScope.launch { alarmSync.save(alarm) }
            }
            AlarmListAction.DismissUndo -> recentlyDeleted.value = null
            is AlarmListAction.Edit -> editing.value = action.alarm
            is AlarmListAction.Save -> {
                editing.value = null
                viewModelScope.launch { alarmSync.save(action.alarm) }
            }
            AlarmListAction.DismissEditor -> editing.value = null
            AlarmListAction.RefreshPermission -> {
                val missing = !scheduler.canScheduleExact()
                // 방금 허용했다면 그동안 예약 못 한 알람을 예약
                if (needsPermission.value && !missing) viewModelScope.launch { alarmSync.rescheduleAll() }
                needsPermission.value = missing
            }
        }
    }
}
