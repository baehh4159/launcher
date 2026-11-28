package com.desklauncher.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * 공책 책등(인덱스) 색 — 낮은 채도 문구 팔레트 (DESIGN_LANGUAGE §2).
 * DB에는 [name]으로 저장하므로 **이름을 바꾸지 않는다.** 글자는 늘 잉크색(onPaper 밝은 값)이다.
 */
enum class DeskIndexColor(val container: Color) {
    Terracotta(Color(0xFFC98B76)),
    Apricot(Color(0xFFD9A27C)),
    Mustard(Color(0xFFD4B66A)),
    Sage(Color(0xFFA3B59C)),
    Teal(Color(0xFF93B2AE)),
    Sky(Color(0xFF9DB1C6)),
    Indigo(Color(0xFFA2A8C3)),
    Lavender(Color(0xFFB5A9C2)),
    Rose(Color(0xFFD1A9AC)),
    Brown(Color(0xFFB59F8A)),
    Gray(Color(0xFFA8ABAF)),
    ;

    /** 책등 위 글자 — 밝은 책등이라 어두운 잉크 고정 (대비 4.5:1 이상) */
    val onContainer: Color get() = Color(0xFF1C1C1E)
}
