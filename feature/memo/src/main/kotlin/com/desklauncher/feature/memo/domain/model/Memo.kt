package com.desklauncher.feature.memo.domain.model

import java.time.Instant

data class Memo(
    val id: String,
    val projectId: String,
    val title: String = "",
    val body: String = "",
    val pinned: Boolean = false,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    /** 목록에 보일 제목: 제목이 없으면 본문 첫 줄 */
    val displayTitle: String get() = title.ifBlank { body.lineSequence().firstOrNull { it.isNotBlank() }.orEmpty() }
}
