package com.desklauncher.feature.diary.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Create
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.diary.R
import com.desklauncher.feature.diary.domain.model.DiaryEntry
import com.desklauncher.feature.diary.domain.model.Mood
import com.desklauncher.feature.diary.ui.component.icon
import com.desklauncher.feature.diary.ui.component.labelRes
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters

@Composable
fun DiaryListRoute(
    onBack: () -> Unit,
    onOpen: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiaryListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DiaryListScreen(
        state = uiState,
        onAction = { action ->
            when (action) {
                DiaryListAction.Back -> onBack()
                is DiaryListAction.Open -> onOpen(action.date)
                else -> viewModel.onAction(action)
            }
        },
        modifier = modifier,
    )
}

@Composable
fun DiaryListScreen(
    state: DiaryListUiState,
    onAction: (DiaryListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Scaffold(
        modifier = modifier,
        topBar = {
            ToolTopBar(
                title = stringResource(R.string.diary_tool_title),
                tabColor = DeskTabColor.Pink,
                backContentDescription = stringResource(R.string.diary_back),
                onBack = { onAction(DiaryListAction.Back) },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAction(DiaryListAction.Open(state.today)) },
                icon = { Icon(Icons.Outlined.Create, contentDescription = null) },
                text = { Text(stringResource(if (state.today in state.written) R.string.diary_continue_today else R.string.diary_write_today)) },
            )
        },
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding)) { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
            return@Scaffold
        }
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(bottom = 96.dp)) {
            item(key = "calendar") {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onAction(DiaryListAction.PrevMonth) }) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, contentDescription = stringResource(R.string.diary_prev_month))
                    }
                    Text(
                        state.month.format(DateTimeFormatter.ofPattern("yyyy년 M월", locale)),
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { onAction(DiaryListAction.NextMonth) }) {
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = stringResource(R.string.diary_next_month))
                    }
                }
                WrittenDaysCalendar(state, onOpen = { onAction(DiaryListAction.Open(it)) })
                HorizontalDivider(Modifier.padding(top = 8.dp))
            }
            if (state.entries.isEmpty()) {
                item(key = "empty") {
                    Text(
                        stringResource(R.string.diary_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            items(state.entries, key = { it.id }) { entry ->
                DiaryEntryRow(entry, onClick = { onAction(DiaryListAction.Open(entry.date)) }, modifier = Modifier.animateItem())
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun WrittenDaysCalendar(state: DiaryListUiState, onOpen: (LocalDate) -> Unit) {
    val locale = LocalConfiguration.current.locales[0]
    Column(Modifier.padding(horizontal = 8.dp)) {
        Row {
            state.days.take(7).forEach { day ->
                Text(
                    day.dayOfWeek.getDisplayName(TextStyle.NARROW, locale),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f),
                )
            }
        }
        state.days.chunked(7).forEach { week ->
            Row {
                week.forEach { day ->
                    val written = day in state.written
                    val mood = state.written[day]
                    val future = day.isAfter(state.today)
                    val description = day.format(DateTimeFormatter.ofPattern("M월 d일", locale)) +
                        if (written) ", " + stringResource(R.string.diary_written) else ""
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(3.dp)
                            .alpha(if (YearMonth.from(day) == state.month) 1f else 0.35f)
                            .then(if (written) Modifier.background(DeskTabColor.Pink.container.copy(alpha = 0.35f), CircleShape) else Modifier)
                            .then(if (day == state.today) Modifier.border(1.5.dp, DeskTabColor.Pink.container, CircleShape) else Modifier)
                            .then(if (!future) Modifier.clickable { onOpen(day) } else Modifier)
                            .semantics { contentDescription = description },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (mood != null) {
                            Icon(mood.icon(), contentDescription = null, modifier = Modifier.size(20.dp), tint = DeskTabColor.Pink.onContainer)
                        } else {
                            Text(day.dayOfMonth.toString(), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryEntryRow(entry: DiaryEntry, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val locale = LocalConfiguration.current.locales[0]
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val mood = entry.mood
        if (mood != null) {
            Icon(mood.icon(), contentDescription = stringResource(mood.labelRes()), tint = DeskTabColor.Pink.container)
        }
        Column(Modifier.weight(1f)) {
            Text(entry.date.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", locale)), style = MaterialTheme.typography.titleSmall)
            if (entry.body.isNotBlank()) {
                Text(entry.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@DeskPreviews
@Composable
private fun DiaryListScreenPreview() {
    val today = LocalDate.of(2026, 11, 26)
    val entries = listOf(
        DiaryEntry("1", today, Mood.Good, "서류철 스트립을 실기기에서 써 봤다.", Instant.EPOCH, Instant.EPOCH),
        DiaryEntry("2", today.minusDays(2), Mood.Okay, "평범한 하루", Instant.EPOCH, Instant.EPOCH),
        DiaryEntry("3", today.minusDays(5), null, "기분은 안 적었다", Instant.EPOCH, Instant.EPOCH),
    )
    val month = YearMonth.of(2026, 11)
    val start = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY))
    DeskTheme {
        DiaryListScreen(
            state = DiaryListUiState(false, today, month, List(42) { start.plusDays(it.toLong()) }, entries.associate { it.date to it.mood }, entries),
            onAction = {},
        )
    }
}
