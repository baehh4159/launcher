package com.desklauncher.feature.desk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.UnfoldMore
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskShape
import com.desklauncher.core.designsystem.theme.DeskSize
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.desk.R

/** 이 앱이 아직 기본 홈이 아닐 때만 보이는 안내 카드 (HOME-01) */
@Composable
fun DefaultHomeCard(onSet: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DeskTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.desk, DeskShape.card)
            .border(DeskLine.hairline, colors.lineStrong, DeskShape.card)
            .padding(start = DeskSpacing.m, end = DeskSpacing.xs, top = DeskSpacing.s, bottom = DeskSpacing.xxs),
    ) {
        Text(stringResource(R.string.desk_default_home_title), style = DeskType.label, color = colors.onPaper)
        Text(stringResource(R.string.desk_default_home_body), style = DeskType.caption, color = colors.muted)
        TextButton(onClick = onSet, modifier = Modifier.align(Alignment.End)) {
            Text(stringResource(R.string.desk_default_home_action), style = DeskType.label, color = colors.ink)
        }
    }
}

/** 첫 실행 훑기 안내 — 보이지 않는 훑기 영역 위에 놓인다. 위아래 아이콘이 동작을 설명한다(모션 없음, D1) */
@Composable
fun ScrubHint(modifier: Modifier = Modifier) {
    val colors = DeskTheme.colors
    Row(
        modifier = modifier
            .height(DeskSize.control)
            .background(colors.paper, DeskShape.pill)
            .border(DeskLine.thin, colors.lineStrong, DeskShape.pill)
            .padding(horizontal = DeskSpacing.s),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DeskSpacing.xxs),
    ) {
        Text(stringResource(R.string.desk_scrub_hint), style = DeskType.caption, color = colors.onPaper)
        Icon(Icons.Outlined.UnfoldMore, contentDescription = null, tint = colors.ink, modifier = Modifier.size(DeskSize.icon))
    }
}

@DeskFontScalePreviews
@Composable
private fun SetupCardsPreview() {
    DeskTheme {
        Column(Modifier.padding(DeskSpacing.m), verticalArrangement = Arrangement.spacedBy(DeskSpacing.s)) {
            DefaultHomeCard(onSet = {})
            ScrubHint()
        }
    }
}
