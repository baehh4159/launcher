package com.desklauncher.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * 낙서장 펜 색 토큰 (DOODLE-01). 그림 데이터에는 실제 ARGB 값을 저장하므로
 * 토큰 이름·값을 바꿔도 기존 낙서의 색은 바뀌지 않는다.
 */
enum class DeskPenColor(val color: Color) {
    Ink(Color(0xFF1F1B16)),
    Blue(Color(0xFF2F5DA8)),
    Red(Color(0xFFC93C3C)),
    Green(Color(0xFF3E8E4F)),
    Yellow(Color(0xFFE2B33C)),
}

/** 낙서장 종이 색 (밝은 종이 — 다크 모드에서도 종이는 밝게 둔다) */
val DeskDoodlePaper = Color(0xFFFFFDF7)
