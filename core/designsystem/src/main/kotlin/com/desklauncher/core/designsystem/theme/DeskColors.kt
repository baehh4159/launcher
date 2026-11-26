package com.desklauncher.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Material 3 색 체계에 없는 책상 전용 색. [DeskTheme]으로만 제공된다. */
@Immutable
data class DeskColors(
    val deskTop: Color,
    val deskBottom: Color,
    val paper: Color,
    val onPaper: Color,
)

internal val LightDeskColors = DeskColors(
    deskTop = DeskTopLight,
    deskBottom = DeskBottomLight,
    paper = Paper98,
    onPaper = Ink10,
)

internal val DarkDeskColors = DeskColors(
    deskTop = DeskTopDark,
    deskBottom = DeskBottomDark,
    paper = Night20,
    onPaper = Ink90,
)

internal val LocalDeskColors = staticCompositionLocalOf { LightDeskColors }
