package com.desklauncher.feature.doodle.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.doodle.DoodleTool
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
