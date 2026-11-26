package com.desklauncher.feature.calendar.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.calendar.R
import com.desklauncher.feature.calendar.ui.DayCell
import com.desklauncher.feature.calendar.ui.preview.CalendarPreviewData
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

/** 6×7 월간 격자 (CAL-01). 일요일은 빨강, 토요일은 파랑 (한국 달력 관례). */
@Composable
internal fun MonthGridView(
    cells: List<DayCell>,
    onSelect: (DayCell) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Column(modifier) {
        Row(Modifier.fillMaxWidth()) {
            cells.take(7).forEach { cell ->
                Text(
                    text = cell.date.dayOfWeek.getDisplayName(TextStyle.SHORT, locale),
                    style = MaterialTheme.typography.labelMedium,
                    color = weekdayColor(cell.date.dayOfWeek),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                )
            }
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth()) {
                week.forEach { cell ->
                    DayCellView(cell = cell, onClick = { onSelect(cell) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCellView(cell: DayCell, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    val description = stringResource(
        R.string.calendar_day_description,
        cell.date.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", locale)),
        cell.itemCount,
    )
    Column(
        modifier = modifier
            .aspectRatio(0.9f)
            .selectable(selected = cell.isSelected, role = Role.Tab, onClick = onClick)
            .semantics { contentDescription = description }
            .alpha(if (cell.inMonth) 1f else 0.38f)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        val primary = MaterialTheme.colorScheme.primary
        Box(
            modifier = Modifier
                .size(30.dp)
                .then(if (cell.isSelected) Modifier.background(primary, CircleShape) else Modifier)
                .then(if (cell.isToday && !cell.isSelected) Modifier.border(1.5.dp, primary, CircleShape) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = cell.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Normal,
                color = if (cell.isSelected) MaterialTheme.colorScheme.onPrimary else weekdayColor(cell.date.dayOfWeek),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            cell.dots.forEach { colorName ->
                val color = DeskTabColor.fromNameOrNull(colorName)?.container ?: MaterialTheme.colorScheme.secondary
                Box(Modifier.size(5.dp).background(color, CircleShape))
            }
        }
    }
}

@Composable
private fun weekdayColor(day: DayOfWeek) = when (day) {
    DayOfWeek.SUNDAY -> MaterialTheme.colorScheme.error
    DayOfWeek.SATURDAY -> DeskTabColor.Blue.container
    else -> MaterialTheme.colorScheme.onSurface
}

@DeskPreviews
@Composable
private fun MonthGridViewPreview() {
    DeskTheme { MonthGridView(cells = CalendarPreviewData.state.cells, onSelect = {}) }
}
