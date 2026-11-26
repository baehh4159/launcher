package com.desklauncher.feature.doodle.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.doodle.DoodleTool
import com.desklauncher.feature.doodle.data.FileDoodleRepository
import com.desklauncher.feature.doodle.domain.repository.DoodleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DoodleModule {
    @Binds
    @IntoSet
    abstract fun bindDoodleTool(tool: DoodleTool): DeskTool
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DoodleDataModule {
    @Binds
    abstract fun bindDoodleRepository(repository: FileDoodleRepository): DoodleRepository
}
