package com.desklauncher.feature.calendar.ui

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.calendar.R
import com.desklauncher.feature.calendar.ui.component.AgendaRow
import com.desklauncher.feature.calendar.ui.component.EventEditorSheet
import com.desklauncher.feature.calendar.ui.component.MonthGridView
import com.desklauncher.feature.calendar.ui.preview.CalendarPreviewData
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun CalendarRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CalendarViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CalendarScreen(
        state = uiState,
        onAction = { action -> if (action == CalendarAction.Back) onBack() else viewModel.onAction(action) },
        modifier = modifier,
    )
}

@Composable
fun CalendarScreen(
    state: CalendarUiState,
    onAction: (CalendarAction) -> Unit,
    modifier: Modifier = Modifier,
    zone: ZoneId = ZoneId.systemDefault(),
) {
    val locale = LocalConfiguration.current.locales[0]
    val context = LocalContext.current
    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }
    EditorHost(state, is24Hour, onAction)
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = state.month.format(DateTimeFormatter.ofPattern("yyyy년 M월", locale)),
                tabColor = ToolColor,
                backContentDescription = stringResource(R.string.calendar_back),
                onBack = { onAction(CalendarAction.Back) },
                actions = {
                    IconButton(onClick = { onAction(CalendarAction.PrevMonth) }) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = stringResource(R.string.calendar_prev_month))
                    }
                    IconButton(onClick = { onAction(CalendarAction.GoToday) }) {
                        Icon(Icons.Outlined.Today, contentDescription = stringResource(R.string.calendar_today))
                    }
                    IconButton(onClick = { onAction(CalendarAction.NextMonth) }) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = stringResource(R.string.calendar_next_month))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAction(CalendarAction.NewEvent) }) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.calendar_add_event))
            }
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding)) { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
            return@Scaffold
        }
        val allDay = stringResource(R.string.calendar_all_day)
        LazyColumn(Modifier.fillMaxSize().padding(padding)) {
            item(key = "grid") {
                MonthGridView(
                    cells = state.cells,
                    onSelect = { onAction(CalendarAction.Select(it.date)) },
                    modifier = Modifier.padding(horizontal = 8.dp),
                )
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }
            item(key = "agenda_header") {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = state.selected.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", locale)),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { heading() },
                    )
                }
            }
            if (state.agenda.isEmpty()) {
                item(key = "agenda_empty") {
                    Text(
                        stringResource(R.string.calendar_agenda_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            items(state.agenda, key = { it.key }) { item ->
                Column(Modifier.animateItem()) {
                    when (item) {
                        is AgendaItem.Event -> AgendaRow(
                            color = DeskTabColor.fromNameOrNull(item.event.colorName)?.container ?: MaterialTheme.colorScheme.secondary,
                            timeText = EventTimeText.format(item.event, state.selected, zone, is24Hour, locale, allDay),
                            title = item.event.title,
                            supporting = item.event.location,
                            onClick = { onAction(CalendarAction.EditEvent(item.event.id)) },
                        )
                        is AgendaItem.Todo -> AgendaRow(
                            color = DeskTabColor.Orange.container,
                            timeText = item.todo.time?.format(DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "a h:mm", locale))
                                ?: stringResource(R.string.calendar_todo_label),
                            title = item.todo.title,
                            supporting = if (item.todo.isDone) stringResource(R.string.calendar_todo_done) else stringResource(R.string.calendar_todo_label),
                            struck = item.todo.isDone,
                        )
                    }
                }
            }
        }
    }
}

private val ToolColor = DeskTabColor.Red

@Composable
private fun EditorHost(state: CalendarUiState, is24Hour: Boolean, onAction: (CalendarAction) -> Unit) {
    val draft = state.editor ?: return
    EventEditorSheet(
        draft = draft,
        is24Hour = is24Hour,
        onSave = { onAction(CalendarAction.SaveEvent(it)) },
        onDelete = draft.id?.let { id -> { onAction(CalendarAction.DeleteEvent(id)) } },
        onDismiss = { onAction(CalendarAction.DismissEditor) },
    )
}

@DeskPreviews
@Composable
private fun CalendarScreenPreview() {
    DeskTheme { CalendarScreen(state = CalendarPreviewData.state, onAction = {}, zone = CalendarPreviewData.zone) }
}

@DeskPreviews
@Composable
private fun CalendarScreenEmptyDayPreview() {
    DeskTheme { CalendarScreen(state = CalendarPreviewData.state.copy(agenda = emptyList()), onAction = {}, zone = CalendarPreviewData.zone) }
}
