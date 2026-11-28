package com.desklauncher.feature.desk.ui.preview

import com.desklauncher.core.designsystem.theme.DeskIndexColor
import com.desklauncher.feature.desk.clock.WallClockText
import com.desklauncher.feature.desk.strip.DefaultStrip
import com.desklauncher.feature.desk.strip.SlotKind
import com.desklauncher.feature.desk.strip.StripSlot
import com.desklauncher.feature.desk.ui.DeskUiState
import com.desklauncher.feature.desk.ui.ResumeUi

/** Preview 전용 가짜 데이터 (Hilt·DB 없이) */
internal object DeskPreviewData {
    val clock = WallClockText(time = "오후 3:24", date = "11월 28일 토요일")

    val empty = DeskUiState()

    val withWork = DeskUiState(
        slots = DefaultStrip.slots(
            pinned = listOf(
                StripSlot("n1", SlotKind.Notebook, notebookName = "보드 검증", alias = "보드", color = DeskIndexColor.Apricot),
                StripSlot("n2", SlotKind.Notebook, notebookName = "앱 개발", alias = "개발", color = DeskIndexColor.Sage),
                StripSlot("n3", SlotKind.Notebook, notebookName = "개인", alias = "개인", color = DeskIndexColor.Rose),
            ),
        ),
        resume = ResumeUi("보드 검증", "11/27 전원부", "부하 연결 후 출력은 3.30V 대비 3.18V로 약 3.6% 감소했다"),
    )
}
