package com.desklauncher.core.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import com.desklauncher.core.designsystem.preview.DeskPreviews
import com.desklauncher.core.designsystem.theme.DeskLine
import com.desklauncher.core.designsystem.theme.DeskTabColor
import com.desklauncher.core.designsystem.theme.DeskTheme

/**
 * 책상 위에 펼친 서류철 창 (SHELL-05). 도구·앱 서랍을 전체 화면 대신 이 창 안에 띄운다.
 * 시스템 바·키보드 영역은 창 바깥에서 소비하므로, 안쪽 Scaffold/TopAppBar는 같은 여백을 다시 넣지 않는다.
 */
@Composable
fun DeskPopupFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(DeskPopupDefaults.Margin),
    ) {
        Surface(
            shape = DeskPopupDefaults.Shape,
            color = DeskTheme.colors.paper,
            border = BorderStroke(DeskLine.Hairline, DeskTheme.colors.lineStrong),
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(content = content)
        }
    }
}

/** 팝업 뒤 책상을 어둡게 덮는 막. 누르면 [onDismiss]. 낭독 대상이 아니다(닫기는 뒤로 버튼으로). */
@Composable
fun DeskPopupScrim(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = DeskPopupDefaults.ScrimAlpha))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onDismiss)
            .clearAndSetSemantics { },
    )
}

object DeskPopupDefaults {
    /** 창 둘레로 책상이 보이는 여백 — 도구 화면 면적을 최대한 남기면서 '창'으로 보이는 최소 폭 */
    val Margin = 10.dp
    val Shape = RoundedCornerShape(20.dp)
    const val ScrimAlpha = 0.32f
}

@DeskPreviews
@Composable
private fun DeskPopupFramePreview() {
    DeskTheme {
        DeskSurface {
            DeskPopupScrim(onDismiss = {})
            DeskPopupFrame {
                ToolTopBar(title = "할일정리", tabColor = DeskTabColor.Orange, backContentDescription = "책상으로", onBack = {})
                Text(
                    text = "도구 화면",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }
}
