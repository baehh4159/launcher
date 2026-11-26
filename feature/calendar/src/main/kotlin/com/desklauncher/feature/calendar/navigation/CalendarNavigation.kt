package com.desklauncher.feature.calendar.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.desklauncher.feature.calendar.ui.CalendarRoute
import kotlinx.serialization.Serializable

@Serializable
data object CalendarGraph

@Serializable
data object CalendarMonthDestination

internal fun NavGraphBuilder.calendarGraph(onBack: () -> Unit) {
    navigation<CalendarGraph>(startDestination = CalendarMonthDestination) {
        composable<CalendarMonthDestination> { CalendarRoute(onBack = onBack) }
    }
}
