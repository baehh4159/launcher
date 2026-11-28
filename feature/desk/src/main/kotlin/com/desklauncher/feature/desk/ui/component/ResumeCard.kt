package com.desklauncher.feature.desk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import com.desklauncher.core.designsystem.component.pressScale
import com.desklauncher.core.designsystem.preview.DeskFontScalePreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskShape
import com.desklauncher.core.designsystem.theme.DeskSpacing
import com.desklauncher.core.designsystem.theme.DeskTheme
import com.desklauncher.core.designsystem.theme.DeskType
import com.desklauncher.feature.desk.R
import com.desklauncher.feature.desk.ui.ResumeUi

/**
 * 책상 중앙 첫 자리 (PRD §3.1). 펼쳐 둔 작업이 있으면 '이어서 쓰기', 없으면 '첫 기록 적기'.
 * 종이 카드: 그림자 없이 얇은 선(D5), 누르면 0.97배(beUI press).
 */
@Composable
fun ResumeCard(resume: ResumeUi?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DeskTheme.colors
    val interaction = remember { MutableInteractionSource() }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(DeskShape.card)
            .background(colors.paper)
            .border(DeskLine.hairline, colors.lineStrong, DeskShape.card)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(DeskSpacing.m),
    ) {
        if (resume == null) {
            Text(stringResource(R.string.desk_first_write_title), style = DeskType.cardTitle, color = colors.onPaper)
            Spacer(Modifier.height(DeskSpacing.xxs))
            Text(stringResource(R.string.desk_first_write_body), style = DeskType.caption, color = colors.muted)
        } else {
            Text(
                text = stringResource(R.string.desk_resume_path, resume.notebookName, resume.pageTitle),
                style = DeskType.caption,
                color = colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(DeskSpacing.xxs))
            Text(
                text = resume.lastSentence,
                style = DeskType.body,
                color = colors.onPaper,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(DeskSpacing.xs))
            Text(stringResource(R.string.desk_resume_action), style = DeskType.label, color = colors.ink)
        }
    }
}

@DeskFontScalePreviews
@Composable
private fun ResumeCardFirstPreview() {
    DeskTheme { ResumeCard(resume = null, onClick = {}, modifier = Modifier.padding(DeskSpacing.m)) }
}

@DeskFontScalePreviews
@Composable
private fun ResumeCardPreview() {
    DeskTheme {
        ResumeCard(
            resume = ResumeUi("보드 검증", "11/27 전원부", "부하 연결 후 출력은 3.30V 대비 3.18V로 약 3.6% 감소했다"),
            onClick = {},
            modifier = Modifier.padding(DeskSpacing.m),
        )
    }
}
