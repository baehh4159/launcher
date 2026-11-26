package com.desklauncher.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * 서류철 인덱스 탭 색상 토큰.
 * DB(`tool_config.tabColor`)에는 [name]이 저장되므로 **항목 이름을 바꾸거나 지우지 않는다** (추가만 허용).
 */
enum class DeskTabColor(val container: Color, val onContainer: Color) {
    Red(Color(0xFFD9534F), Color.White),
    Orange(Color(0xFFE8873A), Color(0xFF2B1A0E)),
    Yellow(Color(0xFFE9C46A), Color(0xFF2B1A0E)),
    Green(Color(0xFF6A9F58), Color.White),
    Teal(Color(0xFF3E9C9A), Color.White),
    Blue(Color(0xFF4A7FC1), Color.White),
    Indigo(Color(0xFF5C6BC0), Color.White),
    Purple(Color(0xFF8E6BB8), Color.White),
    Pink(Color(0xFFD67BA0), Color(0xFF2B1A0E)),
    Brown(Color(0xFF9C7350), Color.White),
    Gray(Color(0xFF8A8F98), Color.White),
    ;

    companion object {
        /** 저장된 이름을 토큰으로 변환. 알 수 없는 값이면 null. */
        fun fromNameOrNull(name: String?): DeskTabColor? = entries.firstOrNull { it.name == name }
    }
}
