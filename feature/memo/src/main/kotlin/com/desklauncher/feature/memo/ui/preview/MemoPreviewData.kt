package com.desklauncher.feature.memo.ui.preview

import com.desklauncher.feature.memo.domain.model.Memo
import com.desklauncher.feature.memo.domain.model.Project
import com.desklauncher.feature.memo.ui.detail.ProjectDetailUiState
import com.desklauncher.feature.memo.ui.project.ProjectListUiState
import java.time.Instant

internal object MemoPreviewData {
    private val now = Instant.parse("2026-11-26T06:00:00Z")
    val projects = listOf(
        Project("p1", "책상 런처", "Blue", createdAt = now, updatedAt = now),
        Project("p2", "이사 준비", "Green", createdAt = now, updatedAt = now),
        Project("p3", "읽을 책", "Purple", createdAt = now, updatedAt = now),
        Project("p4", "2025 회고", "Gray", archived = true, createdAt = now, updatedAt = now),
    )
    val projectList = ProjectListUiState(isLoading = false, active = projects.filterNot { it.archived }, archived = projects.filter { it.archived })
    val projectListEmpty = ProjectListUiState(isLoading = false)
    val memos = listOf(
        Memo("m1", "p1", title = "스트립 튜닝 기록", body = "엣지 패널과 겹침\n그립 가운데가 편함", pinned = true, createdAt = now, updatedAt = now),
        Memo("m2", "p1", body = "앱 서랍 초성 검색 아이디어\nㅋㅋ → 카카오톡", createdAt = now, updatedAt = now),
    )
    val detail = ProjectDetailUiState(isLoading = false, project = projects.first(), memos = memos)
}
