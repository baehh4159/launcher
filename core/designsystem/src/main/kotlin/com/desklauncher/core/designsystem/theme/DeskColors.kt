package com.desklauncher.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Material 3 색 체계에 없는 책상 전용 색. [DeskTheme]으로만 제공된다. */
@Immutable
data class DeskColors(
    val deskTop: Color,
    val deskBottom: Color,
    /** 책상 상판 위 글자(시계 등) */
    val onDesk: Color,
    /** 나뭇결 무늬 선 색 (알파 포함) */
    val deskGrain: Color,
    val paper: Color,
    val onPaper: Color,
)

internal val LightDeskColors = DeskColors(
    deskTop = DeskTopLight,
    deskBottom = DeskBottomLight,
    onDesk = Walnut10,
    deskGrain = DeskGrainLight,
    paper = Paper98,
    onPaper = Ink10,
)

internal val DarkDeskColors = DeskColors(
    deskTop = DeskTopDark,
    deskBottom = DeskBottomDark,
    onDesk = Ink90,
    deskGrain = DeskGrainDark,
    paper = Night20,
    onPaper = Ink90,
)

internal val LocalDeskColors = staticCompositionLocalOf { LightDeskColors }
