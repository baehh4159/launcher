package com.desklauncher.feature.files.domain.model

import java.time.Instant

/** '책상 위에 꺼내 둔 파일' 하나 (FILES-01). 기기 전체 탐색기가 아니다. */
data class FileItem(
    val id: String,
    val name: String,
    val category: FileCategory,
    /** 앱 파일: 내부 저장소 상대 경로 / 외부 즐겨찾기: content:// URI */
    val location: String,
    val mimeType: String,
    val sizeBytes: Long?,
    val modifiedAt: Instant,
)

enum class FileCategory { Scans, Doodles }
