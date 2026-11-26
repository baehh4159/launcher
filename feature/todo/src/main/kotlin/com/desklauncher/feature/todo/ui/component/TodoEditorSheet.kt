package com.desklauncher.feature.todo.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.todo.R
import com.desklauncher.feature.todo.domain.model.ProjectRef
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.domain.model.TodoPriority
import com.desklauncher.feature.todo.ui.DueLabelFormatter
import com.desklauncher.feature.todo.ui.preview.TodoPreviewData
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodoEditorSheet(
    todo: Todo,
    projects: List<ProjectRef>,
    today: LocalDate,
    is24Hour: Boolean,
    onSave: (Todo) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        TodoEditorContent(todo, projects, today, is24Hour, onSave, onDelete)
    }
}

/** 할일 편집: 제목·메모·마감(날짜+시각)·우선순위·프로젝트 (TODO-01, TODO-04). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TodoEditorContent(
    todo: Todo,
    projects: List<ProjectRef>,
    today: LocalDate,
    is24Hour: Boolean,
    onSave: (Todo) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var title by rememberSaveable(todo.id) { mutableStateOf(todo.title) }
    var note by rememberSaveable(todo.id) { mutableStateOf(todo.note) }
    var dueDate by rememberSaveable(todo.id) { mutableStateOf(todo.dueDate) }
    var dueTime by rememberSaveable(todo.id) { mutableStateOf(todo.dueTime) }
    var priority by rememberSaveable(todo.id) { mutableStateOf(todo.priority) }
    var projectId by rememberSaveable(todo.id) { mutableStateOf(todo.projectId) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val locale = LocalConfiguration.current.locales[0]

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.todo_editor_title), style = MaterialTheme.typography.titleLarge)
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text(stringResource(R.string.todo_editor_name)) },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(stringResource(R.string.todo_editor_note)) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )

        // 마감
        Text(stringResource(R.string.todo_editor_due), style = MaterialTheme.typography.labelLarge)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val words = DueLabelFormatter.RelativeWords(
                stringResource(R.string.todo_due_yesterday),
                stringResource(R.string.todo_due_today),
                stringResource(R.string.todo_due_tomorrow),
            )
            val dateLabel = dueDate?.let { DueLabelFormatter.format(it, null, today, is24Hour, locale, words).text }
            AssistChip(
                onClick = { showDatePicker = true },
                label = { Text(dateLabel ?: stringResource(R.string.todo_editor_pick_date)) },
                leadingIcon = { Icon(Icons.Outlined.Event, contentDescription = null) },
            )
            AssistChip(
                onClick = { showTimePicker = true },
                enabled = dueDate != null,
                label = {
                    Text(
                        dueTime?.let { DueLabelFormatter.format(today, it, today, is24Hour, locale, words).text.substringAfter(' ') }
                            ?: stringResource(R.string.todo_editor_pick_time),
                    )
                },
                leadingIcon = { Icon(Icons.Outlined.Schedule, contentDescription = null) },
            )
            if (dueDate != null) {
                IconButton(onClick = { dueDate = null; dueTime = null }) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.todo_editor_clear_due))
                }
            }
        }

        // 우선순위
        Text(stringResource(R.string.todo_editor_priority), style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            TodoPriority.entries.forEachIndexed { index, value ->
                SegmentedButton(
                    selected = priority == value,
                    onClick = { priority = value },
                    shape = SegmentedButtonDefaults.itemShape(index, TodoPriority.entries.size),
                ) { Text(stringResource(value.labelRes()), maxLines = 1) }
            }
        }

        // 프로젝트
        ProjectPicker(projects = projects, selectedId = projectId, onSelect = { projectId = it })

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onDelete) {
                Text(stringResource(R.string.todo_editor_delete), color = MaterialTheme.colorScheme.error)
            }
            Button(
                onClick = {
                    onSave(
                        todo.copy(
                            title = title,
                            note = note,
                            dueDate = dueDate,
                            dueTime = if (dueDate != null) dueTime else null,
                            priority = priority,
                            projectId = projectId,
                        ),
                    )
                },
                enabled = title.isNotBlank(),
            ) { Text(stringResource(R.string.todo_editor_save)) }
        }
    }

    if (showDatePicker) {
        // DatePicker는 UTC 자정 millis를 쓴다
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = (dueDate ?: today).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { dueDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate() }
                    showDatePicker = false
                }) { Text(stringResource(R.string.todo_editor_ok)) }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text(stringResource(R.string.todo_editor_cancel)) } },
        ) { DatePicker(state = pickerState) }
    }
    if (showTimePicker) {
        val initial = dueTime ?: LocalTime.of(9, 0)
        val timeState = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = is24Hour)
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dueTime = LocalTime.of(timeState.hour, timeState.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.todo_editor_ok)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    dueTime = null
                    showTimePicker = false
                }) { Text(stringResource(R.string.todo_editor_no_time)) }
            },
            text = { TimePicker(state = timeState) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProjectPicker(projects: List<ProjectRef>, selectedId: String?, onSelect: (String?) -> Unit) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val noneLabel = stringResource(R.string.todo_editor_no_project)
    val selected = projects.firstOrNull { it.id == selectedId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected?.name ?: noneLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.todo_editor_project)) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            supportingText = if (projects.isEmpty()) {
                { Text(stringResource(R.string.todo_editor_project_hint)) }
            } else {
                null
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(text = { Text(noneLabel) }, onClick = { onSelect(null); expanded = false })
            projects.forEach { project ->
                DropdownMenuItem(text = { Text(project.name) }, onClick = { onSelect(project.id); expanded = false })
            }
        }
    }
}

@DeskPreviews
@Composable
private fun TodoEditorContentPreview() {
    DeskTheme {
        TodoEditorContent(
            todo = TodoPreviewData.todos.first(),
            projects = TodoPreviewData.projects,
            today = TodoPreviewData.today,
            is24Hour = false,
            onSave = {},
            onDelete = {},
        )
    }
}
