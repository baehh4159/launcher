package com.desklauncher.feature.files.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.files.FilesTool
import com.desklauncher.feature.files.data.RoomFilesRepository
import com.desklauncher.feature.files.domain.repository.FilesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class FilesModule {
    @Binds
    abstract fun bindFilesRepository(repository: RoomFilesRepository): FilesRepository

    @Binds
    @IntoSet
    abstract fun bindFilesTool(tool: FilesTool): DeskTool
}
