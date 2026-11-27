package com.desklauncher.core.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * 서류철 인덱스 탭 색상 토큰 — 채도를 낮춘 문구류 포인트 컬러.
 * DB(`tool_config.tabColor`, 프로젝트·일정 색)에는 [name]이 저장되므로 **항목 이름을 바꾸거나 지우지 않는다**.
 * 이름은 예전 원색 이름 그대로이고, 실제 색과 표시 이름(테라코타 등)만 바뀌었다.
 * 글자색은 모두 먹색 — 중간 명도 색 위에서 흰 글씨보다 대비가 높다.
 */
enum class DeskTabColor(val container: Color, val onContainer: Color) {
    /** 테라코타 */
    Red(Color(0xFFC98B76), Color(0xFF1C1C1E)),
    /** 살구 */
    Orange(Color(0xFFD9A27C), Color(0xFF1C1C1E)),
    /** 머스타드 */
    Yellow(Color(0xFFD4B66A), Color(0xFF1C1C1E)),
    /** 세이지 그린 */
    Green(Color(0xFFA3B59C), Color(0xFF1C1C1E)),
    /** 유칼립투스 */
    Teal(Color(0xFF93B2AE), Color(0xFF1C1C1E)),
    /** 더스티 블루 */
    Blue(Color(0xFF9DB1C6), Color(0xFF1C1C1E)),
    /** 슬레이트 블루 */
    Indigo(Color(0xFFA2A8C3), Color(0xFF1C1C1E)),
    /** 라벤더 그레이 */
    Purple(Color(0xFFB5A9C2), Color(0xFF1C1C1E)),
    /** 더스티 로즈 */
    Pink(Color(0xFFD1A9AC), Color(0xFF1C1C1E)),
    /** 월넛 */
    Brown(Color(0xFFB59F8A), Color(0xFF1C1C1E)),
    /** 슬레이트 그레이 */
    Gray(Color(0xFFA8ABAF), Color(0xFF1C1C1E)),
    ;

    companion object {
        /** 저장된 이름을 토큰으로 변환. 알 수 없는 값이면 null. */
        fun fromNameOrNull(name: String?): DeskTabColor? = entries.firstOrNull { it.name == name }
    }
}

/**
 * 도구 id → 사용자가 도구함에서 고른 탭 색. 앱이 레지스트리 값으로 제공한다.
 * 도구 화면의 포인트 요소(상단 바 등)는 [toolTabColor]로 읽어 서류철 색과 맞춘다.
 */
val LocalToolTabColors = staticCompositionLocalOf<Map<String, DeskTabColor>> { emptyMap() }

@Composable
@ReadOnlyComposable
fun toolTabColor(toolId: String, default: DeskTabColor): DeskTabColor = LocalToolTabColors.current[toolId] ?: default
