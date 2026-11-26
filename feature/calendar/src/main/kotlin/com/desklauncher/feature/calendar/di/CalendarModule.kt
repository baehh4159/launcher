package com.desklauncher.feature.calendar.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.calendar.CalendarTool
import com.desklauncher.feature.calendar.data.RoomCalendarRepository
import com.desklauncher.feature.calendar.domain.repository.CalendarRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CalendarModule {
    @Binds
    abstract fun bindCalendarRepository(repository: RoomCalendarRepository): CalendarRepository

    @Binds
    @IntoSet
    abstract fun bindCalendarTool(tool: CalendarTool): DeskTool
}
