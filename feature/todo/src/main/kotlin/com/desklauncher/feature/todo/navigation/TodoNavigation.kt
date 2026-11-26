package com.desklauncher.feature.todo.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.todo.ui.TodoRoute
import kotlinx.serialization.Serializable

@Serializable
data object TodoGraph

@Serializable
data object TodoListDestination

internal fun NavGraphBuilder.todoGraph(onBack: () -> Unit) {
    navigation<TodoGraph>(startDestination = TodoListDestination) {
        composable<TodoListDestination> {
            TodoRoute(onBack = onBack)
        }
    }
}
