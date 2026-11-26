package com.desklauncher.feature.scanner.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.scanner.CabinetTool
import com.desklauncher.feature.scanner.data.RoomDocumentRepository
import com.desklauncher.feature.scanner.domain.repository.DocumentRepository
import com.desklauncher.feature.scanner.ui.component.FileThumbnailLoader
import com.desklauncher.feature.scanner.ui.viewer.PdfSupport
import dagger.Binds
import dagger.Module
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class ScannerModule {
    @Binds
    abstract fun bindDocumentRepository(repository: RoomDocumentRepository): DocumentRepository

    @Binds
    @IntoSet
    abstract fun bindCabinetTool(tool: CabinetTool): DeskTool
}

@EntryPoint
@InstallIn(SingletonComponent::class)
internal interface ThumbnailEntryPoint {
    fun thumbnailLoader(): FileThumbnailLoader
    fun pdfSupport(): PdfSupport
}
