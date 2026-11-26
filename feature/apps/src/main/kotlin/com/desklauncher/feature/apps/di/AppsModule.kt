package com.desklauncher.feature.apps.di

import com.desklauncher.feature.apps.data.LauncherAppsRepository
import com.desklauncher.feature.apps.domain.repository.AppsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AppsModule {
    @Binds
    abstract fun bindAppsRepository(repository: LauncherAppsRepository): AppsRepository
}
