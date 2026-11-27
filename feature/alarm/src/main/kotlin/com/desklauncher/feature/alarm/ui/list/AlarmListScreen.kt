package com.desklauncher.feature.alarm.ui.list

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
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
    val context = LocalContext.current
    LifecycleResumeEffect(Unit) {
        viewModel.onAction(AlarmListAction.RefreshPermission)
        onPauseOrDispose { }
    }
    var notificationsGranted by remember { mutableStateOf(context.hasNotificationPermission()) }
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notificationsGranted = it }
    AlarmListScreen(
        state = uiState,
        onAction = { action ->
            // 첫 알람을 만들 때 알림 권한을 묻는다 (PRD §9)
            if (action is AlarmListAction.Add && !notificationsGranted) notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            viewModel.onAction(action)
        },
        onBack = onBack,
        modifier = modifier,
        needsNotificationPermission = !notificationsGranted && uiState.items.isNotEmpty(),
        onRequestNotification = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
        onRequestExactAlarm = {
            context.startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.fromParts("package", context.packageName, null)))
        },
    )
}

private fun Context.hasNotificationPermission(): Boolean =
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

/** 권한 안내 배너 (ALARM-03) */
@Composable
private fun PermissionBanner(title: String, body: String, onOpen: () -> Unit) {
    Surface(color = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(body, style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onOpen) { Text(stringResource(R.string.alarm_perm_open)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    state: AlarmListUiState,
    onAction: (AlarmListAction) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    needsNotificationPermission: Boolean = false,
    onRequestNotification: () -> Unit = {},
    onRequestExactAlarm: () -> Unit = {},
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
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (state.needsExactAlarmPermission) {
                PermissionBanner(stringResource(R.string.alarm_perm_exact_title), stringResource(R.string.alarm_perm_exact_body), onRequestExactAlarm)
            }
            if (needsNotificationPermission) {
                PermissionBanner(stringResource(R.string.alarm_perm_notification_title), stringResource(R.string.alarm_perm_notification_body), onRequestNotification)
            }
            Box(Modifier.fillMaxSize()) {
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
            state = AlarmListUiState(isLoading = false, items = alarms.map { AlarmItem(it, if (it.enabled) Duration.ofMinutes(500) else null) }),
            onAction = {}, onBack = {},
        )
    }
}

@DeskPreviews
@Composable
private fun AlarmListScreenPermissionPreview() {
    DeskTheme {
        AlarmListScreen(state = AlarmListUiState(false, needsExactAlarmPermission = true), onAction = {}, onBack = {}, needsNotificationPermission = true)
    }
}

@DeskPreviews
@Composable
private fun AlarmListScreenEmptyPreview() {
    DeskTheme { AlarmListScreen(state = AlarmListUiState(false), onAction = {}, onBack = {}) }
}
