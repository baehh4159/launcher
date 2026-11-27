package com.desklauncher.plugin.water.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.plugin.water.WaterTool
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

/** 이 한 줄(@IntoSet)이 플러그인을 책상에 등록한다. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class WaterToolModule {
    @Binds
    @IntoSet
    abstract fun bindWaterTool(tool: WaterTool): DeskTool
}
