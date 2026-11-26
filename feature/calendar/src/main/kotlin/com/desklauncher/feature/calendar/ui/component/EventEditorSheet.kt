package com.desklauncher.feature.calendar.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import com.desklauncher.core.designsystem.component.DeskColorPicker
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.calendar.R
import com.desklauncher.feature.calendar.ui.EventDraft
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventEditorSheet(
    draft: EventDraft,
    is24Hour: Boolean,
    onSave: (EventDraft) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        EventEditorContent(draft, is24Hour, onSave, onDelete)
    }
}

private enum class PickerTarget { StartDate, StartTime, EndDate, EndTime }

/** 일정 편집 (CAL-02): 제목·종일·시작/끝·장소·메모·색. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventEditorContent(
    initial: EventDraft,
    is24Hour: Boolean,
    onSave: (EventDraft) -> Unit,
    onDelete: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    var draft by rememberSaveable(initial.id, initial.startDate) { mutableStateOf(initial) }
    var picker by rememberSaveable { mutableStateOf<PickerTarget?>(null) }
    val locale = LocalConfiguration.current.locales[0]
    val dateFormat = DateTimeFormatter.ofPattern("M월 d일 (E)", locale)
    val timeFormat = DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "a h:mm", locale)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(if (initial.id == null) R.string.calendar_editor_new else R.string.calendar_editor_edit),
            style = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            value = draft.title,
            onValueChange = { draft = draft.copy(title = it) },
            label = { Text(stringResource(R.string.calendar_editor_title)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.calendar_all_day), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = draft.allDay, onCheckedChange = { draft = draft.copy(allDay = it).normalized() })
        }
        DateTimeRow(
            label = stringResource(R.string.calendar_editor_start),
            date = draft.startDate.format(dateFormat),
            time = if (draft.allDay) null else draft.startTime.format(timeFormat),
            onDate = { picker = PickerTarget.StartDate },
            onTime = { picker = PickerTarget.StartTime },
        )
        DateTimeRow(
            label = stringResource(R.string.calendar_editor_end),
            date = draft.endDate.format(dateFormat),
            time = if (draft.allDay) null else draft.endTime.format(timeFormat),
            onDate = { picker = PickerTarget.EndDate },
            onTime = { picker = PickerTarget.EndTime },
        )
        OutlinedTextField(
            value = draft.location,
            onValueChange = { draft = draft.copy(location = it) },
            label = { Text(stringResource(R.string.calendar_editor_location)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = draft.note,
            onValueChange = { draft = draft.copy(note = it) },
            label = { Text(stringResource(R.string.calendar_editor_note)) },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(stringResource(R.string.calendar_editor_color), style = MaterialTheme.typography.labelLarge)
        DeskColorPicker(
            selected = DeskTabColor.fromNameOrNull(draft.colorName) ?: DeskTabColor.Red,
            onSelect = { draft = draft.copy(colorName = it.name) },
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (onDelete != null) {
                TextButton(onClick = onDelete) { Text(stringResource(R.string.calendar_editor_delete), color = MaterialTheme.colorScheme.error) }
            } else {
                Text("")
            }
            Button(onClick = { onSave(draft.normalized()) }, enabled = draft.title.isNotBlank()) {
                Text(stringResource(R.string.calendar_editor_save))
            }
        }
    }

    when (val target = picker) {
        PickerTarget.StartDate, PickerTarget.EndDate -> {
            val current = if (target == PickerTarget.StartDate) draft.startDate else draft.endDate
            val state = rememberDatePickerState(initialSelectedDateMillis = current.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            DatePickerDialog(
                onDismissRequest = { picker = null },
                confirmButton = {
                    TextButton(onClick = {
                        state.selectedDateMillis?.let { millis ->
                            val date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                            draft = if (target == PickerTarget.StartDate) draft.withStartDate(date) else draft.copy(endDate = date)
                            draft = draft.normalized()
                        }
                        picker = null
                    }) { Text(stringResource(R.string.calendar_editor_ok)) }
                },
                dismissButton = { TextButton(onClick = { picker = null }) { Text(stringResource(R.string.calendar_editor_cancel)) } },
            ) { DatePicker(state = state) }
        }
        PickerTarget.StartTime, PickerTarget.EndTime -> {
            val current = if (target == PickerTarget.StartTime) draft.startTime else draft.endTime
            val state = rememberTimePickerState(initialHour = current.hour, initialMinute = current.minute, is24Hour = is24Hour)
            AlertDialog(
                onDismissRequest = { picker = null },
                confirmButton = {
                    TextButton(onClick = {
                        val time = LocalTime.of(state.hour, state.minute)
                        draft = if (target == PickerTarget.StartTime) draft.withStartTime(time) else draft.copy(endTime = time)
                        draft = draft.normalized()
                        picker = null
                    }) { Text(stringResource(R.string.calendar_editor_ok)) }
                },
                dismissButton = { TextButton(onClick = { picker = null }) { Text(stringResource(R.string.calendar_editor_cancel)) } },
                text = { TimePicker(state = state) },
            )
        }
        null -> Unit
    }
}

/** 시작을 옮기면 길이를 유지한 채 끝도 함께 옮긴다. */
private fun EventDraft.withStartDate(date: LocalDate): EventDraft {
    val days = ChronoUnit.DAYS.between(startDate, date)
    return copy(startDate = date, endDate = endDate.plusDays(days))
}

private fun EventDraft.withStartTime(time: LocalTime): EventDraft {
    val start = LocalDateTime.of(startDate, startTime)
    val duration = Duration.between(start, LocalDateTime.of(endDate, endTime))
    val newEnd = LocalDateTime.of(startDate, time).plus(duration)
    return copy(startTime = time, endDate = newEnd.toLocalDate(), endTime = newEnd.toLocalTime())
}

@Composable
private fun DateTimeRow(label: String, date: String, time: String?, onDate: () -> Unit, onTime: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(end = 8.dp))
        AssistChip(onClick = onDate, label = { Text(date) })
        if (time != null) AssistChip(onClick = onTime, label = { Text(time) })
    }
}

@DeskPreviews
@Composable
private fun EventEditorContentPreview() {
    DeskTheme {
        EventEditorContent(
            initial = EventDraft(
                id = "e1", title = "디자인 리뷰", startDate = LocalDate.of(2026, 11, 26), startTime = LocalTime.of(15, 0),
                endDate = LocalDate.of(2026, 11, 26), endTime = LocalTime.of(16, 0), location = "회의실 B", colorName = "Blue",
            ),
            is24Hour = false,
            onSave = {},
            onDelete = {},
        )
    }
}
