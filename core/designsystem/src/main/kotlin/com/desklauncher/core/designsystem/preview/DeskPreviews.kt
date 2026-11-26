package com.desklauncher.core.designsystem.preview

import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview

/** Galaxy S24+ 기본 해상도(FHD+ 1080×2340, 450dpi) ≈ 384×832dp. */
const val DeskPreviewDevice = "spec:width=384dp,height=832dp,dpi=450"

/** 화면 Preview 기본 세트: S24+ × 라이트/다크 (AGENTS.md 규칙 3). */
@Preview(name = "S24+ 라이트", device = DeskPreviewDevice, showBackground = true)
@Preview(
    name = "S24+ 다크",
    device = DeskPreviewDevice,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
annotation class DeskPreviews

/** 디자인시스템 컴포넌트용 글꼴 배율 세트 (1.0 / 1.5 / 2.0). */
@Preview(name = "글꼴 1.0", fontScale = 1.0f, showBackground = true)
@Preview(name = "글꼴 1.5", fontScale = 1.5f, showBackground = true)
@Preview(name = "글꼴 2.0", fontScale = 2.0f, showBackground = true)
annotation class DeskFontScalePreviews
