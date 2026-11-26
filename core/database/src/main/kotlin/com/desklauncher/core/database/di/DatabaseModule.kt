package com.desklauncher.core.database.di

import android.content.Context
import androidx.room.Room
import com.desklauncher.core.database.DeskDatabase
import com.desklauncher.core.database.dao.AppPrefDao
import com.desklauncher.core.database.dao.MemoDao
import com.desklauncher.core.database.dao.ProjectDao
import com.desklauncher.core.database.dao.TodoDao
import com.desklauncher.core.database.dao.ToolConfigDao
import com.desklauncher.core.database.migration.DeskMigrations
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDeskDatabase(@ApplicationContext context: Context): DeskDatabase =
        Room.databaseBuilder(context, DeskDatabase::class.java, DeskDatabase.NAME)
            .addMigrations(*DeskMigrations.ALL)
            .build()

    @Provides
    fun provideToolConfigDao(database: DeskDatabase): ToolConfigDao = database.toolConfigDao()

    @Provides
    fun provideAppPrefDao(database: DeskDatabase): AppPrefDao = database.appPrefDao()

    @Provides
    fun provideProjectDao(database: DeskDatabase): ProjectDao = database.projectDao()

    @Provides
    fun provideTodoDao(database: DeskDatabase): TodoDao = database.todoDao()

    @Provides
    fun provideMemoDao(database: DeskDatabase): MemoDao = database.memoDao()
}
