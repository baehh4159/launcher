package com.desklauncher.core.database.di

import android.content.Context
import androidx.room.Room
import com.desklauncher.core.database.DeskDatabase
import com.desklauncher.core.database.dao.AlarmDao
import com.desklauncher.core.database.dao.AppPrefDao
import com.desklauncher.core.database.dao.CalendarEventDao
import com.desklauncher.core.database.dao.DiaryDao
import com.desklauncher.core.database.dao.DocumentDao
import com.desklauncher.core.database.dao.DoodleDao
import com.desklauncher.core.database.dao.FileBookmarkDao
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

    @Provides
    fun provideCalendarEventDao(database: DeskDatabase): CalendarEventDao = database.calendarEventDao()

    @Provides
    fun provideDiaryDao(database: DeskDatabase): DiaryDao = database.diaryDao()

    @Provides
    fun provideDocumentDao(database: DeskDatabase): DocumentDao = database.documentDao()

    @Provides
    fun provideDoodleDao(database: DeskDatabase): DoodleDao = database.doodleDao()

    @Provides
    fun provideAlarmDao(database: DeskDatabase): AlarmDao = database.alarmDao()

    @Provides
    fun provideFileBookmarkDao(database: DeskDatabase): FileBookmarkDao = database.fileBookmarkDao()
}
