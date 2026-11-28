package com.desklauncher.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** S24+ 기준(384×832dp) × 라이트/다크 (AGENTS.md 규칙 3) */
@Preview(name = "라이트", device = "spec:width=384dp,height=832dp,dpi=450", showBackground = true)
@Preview(
    name = "다크",
    device = "spec:width=384dp,height=832dp,dpi=450",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class DeskPreviews

/** 글씨 배율 1.0 / 1.5 / 2.0 — 디자인시스템 컴포넌트 필수 */
@Preview(name = "글씨 1.0", fontScale = 1f, showBackground = true)
@Preview(name = "글씨 1.5", fontScale = 1.5f, showBackground = true)
@Preview(name = "글씨 2.0", fontScale = 2f, showBackground = true)
annotation class DeskFontScalePreviews
