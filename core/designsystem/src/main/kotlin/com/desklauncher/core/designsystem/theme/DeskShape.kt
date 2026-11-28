package com.desklauncher.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** 모서리 (DESIGN_LANGUAGE §3) */
object DeskShape {
    /** 창 — beUI 패널·시트 24px */
    val window = RoundedCornerShape(24.dp)
    val card = RoundedCornerShape(16.dp)
    val control = RoundedCornerShape(12.dp)
    /** 알약 — 바로 적기·칩 */
    val pill = RoundedCornerShape(percent = 50)
}

/** 4dp 격자 간격 */
object DeskSpacing {
    val xxs: Dp = 4.dp
    val xs: Dp = 8.dp
    val s: Dp = 12.dp
    val m: Dp = 16.dp
    val l: Dp = 24.dp
    val xl: Dp = 32.dp
}

/** 선 두께 — 그림자 대신 선 (D5) */
object DeskLine {
    val hairline: Dp = 0.5.dp
    val thin: Dp = 1.dp
}

/** 컨트롤 크기 — beUI 44px(h-11)을 Android 최소 터치 48dp로 */
object DeskSize {
    val control: Dp = 48.dp
    val icon: Dp = 20.dp
}
