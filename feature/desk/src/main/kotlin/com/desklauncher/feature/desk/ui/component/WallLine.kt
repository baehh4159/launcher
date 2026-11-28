package com.desklauncher.feature.desk.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.desk.R
import com.desklauncher.feature.desk.clock.WallClockText

/**
 * 책상 상단 '벽' 줄 — 펼친 책상에서는 시각·날짜를 작게 한 줄로 (PRD §3.1).
 * 덮개·캘린더 한 줄은 HOME-03·04에서 이 줄에 붙는다.
 */
@Composable
fun WallLine(clock: WallClockText, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DeskTheme.colors
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DeskSpacing.xs),
    ) {
        Text(text = clock.time, style = DeskType.wallClockCompact, color = colors.onPaper, maxLines = 1)
        Text(
            text = clock.date,
            style = DeskType.caption,
            color = colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onOpenSettings) {
            Icon(Icons.Outlined.Settings, contentDescription = stringResource(R.string.desk_settings), tint = colors.muted)
        }
    }
}

@DeskFontScalePreviews
@Composable
private fun WallLinePreview() {
    DeskTheme { WallLine(WallClockText(time = "오후 3:24", date = "11월 28일 토요일"), onOpenSettings = {}) }
}
