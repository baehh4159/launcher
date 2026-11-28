package com.desklauncher.feature.desk.strip

import androidx.compose.runtime.Immutable
import com.desklauncher.core.designsystem.theme.DeskIndexColor

/** 서류철 칸 종류 (PRD §3.1: 위에서부터 고정 공책 3칸 → 전체 → 구분 → 문구 → 앱) */
enum class SlotKind { Notebook, All, Tools, Apps }

/**
 * 서류철 칸 하나.
 * @param notebookName 고정된 공책 이름. 공책 칸인데 null이면 아직 비어 있는 고정 자리.
 * @param alias 책등에 보일 짧은 인덱스(2–4자). 책등에는 앞 2자만 보인다.
 */
@Immutable
data class StripSlot(
    val key: String,
    val kind: SlotKind,
    val notebookName: String? = null,
    val alias: String? = null,
    val color: DeskIndexColor? = null,
) {
    val isEmptyNotebook: Boolean get() = kind == SlotKind.Notebook && notebookName == null
}

/** 서류철이 보이는 가장자리 (PRD §17 실험: 분리형=Left 기본, 오른쪽 표시=Right 비교군) */
enum class StripSide { Left, Right }

/** 기본 6칸: 고정 공책 3 + 전체 / 구분 / 문구 + 앱. 구분은 '문구' 칸 바로 위. */
object DefaultStrip {
    const val PinnedNotebookCount = 3
    /** 구분(진동 없는 여백)이 들어가는 칸 번호 — 이 칸 위에 구분이 있다 */
    const val GapBeforeIndex = PinnedNotebookCount + 1

    fun slots(pinned: List<StripSlot?> = emptyList()): List<StripSlot> =
        List(PinnedNotebookCount) { index ->
            pinned.getOrNull(index) ?: StripSlot(key = "pinned_$index", kind = SlotKind.Notebook)
        } + listOf(
            StripSlot(key = "all", kind = SlotKind.All),
            StripSlot(key = "tools", kind = SlotKind.Tools),
            StripSlot(key = "apps", kind = SlotKind.Apps),
        )
}
