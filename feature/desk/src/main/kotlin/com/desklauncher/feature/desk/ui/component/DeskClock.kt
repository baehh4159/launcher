package com.desklauncher.feature.desk.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.component.DeskSurface
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.feature.desk.clock.DeskClockText

/** 책상 좌상단 시계 (SHELL-02). */
@Composable
internal fun DeskClock(
    clock: DeskClockText,
    modifier: Modifier = Modifier,
) {
    val color = DeskTheme.colors.onDesk
    Column(modifier = modifier.semantics(mergeDescendants = true) { heading() }) {
        Text(
            text = clock.time,
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Light,
            color = color,
            maxLines = 1,
        )
        Text(
            text = clock.date,
            style = MaterialTheme.typography.titleMedium,
            color = DeskTheme.colors.onDeskSecondary,
            maxLines = 1,
        )
    }
}

@DeskPreviews
@DeskFontScalePreviews
@Composable
private fun DeskClockPreview() {
    DeskTheme {
        DeskSurface {
            DeskClock(
                clock = DeskClockText(time = "오후 3:24", date = "11월 26일 목요일"),
                modifier = Modifier.padding(24.dp),
            )
        }
    }
}
