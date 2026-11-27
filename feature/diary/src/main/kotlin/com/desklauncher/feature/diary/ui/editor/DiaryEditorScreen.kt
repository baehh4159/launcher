package com.desklauncher.feature.diary.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.desklauncher.core.designsystem.component.ToolTopBar
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.toolTabColor
import com.desklauncher.feature.diary.R
import com.desklauncher.feature.diary.domain.model.Mood
import com.desklauncher.feature.diary.ui.component.MoodSelector
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.desklauncher.feature.diary.DiaryToolId

@Composable
fun DiaryEditorRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DiaryEditorViewModel = hiltViewModel(),
) {
    DiaryEditorScreen(
        date = viewModel.date,
        isLoading = viewModel.isLoading,
        body = viewModel.body,
        mood = viewModel.mood,
        isSaved = viewModel.isSaved,
        canDelete = viewModel.hasEntry,
        onBodyChange = viewModel::onBodyChange,
        onMoodChange = viewModel::onMoodChange,
        onDelete = { viewModel.delete(onBack) },
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun DiaryEditorScreen(
    date: LocalDate,
    isLoading: Boolean,
    body: String,
    mood: Mood?,
    isSaved: Boolean,
    canDelete: Boolean,
    onBodyChange: (String) -> Unit,
    onMoodChange: (Mood?) -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val locale = LocalConfiguration.current.locales[0]
    Scaffold(
        modifier = modifier.imePadding(),
        topBar = {
            ToolTopBar(
                title = date.format(DateTimeFormatter.ofPattern("M월 d일 EEEE", locale)),
                tabColor = toolTabColor(DiaryToolId, DeskTabColor.Pink),
                backContentDescription = stringResource(R.string.diary_back),
                onBack = onBack,
                actions = {
                    if (isSaved && canDelete) {
                        Text(stringResource(R.string.diary_saved), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(end = 4.dp))
                    }
                    if (canDelete) {
                        IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, contentDescription = stringResource(R.string.diary_delete)) }
                    }
                },
            )
        },
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize().padding(padding)) { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.diary_mood_title), style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp))
            MoodSelector(selected = mood, onSelect = onMoodChange, modifier = Modifier.padding(horizontal = 8.dp))
            TextField(
                value = body,
                onValueChange = onBodyChange,
                placeholder = { Text(stringResource(R.string.diary_body_hint)) },
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                minLines = 8,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@DeskPreviews
@Composable
private fun DiaryEditorScreenPreview() {
    DeskTheme {
        DiaryEditorScreen(
            date = LocalDate.of(2026, 11, 26), isLoading = false,
            body = "서류철 스트립을 처음 실기기에서 써 봤다. 드르륵 넘어가는 느낌이 생각보다 좋다.",
            mood = Mood.Good, isSaved = true, canDelete = true,
            onBodyChange = {}, onMoodChange = {}, onDelete = {}, onBack = {},
        )
    }
}

@DeskPreviews
@Composable
private fun DiaryEditorScreenEmptyPreview() {
    DeskTheme {
        DiaryEditorScreen(
            date = LocalDate.of(2026, 11, 26), isLoading = false, body = "", mood = null, isSaved = true, canDelete = false,
            onBodyChange = {}, onMoodChange = {}, onDelete = {}, onBack = {},
        )
    }
}
