package com.desklauncher.feature.alarm.ui.component

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.alarm.R
import com.desklauncher.feature.alarm.domain.model.Alarm
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** 알람 한 줄: 시각 · 반복/라벨 · 남은 시간 · 스위치. 왼쪽으로 밀면 삭제. */
@Composable
internal fun AlarmRow(
    alarm: Alarm,
    untilNext: Duration?,
    onToggle: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnDelete by rememberUpdatedState(onDelete)
    val swipe = rememberSwipeToDismissBoxState()
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val is24Hour = remember(context) { DateFormat.is24HourFormat(context) }
    SwipeToDismissBox(
        state = swipe,
        enableDismissFromStartToEnd = false,
        onDismiss = { if (it == SwipeToDismissBoxValue.EndToStart) currentOnDelete() },
        backgroundContent = {
            Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = 20.dp), contentAlignment = Alignment.CenterEnd) {
                Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.alarm_delete), tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
        modifier = modifier,
    ) {
        Surface(color = MaterialTheme.colorScheme.surface) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .alpha(if (alarm.enabled) 1f else 0.5f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    val pattern = if (is24Hour) "HH:mm" else "a h:mm"
                    Text(alarm.time.format(DateTimeFormatter.ofPattern(pattern, locale)), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Light)
                    val summary = listOfNotNull(repeatSummary(alarm.repeatDays, locale), alarm.label.takeIf { it.isNotBlank() }).joinToString(" · ")
                    Text(summary, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (untilNext != null) {
                        Text(stringResource(R.string.alarm_in, durationText(untilNext)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                val toggleLabel = stringResource(R.string.alarm_toggle, alarm.time.toString())
                Switch(checked = alarm.enabled, onCheckedChange = onToggle, modifier = Modifier.semantics { contentDescription = toggleLabel })
            }
        }
    }
}

@Composable
internal fun repeatSummary(days: Set<DayOfWeek>, locale: Locale): String = when {
    days.isEmpty() -> stringResource(R.string.alarm_once)
    days.size == 7 -> stringResource(R.string.alarm_everyday)
    days == Weekdays -> stringResource(R.string.alarm_weekdays)
    days == Weekend -> stringResource(R.string.alarm_weekends)
    else -> DayOfWeek.entries.filter { it in days }.joinToString(" ") { it.getDisplayName(TextStyle.SHORT, locale) }
}

@Composable
internal fun durationText(duration: Duration): String {
    val totalMinutes = (duration.toMinutes() + if (duration.toSecondsPart() > 0) 1 else 0).coerceAtLeast(1)
    val days = totalMinutes / (60 * 24)
    val hours = (totalMinutes / 60) % 24
    val minutes = totalMinutes % 60
    return when {
        days > 0 -> stringResource(R.string.alarm_in_days, days.toInt(), hours.toInt())
        hours > 0 -> stringResource(R.string.alarm_in_hours_minutes, hours.toInt(), minutes.toInt())
        else -> stringResource(R.string.alarm_in_minutes, minutes.toInt())
    }
}

private val Weekdays = setOf(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)
private val Weekend = setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun AlarmRowPreview() {
    DeskTheme {
        Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
            AlarmRow(Alarm("1", LocalTime.of(7, 0), Weekdays, "출근", createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH), Duration.ofMinutes(615), {}, {}, {})
            AlarmRow(Alarm("2", LocalTime.of(10, 30), enabled = false, createdAt = Instant.EPOCH, updatedAt = Instant.EPOCH), null, {}, {}, {})
        }
    }
}
