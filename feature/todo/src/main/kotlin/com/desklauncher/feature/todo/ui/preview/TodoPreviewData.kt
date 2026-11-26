package com.desklauncher.feature.todo.ui.preview

import com.desklauncher.feature.todo.domain.model.ProjectRef
import com.desklauncher.feature.todo.domain.model.Todo
import com.desklauncher.feature.todo.domain.model.TodoPriority
import com.desklauncher.feature.todo.ui.TodoUiState
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

internal object TodoPreviewData {
    val today: LocalDate = LocalDate.of(2026, 11, 26)
    private val now: Instant = Instant.parse("2026-11-26T06:00:00Z")

    val projects = listOf(ProjectRef("p1", "책상 런처", "Blue"), ProjectRef("p2", "이사 준비", "Green"))

    val todos = listOf(
        Todo("1", "서류철 스트립 실기기 튜닝", note = "햅틱 간격 12ms 확인", priority = TodoPriority.High, dueDate = today, dueTime = LocalTime.of(15, 0), projectId = "p1", createdAt = now, updatedAt = now),
        Todo("2", "관리비 납부", dueDate = today.minusDays(1), createdAt = now, updatedAt = now),
        Todo("3", "이삿짐 견적 받기", priority = TodoPriority.Medium, dueDate = today.plusDays(3), projectId = "p2", createdAt = now, updatedAt = now),
        Todo("4", "책 반납", createdAt = now, updatedAt = now),
        Todo("5", "우유 사기", isDone = true, completedAt = now, createdAt = now, updatedAt = now),
    )

    val loaded = TodoUiState(isLoading = false, items = todos.filterNot { it.isDone }, done = todos.filter { it.isDone }, projects = projects)
    val empty = TodoUiState(isLoading = false)
    val loading = TodoUiState(isLoading = true)
}
