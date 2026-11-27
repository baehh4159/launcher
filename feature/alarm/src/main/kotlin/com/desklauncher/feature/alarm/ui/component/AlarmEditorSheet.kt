package com.desklauncher.feature.alarm.ui.component

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.alarm.R
import com.desklauncher.feature.alarm.domain.model.Alarm
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmEditorSheet(
    alarm: Alarm,
    onSave: (Alarm) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        AlarmEditorContent(alarm, onSave, onDelete)
    }
}

/** 알람 편집 (ALARM-02): 시각 · 반복 요일 · 라벨 · 진동 · 다시 울림 간격 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AlarmEditorContent(
    alarm: Alarm,
    onSave: (Alarm) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val timeState = rememberTimePickerState(alarm.time.hour, alarm.time.minute, remember(context) { DateFormat.is24HourFormat(context) })
    var days by rememberSaveable(alarm.id) { mutableStateOf(alarm.repeatDays.map { it.value }.toSet()) }
    var label by rememberSaveable(alarm.id) { mutableStateOf(alarm.label) }
    var vibrate by rememberSaveable(alarm.id) { mutableStateOf(alarm.vibrate) }
    var snooze by rememberSaveable(alarm.id) { mutableStateOf(alarm.snoozeMinutes) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(stringResource(R.string.alarm_editor_title), style = MaterialTheme.typography.titleLarge)
        TimeInput(state = timeState, modifier = Modifier.align(Alignment.CenterHorizontally))

        Text(stringResource(R.string.alarm_editor_repeat), style = MaterialTheme.typography.labelLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            // 월요일부터 (한국 알람 앱 관례)
            DayOfWeek.entries.forEach { day ->
                FilterChip(
                    selected = day.value in days,
                    onClick = { days = if (day.value in days) days - day.value else days + day.value },
                    label = { Text(day.getDisplayName(TextStyle.NARROW, locale)) },
                )
            }
        }
        OutlinedTextField(
            value = label,
            onValueChange = { label = it },
            label = { Text(stringResource(R.string.alarm_editor_label)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.alarm_editor_vibrate), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = vibrate, onCheckedChange = { vibrate = it })
        }
        Text(stringResource(R.string.alarm_editor_snooze), style = MaterialTheme.typography.labelLarge)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SnoozeOptions.forEachIndexed { index, minutes ->
                SegmentedButton(
                    selected = snooze == minutes,
                    onClick = { snooze = minutes },
                    shape = SegmentedButtonDefaults.itemShape(index, SnoozeOptions.size),
                ) { Text(stringResource(R.string.alarm_in_minutes, minutes)) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onDelete) { Text(stringResource(R.string.alarm_delete), color = MaterialTheme.colorScheme.error) }
            Button(onClick = {
                onSave(
                    alarm.copy(
                        time = LocalTime.of(timeState.hour, timeState.minute),
                        repeatDays = days.map(DayOfWeek::of).toSet(),
                        label = label.trim(),
                        vibrate = vibrate,
                        snoozeMinutes = snooze,
                        // 편집해서 저장하면 다시 켠다 (꺼 둔 알람의 시각만 바꾸는 경우는 드물다)
                        enabled = true,
                    ),
                )
            }) { Text(stringResource(R.string.alarm_editor_save)) }
        }
    }
}

private val SnoozeOptions = listOf(5, 10, 15, 30)

@DeskPreviews
@Composable
private fun AlarmEditorContentPreview() {
    DeskTheme {
        AlarmEditorContent(
            Alarm("1", LocalTime.of(6, 30), setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY), "출근", createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH),
            onSave = {}, onDelete = {},
        )
    }
}
