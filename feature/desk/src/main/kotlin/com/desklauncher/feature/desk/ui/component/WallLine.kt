package com.desklauncher.feature.desk.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.desk.clock.WallClockText

/** 책상 상단 '벽' 줄: 시각·날짜. (덮기·캘린더 한 줄은 HOME-03·04에서 이 줄에 붙는다) */
@Composable
fun WallLine(clock: WallClockText, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(text = clock.time, style = DeskType.wallClock, color = DeskTheme.colors.onPaper)
        Text(text = clock.date, style = DeskType.caption, color = DeskTheme.colors.muted)
    }
}

@DeskFontScalePreviews
@Composable
private fun WallLinePreview() {
    DeskTheme { WallLine(WallClockText(time = "오후 3:24", date = "11월 28일 토요일")) }
}
