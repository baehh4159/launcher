package com.desklauncher.feature.memo.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.memo.MemoTool
import com.desklauncher.feature.memo.data.RoomMemoRepository
import com.desklauncher.feature.memo.domain.repository.MemoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class MemoModule {

    @Binds
    abstract fun bindMemoRepository(repository: RoomMemoRepository): MemoRepository

    @Binds
    @IntoSet
    abstract fun bindMemoTool(tool: MemoTool): DeskTool
}
