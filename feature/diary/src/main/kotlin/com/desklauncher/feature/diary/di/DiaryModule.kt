package com.desklauncher.feature.diary.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.diary.DiaryTool
import com.desklauncher.feature.diary.data.RoomDiaryRepository
import com.desklauncher.feature.diary.domain.repository.DiaryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DiaryModule {
    @Binds
    abstract fun bindDiaryRepository(repository: RoomDiaryRepository): DiaryRepository

    @Binds
    @IntoSet
    abstract fun bindDiaryTool(tool: DiaryTool): DeskTool
}
