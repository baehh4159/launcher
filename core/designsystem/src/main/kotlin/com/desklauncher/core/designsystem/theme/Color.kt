package com.desklauncher.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// 미색 종이 + 먹색 + 얇은 선 팔레트. 원시 색상값은 이 파일(과 탭·펜 토큰)에만 둔다 (AGENTS.md §5).

// ── 라이트: 종이 ─────────────────────────────────────
/** 화면 바탕 — 눈이 편한 미색 종이 */
internal val PaperBackground = Color(0xFFF7F6F2)
/** 카드·표면 — 바탕보다 살짝 밝은 종이 */
internal val PaperSurface = Color(0xFFFCFBF8)
internal val PaperContainerLow = Color(0xFFF9F8F4)
internal val PaperContainer = Color(0xFFF3F1EC)
internal val PaperContainerHigh = Color(0xFFEFEDE7)
internal val PaperContainerHighest = Color(0xFFEBE8E1)
/** 먹색 본문 */
internal val Ink = Color(0xFF1C1C1E)
/** 보조 글자 회색 */
internal val InkSecondary = Color(0xFF6E6B64)
/** 영역 구분 얇은 선 */
internal val PaperLine = Color(0xFFE2DFD7)
internal val PaperLineStrong = Color(0xFFCDC9BF)

// 포인트: 짙은 더스티 블루(버튼·선택), 세이지(보조), 테라코타(오류)
internal val DustyBlueInk = Color(0xFF3F5A72)
internal val DustyBlueContainer = Color(0xFFE2E8EE)
internal val DustyBlueOnContainer = Color(0xFF1E2F3E)
internal val SageInk = Color(0xFF5F7560)
internal val SageContainer = Color(0xFFE4EAE1)
internal val TerracottaInk = Color(0xFFA9503B)
internal val TerracottaContainer = Color(0xFFF3E1DA)

// ── 다크: 먹지 ───────────────────────────────────────
internal val NightBackground = Color(0xFF1C1C1E)
internal val NightSurface = Color(0xFF242426)
internal val NightContainerLow = Color(0xFF212123)
internal val NightContainer = Color(0xFF2A2A2C)
internal val NightContainerHigh = Color(0xFF303032)
internal val NightContainerHighest = Color(0xFF363638)
internal val NightInk = Color(0xFFEDEBE6)
internal val NightInkSecondary = Color(0xFFA9A59C)
internal val NightLine = Color(0xFF3A3934)
internal val NightLineStrong = Color(0xFF4B4943)
internal val DustyBlueLight = Color(0xFFA9BED2)
internal val DustyBlueNightContainer = Color(0xFF2F4255)
internal val SageLight = Color(0xFFB4C7B2)
internal val TerracottaLight = Color(0xFFE5A894)
