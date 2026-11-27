package com.desklauncher.feature.files.domain.repository

import com.desklauncher.feature.files.domain.model.FileCategory
import com.desklauncher.feature.files.domain.model.FileItem
import kotlinx.coroutines.flow.Flow

interface FilesRepository {
    /** 최근 순 */
    fun files(category: FileCategory): Flow<List<FileItem>>

    /** SAF로 고른 파일을 즐겨찾기에 추가 (영구 읽기 권한을 받아 둔다). 실패하면 false */
    suspend fun addBookmark(uri: String): Boolean

    /** 목록에서만 뺀다 (원본 파일은 그대로, 받아 둔 권한은 반납) */
    suspend fun removeBookmark(id: String)
}
