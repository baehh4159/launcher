package com.desklauncher.feature.alarm.ui.list

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.DeskEmptyState
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.alarm.R
import com.desklauncher.feature.alarm.domain.model.Alarm
import com.desklauncher.feature.alarm.ui.component.AlarmEditorSheet
import com.desklauncher.feature.alarm.ui.component.AlarmRow
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime

@Composable
fun AlarmListRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlarmListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AlarmListScreen(state = uiState, onAction = viewModel::onAction, onBack = onBack, modifier = modifier)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    state: AlarmListUiState,
    onAction: (AlarmListAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    state.editing?.let { alarm ->
        AlarmEditorSheet(
            alarm = alarm,
            onSave = { onAction(AlarmListAction.Save(it)) },
            onDelete = { onAction(AlarmListAction.Delete(alarm)) },
            onDismiss = { onAction(AlarmListAction.DismissEditor) },
        )
    }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val deletedMessage = stringResource(R.string.alarm_deleted)
    val undoLabel = stringResource(R.string.alarm_undo)
    LaunchedEffect(state.recentlyDeleted) {
        if (state.recentlyDeleted != null) {
            val result = snackbarHostState.showSnackbar(deletedMessage, undoLabel)
            onAction(if (result == SnackbarResult.ActionPerformed) AlarmListAction.UndoDelete else AlarmListAction.DismissUndo)
        }
    }
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = stringResource(R.string.alarm_tool_title),
                tabColor = DeskTabColor.Indigo,
                backContentDescription = stringResource(R.string.alarm_back),
                onBack = onBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showTimePicker = true }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.alarm_add))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.items.isEmpty() -> DeskEmptyState(
                    title = stringResource(R.string.alarm_empty_title),
                    body = stringResource(R.string.alarm_empty_body),
                    icon = Icons.Outlined.Alarm,
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                )
                else -> LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 88.dp)) {
                    items(state.items, key = { it.alarm.id }) { item ->
                        Column(Modifier.animateItem()) {
                            AlarmRow(
                                alarm = item.alarm,
                                untilNext = item.untilNext,
                                onToggle = { onAction(AlarmListAction.SetEnabled(item.alarm, it)) },
                                onClick = { onAction(AlarmListAction.Edit(item.alarm)) },
                                onDelete = { onAction(AlarmListAction.Delete(item.alarm)) },
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
    if (showTimePicker) {
        val context = LocalContext.current
        val timeState = rememberTimePickerState(initialHour = 7, initialMinute = 0, is24Hour = DateFormat.is24HourFormat(context))
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onAction(AlarmListAction.Add(LocalTime.of(timeState.hour, timeState.minute)))
                    showTimePicker = false
                }) { Text(stringResource(R.string.alarm_ok)) }
            },
            dismissButton = { TextButton(onClick = { showTimePicker = false }) { Text(stringResource(R.string.alarm_cancel)) } },
            text = { TimePicker(state = timeState) },
        )
    }
}

@DeskPreviews
@Composable
private fun AlarmListScreenPreview() {
    val alarms = listOf(
        Alarm("1", LocalTime.of(6, 30), setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY), "출근", createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH),
        Alarm("2", LocalTime.of(9, 0), setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY), createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH),
        Alarm("3", LocalTime.of(14, 0), label = "약 먹기", enabled = false, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH),
    )
    DeskTheme {
        AlarmListScreen(
            state = AlarmListUiState(false, alarms.map { AlarmItem(it, if (it.enabled) Duration.ofMinutes(500) else null) }),
            onAction = {}, onBack = {},
        )
    }
}

@DeskPreviews
@Composable
private fun AlarmListScreenEmptyPreview() {
    DeskTheme { AlarmListScreen(state = AlarmListUiState(false), onAction = {}, onBack = {}) }
}
