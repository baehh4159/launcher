package com.desklauncher.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Material 3 색 체계 밖의 책상 전용 색. [DeskTheme]으로만 제공된다. */
@Immutable
data class DeskColors(
    /** 책상(홈) 바탕 = 미색 종이 */
    val desk: Color,
    /** 책상 위 글자(시계 등) = 먹색 */
    val onDesk: Color,
    /** 책상 위 보조 글자 */
    val onDeskSecondary: Color,
    /** 종이 카드 표면 */
    val paper: Color,
    val onPaper: Color,
    /** 영역 구분 얇은 선 — 그림자 대신 쓴다 */
    val line: Color,
    val lineStrong: Color,
)

/** 그림자 대신 쓰는 선 두께 */
object DeskLine {
    val Hairline: Dp = 0.5.dp
    val Thin: Dp = 1.dp
}

internal val LightDeskColors = DeskColors(
    desk = PaperBackground,
    onDesk = Ink,
    onDeskSecondary = InkSecondary,
    paper = PaperSurface,
    onPaper = Ink,
    line = PaperLine,
    lineStrong = PaperLineStrong,
)

internal val DarkDeskColors = DeskColors(
    desk = NightBackground,
    onDesk = NightInk,
    onDeskSecondary = NightInkSecondary,
    paper = NightSurface,
    onPaper = NightInk,
    line = NightLine,
    lineStrong = NightLineStrong,
)

internal val LocalDeskColors = staticCompositionLocalOf { LightDeskColors }
