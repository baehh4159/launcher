package com.desklauncher.feature.apps.ui.preview

import com.desklauncher.feature.apps.domain.AppIndex
import com.desklauncher.feature.apps.domain.model.InstalledApp
import com.desklauncher.feature.apps.ui.AppsUiState

internal object AppsPreviewData {
    private val apps = listOf("카카오톡", "캘린더", "갤러리", "네이버", "멜론", "삼성 노트", "설정", "유튜브", "Chrome", "Gmail", "토스")
        .mapIndexed { index, label -> InstalledApp("pkg$index/.Main", "pkg$index", label) }

    val list = AppsUiState(isLoading = false, sections = AppIndex.sections(apps))
    val searching = AppsUiState(isLoading = false, query = "ㅋㅋ", sections = AppIndex.sections(apps.filter { AppIndex.matches(it.label, "ㅋㅋ") }))
    val noResult = AppsUiState(isLoading = false, query = "ㅎㅎㅎ")
}
