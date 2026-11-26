package com.desklauncher.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.desklauncher.core.database.entity.AppPrefEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppPrefDao {

    @Query("SELECT * FROM app_pref")
    fun observeAll(): Flow<List<AppPrefEntity>>

    @Query("INSERT OR IGNORE INTO app_pref (componentKey) VALUES (:componentKey)")
    suspend fun ensure(componentKey: String)

    @Query("UPDATE app_pref SET favorite = :favorite WHERE componentKey = :componentKey")
    suspend fun updateFavorite(componentKey: String, favorite: Boolean)

    @Query("UPDATE app_pref SET hidden = :hidden WHERE componentKey = :componentKey")
    suspend fun updateHidden(componentKey: String, hidden: Boolean)

    @Query("UPDATE app_pref SET launchCount = launchCount + 1, lastLaunchedAt = :at WHERE componentKey = :componentKey")
    suspend fun incrementLaunch(componentKey: String, at: Long)

    @Transaction
    suspend fun setFavorite(componentKey: String, favorite: Boolean) {
        ensure(componentKey)
        updateFavorite(componentKey, favorite)
    }

    @Transaction
    suspend fun setHidden(componentKey: String, hidden: Boolean) {
        ensure(componentKey)
        updateHidden(componentKey, hidden)
    }

    @Transaction
    suspend fun recordLaunch(componentKey: String, at: Long) {
        ensure(componentKey)
        incrementLaunch(componentKey, at)
    }
}
