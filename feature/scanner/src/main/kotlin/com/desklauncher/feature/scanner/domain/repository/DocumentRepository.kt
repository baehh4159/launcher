package com.desklauncher.feature.scanner.domain.repository

import com.desklauncher.feature.scanner.domain.model.Document
import com.desklauncher.feature.scanner.domain.model.DocumentFolder
import com.desklauncher.feature.scanner.domain.model.ScanResult
import kotlinx.coroutines.flow.Flow

interface DocumentRepository {
    /** 최신 순 */
    val documents: Flow<List<Document>>
    val folders: Flow<List<DocumentFolder>>

    /** 스캔 결과를 내부 저장소로 복사하고(PDF + 첫 장 썸네일) 서류로 등록한다. 실패하면 예외. */
    suspend fun importScan(result: ScanResult, folderId: String?): Document

    suspend fun renameDocument(id: String, title: String)
    suspend fun moveDocument(id: String, folderId: String?)

    /** DB 행과 파일(PDF·썸네일)을 함께 지운다 */
    suspend fun deleteDocument(id: String)

    suspend fun addFolder(name: String): String
    suspend fun renameFolder(id: String, name: String)

    /** 폴더 안 서류는 지우지 않고 '미분류'로 옮겨진다 (FK SET NULL) */
    suspend fun deleteFolder(id: String)
}
