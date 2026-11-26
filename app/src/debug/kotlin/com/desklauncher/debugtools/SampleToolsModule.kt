package com.desklauncher.debugtools

import com.desklauncher.core.toolapi.DeskTool
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.ElementsIntoSet

@Module
@InstallIn(SingletonComponent::class)
internal object SampleToolsModule {

    @Provides
    @ElementsIntoSet
    fun provideSampleTools(): Set<DeskTool> = SampleTools.all.toSet()
}
