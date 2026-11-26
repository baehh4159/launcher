package com.desklauncher.feature.diary.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.diary.ui.editor.DiaryEditorRoute
import com.desklauncher.feature.diary.ui.list.DiaryListRoute
import kotlinx.serialization.Serializable

@Serializable
data object DiaryGraph

@Serializable
data object DiaryListDestination

/** epochDay가 null이면 오늘 */
@Serializable
data class DiaryEditorDestination(val epochDay: Long? = null)

internal fun NavGraphBuilder.diaryGraph(navController: NavController) {
    navigation<DiaryGraph>(startDestination = DiaryListDestination) {
        composable<DiaryListDestination> {
            DiaryListRoute(
                onBack = { navController.popBackStack() },
                onOpen = { date -> navController.navigate(DiaryEditorDestination(date.toEpochDay())) },
            )
        }
        composable<DiaryEditorDestination> { DiaryEditorRoute(onBack = { navController.popBackStack() }) }
    }
}
