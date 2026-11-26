package com.desklauncher.feature.scanner.domain.model

import java.time.Instant

data class Document(
    val id: String,
    val folderId: String?,
    val title: String,
    /** 내부 저장소 상대 경로 */
    val pdfPath: String,
    val thumbnailPath: String?,
    val pageCount: Int,
    val sizeBytes: Long,
    val source: DocumentSource,
    val createdAt: Instant,
    val updatedAt: Instant,
)

enum class DocumentSource { Scan, Import }

data class DocumentFolder(
    val id: String,
    val name: String,
    val createdAt: Instant,
)

/** ML Kit 스캐너가 돌려준 결과 (임시 content:// URI 문자열) */
data class ScanResult(
    val pdfUri: String,
    val pageCount: Int,
    val firstPageImageUri: String?,
)
