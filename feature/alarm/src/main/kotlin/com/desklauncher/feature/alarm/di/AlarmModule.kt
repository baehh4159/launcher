package com.desklauncher.feature.alarm.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.alarm.AlarmTool
import com.desklauncher.feature.alarm.data.AndroidAlarmScheduler
import com.desklauncher.feature.alarm.data.RoomAlarmRepository
import com.desklauncher.feature.alarm.domain.AlarmScheduler
import com.desklauncher.feature.alarm.domain.repository.AlarmRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AlarmModule {
    @Binds
    abstract fun bindAlarmRepository(repository: RoomAlarmRepository): AlarmRepository

    @Binds
    abstract fun bindAlarmScheduler(scheduler: AndroidAlarmScheduler): AlarmScheduler

    @Binds
    @IntoSet
    abstract fun bindAlarmTool(tool: AlarmTool): DeskTool
}
