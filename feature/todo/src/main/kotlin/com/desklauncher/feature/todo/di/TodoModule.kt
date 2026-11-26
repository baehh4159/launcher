package com.desklauncher.feature.todo.di

import com.desklauncher.core.toolapi.DeskTool
import com.desklauncher.feature.todo.TodoTool
import com.desklauncher.feature.todo.data.RoomTodoRepository
import com.desklauncher.feature.todo.domain.repository.TodoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
internal abstract class TodoModule {

    @Binds
    abstract fun bindTodoRepository(repository: RoomTodoRepository): TodoRepository

    @Binds
    @IntoSet
    abstract fun bindTodoTool(tool: TodoTool): DeskTool
}
