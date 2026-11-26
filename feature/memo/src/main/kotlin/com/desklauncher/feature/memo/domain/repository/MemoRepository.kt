package com.desklauncher.feature.memo.domain.repository

import com.desklauncher.feature.memo.domain.model.Project
import kotlinx.coroutines.flow.Flow

interface MemoRepository {
    /** 보관하지 않은 것 먼저, 정렬 순서대로 */
    val projects: Flow<List<Project>>

    suspend fun addProject(name: String, colorName: String): String
    suspend fun updateProject(project: Project)

    /** 프로젝트의 메모도 함께 지워진다 (FK CASCADE). 할일의 프로젝트 연결은 비워진다. */
    suspend fun deleteProject(id: String)
}
