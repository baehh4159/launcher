package com.desklauncher.feature.memo.ui.preview

import com.desklauncher.feature.memo.domain.model.Project
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
}
