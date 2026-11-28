package com.desklauncher.feature.desk.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
 * 책상 중앙의 펼친 공책 한 장 (PRD §3.1). 기록이 없으면 '첫 기록 적기' + 빈 줄, 기록이 생기면
 * **같은 자리·같은 모양**에 공책 › 페이지, 마지막 문장, '이어서 쓰기'. 누르면 펼쳐 둔 작업을 이어 쓴다
 * (새 임시 종이는 하단 바로 적기). 줄은 본문 줄높이 간격의 얇은 선 — 실물 줄 공책.
 */
@Composable
fun ResumeCard(resume: ResumeUi?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DeskTheme.colors
    val interaction = remember { MutableInteractionSource() }
    val ruleGap = with(LocalDensity.current) { DeskType.body.lineHeight.toDp() }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .pressScale(interaction)
            .clip(DeskShape.card)
            .background(colors.paper)
            .border(DeskLine.hairline, colors.lineStrong, DeskShape.card)
            .clickable(interactionSource = interaction, indication = null, role = Role.Button, onClick = onClick)
            .padding(horizontal = DeskSpacing.m, vertical = DeskSpacing.m),
    ) {
        if (resume == null) {
            Text(stringResource(R.string.desk_first_write_title), style = DeskType.cardTitle, color = colors.onPaper)
            Spacer(Modifier.height(DeskSpacing.xxs))
            Text(stringResource(R.string.desk_first_write_body), style = DeskType.caption, color = colors.muted)
            // 빈 줄 두 개 — 여기에 쓰면 된다는 신호
            repeat(EmptyRules) { RuledLine(gap = ruleGap) }
        } else {
            Text(
                text = stringResource(R.string.desk_resume_path, resume.notebookName, resume.pageTitle),
                style = DeskType.caption,
                color = colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(DeskSpacing.xs))
            Text(
                text = resume.lastSentence,
                style = DeskType.body,
                color = colors.onPaper,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            RuledLine(gap = DeskSpacing.xs)
            Text(
                text = stringResource(R.string.desk_resume_action),
                style = DeskType.label,
                color = colors.ink,
                modifier = Modifier.align(Alignment.End).padding(top = DeskSpacing.xs),
            )
        }
    }
}

@Composable
private fun RuledLine(gap: Dp) {
    Box(Modifier.fillMaxWidth().heightIn(min = gap), contentAlignment = Alignment.BottomStart) {
        HorizontalDivider(thickness = DeskLine.hairline, color = DeskTheme.colors.line)
    }
}

private const val EmptyRules = 2

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
