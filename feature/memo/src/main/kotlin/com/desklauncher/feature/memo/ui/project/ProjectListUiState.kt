package com.desklauncher.feature.memo.ui.project

import androidx.compose.runtime.Immutable
import com.desklauncher.feature.memo.domain.model.Project

@Immutable
data class ProjectListUiState(
    val isLoading: Boolean = true,
    val active: List<Project> = emptyList(),
    val archived: List<Project> = emptyList(),
    /** 추가/편집 대화상자. id가 null이면 새 프로젝트 */
    val editor: ProjectEditor? = null,
    val confirmDelete: Project? = null,
    val memoCounts: Map<String, Int> = emptyMap(),
)

@Immutable
data class ProjectEditor(val id: String?, val name: String, val colorName: String)

sealed interface ProjectListAction {
    data object Add : ProjectListAction
    data class Edit(val project: Project) : ProjectListAction
    data class SaveEditor(val name: String, val colorName: String) : ProjectListAction
    data object DismissEditor : ProjectListAction
    data class SetArchived(val project: Project, val archived: Boolean) : ProjectListAction
    data class RequestDelete(val project: Project) : ProjectListAction
    data object ConfirmDelete : ProjectListAction
    data object DismissDelete : ProjectListAction
    data class Open(val project: Project) : ProjectListAction
    data object Back : ProjectListAction
}
