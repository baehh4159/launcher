package com.desklauncher.core.toolregistry.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.core.toolapi.ToolRegistry
import com.desklauncher.core.toolregistry.DefaultToolRegistry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ToolRegistryModule {

    /** 등록된 도구가 하나도 없어도 빈 Set을 주입할 수 있도록 선언한다. */
    @Multibinds
    abstract fun deskTools(): Set<DeskTool>

    @Binds
    abstract fun bindToolRegistry(registry: DefaultToolRegistry): ToolRegistry
}
