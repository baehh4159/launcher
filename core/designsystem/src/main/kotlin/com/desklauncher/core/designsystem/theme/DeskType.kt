package com.desklauncher.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 글자 단계 (DESIGN_LANGUAGE §3). 시스템 글꼴, 굵기로 위계를 만들고 크기 단계를 적게 둔다.
 * Material3 Typography 슬롯에 매핑해 기본 컴포넌트도 같은 단계를 쓰게 한다.
 */
object DeskType {
    val wallClock = TextStyle(fontSize = 28.sp, lineHeight = 34.sp, fontWeight = FontWeight.Light)
    val pageTitle = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.Medium)
    val cardTitle = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium)
    /** 공책 본문 — 한글 긴 문단 가독성 위해 줄높이 1.6 */
    val body = TextStyle(fontSize = 16.sp, lineHeight = 26.sp, fontWeight = FontWeight.Normal)
    val label = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.Medium)
    val caption = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Normal)
    /** 책등 별칭 — 칸 기하를 지키기 위해 글씨 배율을 따르지 않는다 (PRD §4.1). 쓰는 쪽에서 배율을 상쇄한다 */
    val spine = TextStyle(fontSize = 12.sp, lineHeight = 14.sp, fontWeight = FontWeight.Medium)
}

internal val DeskMaterialTypography = Typography(
    displaySmall = DeskType.wallClock,
    headlineSmall = DeskType.pageTitle,
    titleLarge = DeskType.pageTitle,
    titleMedium = DeskType.cardTitle,
    titleSmall = DeskType.label,
    bodyLarge = DeskType.body,
    bodyMedium = DeskType.label.copy(fontWeight = FontWeight.Normal),
    bodySmall = DeskType.caption,
    labelLarge = DeskType.label,
    labelMedium = DeskType.caption.copy(fontWeight = FontWeight.Medium),
    labelSmall = DeskType.spine,
)
