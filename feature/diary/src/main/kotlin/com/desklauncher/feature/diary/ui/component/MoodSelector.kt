package com.desklauncher.feature.diary.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SentimentDissatisfied
import androidx.compose.material.icons.outlined.SentimentNeutral
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.SentimentVeryDissatisfied
import androidx.compose.material.icons.outlined.SentimentVerySatisfied
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.toolTabColor
import com.desklauncher.feature.diary.R
import com.desklauncher.feature.diary.domain.model.Mood
import com.desklauncher.feature.diary.DiaryToolId

/** 기분 5단계 선택. 같은 기분을 다시 누르면 선택 해제. */
@Composable
internal fun MoodSelector(
    selected: Mood?,
    onSelect: (Mood?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        Mood.entries.forEach { mood ->
            val isSelected = mood == selected
            val label = stringResource(mood.labelRes())
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(if (isSelected) toolTabColor(DiaryToolId, DeskTabColor.Pink).container else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(if (isSelected) null else mood) })
                    .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = mood.icon(),
                    contentDescription = null,
                    tint = if (isSelected) toolTabColor(DiaryToolId, DeskTabColor.Pink).onContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(4.dp).size(30.dp),
                )
            }
        }
    }
}

internal fun Mood.icon(): ImageVector = when (this) {
    Mood.VeryBad -> Icons.Outlined.SentimentVeryDissatisfied
    Mood.Bad -> Icons.Outlined.SentimentDissatisfied
    Mood.Okay -> Icons.Outlined.SentimentNeutral
    Mood.Good -> Icons.Outlined.SentimentSatisfied
    Mood.VeryGood -> Icons.Outlined.SentimentVerySatisfied
}

@StringRes
internal fun Mood.labelRes(): Int = when (this) {
    Mood.VeryBad -> R.string.diary_mood_very_bad
    Mood.Bad -> R.string.diary_mood_bad
    Mood.Okay -> R.string.diary_mood_okay
    Mood.Good -> R.string.diary_mood_good
    Mood.VeryGood -> R.string.diary_mood_very_good
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun MoodSelectorPreview() {
    DeskTheme { MoodSelector(selected = Mood.Good, onSelect = {}, modifier = Modifier.padding(16.dp)) }
}
