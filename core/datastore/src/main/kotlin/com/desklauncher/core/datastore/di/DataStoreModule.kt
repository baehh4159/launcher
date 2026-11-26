package com.desklauncher.core.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.desklauncher.core.common.dispatcher.ApplicationScope
import com.desklauncher.core.common.dispatcher.DeskDispatchers
import com.desklauncher.core.common.dispatcher.Dispatcher
import com.desklauncher.core.datastore.DataStoreSettingsRepository
import com.desklauncher.core.datastore.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DataStoreModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
        @Dispatcher(DeskDispatchers.IO) ioDispatcher: CoroutineDispatcher,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope + ioDispatcher) {
        context.preferencesDataStoreFile("desk_settings")
    }
}

@Module
@InstallIn(SingletonComponent::class)
internal abstract class SettingsBindModule {
    @Binds
    abstract fun bindSettingsRepository(repository: DataStoreSettingsRepository): SettingsRepository
}
