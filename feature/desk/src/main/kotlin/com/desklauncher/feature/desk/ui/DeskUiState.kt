package com.desklauncher.feature.desk.ui

import androidx.compose.runtime.Immutable
import com.desklauncher.core.datastore.StripLayout
import com.desklauncher.feature.desk.strip.DefaultStrip
import com.desklauncher.feature.desk.strip.StripSlot

@Immutable
data class DeskUiState(
    val slots: List<StripSlot> = DefaultStrip.slots(),
    val stripLayout: StripLayout = StripLayout.Split,
    /** 펼쳐 둔 작업. null이면 '첫 기록 적기' (PRD §3.1) — 공책(M2) 전까지는 늘 null */
    val resume: ResumeUi? = null,
    val showScrubHint: Boolean = false,
)

/** 이어서 쓰기 카드 내용 */
@Immutable
data class ResumeUi(
    val notebookName: String,
    val pageTitle: String,
    val lastSentence: String,
)

sealed interface DeskAction {
    data class OpenSlot(val index: Int, val viaScrub: Boolean) : DeskAction
    data object QuickWrite : DeskAction
    data object Resume : DeskAction
    data object OpenSettings : DeskAction
    data object SetDefaultHome : DeskAction
}
