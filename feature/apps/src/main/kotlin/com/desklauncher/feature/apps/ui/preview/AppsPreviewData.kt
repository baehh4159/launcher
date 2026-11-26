package com.desklauncher.feature.apps.ui.preview

import com.desklauncher.feature.apps.domain.AppIndex
import com.desklauncher.feature.apps.domain.model.InstalledApp
import com.desklauncher.feature.apps.ui.AppSection
import com.desklauncher.feature.apps.ui.AppsUiState

internal object AppsPreviewData {
    private val apps = listOf("갤러리", "계산기", "네이버", "당근", "멜론", "배달의민족", "설정", "시계", "카메라", "카카오톡", "Chrome", "Gmail", "YouTube", "1Password")
        .map { InstalledApp("pkg.$it/.Main", "pkg.$it", it, isFavorite = it == "카카오톡") }

    private fun sections(list: List<InstalledApp>): List<AppSection> {
        val sorted = AppIndex.sorted(list)
        val favorites = sorted.filter { it.isFavorite }
        return listOf(AppSection(AppSection.FavoritesKey, favorites)) +
            sorted.groupBy { AppIndex.keyOf(it.label) }.map { (k, v) -> AppSection(k, v) }
    }

    val loaded = AppsUiState(isLoading = false, sections = sections(apps))
    val searching = AppsUiState(isLoading = false, query = "ㅋㅋ", sections = sections(apps.filter { AppIndex.matches(it, "ㅋㅋ") }))
    val noResult = AppsUiState(isLoading = false, query = "없는앱", sections = emptyList())
    val loading = AppsUiState(isLoading = true)
}
