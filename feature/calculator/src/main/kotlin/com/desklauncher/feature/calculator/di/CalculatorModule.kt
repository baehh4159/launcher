package com.desklauncher.feature.calculator.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.calculator.CalculatorTool
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class CalculatorModule {
    @Binds
    @IntoSet
    abstract fun bindCalculatorTool(tool: CalculatorTool): DeskTool
}
