package com.desklauncher.core.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** 의미 색 토큰 (DESIGN_LANGUAGE §2, D6). feature 모듈은 이 이름으로만 색을 쓴다. */
@Immutable
data class DeskColors(
    /** 책상 면 (background) */
    val desk: Color,
    /** 창·카드·종이 (card) */
    val paper: Color,
    /** 본문 (foreground) */
    val onPaper: Color,
    /** 보조 글 (muted-foreground) */
    val muted: Color,
    /** 0.5dp 구분선 (border) */
    val line: Color,
    /** 1dp 테두리·칸 경계 */
    val lineStrong: Color,
    /** 창 뒤 막 */
    val scrim: Color,
    /** 체크 완료·커서·선택 강조 */
    val ink: Color,
    /** ink 위 글자 */
    val onInk: Color,
    val isDark: Boolean,
)

internal val LightDeskColors = DeskColors(
    desk = Color(0xFFF7F6F2),
    paper = Color(0xFFFCFBF8),
    onPaper = Color(0xFF1C1C1E),
    muted = Color(0xFF6E6B64),
    line = Color(0xFFE2DFD7),
    lineStrong = Color(0xFFCFCBC1),
    scrim = Color(0x52000000),
    ink = Color(0xFF2F3A4A),
    onInk = Color(0xFFFCFBF8),
    isDark = false,
)

internal val DarkDeskColors = DeskColors(
    desk = Color(0xFF151514),
    paper = Color(0xFF1E1E1C),
    onPaper = Color(0xFFECEAE4),
    muted = Color(0xFF9C988F),
    line = Color(0xFF2E2D2A),
    lineStrong = Color(0xFF3D3B37),
    scrim = Color(0x7A000000),
    ink = Color(0xFFC9D3E0),
    onInk = Color(0xFF151514),
    isDark = true,
)

internal val LocalDeskColors = staticCompositionLocalOf { LightDeskColors }
