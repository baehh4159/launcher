package com.desklauncher.feature.scanner.ui.preview

import com.desklauncher.feature.scanner.domain.model.Document
import com.desklauncher.feature.scanner.domain.model.DocumentFolder
import com.desklauncher.feature.scanner.domain.model.DocumentSource
import com.desklauncher.feature.scanner.ui.cabinet.CabinetUiState
import java.time.Instant

internal object CabinetPreviewData {
    private val now = Instant.parse("2026-11-26T06:00:00Z")
    private fun doc(id: String, title: String, pages: Int, folder: String? = null) =
        Document(id, folder, title, "documents/$id.pdf", null, pages, 120_000, DocumentSource.Scan, now, now)

    val folders = listOf(DocumentFolder("f1", "영수증", now), DocumentFolder("f2", "계약서", now))
    val documents = listOf(
        doc("1", "스캔 2026-11-26 15:02", 1),
        doc("2", "관리비 고지서", 2, "f1"),
        doc("3", "전세 계약서", 6, "f2"),
        doc("4", "택배 송장", 1),
    )
    val loaded = CabinetUiState(isLoading = false, documents = documents, folders = folders)
    val empty = CabinetUiState(isLoading = false)
}
