package com.desklauncher.feature.memo.domain.repository

import com.desklauncher.feature.memo.domain.model.Memo
import com.desklauncher.feature.memo.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    /** 보관하지 않은 것 먼저, 정렬 순서대로 */
    val projects: Flow<List<Project>>

    suspend fun addProject(name: String, colorName: String): String
    suspend fun updateProject(project: Project)

    /** 프로젝트의 메모도 함께 지워진다 (FK CASCADE). 할일의 프로젝트 연결은 비워진다. */
    suspend fun deleteProject(id: String)

    /** 프로젝트별 메모 수 */
    val memoCounts: Flow<Map<String, Int>>

    fun project(id: String): Flow<Project?>

    /** 고정 → 최근 수정 순 */
    fun memos(projectId: String): Flow<List<Memo>>

    suspend fun getMemo(id: String): Memo?

    /** 있으면 덮어쓰고 없으면 만든다. updatedAt은 저장 시각으로 바뀐다. */
    suspend fun saveMemo(memo: Memo)

    suspend fun deleteMemo(id: String)
}
